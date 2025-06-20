<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%
    UserBean user = (UserBean) session.getAttribute("user");
    // ログインチェック
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    
    String deptname = (String)session.getAttribute("deptname");
%>
<html>
<head>
    <title>勤怠管理システムメニュー（営業部）</title>
    
    <style>
       	body {
            margin: 0;
            font-family: sans-serif;
            background: #f7f7f7;
            height: 100vh;
        }
       .header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            padding: 10px 20px;
            background: #fff;
            border-bottom: 1px solid #ccc;
        }
        .user-info {
            display: flex;
            flex-direction: column;
            line-height: 1.5;
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
        }
        .logout-button:hover {
            background-color: #c82333;
            border-color: #bd2130;
        }
        .menu {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 20px;
            text-align: center;
        }
        .menu h3 {
            margin-top: 20px;
            margin-bottom: 10px;
            color: #333;
        }
        .menu p {
            margin: 8px 0;
        }
        .menu a {
            color: #007bff;
            text-decoration: none;
            font-size: 16px;
        }
        .menu a:hover {
            text-decoration: underline;
        }
     </style>
    
</head>
<body>
    
    <div class="header">
		<div class="user-info">
	    	<%-- 部署名と氏名を表示 --%>
	   		<p>部署：営業部<%-- <%= deptname %> --%></p>
	    	<p>氏名：<%= user.getName() %></p>
		</div>
	    <%-- ログアウトボタン（修正版） --%>
	    <form method="post" action="<%= request.getContextPath() %>/logout" style="margin: 0;">
    		<input type="submit" value="ログアウト" class="logout-button">
    	</form>
	</div>
    
	<div class="menu">

		<%-- 基本機能 --%>
		<h3>基本メニュー</h3>
		<p><a href="<%= request.getContextPath() %>/showWorkPunchForm">本日分の打刻</a></p>
		<p><a href="<%= request.getContextPath() %>/KintaiRecServlet">従業員別勤怠記録表示</a></p>
		<p><a href="<%= request.getContextPath() %>/KinmuManageServlet">勤務時間管理</a></p>

<%-- 管理用メニューに分離する必要があれば戻す --%>
<%--		<h3>管理用メニュー</h3>
		<p><a href="<%= request.getContextPath() %>/KintaiRecServlet">従業員別勤怠記録表示</a></p>			
 --%>
	</div>
</body>
</html>