<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
    
    // Servletから渡されたデータを取得します
    Map<String, String> workTimeData = (Map<String, String>) request.getAttribute("workTimeData");
    List<Map<String, String>> breakList = (List<Map<String, String>>) request.getAttribute("breakList");
    
    // データがnullの場合の安全対策 (NullPointerExceptionを防ぐため)
    if (workTimeData == null) workTimeData = new java.util.HashMap<>();
    if (breakList == null) breakList = new java.util.ArrayList<>();
%>
<html>
<head>
    <title>勤怠登録</title>
    <style>
        body { font-family: sans-serif; background-color: #f4f4f4; padding: 20px; }
        .container { max-width: 650px; margin: auto; padding: 30px; border: 1px solid #ccc; border-radius: 8px; background-color: white; }
        .header, h4 { text-align: center; }
        .header { margin-bottom: 25px; border-bottom: 1px solid #eee; padding-bottom: 15px; font-size: 1.2em;}
        .section { margin-bottom: 25px; }
        .form-group { margin-bottom: 15px; display: flex; align-items: center; }
        .form-group label { font-weight: bold; width: 130px; }
        .form-group input[type="text"] { flex-grow: 1; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
        .action-button {
            display: block;
            width: 100%;
            font-size: 1.1em;
            padding: 10px;
            cursor: pointer;
            border-radius: 5px;
            border: 1px solid #666;
            background-color: #333;
            color: white;
            text-align: center;
        }
        .add-break-button {
             background-color: #5cb85c; /* 緑色 */
             border-color: #4cae4c;
        }
        .register-button {
            background-color: #337ab7; /* 青色 */
            border-color: #2e6da4;
        }
        .status-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .status-table th, .status-table td { border: 1px solid #ddd; padding: 10px; text-align: center; }
        .status-table th { background-color: #f2f2f2; }
        .delete-form { margin: 0; }
        .delete-form button { background: #d9534f; border-color: #d43f3a; color: white; padding: 5px 10px; border-radius: 3px; cursor: pointer; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h3><%= today %> の勤怠登録</h3>
        </div>

        <%-- セクション1: 出退勤時刻の登録・修正 --%>
        <div class="section">
            <h4>出退勤登録・修正</h4>
            <form action="worktime" method="post">
                <input type="hidden" name="action" value="register_work_time">
                <div class="form-group">
                    <label for="clockInTime">出勤時刻:</label>
                    <input type="text" id="clockInTime" name="clockInTime" value="<%= workTimeData.getOrDefault("clockInTime", "") %>">
                </div>
                <div class="form-group">
                    <label for="clockOutTime">退勤時刻:</label>
                    <input type="text" id="clockOutTime" name="clockOutTime" value="<%= workTimeData.getOrDefault("clockOutTime", "") %>">
                </div>
                <input type="submit" value="出退勤を登録・修正" class="action-button register-button">
            </form>
        </div>

        <hr>
        
        <%-- セクション2: 新しい休憩の追加 --%>
        <div class="section">
            <h4>休憩の追加</h4>
             <form action="worktime" method="post">
                <input type="hidden" name="action" value="add_break">
                 <div class="form-group">
                     <label>休憩開始:</label>
                     <input type="text" name="breakStartTime" placeholder="例: 12:00">
                 </div>
                 <div class="form-group">
                     <label>休憩終了:</label>
                     <input type="text" name="breakEndTime" placeholder="例: 13:00">
                 </div>
                 <input type="submit" value="+ 休憩を追加する" class="action-button add-break-button">
            </form>
        </div>
        
        <hr>
        
        <%-- セクション3: 現在の休憩記録の表示と削除 --%>
        <div class="section">
            <h4>休憩記録</h4>
            <table class="status-table">
                <thead>
                    <tr>
                        <th>休憩開始</th>
                        <th>休憩終了</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, String> breakItem : breakList) { %>
                        <tr>
                            <td><%= breakItem.get("startTime") %></td>
                            <td><%= breakItem.get("endTime") %></td>
                            <td>
                                <form class="delete-form" action="worktime" method="post">
                                    <input type="hidden" name="action" value="delete_break">
                                    <input type="hidden" name="breakId" value="<%= breakItem.get("breakId") %>">
                                    <button type="submit">削除</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                    <% if (breakList.isEmpty()) { %>
                        <tr>
                            <td colspan="3">休憩記録はありません。</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

    </div>
</body>
</html>
