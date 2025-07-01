package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KintaiTestDataGeneratorDB {

    static class Emp {
        String empNo;
        String empName;
        String deptId;
        String postId;
        int roleId;
        String empType;
        String password;
        String mail;
        LocalDate empDate;

        Emp(String empNo, String empName, String deptId, String postId, int roleId,
            String empType, String password, String mail, LocalDate empDate) {
            this.empNo = empNo;
            this.empName = empName;
            this.deptId = deptId;
            this.postId = postId;
            this.roleId = roleId;
            this.empType = empType;
            this.password = password;
            this.mail = mail;
            this.empDate = empDate;
        }
    }

    static DBAccess db = new DBAccess();

    public static void main(String[] args) {
        List<Emp> empList = Arrays.asList(
            new Emp("S0001", "石井 和也", "D02", "P03", 2, "正社員", "pass1", "abc@example.com", LocalDate.of(2015, 10, 29)),
            new Emp("S0002", "山崎 淳", "D02", "P02", 0, "正社員", "pass2", "abc@example.com", LocalDate.of(2017, 4, 21)),
            new Emp("S0003", "鈴木 七夏", "D02", "P02", 0, "正社員", "pass3", "abc@example.com", LocalDate.of(2015, 10, 14)),
            new Emp("S0004", "山下 京助", "D02", "P02", 0, "正社員", "pass4", "abc@example.com", LocalDate.of(2019, 1, 27)),
            new Emp("S0005", "山本 あすか", "D02", "P02", 0, "正社員", "pass5", "abc@example.com", LocalDate.of(2019, 11, 13))
        );

        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        Random rand = new Random();

        try (Connection conn = db.getConnection()) {
            for (Emp emp : empList) {
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    if (!currentDate.isBefore(emp.empDate)) {
                        Boolean isWork = isWorkingDay(currentDate, conn);
                        if (isWork == null) {
                            isWork = isWeekday(currentDate);
                        }
                        double prob = rand.nextDouble();
                        if (isWork) {
                            insertKintaiWithBreak(conn, emp.empNo, currentDate, prob);
                        } else if (prob < 0.1) {
                            insertKintaiWithBreak(conn, emp.empNo, currentDate, prob);
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

    static boolean isWeekday(LocalDate date) {
        int dow = date.getDayOfWeek().getValue();
        return dow >= 1 && dow <= 5;
    }

    static Boolean isWorkingDay(LocalDate date, Connection conn) {
        String sql = "SELECT is_work FROM calendar_event WHERE event_date = ? AND is_deleted = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_work");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void insertKintaiWithBreak(Connection conn, String empId, LocalDate workDate, double prob) throws SQLException {
        LocalTime clockIn = LocalTime.of(9, 0);
        LocalTime clockOut = LocalTime.of(18, 0);
        double overtime = 0.0;
        if (prob < 0.2) {
            int extraMinutes = (prob < 0.1) ? 120 : 60;
            clockOut = clockOut.plusMinutes(extraMinutes);
            overtime = extraMinutes / 60.0;
        }

        double nightHours = 0.0;
        if (clockOut.isAfter(LocalTime.of(22, 0))) {
            nightHours = (clockOut.toSecondOfDay() - LocalTime.of(22, 0).toSecondOfDay()) / 3600.0;
        }

        String sql = "INSERT INTO kintai (KINTAI_DATE, EMP_ID, CLOCK_IN, CLOCK_OUT, WORKING_HOURS, OVERTIME_HOURS, NIGHT_HOURS, IS_FINALIZED, IS_DELETED, CREATED_AT, UPDATED_AT) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(workDate));
            ps.setString(2, empId);
            ps.setTime(3, Time.valueOf(clockIn));
            ps.setTime(4, Time.valueOf(clockOut));
            ps.setDouble(5, 8.00);
            ps.setDouble(6, overtime);
            ps.setDouble(7, nightHours);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int kintaiId = keys.getInt(1);
                    insertBreak(conn, kintaiId, empId);
                }
            }
        }
    }

    static void insertBreak(Connection conn, int kintaiId, String empId) throws SQLException {
        String sql = "INSERT INTO break (KINTAI_REC_ID, EMP_ID, BREAK_START, BREAK_END, IS_DELETED, CREATED_BY, UPDATED_BY) " +
                     "VALUES (?, ?, '12:00:00', '13:00:00', FALSE, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kintaiId);
            ps.setString(2, empId);
            ps.setString(3, empId);
            ps.setString(4, empId);
            ps.executeUpdate();
        }
    }
}
