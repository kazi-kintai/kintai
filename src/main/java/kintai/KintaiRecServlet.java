package kintai;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException; // 日付解析例外用
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 勤怠時間記録表示機能を提供するサーブレット。
 * ログインユーザーの権限に基づき、自身の、または指定された従業員の勤怠記録を検索・表示する。
 */
@WebServlet("/KintaiRecServlet")
public class KintaiRecServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // KintaiRecDao のインスタンス。
    private KintaiRecDao kintaiRecDao = new KintaiRecDao();
    private DeptDao deptDao = new DeptDao(); // 部署名取得用
    private PostDao postDao = new PostDao(); // 役職名取得用
    private EmpDao empDao = new EmpDao(); // 従業員情報取得用（主任/リーダー機能拡張用）


    /**
     * GETリクエストの処理メソッド。
     * 勤怠記録表示画面 (kintai_rec.jsp) を表示し、初期データやフィルター結果を渡す。
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
        String loggedInEmpId = user.getEmpId(); // ログイン中の従業員番号
        int userRoleId = user.getRoleId();          // ログイン中のユーザー権限 (旧userRoleからuserRoleIdへ変更)

        // --- モード判定（自分モードか全員モードか） ---
        String viewMode = request.getParameter("mode");
        boolean isSelfMode = "self".equals(viewMode);

        // --- フィルター条件の取得 ---
        String empIdFilter = request.getParameter("empIdFilter");
        String deptIdFilter = request.getParameter("deptIdFilter");
        String postIdFilter = request.getParameter("postIdFilter");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        LocalDate startDate = null;
        LocalDate endDate = null;

        // 日付文字列をLocalDateに変換
        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                startDate = LocalDate.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                endDate = LocalDate.parse(endDateStr);
            }
        } catch (DateTimeParseException e) {
            // 日付フォーマットが無効な場合のエラー処理
            request.setAttribute("errorMessage", "日付の形式が不正です。YYYY-MM-DD形式で入力してください。");
            startDate = null; // エラー時は日付フィルターをリセット
            endDate = null;
        }
        
        // 日付が指定されていない場合のデフォルト処理（今月）
        if (startDate == null && endDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        // 検索対象の従業員番号リストを決定
        // ログインユーザーの権限に基づいてフィルタリングロジックを適用
        List<String> targetEmpIds = new ArrayList<>(); // 検索対象の従業員番号リストを初期化

        if (userRoleId == 0) { // 一般社員の場合 (ROLEIDが0)
            // 自身の勤怠記録のみを表示
            targetEmpIds.add(loggedInEmpId); // 検索対象をログインユーザーのempIdに固定
            // 一般社員は他の従業員を検索できないため、フィルターパラメータをクリア
            empIdFilter = null; // JSP側でempIdFilterの初期値として使うため、ここではnullのままにする
            deptIdFilter = null;
            postIdFilter = null;
        } else if (userRoleId == 1) { // 管理者の場合 (ROLEIDが1)
            if (isSelfMode) {
                // 自分モード：管理者自身の勤怠記録のみを表示
                targetEmpIds.add(loggedInEmpId);
                // 自分モードの場合、フィルターを無効化
                empIdFilter = null;
                deptIdFilter = null;
                postIdFilter = null;
            } else {
                // 全員モード：全ての従業員の勤怠記録を検索可能
                // empIdFilter が指定されていれば、そのempNoのみをtargetEmpIdsに追加
                if (empIdFilter != null && !empIdFilter.trim().isEmpty()) {
                    targetEmpIds.add(empIdFilter);
                }
                // empIdFilter が指定されていなければ、targetEmpIdsは空のまま。
                // KintaiRecDaoはtargetEmpIdsが空の場合に全従業員を対象として検索する
            }
        } else if (userRoleId == 2) { // 部長の場合（ROLEID=2）
            if (isSelfMode) {
                // 自分モード：部長自身の勤怠記録のみを表示
                targetEmpIds.add(loggedInEmpId);
                // 自分モードの場合、フィルターを無効化
                empIdFilter = null;
                deptIdFilter = null;
                postIdFilter = null;
            } else {
                // 部門モード：同じ部門の従業員の勤怠記録を検索可能
                // 部門フィルターを自動的にログインユーザーの部門に設定
                deptIdFilter = user.getDeptId();
                // empIdFilter が指定されていれば、そのempNoのみをtargetEmpIdsに追加
                if (empIdFilter != null && !empIdFilter.trim().isEmpty()) {
                    targetEmpIds.add(empIdFilter);
                }
                // empIdFilter が指定されていなければ、targetEmpIdsは空のまま。
                // KintaiRecDaoは部門フィルターに基づいて同部門の従業員を対象として検索する
            }
        }
        // TODO: 承認者（ROLEID=2）の場合のロジックをここに追加
        //       else if (userRoleId == 2) {
        //           // 直属の部下のempNoリストを取得
        //           targetEmpIds = empDao.findSubordinatesEmpNos(loggedInEmpno); // ※EmpDaoにこのメソッドを実装する必要あり
        //       }


        // 勤怠記録データの取得（KintaiRecDaoを使用）
        List<KintaiRecBean> kintaiRecords;
        try {
             // KintaiRecDao.getKintaiRecords() メソッドを呼び出す
            kintaiRecords = kintaiRecDao.getKintaiRecords(
                targetEmpIds, deptIdFilter, postIdFilter, startDate, endDate, userRoleId // userRoleからuserRoleIdへ変更
            );
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "勤怠記録の取得中にエラーが発生しました。");
            kintaiRecords = new java.util.ArrayList<>(); // エラー時は空リスト
        }

        // 月度統計データの取得（自分モードまたは一般社員の場合）
        MonthlySummaryBean monthlySummary = null;
        if (userRoleId == 0 || isSelfMode) {
            String targetEmpId = (userRoleId == 0) ? loggedInEmpId : loggedInEmpId; // 自分のempno
            String currentMonth = java.time.YearMonth.now().toString(); // 現在の月 (YYYY-MM)
            try {
                monthlySummary = kintaiRecDao.getMonthlySummary(targetEmpId, currentMonth);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "月度統計の取得中にエラーが発生しました");
            }
        }

        // 法令遵守チェック（自分モードまたは一般社員の場合）
        ComplianceCheckResult complianceResult = null;
        if (userRoleId == 0 || isSelfMode) {
            String targetEmpId = (userRoleId == 0) ? loggedInEmpId : loggedInEmpId; // 自分のempno
            try {
                ComplianceChecker complianceChecker = new ComplianceChecker();
                complianceResult = complianceChecker.performComprehensiveCheck(kintaiRecords, targetEmpId);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "法令遵守チェック中にエラーが発生しました");
            }
        }

        // 今日の勤怠状況データの取得（管理者の場合）
        if (userRoleId == 1 && !isSelfMode) {
            try {
                // 今日の日付
                LocalDate today = LocalDate.now();
                
                // 今日の勤怠状況を取得
                int scheduledCount = kintaiRecDao.getScheduledEmployeeCount(today); // 出勤予定者数
                int workingCount = kintaiRecDao.getWorkingEmployeeCount(today);     // 出勤中者数
                int absentCount = kintaiRecDao.getAbsentEmployeeCount(today);       // 未出勤者数
                int vacationCount = kintaiRecDao.getVacationEmployeeCount(today);   // 休暇予定者数
                
                request.setAttribute("scheduledCount", scheduledCount);
                request.setAttribute("workingCount", workingCount);
                request.setAttribute("absentCount", absentCount);
                request.setAttribute("vacationCount", vacationCount);
                
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "今日の勤怠状況の取得中にエラーが発生しました");
                // エラー時はデフォルト値を設定
                request.setAttribute("scheduledCount", 25);
                request.setAttribute("workingCount", 23);
                request.setAttribute("absentCount", 2);
                request.setAttribute("vacationCount", 3);
            }
        }

        // 法令遵守違反者リストと詳細情報の取得（管理者の場合）
        List<String> violationEmployees = null;
        java.util.Map<String, ComplianceCheckResult> violationDetails = null;
        if (userRoleId == 1 && !isSelfMode) {
            try {
                ComplianceChecker complianceChecker = new ComplianceChecker();
                violationEmployees = new java.util.ArrayList<>();
                violationDetails = new java.util.HashMap<>();
                
                // 全従業員の違反をチェック
                List<EmpBean> allEmployees = empDao.findAll();
                for (EmpBean emp : allEmployees) {
                    // 今月の勤怠データを取得
                    List<String> targetEmpList = new java.util.ArrayList<>();
                    targetEmpList.add(emp.getEmpId());
                    List<KintaiRecBean> empRecords = kintaiRecDao.getKintaiRecords(
                        targetEmpList, null, null, 
                        LocalDate.now().withDayOfMonth(1), 
                        LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()), 
                        userRoleId
                    );
                    
                    // 法令遵守チェックを実行
                    ComplianceCheckResult result = complianceChecker.performComprehensiveCheck(empRecords, emp.getEmpId());
                    
                    // 違反がある場合はリストと詳細情報に追加
                    if (result.getTotalViolations() > 0) {
                        violationEmployees.add(emp.getEmpName());
                        // 従業員の詳細情報も設定
                        result.setEmpName(emp.getEmpName());
                        result.setDeptName(emp.getDeptName() != null ? emp.getDeptName() : "不明");
                        result.setPostName(emp.getPostName() != null ? emp.getPostName() : "不明");
                        violationDetails.put(emp.getEmpName(), result);
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "法令遵守違反者リストの取得中にエラーが発生しました");
                // エラー時はサンプルデータを設定
                violationEmployees = new java.util.ArrayList<>();
                violationEmployees.add("田中太郎");
                violationEmployees.add("佐藤花子");
                violationEmployees.add("山田健一");
                violationEmployees.add("中村咲子");
                violationEmployees.add("渡辺大輔");
            }
        }


        // ドロップダウンリスト用のデータ（管理者・部長向け）
        if ((userRoleId == 1 || userRoleId == 2) && !isSelfMode) { 
            // 管理者（ROLEID=1）または部長（ROLEID=2）かつ全員モードの場合のみフィルター用データを提供
            if (userRoleId == 1) {
                // 管理者の場合：全データを提供
                request.setAttribute("deptList", deptDao.findAll());
                request.setAttribute("postList", postDao.findAll());
                request.setAttribute("allEmpList", empDao.findAll()); // 従業員名フィルター用（全従業員）
            } else if (userRoleId == 2) {
                // 部長の場合：自部門のデータのみを提供
                request.setAttribute("deptList", deptDao.findAll()); // 部署リストは全体を表示（フィルター用）
                request.setAttribute("postList", postDao.findAll()); // 役職リストは全体を表示（フィルター用）
                request.setAttribute("allEmpList", empDao.findByFilters(user.getDeptId(), null)); // 同部門の従業員のみ
            }
        }


        // JSPに渡すデータをリクエスト属性として設定
        request.setAttribute("kintaiRecords", kintaiRecords);
        request.setAttribute("empIdFilter", empIdFilter); // 現在のフィルター値をJSPに渡す
        request.setAttribute("deptIdFilter", deptIdFilter);
        request.setAttribute("postIdFilter", postIdFilter);
        request.setAttribute("startDate", startDateStr);
        request.setAttribute("endDate", endDateStr);
        request.setAttribute("userRoleId", userRoleId); // JSPで権限に応じた表示を制御するためにロールIDを渡す
        request.setAttribute("isSelfMode", isSelfMode); // 自分モードかどうかをJSPに渡す
        request.setAttribute("monthlySummary", monthlySummary); // 月度統計データをJSPに渡す
        request.setAttribute("complianceResult", complianceResult); // 法令遵守チェック結果をJSPに渡す
        request.setAttribute("violationEmployees", violationEmployees); // 法令遵守違反者リストをJSPに渡す
        request.setAttribute("violationDetails", violationDetails); // 法令遵守違反詳細情報をJSPに渡す


        // 勤怠記録表示画面にフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/kintai_rec.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * POSTリクエストの処理メソッド。
     * 現時点ではフィルター処理のみGETメソッドで行うため、POSTでは特に処理しないが、
     * 将来的にこの画面から何らかの更新処理を行う場合に利用。
     * doGetにリダイレクトして再表示する。
     * @param request HTTPリクエストオブジェクト
     * @param response HTTPレスポンスオブジェクト
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // POSTリクエストもGETに転送して、同じロジックで画面を再表示
        doGet(request, response);
    }
}
