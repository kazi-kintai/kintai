package kintai;

import java.math.BigDecimal;


public class MonthlySummaryBean {
    private int totalWorkDays;        
    private int actualAttendanceDays; 
    private BigDecimal totalOvertimeHours;   
    private BigDecimal totalWorkingHours;    
    private BigDecimal totalBreakHours;      
    private String targetMonth;             

    public MonthlySummaryBean() {
        this.totalOvertimeHours = BigDecimal.ZERO;
        this.totalWorkingHours = BigDecimal.ZERO;
        this.totalBreakHours = BigDecimal.ZERO;
    }

    // Getter methods
    public int getTotalWorkDays() {
        return totalWorkDays;
    }

    public int getActualAttendanceDays() {
        return actualAttendanceDays;
    }

    public BigDecimal getTotalOvertimeHours() {
        return totalOvertimeHours;
    }

    public BigDecimal getTotalWorkingHours() {
        return totalWorkingHours;
    }

    public BigDecimal getTotalBreakHours() {
        return totalBreakHours;
    }

    public String getTargetMonth() {
        return targetMonth;
    }

    // Setter methods
    public void setTotalWorkDays(int totalWorkDays) {
        this.totalWorkDays = totalWorkDays;
    }

    public void setActualAttendanceDays(int actualAttendanceDays) {
        this.actualAttendanceDays = actualAttendanceDays;
    }

    public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public void setTotalWorkingHours(BigDecimal totalWorkingHours) {
        this.totalWorkingHours = totalWorkingHours;
    }

    public void setTotalBreakHours(BigDecimal totalBreakHours) {
        this.totalBreakHours = totalBreakHours;
    }

    public void setTargetMonth(String targetMonth) {
        this.targetMonth = targetMonth;
    }

    // 便利メソッド
    /**
     * 出勤率を計算して返す (百分比)
     * @return 出勤率 (例: 96.0)
     */
    public double getAttendanceRate() {
        if (totalWorkDays == 0) {
            return 0.0;
        }
        return (double) actualAttendanceDays / totalWorkDays * 100.0;
    }

    /**
     * 出勤率を文字列形式で返す
     * @return 出勤率文字列 (例: "24/25")
     */
    public String getAttendanceRateString() {
        return String.format("%d/%d", actualAttendanceDays, totalWorkDays);
    }

    /**
     * 時間を文字列形式で返す (小数点1桁)
     * @param hours BigDecimal時間
     * @return 時間文字列 (例: "45.5時間")
     */
    public String formatHours(BigDecimal hours) {
        if (hours == null) {
            return "0.0時間";
        }
        return String.format("%.1f時間", hours.doubleValue());
    }

    /**
     * 残业時間の文字列表現
     */
    public String getTotalOvertimeHoursString() {
        return formatHours(totalOvertimeHours);
    }

    /**
     * 総実働時間の文字列表現
     */
    public String getTotalWorkingHoursString() {
        return formatHours(totalWorkingHours);
    }

    /**
     * 総休憩時間の文字列表現
     */
    public String getTotalBreakHoursString() {
        return formatHours(totalBreakHours);
    }
}