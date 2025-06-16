package kintai;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDate;

/**
 * 勤怠情報（kintaiテーブルのレコード）を保持するJavaBean。
 */
public class WorkTimeBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int recId;           // kintaiテーブルの「RECID」列に対応
    private LocalDate kintaiDate;  // kintaiテーブルの「KINTAIDATE」列に対応
    private String empno;        // kintaiテーブルの「EMPNO」列に対応
    private Time clockIn;        // kintaiテーブルの「CLOCKIN」列に対応
    private Time clockOut;       // kintaiテーブルの「CLOCKOUT」列に対応

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
}