package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
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
}
