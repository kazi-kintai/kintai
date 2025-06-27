<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, kintai.*" %>
<%@ page import="java.time.LocalDate" %>
<%
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null || user.getRoleId() != 1) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }

    String mode = request.getParameter("mode");
    String selectedType = request.getParameter("leaveType");
    if (selectedType == null) selectedType = "annual";

    List<EmpBean> unissuedList = (List<EmpBean>) request.getAttribute("unissuedList");
    Integer grantedCount = (Integer) request.getAttribute("grantedCount");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>休暇付与管理</title>
	<style>
    body {
      font-family: 'Arial', sans-serif;
      background-color: #f5f5f5;
      margin: 0;
      padding: 20px;
    }

    .container {
      max-width: 1200px;
      margin: auto;
      background: #ffffff;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 0 10px rgba(0,0,0,0.1);
    }

    h1, h2 {
      border-bottom: 2px solid #ccc;
      padding-bottom: 5px;
      margin-bottom: 20px;
    }

    .form-inline {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-bottom: 20px;
      align-items: center;
    }

    label {
      font-weight: bold;
      margin-right: 5px;
    }

    input[type="text"],
    input[type="date"],
    select {
      padding: 6px;
      font-size: 14px;
      border: 1px solid #ccc;
      border-radius: 4px;
    }

    button {
      padding: 6px 12px;
      font-size: 14px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .btn-primary {
      background-color: #1976d2;
      color: white;
    }

    .btn-success {
      background-color: #388e3c;
      color: white;
    }

    .btn-danger {
      background-color: #d32f2f;
      color: white;
    }

    .emp-table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 20px;
    }

    .emp-table th,
    .emp-table td {
      border: 1px solid #ccc;
      padding: 8px;
      text-align: left;
    }

    .emp-table th {
      background-color: #eeeeee;
    }

    .message {
      margin: 10px 0;
      padding: 10px;
      border-radius: 5px;
    }

    .success-message {
      background-color: #e8f5e9;
      color: #2e7d32;
      border: 1px solid #c8e6c9;
    }

    .error-message {
      background-color: #ffebee;
      color: #c62828;
      border: 1px solid #ef9a9a;
    }

    .section {
      margin-bottom: 30px;
    }

    .back-link {
      display: inline-block;
      margin-top: 20px;
      text-decoration: none;
      color: #1976d2;
      font-weight: bold;
    }

    .back-link:hover {
      text-decoration: underline;
    }
  </style>
</head>
<body>
<div class="container">
    <h1>休暇付与管理</h1>
    <p>本日：<%= LocalDate.now() %></p>

	<% String message = (String) request.getAttribute("message");
   	Boolean success = (Boolean) request.getAttribute("success"); %>

	<% if (message != null && !message.isEmpty()) { %>
    	<div class="message <%= (success != null && success) ? "success-message" : "error-message" %>">
        	<%= message %>
    	</div>
	<% } %>
	<div class="section">
    <h2>未付与サマリー</h2>
    <ul>
        <li>年次有給休暇： <strong><%= request.getAttribute("unissuedAnnual") %></strong> 名 未付与</li>
        <li>初回付与（3・6か月）： <strong><%= request.getAttribute("unissuedInitial") %></strong> 名 未付与</li>
        <li>特別休暇（期首）： <strong><%= request.getAttribute("unissuedSpecial") %></strong> 名 未付与</li>
    </ul>
	</div>
	
    <form method="get" action="leaveGrantManage" class="form-inline">
        <label>対象休日種別：</label>
        <select name="leaveType">
            <option value="annual" <%= "annual".equals(selectedType) ? "selected" : "" %>>年次有給休暇</option>
            <option value="initial" <%= "initial".equals(selectedType) ? "selected" : "" %>>初回付与（3・6か月）</option>
            <option value="special" <%= "special".equals(selectedType) ? "selected" : "" %>>特別休暇</option>
        </select>
        <input type="hidden" name="mode" value="preview" />
        <button class="btn btn-primary" type="submit">未付与者を表示</button>
    </form>

    <% if ("preview".equals(mode) && unissuedList != null) { %>
        <div class="section">
            <h2>未付与者一覧（<%= unissuedList.size() %>人）</h2>
            <% if (unissuedList.isEmpty()) { %>
                <p>対象者はいません</p>
            <% } else { %>
                <form method="post" action="leaveGrantManage">
                    <input type="hidden" name="mode" value="execute" />
                    <input type="hidden" name="leaveType" value="<%= selectedType %>" />
                    <table class="emp-table">
                        <thead>
                        <tr><th>従業員番号</th><th>氏名</th><th>入社年月日</th></tr>
                        </thead>
                        <tbody>
                        <% for (EmpBean emp : unissuedList) { %>
                            <tr>
                                <td><%= emp.getEmpId() %></td>
                                <td><%= emp.getEmpName() %></td>
                                <td><%= emp.getEmpDate() %></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                    <button class="btn btn-success" type="submit">対象従業員に付与</button>
                </form>
            <% } %>
        </div>
    <% } %>

    <% if ("execute".equals(mode) && grantedCount != null) { %>
        <div class="section">
            <div class="message success-message">
            
                <strong><%= grantedCount %></strong> 名に「<%= selectedType %>」休暇を付与しました。
            </div>
        </div>
    <% } %>

    <a href="<%= request.getContextPath() %>/web/admin_menu.jsp" class="back-link">メニューへ戻る</a>
</div>
</body>
</html>