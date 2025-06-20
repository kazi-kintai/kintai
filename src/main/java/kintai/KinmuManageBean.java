package kintai;

import java.io.Serializable;
import java.time.LocalDate;
// 旧WorkDetailで使用していたTime, LocalTime, Durationはwork_allocで直接時間を入力するため不要になります

/**
 * 勤務時間管理画面（kinmu_manage.jsp）で使用するデータを保持するJavaBean。
 * 特に、工数割り当て（work_allocテーブル）の情報を内部クラスとして定義します。
 */
public class KinmuManageBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // KinmuManageBean自体は、特定の勤怠記録（recId, kintaiDate, empno）
    // とその日の出退勤、休憩時間、工数割り当てのリストを保持するためのコンテナとして使われることが多いです。
    // ここではWorkAlloc内部クラスの定義を主とします。

    /**
     * 工数割り当て（work_allocテーブルのレコード）を保持する内部JavaBean。
     */
    public static class WorkAlloc implements Serializable {
        private static final long serialVersionUID = 1L;

        private int allocationId;   // 割り当てID (ALLOCATION_ID)
        private String empno;       // 従業員番号 (EMPNO) - 参照用（ただしwork_allocのPKの一部）
        private int projectId;      // プロジェクトID (PROJECT_ID)
        private LocalDate workDate; // 作業日 (WORK_DATE)
        private double workHours;   // 作業時間 (WORK_HOURS)

        // 表示用の追加フィールド（JOINで取得）
        private String empName;     // 従業員名
        private String projectName; // プロジェクト名

        /**
         * デフォルトコンストラクタ
         */
        public WorkAlloc() {
        }

        // --- アクセサメソッド (getter/setter) ---

        public int getAllocationId() {
            return allocationId;
        }

        public void setAllocationId(int allocationId) {
            this.allocationId = allocationId;
        }

        public String getEmpno() {
            return empno;
        }

        public void setEmpno(String empno) {
            this.empno = empno;
        }

        public int getProjectId() {
            return projectId;
        }

        public void setProjectId(int projectId) {
            this.projectId = projectId;
        }

        public LocalDate getWorkDate() {
            return workDate;
        }

        public void setWorkDate(LocalDate workDate) {
            this.workDate = workDate;
        }

        public double getWorkHours() {
            return workHours;
        }

        public void setWorkHours(double workHours) {
            this.workHours = workHours;
        }

        public String getEmpName() {
            return empName;
        }

        public void setEmpName(String empName) {
            this.empName = empName;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        /**
         * 作業時間をHH.HH形式の文字列で取得します。
         * @return HH.HH形式の作業時間
         */
        public String getWorkHoursFormatted() {
            // DecimalFormatなどを使用する方が厳密だが、ここではString.formatで簡易的に対応
            return String.format("%.2f", workHours);
        }
    }
}
