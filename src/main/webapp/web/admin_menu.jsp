<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%
    // セッションからユーザー情報を取得（セッションがなければログインページへ）
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null || user.getRole() != 1) { // 管理者でなければアクセスさせない
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    
    // ToDo: 本来はuser.getDeptId()を使って、データベースから部署名を取得すべき
    // (待办：原本应该使用 user.getDeptId() 从数据库中获取部署名)
    String deptName = "管理部"; // 一時的なプレースホルダ (暂时的占位符)
%>
<html>
<head>
    <title>管理者メニュー</title>
    <style>
        body {
            margin: 0;
            font-family: sans-serif;
            background: #f7f7f7;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center; /* 中央揃えに見栄えを良くする */
            padding: 10px 20px;
            background: #fff;
            border-bottom: 1px solid #ccc;
        }
        .user-info {
            line-height: 1.5;
        }
        .logout-button {
            padding: 8px 16px;
            cursor: pointer;
            border-radius: 5px;
            border: 1px solid #666;
            background-color: #f0f0f0;
        }
        .container {
            max-width: 800px;
            margin: auto;
            padding-top: 20px;
        }
        .menu-section {
            border: 1px solid #ccc;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 30px;
            background-color: #fff;
        }
        h2 {
            text-align: center;
            color: #333;
        }
        .menu-section p {
            text-align: center;
        }
        .menu-section a {
            font-size: 1.2em;
            text-decoration: none;
            color: #0066cc;
        }
        .menu-section a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

    <%-- ★★★ ここからヘッダー部分を修正 ★★★ --%>
    <div class="header">
        <div class="user-info">
            <p>部署：<%= deptName %></p>
            <p>氏名：<%= user.getName() %> さん（管理者）</p>
        </div>
        <%-- ログアウト機能は後で実装します --%>
        <form method="post" action="<%= request.getContextPath() %>/logout">
            <input type="submit" value="ログアウト" class="logout-button">
        </form>
    </div>
    <%-- ★★★ ここまでヘッダー部分を修正 ★★★ --%>

    <div class="container">
        <%-- 管理者個人のためのメニュー --%>
        <div class="menu-section">
            <h2>個人用メニュー</h2>
            <p><a href="<%= request.getContextPath() %>/showWorkPunchForm">本日分の打刻</a></p>
            <p><a href="">自身の勤怠記録</a></p>
        </div>

        <%-- 全従業員を管理するためのメニュー --%>
        <div class="menu-section">
            <h2>管理用メニュー</h2>
            <p><a href="">全従業員の勤怠一覧</a></p>
            <p><a href="">社員情報管理</a></p>
            <p><a href="">部署・役職管理</a></p>
        </div>
    </div>

</body>
</html>