package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * kintaiテーブルおよびbreakテーブル、work_allocテーブルへのデータアクセスを担当するクラス (DAO)。
 * 勤怠データ、休憩データ、工数割り当てデータの検索、追加、更新、削除を行う。
 * （旧work_time_detailテーブル関連のメソッドは削除されました）
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
        String sql = "SELECT RECID, CLOCKIN, CLOCKOUT, WORKING_HOURS, OVERTIME_HOURS, NIGHT_HOURS FROM kintai WHERE EMPNO = ? AND KINTAIDATE = ?";

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
                    // 新しいkintaiテーブルのWORKING_HOURS, OVERTIME_HOURS, NIGHT_HOURS も取得
                    // 修正箇所: setWorkingHours(BigDecimal) が WorkTimeBean で定義されたため、ここでのエラーが解消される
                    workTime.setWorkingHours(rs.getBigDecimal("WORKING_HOURS"));
                    workTime.setOvertimeHours(rs.getBigDecimal("OVERTIME_HOURS"));
                    workTime.setNightHours(rs.getBigDecimal("NIGHT_HOURS"));
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
     * 勤怠情報（出勤・退勤時間、および計算済み工数）を保存または更新する。
     * レコードが存在しない場合はINSERT、存在する場合はUPDATEを実行する。
     * @param workTime 保存する勤怠情報
     * @return 更新されたWorkTimeBean（新しいRECIDを含む）
     */
    public WorkTimeBean saveWorkTime(WorkTimeBean workTime) {
        // 先にレコードが存在するか確認
        WorkTimeBean existingWorkTime = findWorkTimeByDate(workTime.getEmpno(), workTime.getKintaiDate());

        String sql;
        if (existingWorkTime == null) {
            // INSERT処理 (WORKING_HOURSなども初期値として含める)
            sql = "INSERT INTO kintai (KINTAIDATE, EMPNO, CLOCKIN, CLOCKOUT, WORKING_HOURS, OVERTIME_HOURS, NIGHT_HOURS) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            // UPDATE処理
            workTime.setRecId(existingWorkTime.getRecId()); // 既存のIDをセット
            sql = "UPDATE kintai SET CLOCKIN = ?, CLOCKOUT = ?, WORKING_HOURS = ?, OVERTIME_HOURS = ?, NIGHT_HOURS = ? WHERE RECID = ?";
        }

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            if (existingWorkTime == null) { // INSERTの場合
                ps.setDate(1, Date.valueOf(workTime.getKintaiDate()));
                ps.setString(2, workTime.getEmpno());
                ps.setTime(3, workTime.getClockIn());
                ps.setTime(4, workTime.getClockOut());
                ps.setBigDecimal(5, workTime.getWorkingHours());
                ps.setBigDecimal(6, workTime.getOvertimeHours());
                ps.setBigDecimal(7, workTime.getNightHours());
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
                ps.setBigDecimal(3, workTime.getWorkingHours());
                ps.setBigDecimal(4, workTime.getOvertimeHours());
                ps.setBigDecimal(5, workTime.getNightHours());
                ps.setInt(6, workTime.getRecId());
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
        // breakテーブルにEMPNOとKINTAIDATE列が追加されたため、SQLを修正
        String sql = "INSERT INTO break (RECID, KINTAIDATE, EMPNO, BREAKSTART, BREAKEND) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // breakテーブルのEMPNOとKINTAIDATEはkintaiテーブルから取得する必要がある
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

    // --- 工数割り当て（work_alloc）関連メソッド ---

    /**
     * 指定された従業員と日付の工数割り当てリストを取得します。
     * （旧findWorkDetailsByRecIdメソッドの代わり）
     * @param empno 従業員番号
     * @param workDate 作業日
     * @return 工数割り当てのリスト。見つからない場合は空のリスト。
     */
    public List<KinmuManageBean.WorkAlloc> findWorkAllocsByEmpNoAndDate(String empno, LocalDate workDate) {
        List<KinmuManageBean.WorkAlloc> workAllocList = new ArrayList<>();
        // SQLを修正: work_allocテーブルとprojectテーブルをJOIN
        String sql = "SELECT wa.ALLOCATION_ID, wa.EMPNO, wa.PROJECT_ID, wa.WORK_DATE, wa.WORK_HOURS, " +
                     "p.PROJECT_NAME " + // プロジェクト名も取得
                     "FROM work_alloc wa " +
                     "LEFT JOIN project p ON wa.PROJECT_ID = p.PROJECT_ID " +
                     "WHERE wa.EMPNO = ? AND wa.WORK_DATE = ? " +
                     "ORDER BY wa.ALLOCATION_ID"; // 割り当てIDでソート

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, empno);
            ps.setDate(2, Date.valueOf(workDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KinmuManageBean.WorkAlloc alloc = new KinmuManageBean.WorkAlloc();
                    alloc.setAllocationId(rs.getInt("ALLOCATION_ID"));
                    alloc.setEmpno(rs.getString("EMPNO"));
                    alloc.setProjectId(rs.getInt("PROJECT_ID"));
                    alloc.setWorkDate(rs.getDate("WORK_DATE").toLocalDate());
                    alloc.setWorkHours(rs.getDouble("WORK_HOURS")); // doubleで取得
                    alloc.setProjectName(rs.getString("PROJECT_NAME")); // プロジェクト名も設定
                    workAllocList.add(alloc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workAllocList;
    }

    /**
     * 新しい工数割り当てをデータベースに追加します。
     * （旧addWorkDetailメソッドの代わり）
     * @param workAlloc 追加する工数割り当て情報
     */
    public void addWorkAlloc(KinmuManageBean.WorkAlloc workAlloc) {
        // SQLを修正: work_allocテーブルの列に合わせてINSERT文を作成
        String sql = "INSERT INTO work_alloc (EMPNO, PROJECT_ID, WORK_DATE, WORK_HOURS) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, workAlloc.getEmpno());
            ps.setInt(2, workAlloc.getProjectId());
            ps.setDate(3, Date.valueOf(workAlloc.getWorkDate()));
            ps.setDouble(4, workAlloc.getWorkHours()); // doubleでセット

            ps.executeUpdate();

        } catch (SQLException e) {
            // UNIQUE制約違反の場合（同一従業員、同一プロジェクト、同一日付で重複挿入を試みた場合）
            if (e.getSQLState().startsWith("23")) {
                System.err.println("工数割り当てが重複しています（EMPNO, PROJECT_ID, WORK_DATE）: " +
                                   workAlloc.getEmpno() + ", " + workAlloc.getProjectId() + ", " + workAlloc.getWorkDate());
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 工数割り当てをデータベースから削除します。
     * （旧deleteWorkDetailメソッドの代わり）
     * @param allocationId 削除する工数割り当てのID
     */
    public void deleteWorkAlloc(int allocationId) {
        String sql = "DELETE FROM work_alloc WHERE ALLOCATION_ID = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, allocationId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定されたRECIDの勤怠記録情報をデータベースから検索する補助メソッド。
     * @param recId 勤怠記録ID
     * @return WorkTimeBeanオブジェクト。見つからない場合はnull
     */
    private WorkTimeBean findWorkTimeByRecId(int recId) {
        WorkTimeBean workTime = null;
        String sql = "SELECT RECID, EMPNO, KINTAIDATE, CLOCKIN, CLOCKOUT, WORKING_HOURS, OVERTIME_HOURS, NIGHT_HOURS FROM kintai WHERE RECID = ?";
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
                    // 新しいkintaiテーブルのWORKING_HOURS, OVERTIME_HOURS, NIGHT_HOURSも取得
                    // 修正箇所: setWorkingHours(BigDecimal) が WorkTimeBean で定義されたため、ここでのエラーが解消される
                    workTime.setWorkingHours(rs.getBigDecimal("WORKING_HOURS"));
                    workTime.setOvertimeHours(rs.getBigDecimal("OVERTIME_HOURS"));
                    workTime.setNightHours(rs.getBigDecimal("NIGHT_HOURS"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workTime;
    }
}
