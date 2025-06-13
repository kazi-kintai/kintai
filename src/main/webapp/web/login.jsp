<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>ログイン</title>
    <style>
        /* 簡単な中央揃えのスタイル */
        body {
            font-family: sans-serif;
            display: flex;
            flex-direction: column; 
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background-color: #f7f7f7;
        }
        .title {
 			text-align: center;
			font-weight: bold;
			font-size: 24px;
			margin: 0 0 30px 0;
			color: #333;
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
    <p class="title">勤怠管理システム</p>
    <div class="login-container">
        <h2>従業員ログイン</h2>
        <p>従業員番号とパスワードを入力してください</p>

        <%-- ログインフォーム --%>
        <form action="<%=request.getContextPath()%>/LoginControl" method="post">
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
