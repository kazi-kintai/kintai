package kintai;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

        try (PrintWriter writer = new PrintWriter(new FileWriter("kintai_test_data.sql"))) {
            for (Emp emp : empList) {
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    if (!currentDate.isBefore(emp.empDate)) {
                        boolean isWork = isWeekday(currentDate);
                        double prob = rand.nextDouble();
                        if (isWork || prob < 0.1) {
                            String kintaiSql = generateKintaiSQL(emp.empNo, currentDate, prob);
                            writer.println(kintaiSql);
                            String breakSql = generateBreakSQL(emp.empNo, currentDate);
                            writer.println(breakSql);
                        }
                    }
                    currentDate = currentDate.plusDays(1);
                }
            }
            System.out.println("SQLファイルに出力完了: kintai_test_data.sql");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean isWeekday(LocalDate date) {
        int dow = date.getDayOfWeek().getValue();
        return dow >= 1 && dow <= 5;
    }

    static String generateKintaiSQL(String empId, LocalDate workDate, double prob) {
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

        return String.format(
            "INSERT INTO kintai (KINTAI_DATE, EMP_ID, CLOCK_IN, CLOCK_OUT, WORKING_HOURS, OVERTIME_HOURS, NIGHT_HOURS, IS_FINALIZED, IS_DELETED, CREATED_AT, UPDATED_AT) " +
            "VALUES ('%s', '%s', '%s', '%s', %.2f, %.2f, %.2f, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);",
            workDate, empId, clockIn, clockOut, 8.00, overtime, nightHours
        );
    }

    static String generateBreakSQL(String empId, LocalDate workDate) {
        return String.format(
            "INSERT INTO break (KINTAI_REC_ID, EMP_ID, BREAK_START, BREAK_END, IS_DELETED, CREATED_BY, UPDATED_BY) " +
            "VALUES ((SELECT KINTAI_REC_ID FROM kintai WHERE KINTAI_DATE = '%s' AND EMP_ID = '%s'), '%s', '12:00:00', '13:00:00', FALSE, '%s', '%s');",
            workDate, empId, empId, empId, empId
        );
    }
}
