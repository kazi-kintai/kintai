<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%
    UserBean user = (UserBean) session.getAttribute("user");
%>
<%--
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
 --%>
<%
    String deptname = (String)session.getAttribute("deptname");
%>
<html>
<head>
    <title>勤怠管理システムメニュー</title>
</head>
<body style="text-align:center;">
    
    <div style="text-align:left">
	    <p>部署：<%= deptname %></p>
	    <p>氏名：</p>  <%-- 開発後に変更 <%= user.getName() --%>
	</div>
    
    <form method="post" action="<%= request.getContextPath() %>/login" style="text-align:right;">
    	<input type="button" name="logout" value="ログアウト">
    </form>

	<h1>基本メニュー</h1>

	<p><a href="">勤怠記録一覧</a></p>
	<p><a href="<%= request.getContextPath() %>/WorkTime">本日分の打刻</a></p>
	<p><a href="">過去の勤務記録</a></p>

</body>
</html>
