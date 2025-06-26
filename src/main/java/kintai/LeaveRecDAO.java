package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeaveRecDAO {

    private DBAccess db = new DBAccess();

    /**
     * 新規の休暇申請を登録する
     */
    public boolean insertLeave(LeaveRecBean l) {
        String sql = """
            INSERT INTO leave_rec 
            (EMPNO, LEAVE_TYPE_ID, STARTDATE, ENDDATE, REASON, APPROVEDBY, CREATED_AT) 
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, l.getEmpNo());
            stmt.setInt(2, l.getLeaveTypeId());
            stmt.setDate(3, l.getStartDate());
            stmt.setDate(4, l.getEndDate());
            stmt.setString(5, l.getReason());
            stmt.setString(6, l.getApprovedBy());
            return stmt.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 既存の休暇申請を更新する
     */
    public boolean updateLeave(LeaveRecBean l) {
        String sql = """
            UPDATE leave_rec 
            SET EMPNO = ?, LEAVE_TYPE_ID = ?, STARTDATE = ?, ENDDATE = ?, 
                REASON = ?, APPROVEDBY = ?
            WHERE LEAVEID = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, l.getEmpNo());
            stmt.setInt(2, l.getLeaveTypeId());
            stmt.setDate(3, l.getStartDate());
            stmt.setDate(4, l.getEndDate());
            stmt.setString(5, l.getReason());
            stmt.setString(6, l.getApprovedBy());
            stmt.setInt(7, l.getLeaveId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 休暇申請を削除する
     */
    public boolean deleteLeave(int leaveId) {
        String sql = "DELETE FROM leave_rec WHERE LEAVEID = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, leaveId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 有給休暇の支給合計を取得
     */
    public int fetchTotalPaidLeave(String empNo) {
        String sql = "SELECT PAID_LEAVE_TOTAL FROM leave_quota WHERE EMPNO = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("PAID_LEAVE_TOTAL") : 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 特別休暇の支給合計を取得
     */
    public int fetchTotalSpecialLeave(String empNo) {
        String sql = "SELECT SPECIAL_LEAVE_TOTAL FROM leave_quota WHERE EMPNO = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("SPECIAL_LEAVE_TOTAL") : 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 代休の支給合計を取得
     */
    public int fetchTotalCompLeave(String empNo) {
        String sql = "SELECT COMP_LEAVE_TOTAL FROM leave_quota WHERE EMPNO = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("COMP_LEAVE_TOTAL") : 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 指定社員の休暇申請一覧を取得（最新順）
     */
    public List<LeaveRecBean> getLeaveList(String empNo) {
        List<LeaveRecBean> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_rec WHERE EMPNO = ? ORDER BY STARTDATE DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRecBean l = new LeaveRecBean();
                    l.setLeaveId(rs.getInt("LEAVEID"));
                    l.setEmpNo(rs.getString("EMPNO"));
                    l.setLeaveTypeId(rs.getInt("LEAVE_TYPE_ID"));
                    l.setStartDate(rs.getDate("STARTDATE"));
                    l.setEndDate(rs.getDate("ENDDATE"));
                    l.setReason(rs.getString("REASON"));
                    l.setApprovedBy(rs.getString("APPROVEDBY"));
                    l.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                    list.add(l);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * LeaveRecBean に有給休暇支給合計をセット
     */
    public void fillLeaveQuotaIntoBean(LeaveRecBean bean) {
        try {
            bean.setTotalPaidLeave(fetchTotalPaidLeave(bean.getEmpNo()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 社員一覧を取得（番号・氏名）
     */
    public List<EmpBean> getEmployeeList() {
        List<EmpBean> list = new ArrayList<>();
        String sql = "SELECT EMPNO, NAME FROM emp ORDER BY EMPNO";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                EmpBean emp = new EmpBean();
                emp.setEmpNo(rs.getString("EMPNO"));
                emp.setEmpName(rs.getString("NAME"));
                list.add(emp);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 部署一覧を取得（deptテーブル利用を推奨）
     */
    public List<String> getDepartmentList() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT DEPT_NAME FROM emp ORDER BY DEPT_NAME";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("DEPT_NAME"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 休暇種別一覧を取得（ID・名称）
     */
    public List<LeaveTypeBean> getLeaveTypeList() {
        List<LeaveTypeBean> list = new ArrayList<>();
        String sql = "SELECT LEAVE_TYPE_ID, LEAVE_TYPE_NAME FROM leave_type ORDER BY LEAVE_TYPE_ID";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                LeaveTypeBean lt = new LeaveTypeBean();
                lt.setLeaveTypeId(rs.getInt("LEAVE_TYPE_ID"));
                lt.setLeaveTypeName(rs.getString("LEAVE_TYPE_NAME"));
                list.add(lt);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }
}