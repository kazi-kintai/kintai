package kintai;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * ログイン処理を受け持つサーブレット。
 */
@WebServlet("/LoginCheck")
public class LoginCheckServlet extends HttpServlet {

@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 1. JSPから送信されたパラメータを取得
    String empno = request.getParameter("empno");
    String password = request.getParameter("password");

    // 2. UserDaoをインスタンス化
    UserDao userDao = new UserDao();

    // 3. UserDaoを使って、データベースにユーザーが存在するか問い合わせる
    UserBean user = userDao.findByLoginInfo(empno, password);

    // 4. 認証結果に応じて処理を分岐
    if (user != null) {
        // --- ログイン成功の処理 ---

        // 新しいセッションを開始し、ユーザー情報を保存する
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        //  ユーザーの役割(ROLE)に応じてリダイレクト先を決定
        if (user.getRole() == 1) {
            // 管理者の場合 -> 管理者用メニューにリダイレクト
            response.sendRedirect(request.getContextPath() + "/web/admin_menu.jsp");
        } else {
            // 普通従業員の場合 -> 通常メニューにリダイレクト
            response.sendRedirect(request.getContextPath() + "/web/menu.jsp");
        }

    } else {
        // --- ログイン失敗の処理 ---
        request.setAttribute("errorMessage", "従業員番号またはパスワードが正しくありません");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/web/login.jsp");
        dispatcher.forward(request, response);
        }
    }
}
