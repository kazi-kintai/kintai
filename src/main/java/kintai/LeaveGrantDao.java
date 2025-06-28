package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeaveGrantDao {

    private DBAccess db = new DBAccess();

    
    public static final int LEAVE_TYPE_ANNUAL = 1;
    public static final int LEAVE_TYPE_SPECIAL = 2;
    public static final int LEAVE_TYPE_COMP = 3;
    public static final int LEAVE_TYPE_INITIAL_3M = 11;
    public static final int LEAVE_TYPE_INITIAL_6M = 12;
    
    
    /**
     * 入社日から指定期間までの所定労働日数をカレンダーイベントからカウントする。
     * 勤務日として設定された日付の数を返す。
     */
    public int countScheduledWorkDays(LocalDate fromDate, LocalDate toDate) throws SQLException {
        int count = 0;
        try (Connection conn = db.getConnection()) {
            for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
                int dow = date.getDayOfWeek().getValue(); // 月=1〜日=7
                if (dow >= 6) continue; // 土日除外

                if (isHoliday(date, conn)) continue; // 特別休日（カレンダーでis_work=FALSE）も除外

                count++; // 平日かつ特別休日でない → 出勤日
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return count;
    }
    
    private boolean isHoliday(LocalDate date, Connection conn) throws SQLException {
        String sql = "SELECT is_work FROM calendar_event WHERE event_date = ? AND is_deleted = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return !rs.getBoolean("is_work"); // is_work=false → 休み
                }
            }
        }
        return false; // 登録なし → 通常日（平日なら出勤日）
    }
    
    /**
     * 指定期間の実際の出勤日数をkintaiテーブルからカウントする。
     * 実出勤日は、kintaiレコードが存在し、かつis_deleted=FALSEのものをカウント。
     */
    public int countActualWorkDays(String empId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT kintai_date) FROM kintai " +
                     "WHERE emp_id = ? AND kintai_date BETWEEN ? AND ? AND is_deleted = FALSE";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
        return 0;
    }
    
    /**
     * 勤務予定期間(fromDate〜toDate)に対し、実出勤日数÷所定労働日数が閾値(例：0.8)以上か判定する。
     */
    public boolean isAttendanceRateAboveThreshold(String empId, LocalDate fromDate, LocalDate toDate, double threshold) throws SQLException {
        int scheduledDays = countScheduledWorkDays(fromDate, toDate);
        if (scheduledDays == 0) return false;  // 予定勤務なしならfalse
        int actualDays = countActualWorkDays(empId, fromDate, toDate);
        double rate = (double) actualDays / scheduledDays;
        return rate >= threshold;
    }
    
    /**
     * 入社日から現在日までの勤続月数を計算する簡易例。
     */
    public int calcMonthsOfService(LocalDate joinDate, LocalDate baseDate) {
        if (joinDate == null || baseDate.isBefore(joinDate)) return 0;
        return (baseDate.getYear() - joinDate.getYear()) * 12 + (baseDate.getMonthValue() - joinDate.getMonthValue());
    }

    
    
 // 未付与社員の抽出（付与日で重複チェック）
    public List<EmpBean> findUnissued(int leaveTypeId, LocalDate grantDate) {
        String sql = """
            SELECT * FROM emp e
            WHERE NOT EXISTS (
                SELECT 1 FROM leave_balance lb
                WHERE lb.EmpId = e.EmpId
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
                    emp.setEmpId(rs.getString("EmpId"));
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
                emp.setEmpId(rs.getString("EmpId"));
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
                    emp.setEmpId(rs.getString("EmpId"));
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
    // 付与判定基準日を引数化し、年次有給休暇を付与
    public boolean grantAnnualLeave(EmpBean emp, LocalDate grantDate, String loginUser) {
        if (!isEligible(emp, grantDate)) return false;
        if (alreadyGranted(emp.getEmpId(), grantDate, LEAVE_TYPE_ANNUAL)) return false;
        int days = calcGrantedDays(emp);
        return insertLeaveBalance(emp.getEmpId(), LEAVE_TYPE_ANNUAL, grantDate, grantDate.plusYears(1), days, "auto", loginUser);
    }

    // 初回5日・5日付与（3か月／6か月）
    public boolean grantInitialAnnualLeave(EmpBean emp, int stage, LocalDate grantDate, String loginUser) {
        int days = 5;
        int leaveTypeId = (stage == 1) ? LEAVE_TYPE_INITIAL_3M : LEAVE_TYPE_INITIAL_6M;
        LocalDate targetDate = (stage == 1)
            ? emp.getEmpDate().plusMonths(3)
            : emp.getEmpDate().plusMonths(6);

        // 基準日と目標日を比較して付与判定（基準日が目標日以降でなければ付与不可）
        if (grantDate.isBefore(targetDate)) return false;
        if (!isEligible(emp, targetDate)) return false;
        if (alreadyGranted(emp.getEmpId(), targetDate, leaveTypeId)) return false;

        return insertLeaveBalance(emp.getEmpId(), leaveTypeId, targetDate, targetDate.plusYears(1), days, "初回" + days + "日付与", loginUser);
    }

    // 特別休暇（7月1日）
    public boolean grantSpecialLeave(EmpBean emp, LocalDate grantDate, String loginUser) {
        if (alreadyGranted(emp.getEmpId(), grantDate, LEAVE_TYPE_SPECIAL)) return false;

        return insertLeaveBalance(emp.getEmpId(), LEAVE_TYPE_SPECIAL, grantDate, grantDate.plusYears(1), 5, "特別休暇", loginUser);
    }

    // 代休（休日勤務があった翌日以降に人手で呼び出し）
    public boolean grantCompLeave(EmpBean emp, LocalDate workDate, String loginUser) {
        if (alreadyGranted(emp.getEmpId(), workDate, LEAVE_TYPE_COMP)) return false;

        return insertLeaveBalance(emp.getEmpId(), LEAVE_TYPE_COMP, workDate, workDate.plusMonths(1), 1, "休日勤務代休", loginUser);
    }

    // 勤続年数による年次有給休暇日数計算（第25条3項）
    public int calcGrantedDays(EmpBean emp) {
        LocalDate joinDate = emp.getEmpDate();
        LocalDate now = LocalDate.now();

        if (joinDate == null) return 0;

        long months = calcMonthsOfService(joinDate, now);
        if (months <= 3) return 0;  // 3か月以下は付与なし（別途初回5日付与の処理あり）
        else if (months <= 12) return 11;  // 3か月超〜1年未満
        else if (months <= 24) return 12;
        else if (months <= 36) return 14;
        else if (months <= 48) return 16;
        else if (months <= 60) return 18;
        else return 20;
    }

    // すでに付与済か確認（同日・同種）
    public boolean alreadyGranted(String empId, LocalDate date, int leaveTypeId) {
        String sql = "SELECT COUNT(*) FROM leave_balance WHERE EmpId = ? AND leave_type_id = ? AND grant_date = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empId);
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
    private boolean insertLeaveBalance(String empId, int leaveTypeId, LocalDate grantDate, LocalDate expireDate, int days, String source, String loginUser) {
        String sql = "INSERT INTO leave_balance (EmpId, leave_type_id, grant_date, expire_date, granted_days, used_days, source, created_at, created_by, updated_at, updated_by)"
                + " VALUES (?, ?, ?, ?, ?, 0, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empId);
            ps.setInt(2, leaveTypeId);
            ps.setDate(3, Date.valueOf(grantDate));
            ps.setDate(4, Date.valueOf(expireDate));
            ps.setInt(5, days);
            ps.setString(6, source);
            ps.setString(7, loginUser);
            ps.setString(8, loginUser); 
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 出勤率8割の判定（就業規則第25条・28条共通）
    public boolean isEligible(EmpBean emp, LocalDate baseDate) {
        LocalDate startDate = emp.getEmpDate();
        if (startDate == null || baseDate.isBefore(startDate)) return false;

        try (Connection conn = db.getConnection()) {
            int totalWorkingDays = countScheduledWorkDays(startDate, baseDate);
            int actualAttendanceDays = countActualWorkDays(emp.getEmpId(), startDate, baseDate);

            if (totalWorkingDays == 0) return false;
            double rate = (double) actualAttendanceDays / totalWorkingDays;
            return rate >= 0.8;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
}