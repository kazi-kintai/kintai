<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="kintai.UserBean" %>
<%@ page import="kintai.ProjectBean" %>

<jsp:useBean id="today" class="java.util.Date" />
<%
    // ログインチェック
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null || user.getRoleId() != 1) { // getRole()からgetRoleId()へ変更
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
       
    }
    
    List<ProjectBean> projectmanagelist = (List<ProjectBean>) request.getAttribute("projectmanagelist");
    String message = (String) request.getAttribute("message");
    Boolean success = (Boolean) request.getAttribute("success");
%>

    
   <%--// リストを取得
   List<EmpBean> empList = (List<EmpBean>) request.getAttribute("empList");
    List<DeptBean> deptList = (List<DeptBean>) request.getAttribute("deptList");
    List<PostBean> postList = (List<PostBean>) request.getAttribute("postList");
    List<RoleBean> roleList = (List<RoleBean>) request.getAttribute("roleList");    // 新規追加
    List<GradeBean> gradeList = (List<GradeBean>) request.getAttribute("gradeList"); // 新規追加
--%>    

    
    
    
<%--
    // nullチェック
    if (empList == null) empList = new java.util.ArrayList<>();
    if (deptList == null) deptList = new java.util.ArrayList<>();
    if (postList == null) postList = new java.util.ArrayList<>();
    if (roleList == null) roleList = new java.util.ArrayList<>();
    if (gradeList == null) gradeList = new java.util.ArrayList<>();
