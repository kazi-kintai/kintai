package kintai;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * 法令遵守チェックと会社規則チェック機能
 * 労働基準法および会社規則に基づく勤怠管理の合規性をチェックする
 */
public class ComplianceChecker {
    
    // 労働基準法の基準値
    private static final int LEGAL_DAILY_WORK_HOURS = 8;        // 法定労働時間（日）
    private static final int LEGAL_WEEKLY_WORK_HOURS = 40;      // 法定労働時間（週）
    private static final int LEGAL_MONTHLY_OVERTIME_LIMIT = 45; // 月間残業時間上限
    private static final int LEGAL_YEARLY_OVERTIME_LIMIT = 360; // 年間残業時間上限
    private static final int CONTINUOUS_WORK_LIMIT = 6;         // 連続勤務日数上限
    
    // 会社規則の基準値（第15条に基づく）
    private static final LocalTime COMPANY_START_TIME = LocalTime.of(9, 0);     // 始業時刻 午前9時00分
    private static final LocalTime COMPANY_END_TIME = LocalTime.of(18, 0);      // 終業時刻 午後6時00分
    private static final LocalTime COMPANY_LUNCH_START = LocalTime.of(12, 0);   // 休憩開始 正午
    private static final LocalTime COMPANY_LUNCH_END = LocalTime.of(13, 0);     // 休憩終了 午後1時
    private static final int COMPANY_LUNCH_BREAK_MINUTES = 60;                  // 昼休憩時間（分）
    private static final int COMPANY_MAX_LATE_MINUTES = 0;                      // 遅刻許容時間（分）規程上は厳格
    private static final int COMPANY_STANDARD_WORK_HOURS = 8;                   // 1日の標準労働時間
    private static final int COMPANY_MAX_MONTHLY_ABSENT_DAYS = 3;               // 月間欠勤日数上限
    
    /**
     * 法令遵守チェックの実行
     */
    public ComplianceCheckResult performLegalComplianceCheck(List<KintaiRecBean> records, String empno) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        result.setEmpno(empno);
        result.setCheckDate(LocalDate.now());
        result.setCheckType("法令遵守チェック");
        
        List<ComplianceViolation> violations = new ArrayList<>();
        
        // 1. 日次労働時間チェック（8時間超過）
        violations.addAll(checkDailyWorkHours(records));
        
        // 2. 月間残業時間チェック（45時間超過）
        violations.addAll(checkMonthlyOvertime(records));
        
        // 3. 連続勤務日数チェック（6日超過）
        violations.addAll(checkContinuousWorkDays(records));
        
        // 4. 休憩時間チェック（6時間以上勤務で休憩なし）
        violations.addAll(checkBreakTime(records));
        
        // 5. 深夜勤務チェック（22時〜5時）
        violations.addAll(checkNightWork(records));
        
        result.setViolations(violations);
        result.setTotalViolations(violations.size());
        result.setComplianceScore(calculateComplianceScore(violations, records.size()));
        
