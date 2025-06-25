package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kintai.LeaveBalance;

public class LeaveBalanceDAO {
    private final Connection conn;

    public LeaveBalanceDAO(Connection conn) {
        this.conn = conn;
    }

    // Get Leave Balance for an employee and leave type
    public LeaveBalance getBalance(String empNo, int leaveTypeId) throws SQLException {
        String sql = "SELECT total_days, used_days FROM leave_balance WHERE empno = ? AND leave_type_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            stmt.setInt(2, leaveTypeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LeaveBalance balance = new LeaveBalance();
                balance.setEmpNo(empNo);
                balance.setLeaveTypeId(leaveTypeId);
                balance.setTotalDays(rs.getInt("total_days"));
                balance.setUsedDays(rs.getInt("used_days"));
                return balance;
            }
        }
        return null; // No record found
    }

    // Update used_days (after leave is taken)
    public void updateUsedDays(String empNo, int leaveTypeId, int addedDays) throws SQLException {
        String sql = "UPDATE leave_balance SET used_days = used_days + ? WHERE empno = ? AND leave_type_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, addedDays);
            stmt.setString(2, empNo);
            stmt.setInt(3, leaveTypeId);
            stmt.executeUpdate();
        }
    }

    // Optional: insert initial balance row
    public void insertInitialBalance(String empNo, int leaveTypeId, int totalDays) throws SQLException {
        String sql = "INSERT INTO leave_balance (empno, leave_type_id, total_days, used_days) VALUES (?, ?, ?, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            stmt.setInt(2, leaveTypeId);
            stmt.setInt(3, totalDays);
            stmt.executeUpdate();
        }
  
   }
    public List<LeaveBalance> findAllByEmpNo(String empNo) throws SQLException {
        List<LeaveBalance> list = new ArrayList<>();
        String sql = "SELECT empno, leave_type_id, total_days, used_days FROM leave_balance WHERE empno = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveBalance balance = new LeaveBalance();
                    balance.setEmpNo(rs.getString("empno"));
                    balance.setLeaveTypeId(rs.getInt("leave_type_id"));
                    balance.setTotalDays(rs.getInt("total_days"));
                    balance.setUsedDays(rs.getInt("used_days"));
                    list.add(balance);
                }
            }
        }

        return list;
    }
}