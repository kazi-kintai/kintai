<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%
    // セッションからユーザー情報を取得し、もし存在しなければログインページに戻す
    // (直接このページにアクセスされるのを防ぐためのセキュリティ対策)
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<html>
<head>
    <title>メニュー</title>
</head>
<body>
    
    <h1>メインメニュー</h1>
    <p>ようこそ、<%= user.getName() %>さん。</p>
    <p>（ここはメインメニュー画面です。これから機能を追加していきます。）</p>

</body>
</html>
