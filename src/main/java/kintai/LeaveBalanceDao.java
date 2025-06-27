package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LeaveBalanceDao {

    private final DBAccess db = new DBAccess();

    public List<LeaveBalanceBean> getLeaveBalances(String empNo) {
        String sql = """
            SELECT lb.empno, lb.leave_type_id, lt.leave_type_name, lb.grant_date, lb.expire_date,
                   lb.granted_days, lb.used_days
            FROM leave_balance lb
            JOIN leave_type lt ON lb.leave_type_id = lt.leave_type_id
            WHERE lb.empno = ?
            ORDER BY lb.grant_date DESC
        """;

        List<LeaveBalanceBean> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, empNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LeaveBalanceBean bean = new LeaveBalanceBean();
                    bean.setEmpNo(rs.getString("empno"));
                    bean.setLeaveTypeId(rs.getInt("leave_type_id"));
                    bean.setLeaveTypeName(rs.getString("leave_type_name"));
                    bean.setGrantedDate(rs.getDate("grant_date").toLocalDate());
                    bean.setExpirationDate(rs.getDate("expire_date").toLocalDate());
                    bean.setGrantedDays(rs.getInt("granted_days"));
                    bean.setUsedDays(rs.getInt("used_days"));
                    list.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