        return result;
    }
    
    /**
     * 会社規則チェックの実行
     */
    public ComplianceCheckResult performCompanyRulesCheck(List<KintaiRecBean> records, String empno) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        result.setEmpno(empno);
        result.setCheckDate(LocalDate.now());
        result.setCheckType("会社規則チェック");
        
        List<ComplianceViolation> violations = new ArrayList<>();
        
        // 1. 遅刻チェック
        violations.addAll(checkLateness(records));
        
        // 2. 早退チェック
        violations.addAll(checkEarlyLeaving(records));
        
        // 3. 欠勤チェック
        violations.addAll(checkAbsence(records));
        
        // 4. 休憩時間の適切性チェック
        violations.addAll(checkProperBreakTime(records));
        
        // 5. 勤務態度チェック（頻繁な遅刻・早退）
        violations.addAll(checkWorkAttitude(records));
        
        // 6. 休日勤務チェック（第16条に基づく）
        violations.addAll(checkHolidayWork(records));
        
        result.setViolations(violations);
        result.setTotalViolations(violations.size());
        result.setComplianceScore(calculateComplianceScore(violations, records.size()));
        
        return result;
    }
    
    /**
     * 日次労働時間チェック（法定8時間超過）
     */
    private List<ComplianceViolation> checkDailyWorkHours(List<KintaiRecBean> records) {
        return records.stream()
            .filter(record -> record.getActualWorkMinutes() > LEGAL_DAILY_WORK_HOURS * 60)
            .map(record -> {
                ComplianceViolation violation = new ComplianceViolation();
                violation.setViolationType("法定労働時間超過");
                violation.setDate(record.getKintaiDate());
                violation.setSeverity("高");
                violation.setDescription(String.format("実働時間%sが法定労働時間8時間を超過しています", 
                    record.getActualWorkTimeFormatted()));
                violation.setLegalBasis("労働基準法第32条");
                return violation;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 月間残業時間チェック（45時間超過）
     */
    private List<ComplianceViolation> checkMonthlyOvertime(List<KintaiRecBean> records) {
        List<ComplianceViolation> violations = new ArrayList<>();
        
        long totalOvertimeMinutes = records.stream()
            .mapToLong(KintaiRecBean::getOvertimeMinutes)
            .sum();
        
        double totalOvertimeHours = totalOvertimeMinutes / 60.0;
        
        if (totalOvertimeHours > LEGAL_MONTHLY_OVERTIME_LIMIT) {
            ComplianceViolation violation = new ComplianceViolation();
            violation.setViolationType("月間残業時間超過");
            violation.setDate(LocalDate.now());
            violation.setSeverity("高");
            violation.setDescription(String.format("月間残業時間%.1f時間が法定上限45時間を超過しています", 
                totalOvertimeHours));
            violation.setLegalBasis("労働基準法第36条");
            violations.add(violation);
        }
        
        return violations;
    }
    
    /**
     * 連続勤務日数チェック（6日超過）
     */
    private List<ComplianceViolation> checkContinuousWorkDays(List<KintaiRecBean> records) {
        List<ComplianceViolation> violations = new ArrayList<>();
        
        int continuousWorkDays = 0;
        LocalDate lastWorkDate = null;
        
        for (KintaiRecBean record : records) {
            if (record.getClockIn() != null) {
                if (lastWorkDate != null && record.getKintaiDate().equals(lastWorkDate.plusDays(1))) {
                    continuousWorkDays++;
                } else {
                    continuousWorkDays = 1;
                }
                
                if (continuousWorkDays > CONTINUOUS_WORK_LIMIT) {
                    ComplianceViolation violation = new ComplianceViolation();
                    violation.setViolationType("連続勤務日数超過");
                    violation.setDate(record.getKintaiDate());
                    violation.setSeverity("中");
                    violation.setDescription(String.format("連続勤務%d日が法定上限6日を超過しています", 
                        continuousWorkDays));
                    violation.setLegalBasis("労働基準法第35条");
                    violations.add(violation);
                }
                
                lastWorkDate = record.getKintaiDate();
            } else {
                continuousWorkDays = 0;
                lastWorkDate = null;
            }
        }
        
        return violations;
    }
    
    /**
     * 休憩時間チェック（6時間以上勤務で休憩なし）
     */
    private List<ComplianceViolation> checkBreakTime(List<KintaiRecBean> records) {
        return records.stream()
            .filter(record -> record.getActualWorkMinutes() > 6 * 60 && 
                             record.getTotalBreakMinutes() < 45)
            .map(record -> {
                ComplianceViolation violation = new ComplianceViolation();
                violation.setViolationType("休憩時間不足");
                violation.setDate(record.getKintaiDate());
                violation.setSeverity("中");
                violation.setDescription(String.format("6時間以上勤務に対し休憩時間%sが不足しています", 
                    record.getTotalBreakTimeFormatted()));
                violation.setLegalBasis("労働基準法第34条");
                return violation;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 深夜勤務チェック（22時〜5時）
     */
    private List<ComplianceViolation> checkNightWork(List<KintaiRecBean> records) {
        return records.stream()
            .filter(record -> {
                if (record.getClockOut() == null) return false;
                LocalTime clockOut = record.getClockOut().toLocalTime();
                return clockOut.isAfter(LocalTime.of(22, 0)) || clockOut.isBefore(LocalTime.of(5, 0));
            })
            .map(record -> {
                ComplianceViolation violation = new ComplianceViolation();
                violation.setViolationType("深夜勤務");
                violation.setDate(record.getKintaiDate());
                violation.setSeverity("中");
                violation.setDescription(String.format("深夜時間帯（22:00〜5:00）での勤務が確認されました（退勤: %s）", 
                    record.getClockOut().toLocalTime()));
                violation.setLegalBasis("労働基準法第37条");
                return violation;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 遅刻チェック
     */
    private List<ComplianceViolation> checkLateness(List<KintaiRecBean> records) {
        return records.stream()
            .filter(record -> {
                if (record.getClockIn() == null) return false;
                LocalTime clockIn = record.getClockIn().toLocalTime();
                return clockIn.isAfter(COMPANY_START_TIME);
            })
            .map(record -> {
                ComplianceViolation violation = new ComplianceViolation();
                violation.setViolationType("遅刻");
                violation.setDate(record.getKintaiDate());
                
                LocalTime clockIn = record.getClockIn().toLocalTime();
                long lateMinutes = java.time.Duration.between(COMPANY_START_TIME, clockIn).toMinutes();
                
                violation.setSeverity(lateMinutes > COMPANY_MAX_LATE_MINUTES ? "高" : "低");
                violation.setDescription(String.format("出勤時刻%sが標準時刻9:00より%d分遅れています", 
                    clockIn, lateMinutes));
                violation.setLegalBasis("就業規則第○条");
                return violation;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 早退チェック
     */
    private List<ComplianceViolation> checkEarlyLeaving(List<KintaiRecBean> records) {
        return records.stream()
            .filter(record -> {
                if (record.getClockOut() == null) return false;
                LocalTime clockOut = record.getClockOut().toLocalTime();
                return clockOut.isBefore(COMPANY_END_TIME);
            })
            .map(record -> {
                ComplianceViolation violation = new ComplianceViolation();
                violation.setViolationType("早退");
                violation.setDate(record.getKintaiDate());
                violation.setSeverity("中");
                
                LocalTime clockOut = record.getClockOut().toLocalTime();
                long earlyMinutes = java.time.Duration.between(clockOut, COMPANY_END_TIME).toMinutes();
                
                violation.setDescription(String.format("退勤時刻%sが標準時刻18:00より%d分早いです", 
                    clockOut, earlyMinutes));
                violation.setLegalBasis("就業規則第○条");
                return violation;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 欠勤チェック
     */
    private List<ComplianceViolation> checkAbsence(List<KintaiRecBean> records) {
        List<ComplianceViolation> violations = new ArrayList<>();
        
        long absentDays = records.stream()
            .filter(record -> record.getClockIn() == null && record.getClockOut() == null)
            .filter(record -> record.getKintaiDate().getDayOfWeek() != DayOfWeek.SATURDAY && 
                             record.getKintaiDate().getDayOfWeek() != DayOfWeek.SUNDAY)
            .count();
        
        if (absentDays > COMPANY_MAX_MONTHLY_ABSENT_DAYS) {
            ComplianceViolation violation = new ComplianceViolation();
            violation.setViolationType("月間欠勤日数超過");
            violation.setDate(LocalDate.now());
            violation.setSeverity("高");
            violation.setDescription(String.format("月間欠勤日数%d日が許容上限%d日を超過しています", 
                absentDays, COMPANY_MAX_MONTHLY_ABSENT_DAYS));
            violation.setLegalBasis("就業規則第○条");
            violations.add(violation);
        }
        
        return violations;
    }
    
    /**
     * 適切な休憩時間チェック
     */
    private List<ComplianceViolation> checkProperBreakTime(List<KintaiRecBean> records) {
        return records.stream()
            .filter(record -> record.getActualWorkMinutes() > 6 * 60)
            .filter(record -> Math.abs(record.getTotalBreakMinutes() - COMPANY_LUNCH_BREAK_MINUTES) > 15)
            .map(record -> {
                ComplianceViolation violation = new ComplianceViolation();
                violation.setViolationType("休憩時間異常");
                violation.setDate(record.getKintaiDate());
                violation.setSeverity("低");
                violation.setDescription(String.format("休憩時間%sが標準時間60分と大きく異なります", 
                    record.getTotalBreakTimeFormatted()));
                violation.setLegalBasis("就業規則第○条");
                return violation;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 勤務態度チェック（頻繁な遅刻・早退）
     */
    private List<ComplianceViolation> checkWorkAttitude(List<KintaiRecBean> records) {
        List<ComplianceViolation> violations = new ArrayList<>();
        
        long lateCount = records.stream()
            .filter(record -> record.getClockIn() != null)
            .filter(record -> record.getClockIn().toLocalTime().isAfter(COMPANY_START_TIME))
            .count();
        
        long earlyLeaveCount = records.stream()
            .filter(record -> record.getClockOut() != null)
            .filter(record -> record.getClockOut().toLocalTime().isBefore(COMPANY_END_TIME))
            .count();
        
        if (lateCount >= 5) {
            ComplianceViolation violation = new ComplianceViolation();
            violation.setViolationType("頻繁な遅刻");
            violation.setDate(LocalDate.now());
            violation.setSeverity("中");
            violation.setDescription(String.format("月間遅刻回数%d回が多発しています", lateCount));
            violation.setLegalBasis("就業規則第○条");
            violations.add(violation);
        }
        
        if (earlyLeaveCount >= 3) {
            ComplianceViolation violation = new ComplianceViolation();
            violation.setViolationType("頻繁な早退");
            violation.setDate(LocalDate.now());
            violation.setSeverity("中");
            violation.setDescription(String.format("月間早退回数%d回が多発しています", earlyLeaveCount));
            violation.setLegalBasis("就業規則第○条");
            violations.add(violation);
        }
        
        return violations;
    }
    
    /**
     * 休日勤務チェック（第16条に基づく：土曜日、日曜日、国民の祝日）
     */
    private List<ComplianceViolation> checkHolidayWork(List<KintaiRecBean> records) {
        return records.stream()
            .filter(record -> {
                // 土曜日または日曜日の勤務をチェック
                DayOfWeek dayOfWeek = record.getKintaiDate().getDayOfWeek();
                return (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) 
                       && (record.getClockIn() != null || record.getClockOut() != null);
            })
            .map(record -> {
                ComplianceViolation violation = new ComplianceViolation();
                violation.setViolationType("休日勤務");
                violation.setDate(record.getKintaiDate());
                violation.setSeverity("中");
                
                String dayName = record.getKintaiDate().getDayOfWeek() == DayOfWeek.SATURDAY ? "土曜日" : "日曜日";
                violation.setDescription(String.format("所定休日（%s）に勤務が確認されました", dayName));
                violation.setLegalBasis("就業規則第16条");
                return violation;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 合規スコアの計算
     */
    private double calculateComplianceScore(List<ComplianceViolation> violations, int totalRecords) {
        if (totalRecords == 0) return 100.0;
        
        double penaltyScore = violations.stream()
            .mapToDouble(violation -> {
                switch (violation.getSeverity()) {
                    case "高": return 5.0;
                    case "中": return 3.0;
                    case "低": return 1.0;
                    default: return 1.0;
                }
            })
            .sum();
        
        double maxPossibleScore = totalRecords * 5.0;
        double score = Math.max(0, 100.0 - (penaltyScore / maxPossibleScore * 100));
        
        return Math.round(score * 10.0) / 10.0;
    }
    
    /**
     * 統合的な合規チェック（法令遵守 + 会社規則）
     */
    public ComplianceCheckResult performComprehensiveCheck(List<KintaiRecBean> records, String empno) {
        ComplianceCheckResult legalResult = performLegalComplianceCheck(records, empno);
        ComplianceCheckResult companyResult = performCompanyRulesCheck(records, empno);
        
        ComplianceCheckResult comprehensiveResult = new ComplianceCheckResult();
        comprehensiveResult.setEmpno(empno);
        comprehensiveResult.setCheckDate(LocalDate.now());
        comprehensiveResult.setCheckType("総合合規チェック");
        
        List<ComplianceViolation> allViolations = new ArrayList<>();
        allViolations.addAll(legalResult.getViolations());
        allViolations.addAll(companyResult.getViolations());
        
        comprehensiveResult.setViolations(allViolations);
        comprehensiveResult.setTotalViolations(allViolations.size());
        
        double averageScore = (legalResult.getComplianceScore() + companyResult.getComplianceScore()) / 2.0;
        comprehensiveResult.setComplianceScore(averageScore);
        
        return comprehensiveResult;
    }
}