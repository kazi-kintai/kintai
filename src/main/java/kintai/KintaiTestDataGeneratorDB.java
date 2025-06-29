package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KintaiTestDataGeneratorDB {

    static class Emp {
        String empNo;
        String empName;
        LocalDate empDate;
        double workRate;
        double holidayWorkRate;

        Emp(String empNo, String empName, LocalDate empDate, double workRate, double holidayWorkRate) {
            this.empNo = empNo;
            this.empName = empName;
            this.empDate = empDate;
            this.workRate = workRate;
            this.holidayWorkRate = holidayWorkRate;
        }
    }

    static DBAccess db = new DBAccess();

    public static void main(String[] args) {
        List<Emp> empList = Arrays.asList(
            new Emp("E001", "山田太郎", LocalDate.of(2024, 6, 1), 0.9, 0.1),
            new Emp("E002", "鈴木一郎", LocalDate.of(2025, 1, 15), 0.7, 0.05),
            new Emp("E003", "佐藤花子", LocalDate.of(2024, 11, 1), 0.95, 0.15),
            new Emp("E004", "田中次郎", LocalDate.of(2023, 5, 1), 0.85, 0.0),
            new Emp("E005", "中村三郎", LocalDate.of(2025, 3, 1), 0.8, 0.1)
        );

        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);

        Random rand = new Random();

        try (Connection conn = db.getConnection()) {
            for (Emp emp : empList) {
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    if (!currentDate.isBefore(emp.empDate)) {
                        Boolean isWork = isWorkingDay(currentDate, conn);
                        if (isWork == null) {
                            // 曜日で判断（例外が登録されていない場合）
                            isWork = isWeekday(currentDate);
                        }

                        double prob = rand.nextDouble();
                     // まずは出勤予定日を確定的に勤務データとして挿入
                        if (isWork) {
                            insertKintai(conn, emp.empNo, currentDate);
                        } else {
                            if (prob < emp.holidayWorkRate) {
                                insertKintai(conn, emp.empNo, currentDate);
                            }
                        }
                    }
                    currentDate = currentDate.plusDays(1);
                }
            }
            System.out.println("データ挿入完了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 曜日ベースで平日か判定（月〜金）
    static boolean isWeekday(LocalDate date) {
        int dow = date.getDayOfWeek().getValue();
        return dow >= 1 && dow <= 5;
    }

    // calendar_event からその日が出勤日（is_work = true）か取得
    static boolean isWorkingDay(LocalDate date, Connection conn) {
        int dow = date.getDayOfWeek().getValue();
        boolean defaultIsWork = (dow >= 1 && dow <= 5); // 平日は勤務日、土日は休日
        String sql = "SELECT is_work FROM calendar_event WHERE event_date = ? AND is_deleted = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 例外的に設定されている勤務情報で上書き
                    return rs.getBoolean("is_work");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultIsWork;
    }

    static void insertKintai(Connection conn, String empId, LocalDate workDate) throws SQLException {
        String sql = "INSERT INTO kintai (KINTAI_DATE, EMP_ID, CLOCK_IN, CLOCK_OUT, WORKING_HOURS, OVERTIME_HOURS, NIGHT_HOURS, IS_FINALIZED, IS_DELETED, CREATED_AT, UPDATED_AT) " +
                     "VALUES (?, ?, '09:00:00', '18:00:00', 8.00, 0.00, 0.00, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(workDate));
            ps.setString(2, empId);
            ps.executeUpdate();
        }
    }
}
