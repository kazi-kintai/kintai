package kintai;

import java.io.Serializable;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 勤務時間管理画面（kinmu_manage.jsp）で使用するデータを保持するJavaBean。
 * 特に、工数明細（work_time_detailテーブル）の情報を内部クラスとして定義します。
 */
public class KinmuManageBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工数明細（work_time_detailテーブルのレコード）を保持する内部JavaBean。
     */
    public static class WorkDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        private int detailId;       // 勤務時間明細ID (DETAILID)
        private int recId;          // 勤怠記録ID (RECID)
        private String empno;       // 従業員番号 (EMPNO) - 参照用
        private LocalDate kintaiDate; // 勤怠日付 (KINTAIDATE) - 参照用
        private String gyomuNo;     // 業務番号 (GYOMUNO) - projectNoから変更
        private Time startTime;     // 作業開始時刻 (STARTTIME)
        private Time endTime;       // 作業終了時刻 (ENDTIME)
        private String description; // 作業内容説明 (DESCRIPTION)

        // 表示用の追加フィールド
        private String gyomuName; // 業務名 - projectNameから変更

        /**
         * デフォルトコンストラクタ
         */
        public WorkDetail() {
        }

        // --- アクセサメソッド (getter/setter) ---

        public int getDetailId() {
            return detailId;
        }

        public void setDetailId(int detailId) {
            this.detailId = detailId;
        }

        public int getRecId() {
            return recId;
        }

        public void setRecId(int recId) {
            this.recId = recId;
        }

        public String getEmpno() {
            return empno;
        }

        public void setEmpno(String empno) {
            this.empno = empno;
        }

        public LocalDate getKintaiDate() {
            return kintaiDate;
        }

        public void setKintaiDate(LocalDate kintaiDate) {
            this.kintaiDate = kintaiDate;
        }

        public String getGyomuNo() { // getProjectNo から変更
            return gyomuNo;
        }

        public void setGyomuNo(String gyomuNo) { // setProjectNo から変更
            this.gyomuNo = gyomuNo;
        }

        public Time getStartTime() {
            return startTime;
        }

        public void setStartTime(Time startTime) {
            this.startTime = startTime;
        }

        public Time getEndTime() {
            return endTime;
        }

        public void setEndTime(Time endTime) {
            this.endTime = endTime;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getGyomuName() { // getProjectName から変更
            return gyomuName;
        }

        public void setGyomuName(String gyomuName) { // setProjectName から変更
            this.gyomuName = gyomuName;
        }

        /**
         * 作業時間をHH:mm形式の文字列で取得します。
         * @return HH:mm形式の作業時間。開始時刻または終了時刻がnullの場合は"---"
         */
        public String getWorkDurationFormatted() {
            if (startTime == null || endTime == null) {
                return "---";
            }
            LocalTime start = startTime.toLocalTime();
            LocalTime end = endTime.toLocalTime();

            long totalMinutes = 0;
            // 終了時刻が開始時刻より前の場合（日付を跨ぐ場合）を考慮
            if (end.isBefore(start)) {
                Duration duration = Duration.between(start, LocalTime.MAX).plus(Duration.between(LocalTime.MIDNIGHT, end));
                totalMinutes = duration.toMinutes();
            } else {
                totalMinutes = Duration.between(start, end).toMinutes();
            }
            return String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60);
        }
    }
}
