<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List, java.util.Map" %>
<%@ page import="kintai.UserBean" %>
<%
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    String backUrl = (user.getRole() == 1) ? request.getContextPath() + "/web/admin_menu.jsp" : request.getContextPath() + "/web/menu.jsp";

    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
    Map<String, String> workTimeData = (Map<String, String>) request.getAttribute("workTimeData");
    List<Map<String, String>> breakList = (List<Map<String, String>>) request.getAttribute("breakList");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");

    if (workTimeData == null) workTimeData = new java.util.HashMap<>();
    if (breakList == null) breakList = new java.util.ArrayList<>();

    boolean hasClockedIn = workTimeData.get("clockInTime") != null;
    boolean hasClockedOut = workTimeData.get("clockOutTime") != null;

    // ★★★ ここで最初の休憩かどうかを判断 ★★★
    boolean isFirstBreak = breakList.isEmpty(); 
%>
<html>
<head>
    <title>勤怠打刻・登録</title>
    <style>
        body { font-family: sans-serif; background-color: #f4f4f4; padding: 20px; }
        .container { max-width: 650px; margin: auto; padding: 30px; border: 1px solid #ccc; border-radius: 8px; background-color: white; }
        .header, h4 { text-align: center; }
        .header { margin-bottom: 25px; border-bottom: 1px solid #eee; padding-bottom: 15px; font-size: 1.2em;}
        .section { margin-bottom: 25px; }
        .punch-panel { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 20px; }
        .punch-button { font-size: 1.2em; padding: 20px; cursor: pointer; border-radius: 8px; border: 1px solid; }
        .punch-button:disabled { background-color: #e9ecef; color: #6c757d; cursor: not-allowed; }
        .clock-in { background-color: #28a745; border-color: #28a745; color: white; }
        .clock-out { background-color: #dc3545; border-color: #dc3545; color: white; }
        
        .form-group { margin-bottom: 15px; display: flex; align-items: center; }
        .form-group label { font-weight: bold; width: 130px; }
        .form-group input[type="text"] { flex-grow: 1; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
        .add-break-button { background-color: #5cb85c; border-color: #4cae4c; color: white; padding: 8px 15px; font-size: 1em; }

        .status-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .status-table th, .status-table td { border: 1px solid #ddd; padding: 10px; text-align: center; }
        .status-table th { background-color: #f2f2f2; }
        .delete-form button { background: #d9534f; border-color: #d43f3a; color: white; padding: 5px 10px; border-radius: 3px; cursor: pointer; }
        
        .message-box { padding: 10px; margin-bottom: 20px; border: 1px solid; border-radius: 4px; text-align: center; }
        .success-message { background-color: #d4edda; color: #155724; border-color: #c3e6cb; }
        .error-message { background-color: #f8d7da; color: #721c24; border-color: #f5c6cb; }

        .button-container { text-align: center; margin-top: 20px; }
        .back-button { display: inline-block; padding: 8px 24px; border-radius: 5px; text-decoration: none; font-size: 1em; background-color: #6c757d; color: white; border: 1px solid #5a6268; }
        .back-button:hover { background-color: #5a6268; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header"><h3><%= today %> の勤怠打刻・登録</h3></div>

        <% if (successMessage != null) { %><div class="message-box success-message"><%= successMessage %></div><% } %>
        <% if (errorMessage != null) { %><div class="message-box error-message"><%= errorMessage %></div><% } %>

        <div class="section">
            <h4>出退勤打刻</h4>
            <div class="status-item" style="text-align:center; margin-bottom: 15px;">
                <strong>出勤:</strong> <%= workTimeData.getOrDefault("clockInTime", "---") %> | 
                <strong>退勤:</strong> <%= workTimeData.getOrDefault("clockOutTime", "---") %>
            </div>
            <form action="workPunch" method="post">
                <div class="punch-panel">
                    <button type="submit" name="action" value="clock_in" class="punch-button clock-in" <%= (hasClockedIn) ? "disabled" : "" %>>出勤</button>
                    <button type="submit" name="action" value="clock_out" class="punch-button clock-out" <%= (!hasClockedIn || hasClockedOut) ? "disabled" : "" %>>退勤</button>
                </div>
            </form>
        </div>
        <hr>

        <div class="section">
            <h4>休憩の登録・管理</h4>
            <form action="workPunch" method="post">
                <input type="hidden" name="action" value="add_break">
                <div class="form-group">
                    <label>休憩開始:</label>
                    <%-- 一回目の休憩 --%>
                    <input type="text" name="breakStartTime" value="<%= isFirstBreak ? "12:00" : "" %>" placeholder="例: 12:00">
                </div>
                <div class="form-group">
                    <label>休憩終了:</label>
                    <input type="text" name="breakEndTime" value="<%= isFirstBreak ? "13:00" : "" %>" placeholder="例: 13:00">
                </div>
                <div style="text-align:right;">
                    <button type="submit" class="add-break-button" <%= (!hasClockedIn || hasClockedOut) ? "disabled" : "" %>>+ 休憩を追加する</button>
                </div>
            </form>
            
            <table class="status-table" style="margin-top:20px;">
                <thead><tr><th>休憩開始</th><th>休憩終了</th><th>操作</th></tr></thead>
                <tbody>
                    <% if (breakList.isEmpty()) { %>
                        <tr><td colspan="3">休憩記録はありません。</td></tr>
                    <% } else { for (Map<String, String> breakItem : breakList) { %>
                        <tr>
                            <td><%= breakItem.get("startTime") %></td>
                            <td><%= breakItem.getOrDefault("endTime", "---") %></td>
                            <td>
                                <form class="delete-form" action="workPunch" method="post">
                                    <input type="hidden" name="action" value="delete_break">
                                    <input type="hidden" name="breakId" value="<%= breakItem.get("breakId") %>">
                                    <button type="submit" <%= (hasClockedOut) ? "disabled" : "" %>>削除</button>
                                </form>
                            </td>
                        </tr>
                    <% }} %>
                </tbody>
            </table>
        </div>
        
        <div class="button-container">
            <a href="<%= backUrl %>" class="back-button">メニューへ戻る</a>
        </div>
    </div>
</body>
</html>