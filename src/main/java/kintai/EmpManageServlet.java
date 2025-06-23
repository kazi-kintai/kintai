package kintai;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 従業員管理機能を提供するサーブレット
 * 従業員の一覧表示、追加、更新、削除を処理する
 */
@WebServlet("/empManage")
public class EmpManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // DAOインスタンス
    private EmpDao empDao = new EmpDao();
    private DeptDao deptDao = new DeptDao();
    private PostDao postDao = new PostDao();
    private RoleDao roleDao = new RoleDao(); // 新規追加: RoleDao
    private GradeDao gradeDao = new GradeDao(); // 新規追加: GradeDao
    
    /**
     * GETリクエストの処理メソッド。
     * 従業員一覧を表示する。
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
        
        // 管理者権限チェック 
        UserBean user = (UserBean) session.getAttribute("user");
        if (user.getRoleId() != 1) { // ROLEIDが1が管理者
            response.sendRedirect(request.getContextPath() + "/web/menu.jsp");
            return;
        }
        
        // 従業員一覧、部署一覧、役職一覧、ロール一覧、等級一覧を取得
        List<EmpBean> empList = empDao.findAll();
        List<DeptBean> deptList = deptDao.findAll();
        List<PostBean> postList = postDao.findAll();
        List<RoleBean> roleList = roleDao.findAll(); 
        List<GradeBean> gradeList = gradeDao.findAll(); 
        
        request.setAttribute("empList", empList);
        request.setAttribute("deptList", deptList);
        request.setAttribute("postList", postList);
        request.setAttribute("roleList", roleList); // 新規追加
        request.setAttribute("gradeList", gradeList); // 新規追加
        
        // 従業員管理画面にフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/emp_manage.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * POSTリクエストの処理メソッド。
     * 従業員の追加、更新、削除を処理する。
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
        
        // 管理者権限チェック (UserBeanのgetRole()からgetRoleId()へ変更)
        UserBean user = (UserBean) session.getAttribute("user");
        if (user.getRoleId() != 1) { // ROLEIDが1が管理者
            response.sendRedirect(request.getContextPath() + "/web/menu.jsp");
            return;
        }
        
        // アクションを取得
        String action = request.getParameter("action");
        
        boolean success = false;
        String message = "";
        
        try {
            switch (action) {
                case "add":
                    // 新規追加処理
                    String newEmpNo = request.getParameter("empNo");
                    String newEmpName = request.getParameter("empName");
                    String newDeptNo = request.getParameter("deptNo");
                    String newPostNo = request.getParameter("postNo");
                    String newRoleIdStr = request.getParameter("roleId"); // 旧roleから変更
                    String newGradeNoStr = request.getParameter("gradeNo"); // 新規追加
                    String newPass = request.getParameter("pass");
                    String newMail = request.getParameter("mail"); // 新規追加
                    String newEmpDateStr = request.getParameter("empDate"); // 新規追加

                    // 入力チェック (最低限のチェック、詳細なビジネスロジックはDAOやサービス層で)
                    if (newEmpNo == null || newEmpNo.trim().isEmpty() || 
                        newEmpName == null || newEmpName.trim().isEmpty() ||
                        newDeptNo == null || newDeptNo.trim().isEmpty() ||
                        newPostNo == null || newPostNo.trim().isEmpty() ||
                        newRoleIdStr == null || newRoleIdStr.trim().isEmpty() ||
                        newGradeNoStr == null || newGradeNoStr.trim().isEmpty() ||
                        newPass == null || newPass.trim().isEmpty()) {
                        message = "必須項目をすべて入力してください";
                        break;
                    }

                    // 従業員番号の重複チェック
                    if (empDao.exists(newEmpNo)) {
                        message = "従業員番号「" + newEmpNo + "」は既に存在します";
                        break;
                    }
                    
                    EmpBean newEmp = new EmpBean();
                    newEmp.setEmpNo(newEmpNo);
                    newEmp.setEmpName(newEmpName);
                    newEmp.setDeptNo(newDeptNo);
                    newEmp.setPostNo(newPostNo);
                    newEmp.setRoleId(Integer.parseInt(newRoleIdStr)); // 旧setRoleから変更
                    newEmp.setGradeNo(Integer.parseInt(newGradeNoStr)); // 新規追加
                    newEmp.setPass(newPass);
                    newEmp.setMail(newMail); // 新規追加
                    // EMPDATEはnull許容として、JSPからの入力がない場合はnull
                    if (newEmpDateStr != null && !newEmpDateStr.trim().isEmpty()) {
                        newEmp.setEmpDate(java.time.LocalDate.parse(newEmpDateStr));
                    }
                    
                    success = empDao.insert(newEmp);
                    message = success ? "従業員を追加しました" : "従業員の追加に失敗しました";
                    break;
                    
                case "update":
                    // 更新処理
                    String updateEmpNo = request.getParameter("empNo");
                    String updateEmpName = request.getParameter("empName");
                    String updateDeptNo = request.getParameter("deptNo");
                    String updatePostNo = request.getParameter("postNo");
                    String updateRoleIdStr = request.getParameter("roleId"); // 旧roleから変更
                    String updateGradeNoStr = request.getParameter("gradeNo"); // 新規追加
                    String updatePass = request.getParameter("pass"); // パスワードは更新時も入力させる想定
                    String updateMail = request.getParameter("mail"); // 新規追加
                    String updateEmpDateStr = request.getParameter("empDate"); // 新規追加
                    
                    // 入力チェック
                    if (updateEmpName == null || updateEmpName.trim().isEmpty() ||
                        updateDeptNo == null || updateDeptNo.trim().isEmpty() ||
                        updatePostNo == null || updatePostNo.trim().isEmpty() ||
                        updateRoleIdStr == null || updateRoleIdStr.trim().isEmpty() ||
                        updateGradeNoStr == null || updateGradeNoStr.trim().isEmpty() ||
                        updatePass == null || updatePass.trim().isEmpty()) { // パスワードも必須
                        message = "必須項目をすべて入力してください";
                        break;
                    }
                    
                    EmpBean updateEmp = new EmpBean();
                    updateEmp.setEmpNo(updateEmpNo);
                    updateEmp.setEmpName(updateEmpName);
                    updateEmp.setDeptNo(updateDeptNo);
                    updateEmp.setPostNo(updatePostNo);
                    updateEmp.setRoleId(Integer.parseInt(updateRoleIdStr)); // 旧setRoleから変更
                    updateEmp.setGradeNo(Integer.parseInt(updateGradeNoStr)); // 新規追加
                    updateEmp.setPass(updatePass);
                    updateEmp.setMail(updateMail); // 新規追加
                    if (updateEmpDateStr != null && !updateEmpDateStr.trim().isEmpty()) {
                        updateEmp.setEmpDate(java.time.LocalDate.parse(updateEmpDateStr));
                    } else {
                        updateEmp.setEmpDate(null); // 入力がない場合はnull
                    }
                    
                    success = empDao.update(updateEmp);
                    message = success ? "従業員情報を更新しました" : "従業員情報の更新に失敗しました";
                    break;
                    
                case "delete":
                    // 削除処理
                    String deleteEmpNo = request.getParameter("empNo");
                    success = empDao.delete(deleteEmpNo);
                    
                    if (success) {
                        message = "従業員を削除しました";
                    } else {
                        message = "従業員の削除に失敗しました。この従業員に関連するデータが存在する可能性があります。"; // メッセージを一般化
                    }
                    break;
                    
                default:
                    message = "不正な操作です";
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            message = "入力された数値（ロールID、等級番号）が不正です";
        } catch (java.time.format.DateTimeParseException e) {
            e.printStackTrace();
            message = "入社年月日の形式が不正です。YYYY-MM-DD形式で入力してください。";
        } catch (Exception e) {
            e.printStackTrace();
            message = "処理中にエラーが発生しました";
        }
        
        // 処理結果をリクエスト属性に設定
        request.setAttribute("message", message);
        request.setAttribute("success", success);
        
        // 従業員一覧を再取得して表示
        doGet(request, response);
    }
}
