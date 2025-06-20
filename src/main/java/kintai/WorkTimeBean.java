package kintai;

import java.io.Serializable;
import java.math.BigDecimal; // BigDecimal をインポート
import java.sql.Time;
import java.time.LocalDate;

/**
 * 勤怠情報（kintaiテーブルのレコード）を保持するJavaBean。
 * 新しいER図のkintaiテーブルの列に対応するよう修正。
 */
public class WorkTimeBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int recId;           // kintaiテーブルの「RECID」列に対応
    private LocalDate kintaiDate;  // kintaiテーブルの「KINTAIDATE」列に対応
    private String empno;        // kintaiテーブルの「EMPNO」列に対応
    private Time clockIn;        // kintaiテーブルの「CLOCKIN」列に対応
    private Time clockOut;       // kintaiテーブルの「CLOCKOUT」列に対応
    private BigDecimal workingHours;    // kintaiテーブルの「WORKING_HOURS」列に対応
    private BigDecimal overtimeHours;   // kintaiテーブルの「OVERTIME_HOURS」列に対応
    private BigDecimal nightHours;      // kintaiテーブルの「NIGHT_HOURS」列に対応

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

    public int getRecId() {
        return recId;
    }

    public void setRecId(int recId) {
        this.recId = recId;
    }

    public LocalDate getKintaiDate() {
        return kintaiDate;
    }

    public void setKintaiDate(LocalDate kintaiDate) {
        this.kintaiDate = kintaiDate;
    }

    public String getEmpno() {
        return empno;
    }

    public void setEmpno(String empno) {
        this.empno = empno;
    }

    public Time getClockIn() {
        return clockIn;
    }

    public void setClockIn(Time clockIn) {
        this.clockIn = clockIn;
    }

    public Time getClockOut() {
        return clockOut;
    }

    public void setClockOut(Time clockOut) {
        this.clockOut = clockOut;
    }

    // 新規追加
    public BigDecimal getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(BigDecimal workingHours) {
        this.workingHours = workingHours;
    }

    // 新規追加
    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    // 新規追加
    public BigDecimal getNightHours() {
        return nightHours;
    }

    public void setNightHours(BigDecimal nightHours) {
        this.nightHours = nightHours;
    }
}
