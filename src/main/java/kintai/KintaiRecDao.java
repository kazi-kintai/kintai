package kintai;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 勤怠記録表示（kintai_rec.jsp）に関連するデータアクセスを担当するクラス (DAO)。
 * kintai、break、emp、dept、postテーブルからデータを結合して取得する。
 */
public class KintaiRecDao {

    private DBAccess db = new DBAccess();

    /**
     * 指定された条件に基づいて勤怠記録のリストを取得します。
     * このメソッドは、一般社員、管理者、および将来の主任/リーダーの権限に対応します。
     * * @param targetEmpNos 検索対象の従業員番号リスト (一般社員の場合は自身のempno、管理者の場合は空リストまたは指定されたempno、主任の場合は部下のempnoリスト)
     * このリストが空の場合、empNoFilterがnullまたは空であれば全従業員を対象とし、そうでない場合は指定されたempNoFilterを適用する
     * @param deptNoFilter 部署番号によるフィルター (nullまたは空の場合はフィルターしない)
     * @param postNoFilter 役職番号によるフィルター (nullまたは空の場合はフィルターしない)
     * @param startDate 検索期間の開始日 (nullの場合はフィルターしない)
     * @param endDate 検索期間の終了日 (nullの場合はフィルターしない)
     * @param userRole ログイン中のユーザーの権限 (0:一般社員, 1:管理者, 2:主任/リーダーなど)
     * @return 勤怠記録のリスト（KintaiRecBeanオブジェクト）。見つからない場合は空のリスト。
     */
    public List<KintaiRecBean> getKintaiRecords(
            List<String> targetEmpNos, String deptNoFilter, String postNoFilter,
            LocalDate startDate, LocalDate endDate, int userRole) {

        List<KintaiRecBean> kintaiRecList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        sql.append("  k.RECID, k.KINTAIDATE, k.EMPNO, k.CLOCKIN, k.CLOCKOUT, ");
        sql.append("  e.EMPNAME, e.DEPTNO, d.DEPTNAME, e.POSTNO, p.POSTNAME ");
        sql.append("FROM kintai k ");
        sql.append("LEFT JOIN emp e ON k.EMPNO = e.EMPNO ");
        sql.append("LEFT JOIN dept d ON e.DEPTNO = d.DEPTNO ");
        sql.append("LEFT JOIN post p ON e.POSTNO = p.POSTNO ");
        sql.append("WHERE 1=1 "); // WHERE句の条件を容易に追加するためのダミー

        List<Object> params = new ArrayList<>(); // プリペアドステートメントのパラメータリスト

        // --- 従業員番号によるフィルター（権限に基づく）---
        if (userRole == 0) { // 一般社員の場合、自身の勤怠のみ
            sql.append("AND k.EMPNO = ? ");
            params.add(targetEmpNos.get(0)); // targetEmpNosには自身のempnoが1つだけ入っている
        } else { // 管理者または主任/リーダーの場合
            if (targetEmpNos != null && !targetEmpNos.isEmpty()) {
                // 特定の従業員リストが指定されている場合 (例: 主任の部下、または管理者による単一従業員検索)
                sql.append("AND k.EMPNO IN (");
                for (int i = 0; i < targetEmpNos.size(); i++) {
                    sql.append("?");
                    if (i < targetEmpNos.size() - 1) {
                        sql.append(", ");
                    }
                }
                sql.append(") ");
                params.addAll(targetEmpNos);
            }
            // else if (empNoFilter != null && !empNoFilter.trim().isEmpty()) {
            //     // 単一のempNoFilterが指定されている場合 (管理者による検索)
            //     sql.append("AND k.EMPNO = ? ");
            //     params.add(empNoFilter);
            // }
            // ↑ KintaiRecServlet側でempNoFilterをtargetEmpNosに変換するようにしたので、上記のコメントアウトは不要
        }


        // --- その他のフィルター条件 ---
        if (deptNoFilter != null && !deptNoFilter.trim().isEmpty()) {
            sql.append("AND e.DEPTNO = ? ");
            params.add(deptNoFilter);
        }
        if (postNoFilter != null && !postNoFilter.trim().isEmpty()) {
            sql.append("AND e.POSTNO = ? ");
            params.add(postNoFilter);
        }
        if (startDate != null) {
            sql.append("AND k.KINTAIDATE >= ? ");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append("AND k.KINTAIDATE <= ? ");
            params.add(Date.valueOf(endDate));
        }

        sql.append("ORDER BY k.KINTAIDATE DESC, k.EMPNO ASC"); // 日付の新しい順、従業員番号の昇順でソート

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // パラメータをセット
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Date) {
                    ps.setDate(i + 1, (Date) param);
                }
                // 他のデータ型が必要な場合はここに追加
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KintaiRecBean bean = new KintaiRecBean();
                    bean.setRecId(rs.getInt("RECID"));
                    bean.setKintaiDate(rs.getDate("KINTAIDATE").toLocalDate());
                    bean.setEmpno(rs.getString("EMPNO"));
                    bean.setClockIn(rs.getTime("CLOCKIN"));
                    bean.setClockOut(rs.getTime("CLOCKOUT"));
                    bean.setEmpName(rs.getString("EMPNAME"));
                    bean.setDeptNo(rs.getString("DEPTNO"));
                    bean.setDeptName(rs.getString("DEPTNAME"));
                    bean.setPostNo(rs.getString("POSTNO"));
                    bean.setPostName(rs.getString("POSTNAME"));

