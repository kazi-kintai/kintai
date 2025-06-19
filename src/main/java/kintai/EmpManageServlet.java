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
    
    private EmpDao empDao = new EmpDao();
    private DeptDao deptDao = new DeptDao();
    private PostDao postDao = new PostDao();
    
    /**
     * GETリクエストの処理
     * 従業員一覧を表示する
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
        if (user.getRole() != 1) {
            response.sendRedirect(request.getContextPath() + "/web/menu.jsp");
            return;
        }
        
        // 従業員一覧、部署一覧、役職一覧を取得
        List<EmpBean> empList = empDao.findAll();
        List<DeptBean> deptList = deptDao.findAll();
        List<PostBean> postList = postDao.findAll();
        
        request.setAttribute("empList", empList);
        request.setAttribute("deptList", deptList);
        request.setAttribute("postList", postList);
        
        // 従業員管理画面にフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/emp_manage.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * POSTリクエストの処理
     * 従業員の追加、更新、削除を処理する
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
        
        // 管理者権限チェック
        UserBean user = (UserBean) session.getAttribute("user");
        if (user.getRole() != 1) {
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
                    String newPass = request.getParameter("pass");
                    String newRoleStr = request.getParameter("role");
                    
                    // 入力チェック
                    if (newEmpNo == null || newEmpNo.trim().isEmpty() || 
                        newEmpName == null || newEmpName.trim().isEmpty() ||
                        newDeptNo == null || newDeptNo.trim().isEmpty() ||
                        newPostNo == null || newPostNo.trim().isEmpty() ||
                        newPass == null || newPass.trim().isEmpty()) {
                        message = "すべての項目は必須入力です";
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
                    newEmp.setPass(newPass);
                    newEmp.setRole(Integer.parseInt(newRoleStr));
                    
                    success = empDao.insert(newEmp);
                    message = success ? "従業員を追加しました" : "従業員の追加に失敗しました";
                    break;
                    
                case "update":
                    // 更新処理
                    String updateEmpNo = request.getParameter("empNo");
                    String updateEmpName = request.getParameter("empName");
                    String updateDeptNo = request.getParameter("deptNo");
                    String updatePostNo = request.getParameter("postNo");
                    String updatePass = request.getParameter("pass");
                    String updateRoleStr = request.getParameter("role");
                    
                    // 入力チェック
                    if (updateEmpName == null || updateEmpName.trim().isEmpty() ||
                        updateDeptNo == null || updateDeptNo.trim().isEmpty() ||
                        updatePostNo == null || updatePostNo.trim().isEmpty() ||
                        updatePass == null || updatePass.trim().isEmpty()) {
                        message = "すべての項目は必須入力です";
                        break;
                    }
                    
                    EmpBean updateEmp = new EmpBean();
                    updateEmp.setEmpNo(updateEmpNo);
                    updateEmp.setEmpName(updateEmpName);
                    updateEmp.setDeptNo(updateDeptNo);
                    updateEmp.setPostNo(updatePostNo);
                    updateEmp.setPass(updatePass);
                    updateEmp.setRole(Integer.parseInt(updateRoleStr));
                    
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
                        message = "従業員の削除に失敗しました。この従業員の勤怠データが存在する可能性があります";
                    }
                    break;
                    
                default:
                    message = "不正な操作です";
            }
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