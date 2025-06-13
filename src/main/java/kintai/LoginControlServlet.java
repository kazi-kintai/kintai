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
@WebServlet("/LoginControl")
public class LoginControlServlet extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // 1. JSPから送信されたパラメータを取得
        String empno = request.getParameter("empno");
        String password = request.getParameter("password");
        
//     // ★★★ デバッグ用のコードを追加 ★★★
//        // (★★★ 添加调试用的代码 ★★★)
//        System.out.println("サーブレットが受け取ったempno: [" + empno + "]");
//        System.out.println("サーブレットが受け取ったpassword: [" + password + "]");
        
        // 2. UserDaoをインスタンス化
        UserDao userDao = new UserDao();
        
        // 3. UserDaoを使って、データベースにユーザーが存在するか問い合わせる
        UserBean user = userDao.findByLoginInfo(empno, password);
        
        // 4. 認証結果に応じて処理を分岐
        if (user != null) {
            // --- ログイン成功の処理 ---
            
            //  新しいセッションを開始し、ユーザー情報を保存する
            //     これにより、ユーザーはログイン状態を維持できる
            HttpSession session = request.getSession();
            session.setAttribute("user", user); // "user"というキーでUserBeanオブジェクトを保存
            
            //  成功ページにリダイレクトする
            response.sendRedirect(request.getContextPath() + "/web/menu.jsp");
            
        } else {
            // --- ログイン失敗の処理 ---
            
            //  エラーメッセージをリクエストに設定する
            request.setAttribute("errorMessage", "従業員番号またはパスワードが正しくありません。");
            
            //  login.jspに処理を「フォワード」して、エラーメッセージを表示させる
            RequestDispatcher dispatcher = request.getRequestDispatcher("/web/login.jsp");
            dispatcher.forward(request, response);
        }
    }
}
