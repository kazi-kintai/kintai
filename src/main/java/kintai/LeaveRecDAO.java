package kintai;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class LeaveRecDAO {
    // データベース接続情報（必要に応じて修正）
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/kintai_db"; // DB名
        String user = "root";
        String pass = "";
        return DriverManager.getConnection(url, user, pass);
    }
    // 休暇を新規登録する
    public boolean insertLeave(LeaveRequest l) throws SQLException {
        String sql = "INSERT INTO leave_rec (EMPNO, LEAVE_TYPE_ID, STARTDATE, ENDDATE, REASON, APPROVEDBY) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, l.getEmpNo());
            stmt.setInt(2, l.getLeaveTypeId());
            stmt.setDate(3, l.getStartDate());
            stmt.setDate(4, l.getEndDate());
            stmt.setString(5, l.getReason());
            stmt.setString(6, l.getApprovedBy());
            return stmt.executeUpdate() > 0;
        }
    }
    // 休暇を更新する
    public boolean updateLeave(LeaveRequest l) throws SQLException {
        String sql = "UPDATE leave_rec SET EMPNO=?, LEAVE_TYPE_ID=?, STARTDATE=?, ENDDATE=?, REASON=?, APPROVEDBY=? WHERE LEAVEID=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
    // 休暇を削除する
    public boolean deleteLeave(int leaveId) throws SQLException {
        String sql = "DELETE FROM leave_rec WHERE LEAVEID=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, leaveId);
            return stmt.executeUpdate() > 0;
        }
    }
    // 使用済みの有給（LEAVE_TYPE_ID = 1）の件数を取得
    public int countUsedPaidLeave(String empNo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leave_rec WHERE EMPNO = ? AND LEAVE_TYPE_ID = 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    // 支給された有給日数（例: empテーブルにleave_quota列をつくる）
    public int fetchTotalPaidLeave(String empNo) throws SQLException {
        String sql = "SELECT leave_quota FROM emp WHERE EMPNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("leave_quota") : 0;
        }
    }
}






