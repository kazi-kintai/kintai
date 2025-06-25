package kintai;

import java.io.IOException;
import java.sql.Time; // java.sql.Time をインポート
import java.time.LocalDate;
import java.time.LocalTime; // java.time.LocalTime をインポート
import java.time.format.DateTimeParseException; // 日付/時間解析例外用
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 勤務時間管理機能を提供するサーブレット。
 * 従業員が自身の勤怠（出勤、退勤、休憩）や工数割り当て（プロジェクト）を管理・修正する画面を制御する。
 */
@WebServlet("/KinmuManageServlet") // 全体ファイルまとめ.xlsx - Sheet1.pdf の kinmu_manage.jsp に対応するサーブレット
public class KinmuManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // WorkTimeDaoを流用して勤怠および休憩データ、工数割り当てデータを操作
    private WorkTimeDao workTimeDao = new WorkTimeDao();
    // ProjectDaoのインスタンス (工数割り当てはプロジェクトに紐づくため)
    private ProjectDao projectDao = new ProjectDao();
    // GyomuDaoはもう使用しないため削除

    /**
     * GETリクエストの処理メソッド。
     * 勤務時間管理画面 (kinmu_manage.jsp) を表示し、指定された日付の勤怠データを渡す。
     * @param request HTTPリクエストオブジェクト
     * @param response HTTPレスポンスオブジェクト
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // セッションチェック
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/web/login.jsp");
            return;
        }

        UserBean user = (UserBean) session.getAttribute("user");
        String empno = user.getEmpno(); // ログイン中の従業員番号

        // 表示する日付を取得 (リクエストパラメータがなければ今日の日付)
        String targetDateStr = request.getParameter("targetDate");
        LocalDate targetDate;
        try {
            if (targetDateStr != null && !targetDateStr.trim().isEmpty()) {
                targetDate = LocalDate.parse(targetDateStr);
            } else {
                targetDate = LocalDate.now(); // デフォルトは今日の日付
            }
        } catch (DateTimeParseException e) {
            request.setAttribute("errorMessage", "日付の形式が不正です。YYYY-MM-DD形式で入力してください。");
            targetDate = LocalDate.now(); // エラー時は今日の日付にリセット
        }

        // --- 勤怠データ（出退勤、休憩）の取得 ---
        WorkTimeBean workTime = workTimeDao.findWorkTimeByDate(empno, targetDate);
        List<BreakBean> breakList = workTimeDao.findBreaksByDate(empno, targetDate);

        // JSPに渡すための勤怠データマップを作成
        Map<String, String> workTimeData = new HashMap<>();
        if (workTime != null) {
            workTimeData.put("recId", String.valueOf(workTime.getRecId())); // RECIDを渡す
            if (workTime.getClockIn() != null) {
                workTimeData.put("clockInTime", workTime.getClockIn().toLocalTime().toString().substring(0, 5));
            }
            if (workTime.getClockOut() != null) {
                workTimeData.put("clockOutTime", workTime.getClockOut().toLocalTime().toString().substring(0, 5));
            }
        }
        
        // 休憩データをJSPで扱いやすい形式に変換
        List<Map<String, String>> formattedBreakList = new ArrayList<>();
        for (BreakBean breakBean : breakList) {
            Map<String, String> breakItem = new HashMap<>();
            breakItem.put("breakId", String.valueOf(breakBean.getBreakId()));
            if (breakBean.getBreakStart() != null) {
                breakItem.put("startTime", breakBean.getBreakStart().toLocalTime().toString().substring(0, 5));
            }
            if (breakBean.getBreakEnd() != null) {
                breakItem.put("endTime", breakBean.getBreakEnd().toLocalTime().toString().substring(0, 5));
            }
            formattedBreakList.add(breakItem);
        }

        // --- 工数割り当て（プロジェクト）の取得 ---
        // WorkTimeDao に work_alloc の操作メソッドを追加する想定
        List<KinmuManageBean.WorkAlloc> workAllocs = new ArrayList<>(); // WorkDetailからWorkAllocに変更
        // work_allocはRECIDに紐づかないため、EMPNOとWORK_DATEで直接取得
        workAllocs = workTimeDao.findWorkAllocsByEmpNoAndDate(empno, targetDate); // 新規メソッド呼び出し

        // プロジェクトのドロップダウンリスト用データ
        List<ProjectBean> projectList = projectDao.findAll();


        // リクエスト属性にデータを設定
        request.setAttribute("targetDate", targetDate.toString());
        request.setAttribute("workTimeData", workTimeData);
        request.setAttribute("breakList", formattedBreakList);
        request.setAttribute("workAllocs", workAllocs); // workDetailsからworkAllocsに変更
        request.setAttribute("projectList", projectList);
        // gyomuListはもう使用しないため削除


        // セッションからメッセージを取得し、リクエスト属性に設定
        String successMessage = (String) session.getAttribute("successMessage");
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage"); // セッションからクリア
        }
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage"); // セッションからクリア
        }


        // 勤務時間管理画面にフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/kinmu_manage.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * POSTリクエストの処理メソッド。
     * 勤務時間管理画面からのデータ送信（出退勤更新、休憩追加/削除、工数割り当て追加/削除）を受け付ける。
     * @param request HTTPリクエストオブジェクト
     * @param response HTTPレスポンスオブジェクト
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // セッションチェック
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/web/login.jsp");
            return;
        }

        UserBean user = (UserBean) session.getAttribute("user");
        String empno = user.getEmpno();
        LocalDate targetDate = LocalDate.parse(request.getParameter("targetDate")); // 処理対象の日付

        String action = request.getParameter("action"); // 実行するアクション（更新、追加、削除など）
        String successMessage = null;
        String errorMessage = null;

        try {
            switch (action) {
                case "update_work_time": // 出退勤時間の更新
                    String recIdStr = request.getParameter("recId");
                    String clockInStr = request.getParameter("clockInTime");
                    String clockOutStr = request.getParameter("clockOutTime");

                    int recId = -1;
                    if (recIdStr != null && !recIdStr.isEmpty()) {
                        recId = Integer.parseInt(recIdStr);
                    }

                    Time clockIn = parseTime(clockInStr);
                    Time clockOut = parseTime(clockOutStr);

                    WorkTimeBean workTime = null;
                    if (recId != -1) {
                        // 既存の勤怠記録を取得
                        workTime = workTimeDao.findWorkTimeByDate(empno, targetDate);
                        if (workTime == null) { // RECIDがあったにもかかわらず見つからない場合はエラー
                           errorMessage = "勤怠記録の更新に失敗しました: 対象の記録が見つかりません。";
                           break;
                        }
                        workTime.setClockIn(clockIn);
                        workTime.setClockOut(clockOut);
                    } else { // RECIDがない場合は新規作成
                        workTime = new WorkTimeBean();
                        workTime.setEmpno(empno);
                        workTime.setKintaiDate(targetDate);
                        workTime.setClockIn(clockIn);
                        workTime.setClockOut(clockOut);
                    }
                    
                    workTimeDao.saveWorkTime(workTime); // 保存または更新
                    successMessage = "出退勤時間を更新しました";
                    break;

                case "add_break": // 休憩時間の追加
                    String breakStartStr = request.getParameter("newBreakStartTime");
                    String breakEndStr = request.getParameter("newBreakEndTime");

                    WorkTimeBean currentWorkTime = workTimeDao.findWorkTimeByDate(empno, targetDate);
                    if (currentWorkTime == null) {
                        errorMessage = "勤怠記録がないため、休憩を追加できません。先に出勤時間を登録してください。";
                        break;
                    }

                    // 出勤時間が未設定の場合のチェック
                    if (currentWorkTime.getClockIn() == null) {
                        errorMessage = "出勤時間が未設定のため、休憩を追加できません。先に出勤してください。";
                        break;
                    }

                    Time breakStart = parseTime(breakStartStr);
                    Time breakEnd = parseTime(breakEndStr);

                    // 休憩時間が出勤退勤時間範囲内かチェック
                    if (currentWorkTime.getClockOut() != null) {
                        if (breakStart.before(currentWorkTime.getClockIn()) || 
                            breakEnd.after(currentWorkTime.getClockOut()) ||
                            breakStart.after(currentWorkTime.getClockOut()) ||
                            breakEnd.before(currentWorkTime.getClockIn())) {
                            errorMessage = "休憩時間が出勤・退勤時間の範囲外です。出勤時間: " + 
                                         currentWorkTime.getClockIn() + " ～ 退勤時間: " + 
                                         currentWorkTime.getClockOut() + " の範囲内で設定してください。";
                            break;
                        }
                    } else {
                        // 退勤時間が未設定の場合は出勤時間以降かのみチェック
                        if (breakStart.before(currentWorkTime.getClockIn()) || 
                            breakEnd.before(currentWorkTime.getClockIn())) {
                            errorMessage = "休憩時間は出勤時間以降に設定してください。出勤時間: " + 
                                         currentWorkTime.getClockIn();
                            break;
                        }
                    }

                    BreakBean newBreak = new BreakBean();
                    newBreak.setRecId(currentWorkTime.getRecId());
                    newBreak.setBreakStart(breakStart);
                    newBreak.setBreakEnd(breakEnd);
                    
                    workTimeDao.addBreak(newBreak); // 休憩を追加
                    successMessage = "休憩時間を追加しました。";
                    break;

                case "delete_break": // 休憩時間の削除
                    String deleteBreakIdStr = request.getParameter("breakId");
                    int deleteBreakId = Integer.parseInt(deleteBreakIdStr);
                    workTimeDao.deleteBreak(deleteBreakId); // 休憩を削除
                    successMessage = "休憩時間を削除しました。";
                    break;

                case "add_work_alloc": // 工数割り当ての追加 (add_work_detailから変更)
                    String projectIdStr = request.getParameter("newProjectId"); // gyomuNoからprojectIdに変更
                    String workHoursStr = request.getParameter("newWorkHours"); // 開始/終了時刻からworkHoursに変更
                    String description = request.getParameter("newDescription"); // 説明は残す

                    if (projectIdStr == null || projectIdStr.trim().isEmpty() ||
                        workHoursStr == null || workHoursStr.trim().isEmpty()) {
                        errorMessage = "プロジェクトと作業時間は必須入力です。";
                        break;
                    }

                    int projectId = Integer.parseInt(projectIdStr);
                    double workHours = Double.parseDouble(workHoursStr);

                    KinmuManageBean.WorkAlloc newWorkAlloc = new KinmuManageBean.WorkAlloc(); // WorkDetailからWorkAllocに変更
                    newWorkAlloc.setEmpno(empno);
                    newWorkAlloc.setProjectId(projectId);
                    newWorkAlloc.setWorkDate(targetDate);
                    newWorkAlloc.setWorkHours(workHours);
                    // newWorkAlloc.setDescription(description); // work_allocテーブルにはdescription列はないため設定しない

                    // WorkTimeDaoに新しく実装するメソッド (addWorkDetailから変更)
                    workTimeDao.addWorkAlloc(newWorkAlloc); 
                    successMessage = "工数割り当てを追加しました。";
                    break;
                
                case "delete_work_alloc": // 工数割り当ての削除 (delete_work_detailから変更)
                    String deleteAllocationIdStr = request.getParameter("allocationId"); // detailIdからallocationIdに変更
                    int deleteAllocationId = Integer.parseInt(deleteAllocationIdStr);
                    // WorkTimeDaoに新しく実装するメソッド (deleteWorkDetailから変更)
                    workTimeDao.deleteWorkAlloc(deleteAllocationId);
                    successMessage = "工数割り当てを削除しました。";
                    break;

                // TODO: 必要に応じて工数割り当ての更新機能を追加
                // case "update_work_alloc":
                //    ...
                //    break;

                default:
                    errorMessage = "不正な操作です。";
                    break;
            }
        } catch (NumberFormatException e) {
            errorMessage = "入力された数値が不正です。時間またはIDを確認してください。";
            e.printStackTrace();
        } catch (DateTimeParseException e) {
            errorMessage = "入力された日付または時間の形式が不正です。";
            e.printStackTrace();
        } catch (Exception e) {
            errorMessage = "処理中に予期せぬエラーが発生しました。";
            e.printStackTrace();
        }

        // 処理結果をセッションに設定し、GETにリダイレクトして画面を再表示
        HttpSession httpSession = request.getSession();
        if (successMessage != null) {
            httpSession.setAttribute("successMessage", successMessage);
        }
        if (errorMessage != null) {
            httpSession.setAttribute("errorMessage", errorMessage);
        }
        response.sendRedirect(request.getContextPath() + "/KinmuManageServlet?targetDate=" + targetDate.toString());
    }

    /**
     * 時間文字列を java.sql.Time オブジェクトに変換する補助メソッド
     * "HH:mm"または"H:mm"形式の文字列に対応（例: "09:00" または "9:00"）
     * WorkPunchServletからコピーして利用
     * @param timeStr 変換する時間文字列（例: "14:30", "9:15"）
     * @return 変換後のTimeオブジェクト。変換できない場合はnullを返す
     */
    private Time parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalTime localTime = LocalTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("H:mm"));
            return Time.valueOf(localTime);
        } catch (DateTimeParseException e) {
            System.err.println("時間フォーマットのパースに失敗しました: " + timeStr);
            return null;
        }
    }
}