                    // 各勤怠記録の休憩時間を取得し、合計休憩時間を計算
                    long totalBreakMinutes = calculateTotalBreakMinutes(bean.getRecId());
                    bean.setTotalBreakMinutes(totalBreakMinutes);

                    // 実働時間を計算
                    long actualWorkMinutes = calculateActualWorkMinutes(bean.getClockIn(), bean.getClockOut(), totalBreakMinutes);
                    bean.setActualWorkMinutes(actualWorkMinutes);

                    // 残業時間を計算（実働時間が8時間を超えた分）
                    long overtimeMinutes = Math.max(0, actualWorkMinutes - (8 * 60)); // 8時間 = 480分
                    bean.setOvertimeMinutes(overtimeMinutes);

                    kintaiRecList.add(bean);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // エラーハンドリング：必要に応じてログ出力や例外スロー
        }
        return kintaiRecList;
    }

    /**
     * 指定されたRECIDの勤怠記録に関連する全ての休憩時間の合計を計算します。
     * @param recId 勤怠記録ID
     * @return 合計休憩時間（分単位）
     */
    private long calculateTotalBreakMinutes(int recId) {
        long totalMinutes = 0;
        String sql = "SELECT BREAKSTART, BREAKEND FROM break WHERE RECID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time breakStart = rs.getTime("BREAKSTART");
                    Time breakEnd = rs.getTime("BREAKEND");

                    if (breakStart != null && breakEnd != null) {
                        LocalTime start = breakStart.toLocalTime();
                        LocalTime end = breakEnd.toLocalTime();
                        // 休憩終了が休憩開始より前の場合は、日付を跨いだと見なして24時間を加算
                        if (end.isBefore(start)) {
                            Duration duration = Duration.between(start, LocalTime.MAX).plus(Duration.between(LocalTime.MIDNIGHT, end));
                            totalMinutes += duration.toMinutes();
                        } else {
                            totalMinutes += Duration.between(start, end).toMinutes();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // エラーハンドリング
        }
        return totalMinutes;
    }

    /**
     * 出勤時刻、退勤時刻、合計休憩時間から実働時間を計算します。
     * @param clockIn 出勤時刻
     * @param clockOut 退勤時刻
     * @param totalBreakMinutes 合計休憩時間（分単位）
     * @return 実働時間（分単位）
     */
    private long calculateActualWorkMinutes(Time clockIn, Time clockOut, long totalBreakMinutes) {
        if (clockIn == null || clockOut == null) {
            return 0; // 出勤または退勤がない場合は実働時間0
        }

        LocalTime start = clockIn.toLocalTime();
        LocalTime end = clockOut.toLocalTime();

        long workDurationMinutes = 0;
        // 退勤時刻が翌日になる場合（例: 22:00出勤 -> 02:00退勤）を考慮
        if (end.isBefore(start)) {
            Duration duration = Duration.between(start, LocalTime.MAX).plus(Duration.between(LocalTime.MIDNIGHT, end));
            workDurationMinutes = duration.toMinutes();
        } else {
            workDurationMinutes = Duration.between(start, end).toMinutes();
        }
        
        // 実働時間 = (退勤時刻 - 出勤時刻) - 総休憩時間
        long actualMinutes = workDurationMinutes - totalBreakMinutes;
        return Math.max(0, actualMinutes); // マイナスにならないように0以上を保証
    }

    /**
     * 指定された従業員・月の月度勤怠統計を取得します
     * @param empno 従業員番号
     * @param targetMonth 対象月 (YYYY-MM)
     * @return 月度統計データ
     */
    public MonthlySummaryBean getMonthlySummary(String empno, String targetMonth) {
        MonthlySummaryBean summary = new MonthlySummaryBean();
        summary.setTargetMonth(targetMonth);

        try {
            // 対象月の開始日と終了日を計算
            YearMonth ym = YearMonth.parse(targetMonth);
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();

            // 1. 総出社日数を計算
            int totalWorkDays = calculateTotalWorkDays(monthStart, monthEnd);
            summary.setTotalWorkDays(totalWorkDays);

            // 2. 実際の出勤日数を取得
            int actualAttendanceDays = getActualAttendanceDays(empno, monthStart, monthEnd);
            summary.setActualAttendanceDays(actualAttendanceDays);

            // 3. 月度の勤怠統計を計算
            calculateMonthlyWorkingHours(empno, monthStart, monthEnd, summary);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return summary;
    }

    /**
     * 指定期間の総出社日数を計算（週末とカレンダーの休日を除く）
     */
    private int calculateTotalWorkDays(LocalDate monthStart, LocalDate monthEnd) {
        int workDays = 0;
        LocalDate current = monthStart;

        while (!current.isAfter(monthEnd)) {
            // 週末を除外
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && 
                current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                // カレンダーテーブルの休日をチェック
                if (!isHolidayInCalendar(current)) {
                    workDays++;
                }
            }
            current = current.plusDays(1);
        }

        return workDays;
    }

    /**
     * カレンダーテーブルで指定日が休日かどうかをチェック
     * カレンダーテーブルが存在しない場合は、基本的な休日判定のみ行う
     */
    private boolean isHolidayInCalendar(LocalDate date) {
        // まずカレンダーテーブルの存在をチェック
        try (Connection conn = db.getConnection()) {
            String checkTableSql = "SHOW TABLES LIKE 'calendar'";
            PreparedStatement checkStmt = conn.prepareStatement(checkTableSql);
            ResultSet tableRs = checkStmt.executeQuery();
            
            if (tableRs.next()) {
                // カレンダーテーブルが存在する場合の処理
                String sql = "SELECT COUNT(*) FROM calendar WHERE EVENT_DATE = ? AND IS_HOLIDAY = 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setDate(1, Date.valueOf(date));
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } else {
                // カレンダーテーブルが存在しない場合は、基本的な祝日をハードコーディングで判定
                return isBasicHoliday(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // エラー時は基本的な祝日判定にフォールバック
            return isBasicHoliday(date);
        }
        
        return false;
    }
    
    /**
     * 基本的な日本の祝日判定（簡易版）
     */
    private boolean isBasicHoliday(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        
        // 基本的な固定祝日のみをチェック
        switch (month) {
            case 1: // 元日、成人の日
                return day == 1 || (day >= 8 && day <= 14 && date.getDayOfWeek() == DayOfWeek.MONDAY);
            case 2: // 建国記念の日、天皇誕生日
                return day == 11 || day == 23;
            case 3: // 春分の日（概算：20日または21日）
                return day == 20 || day == 21;
            case 4: // 昭和の日
                return day == 29;
            case 5: // 憲法記念日、みどりの日、こどもの日
                return day == 3 || day == 4 || day == 5;
            case 7: // 海の日（7月第3月曜日）
                return day >= 15 && day <= 21 && date.getDayOfWeek() == DayOfWeek.MONDAY;
            case 8: // 山の日
                return day == 11;
            case 9: // 敬老の日、秋分の日
                return (day >= 15 && day <= 21 && date.getDayOfWeek() == DayOfWeek.MONDAY) || day == 22 || day == 23;
            case 10: // スポーツの日（10月第2月曜日）
                return day >= 8 && day <= 14 && date.getDayOfWeek() == DayOfWeek.MONDAY;
            case 11: // 文化の日、勤労感謝の日
                return day == 3 || day == 23;
            case 12: // なし
                return false;
            default:
                return false;
        }
    }

    /**
     * 指定期間の実際の出勤日数を取得
     */
    private int getActualAttendanceDays(String empno, LocalDate monthStart, LocalDate monthEnd) {
        String sql = "SELECT COUNT(*) FROM kintai WHERE EMPNO = ? AND KINTAIDATE >= ? AND KINTAIDATE <= ? AND CLOCKIN IS NOT NULL";
        
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, empno);
            stmt.setDate(2, Date.valueOf(monthStart));
            stmt.setDate(3, Date.valueOf(monthEnd));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * 月度の労働時間統計を計算
     */
    private void calculateMonthlyWorkingHours(String empno, LocalDate monthStart, LocalDate monthEnd, MonthlySummaryBean summary) {
        String sql = "SELECT RECID, KINTAIDATE, CLOCKIN, CLOCKOUT, WORKING_HOURS, OVERTIME_HOURS FROM kintai " +
                     "WHERE EMPNO = ? AND KINTAIDATE >= ? AND KINTAIDATE <= ? AND CLOCKIN IS NOT NULL";
        
        BigDecimal totalWorkingHours = BigDecimal.ZERO;
        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        BigDecimal totalBreakHours = BigDecimal.ZERO;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, empno);
            stmt.setDate(2, Date.valueOf(monthStart));
            stmt.setDate(3, Date.valueOf(monthEnd));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int recId = rs.getInt("RECID");
                LocalDate kintaiDate = rs.getDate("KINTAIDATE").toLocalDate();
                
                // 実働時間を累積
                BigDecimal workingHours = rs.getBigDecimal("WORKING_HOURS");
                if (workingHours != null) {
                    totalWorkingHours = totalWorkingHours.add(workingHours);
                }
                
                // 残業時間を累積
                BigDecimal overtimeHours = rs.getBigDecimal("OVERTIME_HOURS");
                if (overtimeHours != null) {
                    totalOvertimeHours = totalOvertimeHours.add(overtimeHours);
                }
                
                // 休憩時間を計算して累積（正しいrecIdを使用）
                long breakMinutes = calculateTotalBreakMinutes(recId);
                BigDecimal breakHours = BigDecimal.valueOf(breakMinutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
                totalBreakHours = totalBreakHours.add(breakHours);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        summary.setTotalWorkingHours(totalWorkingHours);
        summary.setTotalOvertimeHours(totalOvertimeHours);
        summary.setTotalBreakHours(totalBreakHours);
    }
    
    /**
     * 指定日の出勤予定者数を取得（全従業員数を返す）
     * @param date 対象日
     * @return 出勤予定者数
     */
    public int getScheduledEmployeeCount(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM emp WHERE ROLEID != 999"; // 999は退職者など除外
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 指定日の出勤中者数を取得（CLOCKINがあってCLOCKOUTがないレコード）
     * @param date 対象日
     * @return 出勤中者数
     */
    public int getWorkingEmployeeCount(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM kintai WHERE KINTAIDATE = ? AND CLOCKIN IS NOT NULL AND CLOCKOUT IS NULL";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 指定日の未出勤者数を取得（その日の勤怠記録がない従業員）
     * @param date 対象日
     * @return 未出勤者数
     */
    public int getAbsentEmployeeCount(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM emp e WHERE e.ROLEID != 999 AND NOT EXISTS " +
                    "(SELECT 1 FROM kintai k WHERE k.EMPNO = e.EMPNO AND k.KINTAIDATE = ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 指定日の休暇予定者数を取得
     * 実装注：現在の段階では休暇管理テーブルがないため、固定値またはダミー計算を返す
     * @param date 対象日
     * @return 休暇予定者数
     */
    public int getVacationEmployeeCount(LocalDate date) {
        // TODO: 将来的に休暇管理テーブルが実装されたら、以下のようなSQLに変更
        // String sql = "SELECT COUNT(*) FROM vacation WHERE vacation_date = ? AND status = 'approved'";
        
        // 現在はダミーデータとして、土日の場合は多め、平日は少なめの休暇者数を返す
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return 5; // 土日は休暇扱いが多い
        } else {
            return 2; // 平日は有給休暇者が少数
        }
    }
}
