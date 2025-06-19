package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * kintaiテーブルおよびbreakテーブル、work_time_detailテーブルへのデータアクセスを担当するクラス (DAO)。
 * 勤怠データ、休憩データ、工数明細データの検索、追加、更新、削除を行う。
 */
public class WorkTimeDao {

    private DBAccess db = new DBAccess();

    /**
     * 指定された従業員と日付の出退勤情報をデータベースから検索する。
     * @param empno 従業員番号
     * @param date 検索する日付
     * @return 見つかった場合はWorkTimeBeanオブジェクト、見つからない場合はnull
     */
    public WorkTimeBean findWorkTimeByDate(String empno, LocalDate date) {
        WorkTimeBean workTime = null;
        String sql = "SELECT RECID, CLOCKIN, CLOCKOUT FROM kintai WHERE EMPNO = ? AND KINTAIDATE = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, empno);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    workTime = new WorkTimeBean();
                    workTime.setRecId(rs.getInt("RECID"));
                    workTime.setEmpno(empno);
                    workTime.setKintaiDate(date);
                    workTime.setClockIn(rs.getTime("CLOCKIN"));
                    workTime.setClockOut(rs.getTime("CLOCKOUT"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workTime;
    }

    /**
     * 指定された従業員と日付の休憩情報をすべてデータベースから検索する。
     * @param empno 従業員番号
     * @param date 検索する日付
     * @return 休憩情報のリスト。見つからない場合は空のリスト
     */
    public List<BreakBean> findBreaksByDate(String empno, LocalDate date) {
        List<BreakBean> breakList = new ArrayList<>();
        // RECIDを取得し、そのRECIDに紐づく休憩データを取得するようにSQLを修正
        String sql = "SELECT b.BREAKID, b.RECID, b.BREAKSTART, b.BREAKEND " +
                     "FROM break b " +
                     "JOIN kintai k ON b.RECID = k.RECID " +
                     "WHERE k.EMPNO = ? AND k.KINTAIDATE = ? " +
                     "ORDER BY b.BREAKSTART"; // 休憩開始時刻でソートして表示順を制御

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, empno);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BreakBean breakBean = new BreakBean();
                    breakBean.setBreakId(rs.getInt("BREAKID"));
                    breakBean.setRecId(rs.getInt("RECID"));
                    breakBean.setBreakStart(rs.getTime("BREAKSTART"));
                    breakBean.setBreakEnd(rs.getTime("BREAKEND"));
                    breakList.add(breakBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return breakList;
    }

    /**
     * 勤怠情報（出勤・退勤時間）を保存または更新する。
     * レコードが存在しない場合はINSERT、存在する場合はUPDATEを実行する。
     * @param workTime 保存する勤怠情報
     * @return 更新されたWorkTimeBean（新しいRECIDを含む）
     */
    public WorkTimeBean saveWorkTime(WorkTimeBean workTime) {
        // 先にレコードが存在するか確認
        WorkTimeBean existingWorkTime = findWorkTimeByDate(workTime.getEmpno(), workTime.getKintaiDate());

        String sql;
        if (existingWorkTime == null) {
            // INSERT処理
            sql = "INSERT INTO kintai (KINTAIDATE, EMPNO, CLOCKIN, CLOCKOUT) VALUES (?, ?, ?, ?)";
        } else {
            // UPDATE処理
            workTime.setRecId(existingWorkTime.getRecId()); // 既存のIDをセット
            sql = "UPDATE kintai SET CLOCKIN = ?, CLOCKOUT = ? WHERE RECID = ?";
        }

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            if (existingWorkTime == null) { // INSERTの場合
                ps.setDate(1, Date.valueOf(workTime.getKintaiDate()));
                ps.setString(2, workTime.getEmpno());
                ps.setTime(3, workTime.getClockIn());
                ps.setTime(4, workTime.getClockOut());
                ps.executeUpdate();
                // 新しく生成されたRECIDを取得
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        workTime.setRecId(generatedKeys.getInt(1));
                    }
                }
            } else { // UPDATEの場合
                ps.setTime(1, workTime.getClockIn());
                ps.setTime(2, workTime.getClockOut());
                ps.setInt(3, workTime.getRecId());
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return workTime;
    }

    /**
     * 新しい休憩記録をデータベースに追加する。
     * @param breakBean 追加する休憩情報（RECID, 開始時間, 終了時間を含む）
     */
    public void addBreak(BreakBean breakBean) {
        String sql = "INSERT INTO break (RECID, KINTAIDATE, EMPNO, BREAKSTART, BREAKEND) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            WorkTimeBean workTime = findWorkTimeByRecId(breakBean.getRecId());
            if (workTime == null) {
                System.err.println("addBreak: 対応する勤怠レコードが見つかりません。RECID=" + breakBean.getRecId());
                return;
            }

            ps.setInt(1, breakBean.getRecId());
            ps.setDate(2, Date.valueOf(workTime.getKintaiDate()));
            ps.setString(3, workTime.getEmpno());
            ps.setTime(4, breakBean.getBreakStart());
            ps.setTime(5, breakBean.getBreakEnd());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 休憩記録をデータベースから削除する。
     * @param breakId 削除する休憩記録のID
     */
    public void deleteBreak(int breakId) {
        String sql = "DELETE FROM break WHERE BREAKID = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, breakId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定されたRECIDの勤怠記録に紐づく工数明細のリストを取得します。
     * @param recId 勤怠記録ID
     * @return 工数明細のリスト。見つからない場合は空のリスト。
     */
    public List<KinmuManageBean.WorkDetail> findWorkDetailsByRecId(int recId) {
        List<KinmuManageBean.WorkDetail> workDetailList = new ArrayList<>();
        // SQLを修正: projectテーブルの代わりにgyomuテーブルをJOIN
        String sql = "SELECT wd.DETAILID, wd.RECID, wd.EMPNO, wd.KINTAIDATE, wd.GYOMUNO, " +
                     "wd.STARTTIME, wd.ENDTIME, wd.DESCRIPTION, g.GYOMUNAME " + // g.GYOMUNAME を取得
                     "FROM work_time_detail wd " +
                     "LEFT JOIN gyomu g ON wd.GYOMUNO = g.GYOMUNO " + // gyomuテーブルとJOIN
                     "WHERE wd.RECID = ? " +
                     "ORDER BY wd.STARTTIME"; // 作業開始時刻でソート

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KinmuManageBean.WorkDetail detail = new KinmuManageBean.WorkDetail();
                    detail.setDetailId(rs.getInt("DETAILID"));
                    detail.setRecId(rs.getInt("RECID"));
                    detail.setEmpno(rs.getString("EMPNO"));
                    detail.setKintaiDate(rs.getDate("KINTAIDATE").toLocalDate());
                    detail.setGyomuNo(rs.getString("GYOMUNO")); // setProjectNo から setGyomuNo に変更
                    detail.setStartTime(rs.getTime("STARTTIME"));
                    detail.setEndTime(rs.getTime("ENDTIME"));
                    detail.setDescription(rs.getString("DESCRIPTION"));
                    detail.setGyomuName(rs.getString("GYOMUNAME")); // setProjectName から setGyomuName に変更
                    workDetailList.add(detail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workDetailList;
    }

    /**
     * 新しい工数明細をデータベースに追加します。
     * @param workDetail 追加する工数明細情報
     */
    public void addWorkDetail(KinmuManageBean.WorkDetail workDetail) {
        // SQLを修正: PROJECTNO の代わりに GYOMUNO を使用
        String sql = "INSERT INTO work_time_detail (RECID, EMPNO, KINTAIDATE, GYOMUNO, STARTTIME, ENDTIME, DESCRIPTION) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, workDetail.getRecId());
            ps.setString(2, workDetail.getEmpno());
            ps.setDate(3, Date.valueOf(workDetail.getKintaiDate()));
            ps.setString(4, workDetail.getGyomuNo()); // setProjectNo から setGyomuNo に変更
            ps.setTime(5, workDetail.getStartTime());
            ps.setTime(6, workDetail.getEndTime());
            ps.setString(7, workDetail.getDescription());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 工数明細をデータベースから削除します。
     * @param detailId 削除する工数明細のID
     */
    public void deleteWorkDetail(int detailId) {
        String sql = "DELETE FROM work_time_detail WHERE DETAILID = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detailId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定されたRECIDの勤怠記録情報をデータベースから検索する補助メソッド。
     * KinmuManageServletから呼び出されることを想定。
     * @param recId 勤怠記録ID
     * @return WorkTimeBeanオブジェクト。見つからない場合はnull
     */
    private WorkTimeBean findWorkTimeByRecId(int recId) {
        WorkTimeBean workTime = null;
        String sql = "SELECT RECID, EMPNO, KINTAIDATE, CLOCKIN, CLOCKOUT FROM kintai WHERE RECID = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    workTime = new WorkTimeBean();
                    workTime.setRecId(rs.getInt("RECID"));
                    workTime.setEmpno(rs.getString("EMPNO"));
                    workTime.setKintaiDate(rs.getDate("KINTAIDATE").toLocalDate());
                    workTime.setClockIn(rs.getTime("CLOCKIN"));
                    workTime.setClockOut(rs.getTime("CLOCKOUT"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workTime;
    }
}
