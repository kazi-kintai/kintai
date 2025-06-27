package kintai;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
 * 勤怠登録に関連するリクエストを処理するサーブレット。
 * データベースと連携して勤怠データの表示・登録・更新・削除を行う。
 *
 * 主な機能：
 * - 出勤・退勤の打刻処理
 * - 休憩時間の登録・削除
 * - 当日の勤怠データの表示
 */
@WebServlet("/workPunch") // URLパターン「/workPunch」にマッピング
public class WorkPunchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 勤怠データアクセス用のDAOインスタンス
    private WorkTimeDao workTimeDao = new WorkTimeDao();

    /**
     * GETリクエストの処理メソッド
     * 勤怠登録画面(dakoku.jsp)を表示する前に、
     * ログイン中のユーザーの今日の勤怠データをデータベースから取得してJSPに渡す。
     *
     * @param request HTTPリクエストオブジェクト
     * @param response HTTPレスポンスオブジェクト
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // セッション情報を取得（既存のセッションのみ、新規作成はしない）
        HttpSession session = request.getSession(false);

        // セッションが存在しない、またはユーザー情報がない場合はログイン画面にリダイレクト
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/web/login.jsp");
            return;
        }

        // セッションからログインユーザーの情報を取得
        UserBean user = (UserBean) session.getAttribute("user");
        String empId = user.getEmpId(); // 従業員番号を取得
        LocalDate today = LocalDate.now(); // 今日の日付を取得

        // データベースから今日の勤怠データと休憩データリストを取得
        WorkTimeBean workTime = workTimeDao.findWorkTimeByDate(empId, today);
        List<BreakBean> breaks = workTimeDao.findBreaksByDate(empId, today);

        // JSPに渡すための勤怠データ用のマップを作成
        Map<String, String> workTimeData = new HashMap<>();
        // 時間表示用のフォーマッター（HH:mm形式）
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 勤怠データが存在する場合、出勤・退勤時刻をフォーマットしてマップに格納
        if (workTime != null) {
            // 出勤時刻が記録されている場合
            if (workTime.getClockIn() != null) {
                workTimeData.put("clockInTime", workTime.getClockIn().toLocalTime().format(timeFormatter));
            }
            // 退勤時刻が記録されている場合
            if (workTime.getClockOut() != null) {
                workTimeData.put("clockOutTime", workTime.getClockOut().toLocalTime().format(timeFormatter));
            }
        }

        // 休憩データをJSPで扱いやすい形式に変換
        List<Map<String, String>> breakList = new ArrayList<>();
        for (BreakBean breakBean : breaks) {
            // 各休憩データを個別のマップとして作成
            Map<String, String> breakItem = new HashMap<>();
            // 休憩ID（削除処理で使用）
            breakItem.put("breakId", String.valueOf(breakBean.getBreakId()));

            // 休憩開始時刻が記録されている場合
            if (breakBean.getBreakStart() != null) {
                breakItem.put("startTime", breakBean.getBreakStart().toLocalTime().format(timeFormatter));
            }
            // 休憩終了時刻が記録されている場合
            if (breakBean.getBreakEnd() != null) {
                breakItem.put("endTime", breakBean.getBreakEnd().toLocalTime().format(timeFormatter));
            }
            // 休憩データをリストに追加
            breakList.add(breakItem);
        }

        // JSPに渡すデータをリクエスト属性として設定
        request.setAttribute("workTimeData", workTimeData);
        request.setAttribute("breakList", breakList);

        // dakoku.jspにフォワード（画面表示）
        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/dakoku.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * POSTリクエストの処理メソッド
     * dakoku.jspのボタンに応じた各種処理を実行する：
     * - 出勤打刻（clock_in）
     * - 退勤打刻（clock_out）
     * - 休憩時間追加（add_break）
     * - 休憩時間削除（delete_break）
     * @param request HTTPリクエストオブジェクト
     * @param response HTTPレスポンスオブジェクト
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // セッション情報を取得（既存のセッションのみ）
        HttpSession session = request.getSession(false);

        // ログインチェック：セッションが存在しない、またはユーザー情報がない場合
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/web/login.jsp");
            return;
        }

        // ログインユーザーの情報を取得
        UserBean user = (UserBean) session.getAttribute("user");
        String empId = user.getEmpId(); // 従業員番号
        LocalDate today = LocalDate.now(); // 今日の日付

        // リクエストパラメータから実行するアクション（処理種別）を取得
        String action = request.getParameter("action");
        if (action == null) action = ""; // nullの場合は空文字にする

        // データベースから今日の勤怠データを取得
        WorkTimeBean workTime = workTimeDao.findWorkTimeByDate(empId, today);

        // アクションに応じて処理を分岐
        switch (action) {
            case "clock_in": // 出勤打刻処理
                // まだ出勤記録がない場合のみ新規作成
                if (workTime == null) {
                    // 新しい勤怠データオブジェクトを作成
                    WorkTimeBean newWorkTime = new WorkTimeBean();
                    newWorkTime.setEmpId(empId); // 従業員番号設定
                    newWorkTime.setKintaiDate(today); // 勤怠日付設定
                    newWorkTime.setClockIn(Time.valueOf(LocalTime.now())); // 現在時刻を出勤時刻として設定

                    // データベースに保存
                    workTimeDao.saveWorkTime(newWorkTime);
                    // 成功メッセージを設定
                    request.setAttribute("successMessage", "出勤打刻を記録しました");
                }
                // 既に出勤記録がある場合は何もしない（重複打刻防止）
                break;

            case "clock_out": // 退勤打刻処理
                // 出勤記録があり、かつ退勤記録がまだない場合のみ処理
                if (workTime != null && workTime.getClockOut() == null) {
                    workTime.setClockOut(Time.valueOf(LocalTime.now())); // 現在時刻を退勤時刻として設定
                    workTimeDao.saveWorkTime(workTime); // データベースを更新
                    request.setAttribute("successMessage", "退勤打刻を記録しました");
                }
                // 出勤記録がない、または既に退勤済みの場合は何もしない
                break;

            case "add_break": // 休憩時間追加処理
                // 出勤記録がないと休憩は追加できない（業務ルール）
                if (workTime == null) {
                    request.setAttribute("errorMessage", "先に出勤打刻をしてください");
                    break;
                }

                // リクエストパラメータから休憩開始・終了時刻を取得
                String breakStartStr = request.getParameter("breakStartTime");
                String breakEndStr = request.getParameter("breakEndTime");

                // 新しい休憩データオブジェクトを作成
                BreakBean newBreak = new BreakBean();
                newBreak.setKintaiRecId(workTime.getKintaiRecId()); // 勤怠記録IDを関連付け
                newBreak.setBreakStart(parseTime(breakStartStr)); // 休憩開始時刻を設定
                newBreak.setBreakEnd(parseTime(breakEndStr)); // 休憩終了時刻を設定

                // データベースに保存
                workTimeDao.addBreak(newBreak);
                request.setAttribute("successMessage", "休憩時間を追加しました");
                break;

            case "delete_break": // 休憩時間削除処理
                // リクエストパラメータから削除対象の休憩IDを取得
                String breakIdStr = request.getParameter("breakId");
                try {
                    // 文字列を整数に変換
                    int breakId = Integer.parseInt(breakIdStr);
                    // データベースから削除
                    workTimeDao.deleteBreak(breakId);
                    request.setAttribute("successMessage", "休憩記録を削除しました");
                } catch (NumberFormatException e) {
                    // 休憩IDが数値でない場合のエラーハンドリング
                    System.err.println("無効な休憩IDです: " + breakIdStr);
                }
                break;
        }

        // 処理完了後、画面を再表示するためdoGetメソッドを呼び出し
        doGet(request, response);
    }

    /**
     * 時間文字列を java.sql.Time オブジェクトに変換する補助メソッド
     * "HH:mm"または"H:mm"形式の文字列に対応（例: "09:00" または "9:00"）
     *
     * @param timeStr 変換する時間文字列（例: "14:30", "9:15"）
     * @return 変換後のTimeオブジェクト。変換できない場合はnullを返す
     */
    private Time parseTime(String timeStr) {
        // 入力値チェック：nullまたは空文字の場合
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        try {
            // "H:mm"フォーマットでパース（先頭の0を省略した形式にも対応）
            // 例: "9:00" → LocalTime(09:00), "14:30" → LocalTime(14:30)
            LocalTime localTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));

            // LocalTimeをjava.sql.Timeに変換して返す
            return Time.valueOf(localTime);
        } catch (DateTimeParseException e) {
            // パースに失敗した場合のエラーログ出力
            System.err.println("時間フォーマットのパースに失敗しました: " + timeStr);
            return null; // フォーマットが不正な場合はnullを返す
        }
    }
}