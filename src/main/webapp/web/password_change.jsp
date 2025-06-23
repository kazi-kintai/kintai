<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%
    // ログインチェック
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }

    // セッションからユーザー情報を取得
    String loggedInUserName = user.getName();
    String loggedInDeptName = (String) session.getAttribute("deptName");
    int userRoleId = user.getRoleId(); 

    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");

    // メニューへ戻るリンクのURLを権限に応じて設定
    String backUrl = (userRoleId == 1) ? request.getContextPath() + "/web/admin_menu.jsp" : request.getContextPath() + "/web/menu.jsp";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>パスワード変更</title>
    <style>
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
            display: flex;
            flex-direction: column;
            align-items: center;
            min-height: 100vh;
        }
        .container {
            max-width: 500px;
            width: 100%;
            margin: auto;
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            padding: 10px 20px;
            background: #fff;
            border-bottom: 1px solid #ccc;
            margin: -30px -30px 20px -30px; /* 親コンテナのパディングを相殺 */
            border-top-left-radius: 8px;
            border-top-right-radius: 8px;
        }
        .user-info {
            display: flex;
            flex-direction: column;
            line-height: 1.5;
            text-align: left;
        }
        .logout-button {
            background-color: #dc3545;
            color: white;
            border: 1px solid #dc3545;
            border-radius: 5px;
            padding: 8px 16px;
            cursor: pointer;
            font-size: 1em;
            text-decoration: none;
            align-self: center;
        }
        .logout-button:hover {
            background-color: #c82333;
            border-color: #bd2130;
        }
        h1 {
            color: #333;
            text-align: center;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
            margin-top: 0;
            margin-bottom: 30px;
        }
        /* メッセージ表示エリア */
        .message {
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 4px;
            text-align: center;
        }
        .success-message {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .error-message {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #555;
        }
        .form-group input[type="password"] {
            width: calc(100% - 16px);
            padding: 10px;
            border: 1px solid #ced4da;
            border-radius: 4px;
            font-size: 1em;
        }
        .button-container {
            text-align: center;
            margin-top: 30px;
        }
        .btn-primary {
            background-color: #007bff;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1.1em;
            transition: background-color 0.2s ease;
        }
        .btn-primary:hover {
            background-color: #0056b3;
        }
        .back-link {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background-color: #6c757d;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            text-align: center;
            transition: background-color 0.2s ease;
        }
        .back-link:hover {
            background-color: #545b62;
        }
    </style>
    <script>
        function validateForm() {
            var currentPass = document.getElementById("currentPassword").value;
            var newPass = document.getElementById("newPassword").value;
            var confirmNewPass = document.getElementById("confirmNewPassword").value;
            var errorMessageDiv = document.getElementById("errorMessage");

            if (currentPass.trim() === "" || newPass.trim() === "" || confirmNewPass.trim() === "") {
                errorMessageDiv.innerHTML = "すべてのパスワードフィールドを入力してください";
                return false;
            }

            if (newPass !== confirmNewPass) {
                errorMessageDiv.innerHTML = "新しいパスワードと確認用パスワードが一致しません";
                return false;
            }

            // TODO: 必要に応じてパスワードの複雑性チェックを追加

            errorMessageDiv.innerHTML = ""; // エラーメッセージをクリア
            return confirm("パスワードを変更してもよろしいですか？");
        }
    </script>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="user-info">
                <p>部署：<%= loggedInDeptName != null ? loggedInDeptName : "情報なし" %></p>
                <p>氏名：<%= loggedInUserName %></p>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/logout" style="margin: 0;">
                <input type="submit" value="ログアウト" class="logout-button">
            </form>
        </div>

        <h1>パスワード変更</h1>

        <%-- メッセージ表示 --%>
        <% if (successMessage != null) { %>
            <div class="message success-message">
                <%= successMessage %>
            </div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message error-message">
                <%= errorMessage %>
            </div>
        <% } %>

        <form action="<%= request.getContextPath() %>/PasswordChangeServlet" method="post" onsubmit="return validateForm()">
            <div class="form-group">
                <label for="currentPassword">現在のパスワード:</label>
                <input type="password" id="currentPassword" name="currentPassword" required>
            </div>
            <div class="form-group">
                <label for="newPassword">新しいパスワード:</label>
                <input type="password" id="newPassword" name="newPassword" required>
            </div>
            <div class="form-group">
                <label for="confirmNewPassword">新しいパスワード（確認用）:</label>
                <input type="password" id="confirmNewPassword" name="confirmNewPassword" required>
            </div>
            <div class="button-container">
                <button type="submit" class="btn-primary">パスワード変更</button>
            </div>
        </form>

        <div class="button-container">
            <a href="<%= backUrl %>" class="back-link">メニューへ戻る</a>
        </div>
    </div>
</body>
</html>
