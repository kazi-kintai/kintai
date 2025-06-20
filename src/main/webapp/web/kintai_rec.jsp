<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%@ page import="kintai.KintaiRecBean" %>
<%@ page import="kintai.DeptBean" %>
<%@ page import="kintai.PostBean" %>
<%@ page import="kintai.EmpBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.DayOfWeek" %>
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
    // 修正箇所: user.getRole() を user.getRoleId() に変更
    // UserBeanのロールIDを取得
    int userRoleId = user.getRoleId(); 

    // サーブレットから渡されたデータを取得
    List<KintaiRecBean> kintaiRecords = (List<KintaiRecBean>) request.getAttribute("kintaiRecords");
    String empNoFilter = (String) request.getAttribute("empNoFilter");
    String deptNoFilter = (String) request.getAttribute("deptNoFilter");
    String postNoFilter = (String) request.getAttribute("postNoFilter");
    String startDate = (String) request.getAttribute("startDate");
    String endDate = (String) request.getAttribute("endDate");
    // 修正箇所: userRole を userRoleId に変更
    // int userRole = (Integer) request.getAttribute("userRole"); // 旧変数
    Integer retrievedUserRoleId = (Integer) request.getAttribute("userRoleId"); // サーブレットから取得

    // nullチェックと初期化
    if (kintaiRecords == null) kintaiRecords = new java.util.ArrayList<>();
    // deptList, postList, allEmpList は管理者ロールでのみ使用されるため、
    // ここでnullチェックと初期化を行わないと、一般社員の場合にエラーになる可能性がある。
    // サーブレットで適切に初期化されていることを前提とするか、ここで初期化
    List<DeptBean> deptList = (List<DeptBean>) request.getAttribute("deptList");
    List<PostBean> postList = (List<PostBean>) request.getAttribute("postList");
    List<EmpBean> allEmpList = (List<EmpBean>) request.getAttribute("allEmpList");

    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");

    if (deptList == null) deptList = new java.util.ArrayList<>();
    if (postList == null) postList = new java.util.ArrayList<>();
    if (allEmpList == null) allEmpList = new java.util.ArrayList<>();

    // 曜日の日本語表示用マップ
    Map<DayOfWeek, String> dayOfWeekMap = new HashMap<>();
    dayOfWeekMap.put(DayOfWeek.MONDAY, "月");
    dayOfWeekMap.put(DayOfWeek.TUESDAY, "火");
    dayOfWeekMap.put(DayOfWeek.WEDNESDAY, "水");
    dayOfWeekMap.put(DayOfWeek.THURSDAY, "木");
    dayOfWeekMap.put(DayOfWeek.FRIDAY, "金");
    dayOfWeekMap.put(DayOfWeek.SATURDAY, "土");
    dayOfWeekMap.put(DayOfWeek.SUNDAY, "日");

    // メニューへ戻るリンクのURLを権限に応じて設定 (userRole から userRoleId へ変更)
    // JSPファイル内で直接UserBeanのuserRoleIdを使用
    String backUrl = (userRoleId == 1) ? request.getContextPath() + "/web/admin_menu.jsp" : request.getContextPath() + "/web/menu.jsp";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>勤怠時間記録表示</title>
    <style>
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
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
            margin: -20px -20px 20px -20px; /* 親コンテナのパディングを相殺 */
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
            align-self: center; /* 垂直方向中央寄せ */
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

        /* フィルターフォーム */
        .filter-form {
            background-color: #f8f9fa;
            padding: 20px;
            margin-bottom: 30px;
            border-radius: 4px;
            border: 1px solid #dee2e6;
            display: flex;
            flex-wrap: wrap; /* 小画面で折り返す */
            gap: 15px; /* 要素間のスペース */
            align-items: flex-end; /* ボタンを下揃え */
        }
        .filter-group {
            display: flex;
            flex-direction: column;
        }
        .filter-group label {
            font-weight: bold;
            margin-bottom: 5px;
        }
        .filter-form input[type="text"],
        .filter-form select {
            padding: 8px;
            border: 1px solid #ced4da;
            border-radius: 4px;
            width: 180px; /* 幅を調整 */
        }
        .filter-form button {
            padding: 8px 16px;
            background-color: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 1em;
            align-self: flex-end; /* 検索ボタンを下揃え */
        }
        .filter-form button:hover {
            background-color: #0056b3;
        }

        /* テーブルスタイル */
        .kintai-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        .kintai-table th, .kintai-table td {
            border: 1px solid #dee2e6;
            padding: 10px;
            text-align: center; /* 中央寄せ */
            vertical-align: middle; /* 垂直方向中央寄せ */
        }
        .kintai-table th {
            background-color: #e9ecef; /* ライトグレー */
            font-weight: bold;
            color: #495057;
        }
        .kintai-table tr:nth-child(even) {
            background-color: #f8f9fa; /* ストライプ */
        }
        .kintai-table tr:hover {
            background-color: #e2e6ea; /* ホバー効果 */
        }
        /* 曜日によって色を変える */
        .kintai-table .saturday {
            color: blue;
        }
        .kintai-table .sunday {
            color: red;
        }

        /* 戻るボタン */
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

        <h1>勤怠時間記録表示</h1>

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

        <%-- フィルター/検索エリア (管理者向けにのみ表示) --%>
        <%-- userRole から userRoleId へ変更 --%>
        <% if (userRoleId == 1) { %>
            <div class="filter-form">
                <form action="<%= request.getContextPath() %>/KintaiRecServlet" method="get" style="display: flex; flex-wrap: wrap; gap: 15px;">
                    <div class="filter-group">
                        <label for="empNoFilter">従業員番号 / 氏名:</label>
                        <select id="empNoFilter" name="empNoFilter">
                            <option value="">全ての従業員</option>
                            <% for (EmpBean emp : allEmpList) { %>
                                <option value="<%= emp.getEmpNo() %>" <%= emp.getEmpNo().equals(empNoFilter != null ? empNoFilter : "") ? "selected" : "" %>>
                                    <%= emp.getEmpNo() %> <%= emp.getEmpName() %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="filter-group">
                        <label for="deptNoFilter">部署:</label>
                        <select id="deptNoFilter" name="deptNoFilter">
                            <option value="">全ての部署</option>
                            <% for (DeptBean dept : deptList) { %>
                                <option value="<%= dept.getDeptNo() %>" <%= dept.getDeptNo().equals(deptNoFilter != null ? deptNoFilter : "") ? "selected" : "" %>>
                                    <%= dept.getDeptName() %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="filter-group">
                        <label for="postNoFilter">役職:</label>
                        <select id="postNoFilter" name="postNoFilter">
                            <option value="">全ての役職</option>
                            <% for (PostBean post : postList) { %>
                                <option value="<%= post.getPostNo() %>" <%= post.getPostNo().equals(postNoFilter != null ? postNoFilter : "") ? "selected" : "" %>>
                                    <%= post.getPostName() %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="filter-group">
                        <label for="startDate">期間(開始):</label>
                        <input type="date" id="startDate" name="startDate" value="<%= startDate != null ? startDate : "" %>">
                    </div>
                    <div class="filter-group">
                        <label for="endDate">期間(終了):</label>
                        <input type="date" id="endDate" name="endDate" value="<%= endDate != null ? endDate : "" %>">
                    </div>
                    <button type="submit">検索</button>
                </form>
            </div>
        <% } else { %>
            <%-- 一般社員は日付フィルターのみ --%>
            <div class="filter-form">
                <form action="<%= request.getContextPath() %>/KintaiRecServlet" method="get" style="display: flex; flex-wrap: wrap; gap: 15px;">
                    <div class="filter-group">
                        <label for="startDate">期間(開始):</label>
                        <input type="date" id="startDate" name="startDate" value="<%= startDate != null ? startDate : "" %>">
                    </div>
                    <div class="filter-group">
                        <label for="endDate">期間(終了):</label>
                        <input type="date" id="endDate" name="endDate" value="<%= endDate != null ? endDate : "" %>">
                    </div>
                    <button type="submit">検索</button>
                </form>
            </div>
        <% } %>

        <%-- 勤怠記録一覧テーブル --%>
        <table class="kintai-table">
            <thead>
                <tr>
                    <th>日付</th>
                    <th>曜日</th>
                    <%-- userRole から userRoleId へ変更 --%>
                    <% if (userRoleId == 1) { %>
                        <th>従業員番号</th>
                        <th>氏名</th>
                        <th>部署</th>
                        <th>役職</th>
                    <% } %>
                    <th>出勤時刻</th>
                    <th>退勤時刻</th>
                    <th>休憩時間合計</th>
                    <th>実働時間</th>
                </tr>
            </thead>
            <tbody>
                <% if (kintaiRecords != null && !kintaiRecords.isEmpty()) { %>
                    <% for (KintaiRecBean record : kintaiRecords) { %>
                        <%
                            String dayClass = "";
                            if (record.getKintaiDate() != null) {
                                DayOfWeek dayOfWeek = record.getKintaiDate().getDayOfWeek();
                                if (dayOfWeek == DayOfWeek.SATURDAY) {
                                    dayClass = "saturday";
                                } else if (dayOfWeek == DayOfWeek.SUNDAY) {
                                    dayClass = "sunday";
                                }
                            }
                        %>
                        <tr class="<%= dayClass %>">
                            <td><%= record.getKintaiDate() != null ? record.getKintaiDate().toString() : "---" %></td>
                            <td><%= record.getKintaiDate() != null ? dayOfWeekMap.get(record.getKintaiDate().getDayOfWeek()) : "---" %></td>
                            <%-- userRole から userRoleId へ変更 --%>
                            <% if (userRoleId == 1) { %>
                                <td><%= record.getEmpno() != null ? record.getEmpno() : "---" %></td>
                                <td><%= record.getEmpName() != null ? record.getEmpName() : "---" %></td>
                                <td><%= record.getDeptName() != null ? record.getDeptName() : "---" %></td>
                                <td><%= record.getPostName() != null ? record.getPostName() : "---" %></td>
                            <% } %>
                            <td><%= record.getClockIn() != null ? record.getClockIn().toString().substring(0, 5) : "---" %></td>
                            <td><%= record.getClockOut() != null ? record.getClockOut().toString().substring(0, 5) : "---" %></td>
                            <td><%= record.getTotalBreakTimeFormatted() %></td>
                            <td><%= record.getActualWorkTimeFormatted() %></td>
                        </tr>
                    <% } %>
                <% } else { %>
                    <tr>
                        <%-- userRole から userRoleId へ変更 --%>
                        <td colspan="<%= (userRoleId == 1) ? 10 : 6 %>" style="text-align: center;">
                            勤怠記録がありません。
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>

        <div style="text-align: center; margin-top: 30px;">
            <a href="<%= backUrl %>" class="back-link">メニューへ戻る</a>
        </div>
    </div>
</body>
</html>
