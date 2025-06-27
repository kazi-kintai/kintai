package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeaveRecDao {

    private DBAccess db = new DBAccess();

    // 休暇種別IDの定数（適宜変更してください）
    public static final int LEAVE_TYPE_PAID = 1;      // 有給
    public static final int LEAVE_TYPE_SPECIAL = 2;   // 特別休暇
    public static final int LEAVE_TYPE_COMP = 3;      // 代休

    /**
     * 新規休暇申請を登録する
     */
    public boolean insertLeave(LeaveRecBean bean) throws SQLException, ClassNotFoundException {
        String sql = """
            INSERT INTO leave_rec 
            (empno, leave_type_id, startdate, enddate, reason, approvedby, is_deleted, created_at, created_by, updated_at, updated_by)
            VALUES (?, ?, ?, ?, ?, ?, false, NOW(), ?, NOW(), ?)
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bean.getEmpNo());
            stmt.setInt(2, bean.getLeaveTypeId());
            stmt.setDate(3, bean.getStartDate());
            stmt.setDate(4, bean.getEndDate());
            stmt.setString(5, bean.getReason());
            stmt.setString(6, bean.getApprovedBy());
            stmt.setString(7, bean.getCreatedBy());
            stmt.setString(8, bean.getUpdatedBy());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * 既存の休暇申請を更新する
     */
    public boolean updateLeave(LeaveRecBean bean) throws SQLException, ClassNotFoundException {
        String sql = """
            UPDATE leave_rec
            SET empno = ?, leave_type_id = ?, startdate = ?, enddate = ?, reason = ?, approvedby = ?,
                updated_at = NOW(), updated_by = ?
            WHERE leaveid = ? AND is_deleted = false
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bean.getEmpNo());
            stmt.setInt(2, bean.getLeaveTypeId());
            stmt.setDate(3, bean.getStartDate());
            stmt.setDate(4, bean.getEndDate());
            stmt.setString(5, bean.getReason());
            stmt.setString(6, bean.getApprovedBy());
            stmt.setString(7, bean.getUpdatedBy());
            stmt.setInt(8, bean.getLeaveId());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * 論理削除（is_deleted = true）で休暇申請を削除する
     */
    public boolean logicalDeleteLeave(int leaveId, String deletedBy) throws SQLException, ClassNotFoundException {
        String sql = """
            UPDATE leave_rec
            SET is_deleted = true, deleted_at = NOW(), deleted_by = ?
            WHERE leaveid = ? AND is_deleted = false
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, deletedBy);
            stmt.setInt(2, leaveId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * 指定社員の休暇申請一覧を取得（論理削除されていないもの、開始日降順）
     */
    public List<LeaveRecBean> getLeaveList(String empNo) throws SQLException, ClassNotFoundException {
        List<LeaveRecBean> list = new ArrayList<>();
        String sql = """
            SELECT leaveid, empno, leave_type_id, startdate, enddate, reason, approvedby, 
                   is_deleted, created_at, created_by, updated_at, updated_by
            FROM leave_rec
            WHERE empno = ? AND is_deleted = false
            ORDER BY startdate DESC
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRecBean bean = new LeaveRecBean();
                    bean.setLeaveId(rs.getInt("leaveid"));
                    bean.setEmpNo(rs.getString("empno"));
                    bean.setLeaveTypeId(rs.getInt("leave_type_id"));
                    bean.setStartDate(rs.getDate("startdate"));
                    bean.setEndDate(rs.getDate("enddate"));
                    bean.setReason(rs.getString("reason"));
                    bean.setApprovedBy(rs.getString("approvedby"));
                    bean.setDeleted(rs.getBoolean("is_deleted"));
                    bean.setCreatedAt(rs.getTimestamp("created_at"));
                    bean.setCreatedBy(rs.getString("created_by"));
                    bean.setUpdatedAt(rs.getTimestamp("updated_at"));
                    bean.setUpdatedBy(rs.getString("updated_by"));
                    list.add(bean);
                }
            }
        }
        return list;
    }

    /**
     * 残日数を leave_balance テーブルから取得する
     * leaveTypeId に応じてカラムを変える
     */
    public int fetchRemainingLeave(String empNo, int leaveTypeId) throws SQLException, ClassNotFoundException {
        String column;
        switch (leaveTypeId) {
            case LEAVE_TYPE_PAID:
                column = "paid_leave";
                break;
            case LEAVE_TYPE_SPECIAL:
                column = "special_leave";
                break;
            case LEAVE_TYPE_COMP:
                column = "comp_leave";
                break;
            default:
                throw new IllegalArgumentException("不明な休暇種別ID: " + leaveTypeId);
        }
        String sql = "SELECT " + column + " FROM leave_balance WHERE empno = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(column) : 0;
            }
        }
    }

}
