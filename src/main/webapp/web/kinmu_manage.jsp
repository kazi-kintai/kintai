<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%@ page import="kintai.WorkTimeBean" %>
<%@ page import="kintai.BreakBean" %>
<%@ page import="kintai.GyomuBean" %> <%-- ProjectBean から GyomuBean へ変更 --%>
<%@ page import="kintai.KinmuManageBean" %> <%-- WorkDetail 内部クラスを使用するため --%>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%
    // ログインチェック
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }

    // セッションからユーザー情報を取得
    String loggedInUserName = user.getName();
    String loggedInDeptName = (String) session.getAttribute("deptName");
    int userRole = user.getRole(); // ロールも取得

    // サーブレットから渡されたデータを取得
    String targetDateStr = (String) request.getAttribute("targetDate");
    Map<String, String> workTimeData = (Map<String, String>) request.getAttribute("workTimeData");
    List<Map<String, String>> breakList = (List<Map<String, String>>) request.getAttribute("breakList");
    List<KinmuManageBean.WorkDetail> workDetails = (List<KinmuManageBean.WorkDetail>) request.getAttribute("workDetails");
    List<GyomuBean> gyomuList = (List<GyomuBean>) request.getAttribute("gyomuList"); // projectList から gyomuList へ変更

    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");

    // nullチェックと初期化
    if (workTimeData == null) workTimeData = new HashMap<>();
    if (breakList == null) breakList = new java.util.ArrayList<>();
    if (workDetails == null) workDetails = new java.util.ArrayList<>();
    if (gyomuList == null) gyomuList = new java.util.ArrayList<>(); // projectList から gyomuList へ変更
    
    // 現在表示している日付をLocalDateオブジェクトに変換
    LocalDate targetDate = LocalDate.parse(targetDateStr);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    String formattedTargetDate = targetDate.format(formatter);

    // メニューへ戻るリンクのURLを権限に応じて設定
    String backUrl = (userRole == 1) ? request.getContextPath() + "/web/admin_menu.jsp" : request.getContextPath() + "/web/menu.jsp";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>勤務時間管理</title>
    <style>
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 960px; /* 少し広めに設定 */
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            padding: 10px 20px;
            background: #fff;
            border-bottom: 1px solid #ccc;
            margin: -20px -20px 20px -20px;
            border-top-left-radius: 8px;
            border-top-right-radius: 8px;
        }
        .user-info {
            display: flex;
            flex-direction: column;
            line-height: 1.5;
            text-align: left;
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
            align-self: center;
        }
        .logout-button:hover {
            background-color: #c82333;
            border-color: #bd2130;
        }
        h1 {
            color: #333;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
            margin-top: 0;
        }
        /* メッセージ表示エリア */
        .message {
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 4px;
            text-align: center;
        }
        .success-message {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .error-message {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .section {
            margin-bottom: 30px;
            padding: 20px;
            border: 1px solid #eee;
            border-radius: 5px;
            background-color: #fcfcfc;
        }
        .section h2 {
            margin-top: 0;
            color: #555;
            border-bottom: 1px dashed #ddd;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }
        .form-group {
            margin-bottom: 15px;
            display: flex;
            align-items: center;
        }
        .form-group label {
            width: 120px;
            font-weight: bold;
            flex-shrink: 0; /* ラベルが縮まないように */
        }
        .form-group input[type="date"],
        .form-group input[type="time"],
        .form-group input[type="text"],
        .form-group select {
            flex-grow: 1; /* 入力フィールドが残りのスペースを占める */
            padding: 8px;
            border: 1px solid #ced4da;
            border-radius: 4px;
        }
        .form-group input[type="text"].description-input {
            width: 100%; /* 説明欄は広めに */
        }
        button[type="submit"], .button {
            padding: 8px 16px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 1em;
            margin-left: 10px; /* ボタン間のスペース */
        }
        .btn-primary { background-color: #007bff; color: white; }
        .btn-primary:hover { background-color: #0056b3; }
        .btn-success { background-color: #28a745; color: white; }
        .btn-success:hover { background-color: #218838; }
        .btn-danger { background-color: #dc3545; color: white; }
        .btn-danger:hover { background-color: #c82333; }
        .btn-secondary { background-color: #6c757d; color: white; }
        .btn-secondary:hover { background-color: #545b62; }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }
        table th, table td {
            border: 1px solid #ddd;
            padding: 10px;
            text-align: center;
        }
        table th {
            background-color: #f2f2f2;
        }
        .action-cell button {
            margin: 0 3px;
        }

        .date-navigation {
            display: flex;
            justify-content: center;
            align-items: center;
            margin-bottom: 20px;
            gap: 15px;
        }
        .date-navigation button {
            background-color: #007bff;
            color: white;
            padding: 10px 15px;
            border-radius: 5px;
            cursor: pointer;
            border: none;
            font-size: 1.2em;
        }
        .date-navigation button:hover {
            background-color: #0056b3;
        }
        .date-navigation span {
            font-size: 1.5em;
            font-weight: bold;
            color: #333;
        }

        .back-link {
            display: inline-block;
            margin-top: 30px;
            padding: 10px 20px;
            background-color: #6c757d;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            text-align: center;
        }
        .back-link:hover {
            background-color: #545b62;
        }
    </style>
    <script>
        // 日付ナビゲーション用
        function navigateDate(days) {
            const currentDate = new Date('<%= targetDate.toString() %>');
            currentDate.setDate(currentDate.getDate() + days);
            const year = currentDate.getFullYear();
            const month = String(currentDate.getMonth() + 1).padStart(2, '0');
            const day = String(currentDate.getDate()).padStart(2, '0');
            window.location.href = '<%= request.getContextPath() %>/KinmuManageServlet?targetDate=' + year + '-' + month + '-' + day;
        }

        // 確認メッセージ用
        function confirmAction(message) {
            // alert() の代わりに確認ダイアログを作成
            // 実運用では、もっとリッチなモーダルダイアログを実装することが推奨されます
            return confirm(message);
        }

        // 新しい関数: input[type="time"] がフォーカスされたときにピッカーを表示
        document.addEventListener('DOMContentLoaded', (event) => {
            const timeInputs = document.querySelectorAll('input[type="time"]');
            timeInputs.forEach(input => {
                // 'focus' イベントと 'click' イベントの両方で showPicker を呼び出す
                // これにより、クリック（フォーカスと同時に発生）またはタブキーでのフォーカス移動でピッカーが表示される
                input.addEventListener('focus', () => {
                    if (input.showPicker) { // showPicker がサポートされているかチェック
                        input.showPicker();
                    }
                });
                input.addEventListener('click', () => {
                    if (input.showPicker) {
                        input.showPicker();
                    }
                });
            });
        });
    </script>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="user-info">
                <p>部署：<%= loggedInDeptName != null ? loggedInDeptName : "情報なし" %></p>
                <p>氏名：<%= loggedInUserName %></p>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/logout" style="margin: 0;">
                <input type="submit" value="ログアウト" class="logout-button">
            </form>
        </div>

        <h1>勤務時間管理</h1>

        <%-- メッセージ表示 --%>
        <% if (successMessage != null) { %>
            <div class="message success-message">
                <%= successMessage %>
            </div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message error-message">
                <%= errorMessage %>
            </div>
        <% } %>

        <%-- 日付ナビゲーション --%>
        <div class="date-navigation">
            <button type="button" onclick="navigateDate(-1)">&lt; 前日</button>
            <span><%= formattedTargetDate %></span>
            <button type="button" onclick="navigateDate(1)">翌日 &gt;</button>
        </div>

        <%-- 出退勤・休憩時間修正エリア --%>
        <div class="section">
            <h2>出退勤・休憩時間</h2>
            <form action="<%= request.getContextPath() %>/KinmuManageServlet" method="post" onsubmit="return confirmAction('この内容で出退勤時間を更新しますか？');">
                <input type="hidden" name="action" value="update_work_time">
                <input type="hidden" name="targetDate" value="<%= targetDateStr %>">
                <input type="hidden" name="recId" value="<%= workTimeData.getOrDefault("recId", "") %>">
                
                <div class="form-group">
                    <label for="clockInTime">出勤時刻:</label>
                    <input type="time" id="clockInTime" name="clockInTime" value="<%= workTimeData.getOrDefault("clockInTime", "") %>">
                </div>
                <div class="form-group">
                    <label for="clockOutTime">退勤時刻:</label>
                    <input type="time" id="clockOutTime" name="clockOutTime" value="<%= workTimeData.getOrDefault("clockOutTime", "") %>">
                </div>
                <div style="text-align: right;">
                    <button type="submit" class="btn-primary">出退勤 更新</button>
                </div>
            </form>

            <h3 style="margin-top: 30px; border-bottom: 1px solid #eee; padding-bottom: 10px;">休憩時間</h3>
            <table class="break-table">
                <thead>
                    <tr>
                        <th>開始時刻</th>
                        <th>終了時刻</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (breakList.isEmpty()) { %>
                        <tr><td colspan="3">休憩記録はありません</td></tr>
                    <% } else { %>
                        <% for (Map<String, String> breakItem : breakList) { %>
                            <tr>
                                <td><%= breakItem.getOrDefault("startTime", "---") %></td>
                                <td><%= breakItem.getOrDefault("endTime", "---") %></td>
                                <td class="action-cell">
                                    <form action="<%= request.getContextPath() %>/KinmuManageServlet" method="post" onsubmit="return confirmAction('この休憩記録を削除しますか？');" style="display: inline;">
                                        <input type="hidden" name="action" value="delete_break">
                                        <input type="hidden" name="targetDate" value="<%= targetDateStr %>">
                                        <input type="hidden" name="breakId" value="<%= breakItem.get("breakId") %>">
                                        <button type="submit" class="btn-danger">削除</button>
                                    </form>
                                </td>
                            </tr>
                        <% } %>
                    <% } %>
                    <tr>
                        <form action="<%= request.getContextPath() %>/KinmuManageServlet" method="post" onsubmit="return confirmAction('新しい休憩記録を追加しますか？');">
                            <input type="hidden" name="action" value="add_break">
                            <input type="hidden" name="targetDate" value="<%= targetDateStr %>">
                            <td><input type="time" name="newBreakStartTime" placeholder="HH:mm" required></td>
                            <td><input type="time" name="newBreakEndTime" placeholder="HH:mm" required></td>
                            <td class="action-cell"><button type="submit" class="btn-success">追加</button></td>
                        </form>
                    </tr>
                </tbody>
            </table>
        </div>

        <%-- 業務明細管理エリア --%>
        <div class="section">
            <h2>業務明細</h2> <%-- プロジェクトから業務へ変更 --%>
            <table class="work-detail-table">
                <thead>
                    <tr>
                        <th>業務</th> <%-- プロジェクトから業務へ変更 --%>
                        <th>開始時刻</th>
                        <th>終了時刻</th>
                        <th>作業時間</th>
                        <th>説明</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (workDetails.isEmpty()) { %>
                        <tr><td colspan="6">業務明細はありません</td></tr>
                    <% } else { %>
                        <% for (KinmuManageBean.WorkDetail detail : workDetails) { %>
                            <tr>
                                <td><%= detail.getGyomuName() != null ? detail.getGyomuName() : "---" %></td> <%-- getProjectName から getGyomuName へ変更 --%>
                                <td><%= detail.getStartTime() != null ? detail.getStartTime().toString().substring(0, 5) : "---" %></td>
                                <td><%= detail.getEndTime() != null ? detail.getEndTime().toString().substring(0, 5) : "---" %></td>
                                <td><%= detail.getWorkDurationFormatted() %></td>
                                <td><%= detail.getDescription() != null ? detail.getDescription() : "" %></td>
                                <td class="action-cell">
                                    <form action="<%= request.getContextPath() %>/KinmuManageServlet" method="post" onsubmit="return confirmAction('この業務明細を削除しますか？');" style="display: inline;">
                                        <input type="hidden" name="action" value="delete_work_detail">
                                        <input type="hidden" name="targetDate" value="<%= targetDateStr %>">
                                        <input type="hidden" name="detailId" value="<%= detail.getDetailId() %>">
                                        <button type="submit" class="btn-danger">削除</button>
                                    </form>
                                    <%-- TODO: 業務明細の編集機能が必要な場合はここに追加 --%>
                                </td>
                            </tr>
                        <% } %>
                    <% } %>
                    <tr>
                        <form action="<%= request.getContextPath() %>/KinmuManageServlet" method="post" onsubmit="return confirmAction('新しい業務明細を追加しますか？');">
                            <input type="hidden" name="action" value="add_work_detail">
                            <input type="hidden" name="targetDate" value="<%= targetDateStr %>">
                            <td>
                                <select name="newGyomuNo" required> <%-- name="newProjectNo" から name="newGyomuNo" へ変更 --%>
                                    <option value="">選択</option>
                                    <% for (GyomuBean gyomu : gyomuList) { %> <%-- projectList から gyomuList へ、ProjectBean から GyomuBean へ変更 --%>
                                        <option value="<%= gyomu.getGyomuNo() %>"><%= gyomu.getGyomuName() %></option>
                                    <% } %>
                                </select>
                            </td>
                            <td><input type="time" name="newDetailStartTime" placeholder="HH:mm" required></td>
                            <td><input type="time" name="newDetailEndTime" placeholder="HH:mm" required></td>
                            <td>---</td> <%-- 作業時間は入力せず、表示時に計算するため --%>
                            <td><input type="text" name="newDescription" class="description-input" maxlength="255" placeholder="作業内容（任意）"></td>
                            <td class="action-cell"><button type="submit" class="btn-success">追加</button></td>
                        </form>
                    </tr>
                </tbody>
            </table>
        </div>

        <div style="text-align: center; margin-top: 30px;">
            <a href="<%= backUrl %>" class="back-link">メニューへ戻る</a>
        </div>
    </div>
</body>
</html>