--%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>従業員管理</title>
    <style>
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        
        .container {
            max-width: 1400px; /* 幅を少し広めに調整 */
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        h1 {
            color: #333;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
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
        
        /* 新規追加フォーム */
        .add-form {
            background-color: #f8f9fa;
            padding: 20px;
            margin-bottom: 30px;
            border-radius: 4px;
            border: 1px solid #dee2e6;
        }
        
        .add-form h2 {
            margin-top: 0;
            color: #495057;
        }
        
        .form-group {
            margin-bottom: 15px;
            display: inline-block;
            margin-right: 20px;
            vertical-align: top; /* フォーム要素の上下位置を揃える */
        }
        
        .form-group label {
            display: block; /* ラベルをブロック要素にして、入力フィールドの上に配置 */
            width: auto; /* 幅を自動調整 */
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .form-group input[type="text"],
        .form-group input[type="password"],
        .form-group input[type="email"], /* 新規追加 */
        .form-group input[type="date"],   /* 新規追加 */
        .form-group select {
            width: 180px; /* 幅を調整 */
            padding: 5px;
            border: 1px solid #ced4da;
            border-radius: 4px;
        }
        
        /* ボタンスタイル */
        .btn {
            padding: 6px 12px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            margin-right: 5px;
        }
        
        .btn-primary {
            background-color: #007bff;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #0056b3;
        }
        
        .btn-success {
            background-color: #28a745;
            color: white;
        }
        
        .btn-success:hover {
            background-color: #218838;
        }
        
        .btn-danger {
            background-color: #dc3545;
            color: white;
        }
        
        .btn-danger:hover {
            background-color: #c82333;
        }
        
        .btn-secondary {
            background-color: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background-color: #545b62;
        }
        
        /* テーブルスタイル */
        .emp-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        
        .emp-table th, .emp-table td {
            border: 1px solid #dee2e6;
            padding: 10px;
            text-align: left;
            vertical-align: middle; /* セル内容を中央揃え */
        }
        
        .emp-table th {
            background-color: #f8f9fa;
            font-weight: bold;
            color: #495057;
        }
        
        .emp-table tr:nth-child(even) {
            background-color: #f8f9fa;
        }
        
        .emp-table tr:hover {
            background-color: #e9ecef;
        }
        
        /* 編集フォーム */
        .edit-row {
            display: none;
        }
        
        .edit-row input[type="text"],
        .edit-row input[type="password"],
        .edit-row input[type="email"], /* 新規追加 */
        .edit-row input[type="date"],   /* 新規追加 */
        .edit-row select {
            width: 120px; /* 編集行の入力フィールドの幅 */
            padding: 3px;
            border: 1px solid #ced4da;
            border-radius: 4px;
            margin-right: 5px; /* 要素間のスペース */
        }
        
        /* 戻るボタン */
        .back-link {
            display: inline-block;
            margin-top: 20px;
            padding: 8px 16px;
            background-color: #6c757d;
            color: white;
            text-decoration: none;
            border-radius: 4px;
        }
        
        .back-link:hover {
            background-color: #545b62;
        }
        
        .form-inline {
            display: flex; /* 横並びにするためにflexを使用 */
            flex-wrap: wrap; /* 必要に応じて折り返す */
            align-items: center;
            gap: 10px; /* 要素間のスペース */
        }
        .form-inline label {
             font-weight: bold;
             flex-shrink: 0; /* ラベルが縮まないように */
        }
    </style>
    <script>
        // 編集モードの切り替え
        function toggleEdit(empNo) {
            var displayRow = document.getElementById('display-' + empNo);
            var editRow = document.getElementById('edit-' + empNo);
            
            if (editRow.style.display === 'table-row') {
                displayRow.style.display = 'table-row';
                editRow.style.display = 'none';
            } else {
                displayRow.style.display = 'none';
                editRow.style.display = 'table-row';
            }
        }
        
        // 削除確認
        function confirmDelete(empNo, empName) {
            if (confirm('プロジェクト「' + empName + '」を削除してもよろしいですか？')) {
                document.getElementById('deleteForm-' + empNo).submit();
            }
        }

        // 追加確認
        function confirmAdd() {
            return confirm('この内容で新しいプロジェクトを追加してもよろしいですか？');
        }

        // 変更確認
        function confirmUpdate(empName) {
            return confirm('プロジェクト「' + empName + '」の情報を変更してもよろしいですか？');
        }
    </script>
</head>
<body>
    <div class="container">
        <h1>プロジェクト管理</h1>
        
        <%-- メッセージ表示 --%>
        <% if (message != null && !message.isEmpty()) { %>
            <div class="message <%= (success != null && success) ? "success-message" : "error-message" %>">
                <%= message %>
            </div>
        <% } %>
        
        <%-- 新規追加フォーム --%>
        <div class="add-form">
    <h2>新規プロジェクト追加</h2>
    <form method="post" action="<%= request.getContextPath() %>/ProjectManageServlet" onsubmit="return confirmAdd();">
        <input type="hidden" name="action" value="add">

        <div class="form-group">
            <label for="newPrjName">プロジェクト名：</label>
            <input type="text" id="newPrjName" name="ProjectName" maxlength="50" required>
        </div>
        
        <div class="form-group">
            <label for="newPrjBudget">予算：</label>
            <input type="number" id="newPrjBudget" name="Budget" required>
        </div>

        <div class="form-group">
            <label for="startDate">期間(開始日)：</label>
            <input type="date" id="startDate" name="StartDate" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(today) %>">
        </div>

        <div class="form-group">
            <label for="endDate">期間(終了日)：</label>
            <input type="date" id="endDate" name="EndDate" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(today) %>">
        </div>

        <div style="margin-top: 15px;">
            <button type="submit" class="btn btn-primary">追加</button>
        </div>
    </form>
		</div>
        
        <%-- プロジェクト一覧テーブル --%>
        <h2>プロジェクト一覧</h2>
        <table class="emp-table">
            <thead>
                <tr>
                	<th>プロジェクトID</th>
                    <th>プロジェクト名</th>
                    <th>予算</th>
                    <th>開始日</th>
                    <th>終了日</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <% if (projectmanagelist != null && !projectmanagelist.isEmpty()) { %>
                    <% for (ProjectBean emp : projectmanagelist) { %>
                        <%-- 表示行 --%>
                        <tr id="display-<%= emp.getProjectId() %>">
                            <td><%= emp.getProjectId() %></td>
                            <%-- プロジェクト名の表示（nullの場合の処理） --%>
                            <td><%= (emp.getProjectName() != null) ? emp.getProjectName() : "情報なし" %></td>
                            <%-- 予算の表示 --%>
                            <td><%= emp.getBudget() %></td>
                            <%-- 開始日の表示 --%>
                            <td><%= (emp.getStartDate() != null) ? emp.getStartDate() : "---" %></td>
                            <%-- 終了日の表示 --%>
                            <td><%= (emp.getEndDate() != null) ? emp.getEndDate(): "---" %></td>
                            <td>
                                <button class="btn btn-success" onclick="toggleEdit('<%= emp.getProjectId() %>')">編集</button>
                                <button class="btn btn-danger" onclick="confirmDelete('<%= emp.getProjectId() %>', '<%= emp.getProjectName() %>')">削除</button>
                                
                                <%-- 削除用フォーム（非表示） --%>
                                <form id="deleteForm-<%= emp.getProjectId() %>" method="post" 
                                      action="<%= request.getContextPath() %>/ProjectManageServlet" style="display: none;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="ProjectId" value="<%= emp.getProjectId() %>">
                                </form>
                            </td>
                        </tr>
                        
                        <%-- 編集行（初期状態では非表示） --%>
                        <tr id="edit-<%= emp.getProjectId() %>" class="edit-row" style="display: none;">
                            <td><%= emp.getProjectId() %></td>
                            <td colspan="5"> <%-- 列数を調整 --%>
                                <form method="post" action="<%= request.getContextPath() %>/ProjectManageServlet" class="form-inline" onsubmit="return confirmUpdate('<%= emp.getProjectName() %>');">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="ProjectId" value="<%= emp.getProjectId() %>">
                                    
                                    <label>プロジェクト名：</label><input type="text" name="ProjectName" value="<%= emp.getProjectName() %>" maxlength="50" required>
                                    <label>予算：</label><input type="number" name="Budget" value="<%= emp.getBudget() %>" required>
                                    <label>開始日：</label><input type="date" name="StartDate" value="<%= emp.getStartDate() != null ? emp.getStartDate().toString() : "" %>"> <%-- 新規追加 --%>
                                    <label>終了日：</label><input type="date" name="EndDate" value="<%= emp.getEndDate() != null ? emp.getEndDate().toString() : "" %>"> <%-- 新規追加 --%>
                                    
                                    <button type="submit" class="btn btn-primary">保存</button>
                                    <button type="button" class="btn btn-secondary" onclick="toggleEdit('<%= emp.getProjectId() %>')">キャンセル</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                <% } else { %>
                    <tr>
                        <td colspan="9" style="text-align: center;">プロジェクトデータがありません</td> <%-- 列数を調整 --%>
                    </tr>
                <% } %>
            </tbody>
        </table>
        
        <a href="<%= request.getContextPath() %>/web/admin_menu.jsp" class="back-link">管理部基本メニューへ戻る</a>
    </div>
</body>
</html>

