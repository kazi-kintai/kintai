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
 * 役職管理機能を提供するサーブレット
 * 役職の一覧表示、追加、更新、削除を処理する
 */
@WebServlet("/postManage")
public class PostManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private PostDao postDao = new PostDao();
    
    /**
     * GETリクエストの処理
     * 役職一覧を表示する
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
        
        // 役職一覧を取得
        List<PostBean> postList = postDao.findAll();
        request.setAttribute("postList", postList);
        
        // 役職管理画面にフォワード
        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/post_manage.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * POSTリクエストの処理
     * 役職の追加、更新、削除を処理する
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
                    String newPostNo = request.getParameter("postNo");
                    String newPostName = request.getParameter("postName");
                    
                    // 入力チェック
                    if (newPostNo == null || newPostNo.trim().isEmpty() || 
                        newPostName == null || newPostName.trim().isEmpty()) {
                        message = "役職番号と役職名は必須入力です";
                        break;
                    }
                    
                    // 役職番号の重複チェック
                    if (postDao.exists(newPostNo)) {
                        message = "役職番号「" + newPostNo + "」は既に存在します";
                        break;
                    }
                    
                    PostBean newPost = new PostBean(newPostNo, newPostName);
                    success = postDao.insert(newPost);
                    message = success ? "役職を追加しました" : "役職の追加に失敗しました";
                    break;
                    
                case "update":
                    // 更新処理
                    String updatePostNo = request.getParameter("postNo");
                    String updatePostName = request.getParameter("postName");
                    
                    // 入力チェック
                    if (updatePostName == null || updatePostName.trim().isEmpty()) {
                        message = "役職名は必須入力です";
                        break;
                    }
                    
                    PostBean updatePost = new PostBean(updatePostNo, updatePostName);
                    success = postDao.update(updatePost);
                    message = success ? "役職を更新しました" : "役職の更新に失敗しました";
                    break;
                    
                case "delete":
                    // 削除処理
                    String deletePostNo = request.getParameter("postNo");
                    success = postDao.delete(deletePostNo);
                    
                    if (success) {
                        message = "役職を削除しました";
                    } else {
                        message = "役職の削除に失敗しました。この役職に所属する社員が存在する可能性があります";
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
        
        // 役職一覧を再取得して表示
        doGet(request, response);
    }
}