package kintai;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * LeaveRecDAO は、休暇申請および支給情報を操作する DAO クラスです。
 */
public class LeaveRecDAO {

    // データベース接続メソッド
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/kintai_db"; // ← あなたのDB名に合わせて変更
        String user = "root"; // DBユーザー名
        String pass = "";     // パスワード（未設定なら空）
        return DriverManager.getConnection(url, user, pass);
    }

    // 1. 新しい休暇申請を登録する
    public boolean insertLeave(LeaveRequest l) throws SQLException {
        String sql = "INSERT INTO leave_rec (EMPNO, LEAVE_TYPE_ID, STARTDATE, ENDDATE, REASON, APPROVEDBY) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, l.getEmpNo());
            stmt.setInt(2, l.getLeaveTypeId());
            stmt.setDate(3, l.getStartDate());
            stmt.setDate(4, l.getEndDate());
            stmt.setString(5, l.getReason());
            stmt.setString(6, l.getApprovedBy());
            return stmt.executeUpdate() > 0;
        }
    }

    // 2. 既存の休暇申請を更新する
    public boolean updateLeave(LeaveRequest l) throws SQLException {
        String sql = "UPDATE leave_rec SET EMPNO=?, LEAVE_TYPE_ID=?, STARTDATE=?, ENDDATE=?, REASON=?, APPROVEDBY=? WHERE LEAVEID=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, l.getEmpNo());
            stmt.setInt(2, l.getLeaveTypeId());
            stmt.setDate(3, l.getStartDate());
            stmt.setDate(4, l.getEndDate());
            stmt.setString(5, l.getReason());
            stmt.setString(6, l.getApprovedBy());
            stmt.setInt(7, l.getLeaveId());
            return stmt.executeUpdate() > 0;
        }
    }

    // 3. 休暇申請を削除する
    public boolean deleteLeave(int leaveId) throws SQLException {
        String sql = "DELETE FROM leave_rec WHERE LEAVEID=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, leaveId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 4. 有給休暇（LEAVE_TYPE_ID=1）の使用件数をカウントする
    public int countUsedPaidLeave(String empNo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leave_rec WHERE EMPNO = ? AND LEAVE_TYPE_ID = 1";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // 5. 支給された有給休暇の合計を取得する
    public int fetchTotalPaidLeave(String empNo) throws SQLException {
        String sql = "SELECT PAID_LEAVE_TOTAL FROM leave_quota WHERE EMPNO = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("PAID_LEAVE_TOTAL") : 0;
        }
    }

    // 6. 支給された特別休暇の合計を取得する
    public int fetchTotalSpecialLeave(String empNo) throws SQLException {
        String sql = "SELECT SPECIAL_LEAVE_TOTAL FROM leave_quota WHERE EMPNO = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("SPECIAL_LEAVE_TOTAL") : 0;
        }
    }

    // 7. 支給された代休の合計を取得する
    public int fetchTotalCompLeave(String empNo) throws SQLException {
        String sql = "SELECT COMP_LEAVE_TOTAL FROM leave_quota WHERE EMPNO = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("COMP_LEAVE_TOTAL") : 0;
        }
    }

    // 8. 指定社員の休暇申請リストを取得する
    public List<LeaveRequest> getLeaveList(String empNo) throws SQLException {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_rec WHERE EMPNO = ? ORDER BY STARTDATE DESC";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LeaveRequest l = new LeaveRequest();
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
        return list;
    }

    // 9. LeaveRequest に支給有給日数をセットする（補助メソッド）
    public void fillLeaveQuotaIntoBean(LeaveRequest bean) throws SQLException {
        int quota = fetchTotalPaidLeave(bean.getEmpNo());
        bean.setTotalPaidLeave(quota);
    }
}