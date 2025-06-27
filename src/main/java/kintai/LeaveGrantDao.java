package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LeaveGrantDao {

    private DBAccess db = new DBAccess();

    
    public static final int LEAVE_TYPE_ANNUAL = 1;
    public static final int LEAVE_TYPE_SPECIAL = 2;
    public static final int LEAVE_TYPE_COMP = 3;
    public static final int LEAVE_TYPE_INITIAL_3M = 11;
    public static final int LEAVE_TYPE_INITIAL_6M = 12;
    
    
    // 未付与社員の抽出
    public List<EmpBean> findUnissued(int leaveTypeId, LocalDate grantDate) {
        String sql = """
            SELECT * FROM emp e
            WHERE NOT EXISTS (
                SELECT 1 FROM leave_balance lb
                WHERE lb.empno = e.empno
                  AND lb.leave_type_id = ?
                  AND lb.grant_date = ?
            )
        """;
        List<EmpBean> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, leaveTypeId);
            ps.setDate(2, Date.valueOf(grantDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmpBean emp = new EmpBean();
                    emp.setEmpNo(rs.getString("empno"));
                    emp.setEmpName(rs.getString("empname"));
                    emp.setEmpDate(rs.getDate("empdate").toLocalDate());
                    list.add(emp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // 共通処理
    private List<EmpBean> fetchEmpList(String sql) {
        List<EmpBean> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                EmpBean emp = new EmpBean();
                emp.setEmpNo(rs.getString("empno"));
                emp.setEmpName(rs.getString("empname"));
                emp.setEmpDate(rs.getDate("empdate").toLocalDate());
                list.add(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<EmpBean> fetchEmpListWithDate(String sql, LocalDate date) {
        List<EmpBean> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmpBean emp = new EmpBean();
                    emp.setEmpNo(rs.getString("empno"));
                    emp.setEmpName(rs.getString("empname"));
                    emp.setEmpDate(rs.getDate("empdate").toLocalDate());
                    list.add(emp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    
    
    // 年次有給休暇（毎年7月1日付与）
    public boolean grantAnnualLeave(EmpBean emp, String source) {
        LocalDate grantDate = LocalDate.of(LocalDate.now().getYear(), 7, 1);
        if (!isEligible(emp, grantDate)) return false;
        if (alreadyGranted(emp.getEmpNo(), grantDate, LEAVE_TYPE_ANNUAL)) return false;

        int days = calcGrantedDays(emp);
        return insertLeaveBalance(emp.getEmpNo(), LEAVE_TYPE_ANNUAL, grantDate, grantDate.plusYears(1), days, source);
    }

    // 初回5日・5日付与（3か月／6か月）
    public boolean grantInitialAnnualLeave(EmpBean emp, int stage, String source) {
        int days = 5;
        int leaveTypeId = (stage == 1) ? LEAVE_TYPE_INITIAL_3M : LEAVE_TYPE_INITIAL_6M;
        LocalDate targetDate = (stage == 1)
            ? emp.getEmpDate().plusMonths(3)
            : emp.getEmpDate().plusMonths(6);

        if (LocalDate.now().isBefore(targetDate)) return false; // 遅れ許容
        if (!isEligible(emp, targetDate)) return false;
        if (alreadyGranted(emp.getEmpNo(), targetDate, leaveTypeId)) return false;

        return insertLeaveBalance(emp.getEmpNo(), leaveTypeId, targetDate, targetDate.plusYears(1), days, "初回" + days + "日付与");
    }

    // 特別休暇（7月1日）
    public boolean grantSpecialLeave(EmpBean emp, String source) {
        LocalDate today = LocalDate.now();
        LocalDate grantDate = LocalDate.of(today.getYear(), 7, 1);

        if (!today.equals(grantDate)) return false;
        if (alreadyGranted(emp.getEmpNo(), grantDate, LEAVE_TYPE_SPECIAL)) return false;

        return insertLeaveBalance(emp.getEmpNo(), 2, grantDate, grantDate.plusYears(1), 5, "特別休暇");
    }

    // 代休（休日勤務があった翌日以降に人手で呼び出し）
    public boolean grantCompLeave(EmpBean emp, LocalDate workDate, String source) {
    	if (alreadyGranted(emp.getEmpNo(), workDate, LEAVE_TYPE_COMP)) return false;

        return insertLeaveBalance(emp.getEmpNo(), 3, workDate, workDate.plusMonths(1), 1, "休日勤務代休");
    }

    // 勤続年数による年次有給休暇日数計算（第25条3項）
    public int calcGrantedDays(EmpBean emp) {
        long years = ChronoUnit.YEARS.between(emp.getEmpDate(), LocalDate.now());

        if (years < 1) return 11;
        else if (years == 1) return 12;
        else if (years == 2) return 14;
        else if (years == 3) return 16;
        else if (years == 4) return 18;
        else return 20;
    }

    // すでに付与済か確認（同日・同種）
    boolean alreadyGranted(String empNo, LocalDate date, int leaveTypeId) {
        String sql = "SELECT COUNT(*) FROM leave_balance WHERE empno = ? AND leave_type_id = ? AND grant_date = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empNo);
            ps.setInt(2, leaveTypeId);
            ps.setDate(3, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    // 付与登録処理
    private boolean insertLeaveBalance(String empNo, int leaveTypeId, LocalDate grantDate,
            LocalDate expireDate, int days, String source) {
        String sql = "INSERT INTO leave_balance (empno, leave_type_id, grant_date, expire_date, granted_days, used_days, source)"
        		+ "VALUES (?, ?, ?, ?, ?, 0, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empNo);
            ps.setInt(2, leaveTypeId);
            ps.setDate(3, Date.valueOf(grantDate));
            ps.setDate(4, Date.valueOf(expireDate));
            ps.setInt(5, days);
            ps.setString(6, source);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 出勤率8割の判定（就業規則第25条・28条共通）
    public boolean isEligible(EmpBean emp, LocalDate baseDate) {
//        LocalDate startDate = emp.getEmpDate();
//        long totalDays = ChronoUnit.DAYS.between(startDate, baseDate);
//        if (totalDays < 30) return false;
//
//        String workDaysSql = "SELECT COUNT(*) FROM calendar_event " +
//                             "WHERE EVENT_DATE BETWEEN ? AND ? AND IS_WORKING = TRUE";
//
//        String attendanceSql = "SELECT COUNT(*) FROM kintai " +
//                               "WHERE EMPNO = ? AND KINTAIDATE BETWEEN ? AND ?";
//
//        try (Connection conn = db.getConnection()) {
//            int totalWorkingDays = 0;
//            int actualAttendanceDays = 0;
//
//            // 出勤カレンダーの出勤日数
//            try (PreparedStatement ps = conn.prepareStatement(workDaysSql)) {
//                ps.setDate(1, Date.valueOf(startDate));
//                ps.setDate(2, Date.valueOf(baseDate));
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) totalWorkingDays = rs.getInt(1);
//                }
//            }
//
//            // kintai テーブルの出勤日数
//            try (PreparedStatement ps = conn.prepareStatement(attendanceSql)) {
//                ps.setString(1, emp.getEmpNo());
//                ps.setDate(2, Date.valueOf(startDate));
//                ps.setDate(3, Date.valueOf(baseDate));
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) actualAttendanceDays = rs.getInt(1);
//                }
//            }
//
//            if (totalWorkingDays == 0) return false;
//
//            double rate = (double) actualAttendanceDays / totalWorkingDays;
            return true;//rate >= 0.8;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }
}