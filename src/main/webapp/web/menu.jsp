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
            color: white;
            border: 1px solid #666;
            border-radius: 5px;
            padding: 8px 16px;
            cursor: pointer;
            font-size: 1em;
        }
        .menu {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: calc(100vh - 60px);
            text-align: center;
        }
     </style>
    
</head>
<body>
    
    <div class="header">
		<div class="userinfo">
	    	<%--LoginControlに部署名と氏名を取得する処理を要追加 --%>
	   		<p>部署：営業部<%-- <%= deptname --%></p>
	    	<p>氏名：山田太郎<%--<%= user.getName()--%></p>
		</div>
	    <form method="post" action="<%= request.getContextPath() %>/login" class="logout-button">
    		<input type="button" name="logout" value="ログアウト">
    	</form>
	</div>
    
	<div class="menu">
		<h1>基本メニュー</h1>

		<p><a href="">勤怠記録一覧</a></p>
		<p><a href="<%= request.getContextPath() %>/WorkControl">本日分の打刻</a></p>
		<p><a href="">過去の勤務記録</a></p>
	</div>
</body>
</html>
