package kintai;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDate;

/**
 * 勤怠記録表示画面で使用するデータを保持するJavaBean。
 * kintai、emp、dept、post、breakテーブルの情報を集約し、
 * 計算済みの休憩時間合計や実働時間も保持します。
 */
public class KintaiRecBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- kintaiテーブルからの情報 ---
    private int recId;           // 勤怠記録ID
    private LocalDate kintaiDate;  // 勤怠日付
    private String empno;        // 従業員番号
    private Time clockIn;        // 出勤時刻
    private Time clockOut;       // 退勤時刻

    // --- emp、dept、postテーブルからの情報（表示用）---
    private String empName;      // 従業員名
    private String deptNo;       // 部署番号
    private String deptName;     // 部署名
    private String postNo;       // 役職番号
    private String postName;     // 役職名

    // --- 計算済みの集計情報 ---
    private long totalBreakMinutes; // 総休憩時間（分単位）
    private long actualWorkMinutes; // 実働時間（分単位）
    private long overtimeMinutes;   // 残業時間（分単位）

    /**
     * デフォルトコンストラクタ
     */
    public KintaiRecBean() {
    }

    // --- アクセサメソッド (getter/setter) ---

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

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getPostNo() {
        return postNo;
    }

    public void setPostNo(String postNo) {
        this.postNo = postNo;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public long getTotalBreakMinutes() {
        return totalBreakMinutes;
    }

    public void setTotalBreakMinutes(long totalBreakMinutes) {
        this.totalBreakMinutes = totalBreakMinutes;
    }

    public long getActualWorkMinutes() {
        return actualWorkMinutes;
    }

    public void setActualWorkMinutes(long actualWorkMinutes) {
        this.actualWorkMinutes = actualWorkMinutes;
    }
    
    /**
     * 総休憩時間をHH:mm形式の文字列で取得します。
     * @return HH:mm形式の休憩時間
     */
    public String getTotalBreakTimeFormatted() {
        return formatMinutesToHHMM(totalBreakMinutes);
    }

    /**
     * 実働時間をHH:mm形式の文字列で取得します。
     * @return HH:mm形式の実働時間
     */
    public String getActualWorkTimeFormatted() {
        return formatMinutesToHHMM(actualWorkMinutes);
    }

    public long getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(long overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    /**
     * 残業時間をHH:mm形式で返すメソッド。
     * @return 残業時間のHH:mm形式文字列
     */
    public String getOvertimeFormatted() {
        return formatMinutesToHHMM(overtimeMinutes);
    }

    /**
     * 分数をHH:mm形式の文字列に変換するヘルパーメソッド。
     * @param minutes 分数
     * @return HH:mm形式の文字列
     */
    private String formatMinutesToHHMM(long minutes) {
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return String.format("%02d:%02d", hours, remainingMinutes);
    }
}
