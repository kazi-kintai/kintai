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
        String loggedInEmpno = user.getEmpno(); // ログイン中の従業員番号
        int userRoleId = user.getRoleId();          // ログイン中のユーザー権限 (旧userRoleからuserRoleIdへ変更)

        // --- フィルター条件の取得 ---
        String empNoFilter = request.getParameter("empNoFilter");
        String deptNoFilter = request.getParameter("deptNoFilter");
        String postNoFilter = request.getParameter("postNoFilter");
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

        // 検索対象の従業員番号リストを決定
        // ログインユーザーの権限に基づいてフィルタリングロジックを適用
        List<String> targetEmpNos = new ArrayList<>(); // 検索対象の従業員番号リストを初期化

        if (userRoleId == 0) { // 一般社員の場合 (ROLEIDが0)
            // 自身の勤怠記録のみを表示
            targetEmpNos.add(loggedInEmpno); // 検索対象をログインユーザーのempnoに固定
            // 一般社員は他の従業員を検索できないため、フィルターパラメータをクリア
            empNoFilter = null; // JSP側でempNoFilterの初期値として使うため、ここではnullのままにする
            deptNoFilter = null;
            postNoFilter = null;
        } else if (userRoleId == 1) { // 管理者の場合 (ROLEIDが1)
            // 全ての従業員の勤怠記録を検索可能。
            // empNoFilter が指定されていれば、そのempNoのみをtargetEmpNosに追加
            if (empNoFilter != null && !empNoFilter.trim().isEmpty()) {
                targetEmpNos.add(empNoFilter);
            }
            // empNoFilter が指定されていなければ、targetEmpNosは空のまま。
            // KintaiRecDaoはtargetEmpNosが空の場合に全従業員を対象として検索する
        }
        // TODO: 承認者（ROLEID=2）の場合のロジックをここに追加
        //       else if (userRoleId == 2) {
        //           // 直属の部下のempNoリストを取得
        //           targetEmpNos = empDao.findSubordinatesEmpNos(loggedInEmpno); // ※EmpDaoにこのメソッドを実装する必要あり
        //       }


        // 勤怠記録データの取得（KintaiRecDaoを使用）
        List<KintaiRecBean> kintaiRecords;
        try {
             // KintaiRecDao.getKintaiRecords() メソッドを呼び出す
            kintaiRecords = kintaiRecDao.getKintaiRecords(
                targetEmpNos, deptNoFilter, postNoFilter, startDate, endDate, userRoleId // userRoleからuserRoleIdへ変更
            );
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "勤怠記録の取得中にエラーが発生しました。");
            kintaiRecords = new java.util.ArrayList<>(); // エラー時は空リスト
        }


        // ドロップダウンリスト用のデータ（管理者向け）
        if (userRoleId == 1) { // 管理者のみフィルター用データを提供
            request.setAttribute("deptList", deptDao.findAll());
            request.setAttribute("postList", postDao.findAll());
            request.setAttribute("allEmpList", empDao.findAll()); // 従業員名フィルター用（全従業員）
        }


        // JSPに渡すデータをリクエスト属性として設定
        request.setAttribute("kintaiRecords", kintaiRecords);
        request.setAttribute("empNoFilter", empNoFilter); // 現在のフィルター値をJSPに渡す
        request.setAttribute("deptNoFilter", deptNoFilter);
        request.setAttribute("postNoFilter", postNoFilter);
        request.setAttribute("startDate", startDateStr);
        request.setAttribute("endDate", endDateStr);
        request.setAttribute("userRoleId", userRoleId); // JSPで権限に応じた表示を制御するためにロールIDを渡す


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
