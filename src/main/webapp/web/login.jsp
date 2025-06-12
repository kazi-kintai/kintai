<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>ログイン</title>
    <style>
        /* 簡単な中央揃えのスタイル */
        body {
            font-family: sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background-color: #f7f7f7;
        }
        .login-container {
            text-align: center;
            padding: 40px;
            border: 1px solid #ccc;
            border-radius: 10px;
            background-color: white;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .form-group {
            margin-bottom: 15px;
            text-align: left;
        }
        .form-group label {
            display: inline-block;
            width: 100px; /* ラベルの幅を固定 */
        }
        .error-message {
            color: red;
            margin-top: 20px;
            height: 20px; /* エラーメッセージの高さを確保 */
        }
        input[type="submit"] {
            padding: 10px 20px;
            cursor: pointer;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h2>社員ログイン</h2>
        <p>従業員番号とパスワードを入力してください</p>

        <%-- ログインフォーム --%>
        <%-- action属性は、あとで作成するサーブレットのパスに設定します --%>
        <form action="LoginControlServlet" method="post">
            <div class="form-group">
                <label for="empno">従業員番号:</label>
                <input type="text" id="empno" name="empno" required>
            </div>
            <div class="form-group">
                <label for="password">パスワード:</label>
                <input type="password" id="password" name="password" required>
            </div>

            <input type="submit" value="ログイン">
        </form>

        <%-- エラーメッセージ表示エリア  --%>
        <div class="error-message">
            <%-- あとでサーブレットから渡されたエラーメッセージをここに表示します --%>
            <%
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null) {
                    out.println(errorMessage);
                }
            %>
        </div>
    </div>
</body>
</html>
