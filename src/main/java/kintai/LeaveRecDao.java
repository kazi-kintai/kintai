package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LeaveRecDao {

    public static final int LEAVE_TYPE_PAID = 1;
    public static final int LEAVE_TYPE_SPECIAL = 2;
    public static final int LEAVE_TYPE_COMP = 3;

    private final DBAccess db = new DBAccess();

    // 休暇申請登録
    public boolean insertLeave(LeaveRecBean bean) throws Exception {
        if (isOverlapping(bean.getEmpId(), bean.getLeaveTypeId(), bean.getStartDate(), bean.getEndDate(), null)) {
            throw new Exception("申請期間が重複しています");
        }

        String sql = """
            INSERT INTO leave_rec (emp_id, leave_type_id, start_date, end_date, reason, created_by, updated_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bean.getEmpId());
            ps.setInt(2, bean.getLeaveTypeId());
            ps.setDate(3, bean.getStartDate());
            ps.setDate(4, bean.getEndDate());
            ps.setString(5, bean.getReason());
            ps.setString(6, bean.getCreatedBy());
            ps.setString(7, bean.getUpdatedBy());
            int result = ps.executeUpdate();
            if (result > 0) {
                recalculateUsedDays(bean.getEmpId(), bean.getLeaveTypeId());
                return true;
            }
        }
        return false;
    }

    // 休暇申請更新
    public boolean updateLeave(LeaveRecBean bean) throws Exception {
        if (isOverlapping(bean.getEmpId(), bean.getLeaveTypeId(), bean.getStartDate(), bean.getEndDate(), bean.getLeaveId())) {
            throw new Exception("申請期間が重複しています");
        }

        String sql = """
            UPDATE leave_rec
            SET start_date = ?, end_date = ?, reason = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE leave_id = ?
        """;

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, bean.getStartDate());
            ps.setDate(2, bean.getEndDate());
            ps.setString(3, bean.getReason());
            ps.setString(4, bean.getUpdatedBy());
            ps.setInt(5, bean.getLeaveId());
            int result = ps.executeUpdate();
            if (result > 0) {
                recalculateUsedDays(bean.getEmpId(), bean.getLeaveTypeId());
                return true;
            }
        }
        return false;
    }

    // 論理削除
    public boolean logicalDeleteLeave(int leaveId, String updatedBy) throws Exception {
        LeaveRecBean bean = findById(leaveId);
        if (bean == null) throw new Exception("指定された休暇申請が存在しません");

        String sql = """
            UPDATE leave_rec
            SET is_deleted = TRUE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE leave_id = ?
        """;

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updatedBy);
            ps.setInt(2, leaveId);
            int result = ps.executeUpdate();
            if (result > 0) {
                recalculateUsedDays(bean.getEmpId(), bean.getLeaveTypeId());
                return true;
            }
        }
        return false;
    }

    // 使用日数再計算（消化優先：grant_dateの新しい順）
    public void recalculateUsedDays(String empId, int leaveTypeId) throws Exception {
        String sumSql = """
            SELECT SUM(DATEDIFF(end_date, start_date) + 1) AS total_days
            FROM leave_rec
            WHERE emp_id = ? AND leave_type_id = ? AND (is_deleted IS NULL OR is_deleted = FALSE)
        """;

        
        
        int remaining = 0;
        try (Connection conn = db.getConnection()) {
        	
        	String resetSql = "UPDATE leave_balance SET used_days = 0 WHERE emp_id = ? AND leave_type_id = ?";
        	try (PreparedStatement resetPs = conn.prepareStatement(resetSql)) {
        	    resetPs.setString(1, empId);
        	    resetPs.setInt(2, leaveTypeId);
        	    resetPs.executeUpdate();
        	}
        	
            try (PreparedStatement sumPs = conn.prepareStatement(sumSql)) {
                sumPs.setString(1, empId);
                sumPs.setInt(2, leaveTypeId);
                try (ResultSet rs = sumPs.executeQuery()) {
                    if (rs.next()) {
                        remaining = rs.getInt("total_days");
                    }
                }
            }

            String getBalances = """
                SELECT balance_id, granted_days
                FROM leave_balance
                WHERE emp_id = ? AND leave_type_id = ? AND grant_date <= CURRENT_DATE AND expire_date >= CURRENT_DATE
                ORDER BY grant_date DESC
            """;

            try (PreparedStatement balPs = conn.prepareStatement(getBalances)) {
                balPs.setString(1, empId);
                balPs.setInt(2, leaveTypeId);
                try (ResultSet rs = balPs.executeQuery()) {
                    while (rs.next()) {
                        int balanceId = rs.getInt("balance_id");
                        int granted = rs.getInt("granted_days");
                        int used = Math.min(granted, remaining);

                        String upd = "UPDATE leave_balance SET used_days = ? WHERE balance_id = ?";
                        try (PreparedStatement updPs = conn.prepareStatement(upd)) {
                            updPs.setInt(1, used);
                            updPs.setInt(2, balanceId);
                            updPs.executeUpdate();
                        }
                        remaining -= used;
                    }
                }
            }
        }
    }

    // 期間重複チェック
    public boolean isOverlapping(String empId, int leaveTypeId, Date startDate, Date endDate, Integer excludeId) throws Exception {
    	String sql = """
    		    SELECT COUNT(*) FROM leave_rec
    		    WHERE emp_id = ? 
    		      AND leave_type_id = ? 
    		      AND (is_deleted IS NULL OR is_deleted = FALSE)
    		      AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?))
    		""" + (excludeId != null ? " AND leave_id <> ?" : "");
    	
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, empId);
            ps.setInt(idx++, leaveTypeId);
            ps.setDate(idx++, endDate);
            ps.setDate(idx++, startDate);
            ps.setDate(idx++, startDate);
            ps.setDate(idx++, endDate);
            if (excludeId != null) ps.setInt(idx, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // 単一取得
    public LeaveRecBean findById(int leaveId) throws Exception {
        String sql = "SELECT * FROM leave_rec WHERE leave_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, leaveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LeaveRecBean bean = new LeaveRecBean();
                    bean.setLeaveId(rs.getInt("leave_id"));
                    bean.setEmpId(rs.getString("emp_id"));
                    bean.setLeaveTypeId(rs.getInt("leave_type_id"));
                    bean.setStartDate(rs.getDate("start_date"));
                    bean.setEndDate(rs.getDate("end_date"));
                    bean.setReason(rs.getString("reason"));
                    return bean;
                }
            }
        }
        return null;
    }

    // 一覧取得
    public List<LeaveRecBean> getLeaveList(String empId) throws Exception {
        String sql = "SELECT * FROM leave_rec WHERE emp_id = ? AND (is_deleted IS NULL OR is_deleted = FALSE) ORDER BY start_date DESC";
        List<LeaveRecBean> list = new ArrayList<>();

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LeaveRecBean bean = new LeaveRecBean();
                    bean.setLeaveId(rs.getInt("leave_id"));
                    bean.setEmpId(rs.getString("emp_id"));
                    bean.setLeaveTypeId(rs.getInt("leave_type_id"));
                    bean.setStartDate(rs.getDate("start_date"));
                    bean.setEndDate(rs.getDate("end_date"));
                    bean.setReason(rs.getString("reason"));
                    list.add(bean);
                }
            }
        }
        return list;
    }

    // 残日数取得（全未消化分の合計）
    public int fetchRemainingLeave(String empId, int leaveTypeId) throws Exception {
        String sql = """
            SELECT SUM(granted_days - used_days) AS remaining
            FROM leave_balance
            WHERE emp_id = ? AND leave_type_id = ? AND expire_date >= CURRENT_DATE
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, empId);
            ps.setInt(2, leaveTypeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("remaining");
                }
            }
        }
        return 0;
    }
} 
