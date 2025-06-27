<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="kintai.EmpBean" %>
<%@ page import="kintai.DeptBean" %>
<%@ page import="kintai.PostBean" %>
<%@ page import="kintai.UserBean" %>
<%@ page import="kintai.ProjectBean" %>
<%@ page import="java.text.NumberFormat" %>

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
            margin: -20px -20px 20px -20px;
            border-top-left-radius: 8px;
            border-top-right-radius: 8px;
        }
        
        .user-info {
            display: flex;
            flex-direction: column;
            line-height: 1.5;
            text-align: left;
            font-size: 13px;
        }
        
        .logout-button {
            background-color: #dc3545;
            color: white;
            border: 1px solid #dc3545;
            border-radius: 5px;
            padding: 8px 16px;
            cursor: pointer;
            font-size: 13px;
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
        
        /* フィルターフォーム・追加フォーム */
        .add-form {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 8px;
            border: 1px solid #dee2e6;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        }
        
        .add-form h2 {
            margin-top: 0;
            color: #495057;
            font-size: 16px;
        }
        
        .form-group {
            display: flex;
            flex-direction: column;
            margin-right: 20px;
            margin-bottom: 15px;
            min-width: 160px;
        }
        
        .form-group label {
            font-weight: 600;
            margin-bottom: 4px;
            color: #495057;
            font-size: 12px;
        }
        
        .form-group input[type="text"],
        .form-group input[type="number"],
        .form-group input[type="date"],
        .form-group select {
            padding: 8px 10px;
            border: 1px solid #ced4da;
            border-radius: 6px;
            font-size: 13px;
            transition: border-color 0.2s, box-shadow 0.2s;
            background-color: white;
        }
        
        .form-group input[type="text"]:focus,
        .form-group input[type="number"]:focus,
        .form-group input[type="date"]:focus,
        .form-group select:focus {
            outline: none;
            border-color: #007bff;
            box-shadow: 0 0 0 2px rgba(0,123,255,0.1);
        }
        
        /* ボタンスタイル（kintai_rec.jspと統一） */
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            margin-right: 10px;
            transition: all 0.2s;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
            color: white;
            box-shadow: 0 2px 4px rgba(0,123,255,0.2);
        }
        
        .btn-primary:hover {
            background: linear-gradient(135deg, #0056b3 0%, #004085 100%);
            transform: translateY(-1px);
            box-shadow: 0 3px 6px rgba(0,123,255,0.3);
        }
        
        .btn-success {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
            box-shadow: 0 2px 4px rgba(40,167,69,0.2);
        }
        
        .btn-success:hover {
            background: linear-gradient(135deg, #218838 0%, #1e7e34 100%);
            transform: translateY(-1px);
            box-shadow: 0 3px 6px rgba(40,167,69,0.3);
        }
        
        .btn-danger {
            background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
            color: white;
            box-shadow: 0 2px 4px rgba(220,53,69,0.2);
        }
        
        .btn-danger:hover {
            background: linear-gradient(135deg, #c82333 0%, #bd2130 100%);
            transform: translateY(-1px);
            box-shadow: 0 3px 6px rgba(220,53,69,0.3);
        }
        
        .btn-secondary {
            background: linear-gradient(135deg, #6c757d 0%, #545b62 100%);
            color: white;
            box-shadow: 0 2px 4px rgba(108,117,125,0.2);
        }
        
        .btn-secondary:hover {
            background: linear-gradient(135deg, #545b62 0%, #495057 100%);
            transform: translateY(-1px);
            box-shadow: 0 3px 6px rgba(108,117,125,0.3);
        }
        
        /* テーブルスタイル（kintai_rec.jspと統一） */
        .emp-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            background-color: white;
        }
        
        .emp-table th, .emp-table td {
            border: 1px solid #dee2e6;
            padding: 10px 8px;
            text-align: center;
            vertical-align: middle;
            font-size: 12px;
        }
        
        .emp-table th {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            font-weight: 600;
            color: #495057;
            border-bottom: 2px solid #dee2e6;
        }
        
        .emp-table tr:nth-child(even) {
            background-color: rgba(0,123,255,0.02);
        }
        
        .emp-table tr:hover {
            background-color: rgba(0,123,255,0.05);
            transition: background-color 0.2s;
        }
        
        /* 編集フォーム */
        .edit-row {
            display: none;
        }
        
        .edit-row input[type="number"]{
 		    width: 120px; /* 編集行の入力フィールドの幅 */
            padding: 3px;
            border: 1px solid #ced4da;
            border-radius: 4px;
            margin-right: 5px; /* 要素間のスペース */
 		    text-align: right;/* 右寄せ */
		}
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
            display: flex;
            flex-wrap: wrap;
            align-items: flex-end;
            gap: 12px;
        }
        .form-inline label {
            font-weight: 600;
            margin-bottom: 4px;
            color: #495057;
            font-size: 12px;
            flex-shrink: 0;
        }
        
        /* 並排配置のためのコンテナスタイル */
        .form-container-wrapper {
            display: flex;
            gap: 20px;
            margin-bottom: 20px;
            align-items: stretch;
        }
        
        .form-container {
            flex: 1;
            min-height: 280px;
            display: flex;
            flex-direction: column;
        }
        
        .form-container form {
            display: flex;
            flex-direction: column;
            height: 100%;
        }
        
        .project-info-display {
            font-size: 16px;
            padding: 20px;
            flex-grow: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }
        
        .info-grid div {
            margin-bottom: 12px;
            line-height: 1.6;
        }
        
        .info-grid strong {
            font-size: 18px;
            color: #495057;
        }
        
        .info-grid span {
            font-size: 17px;
            font-weight: 500;
            color: #1976d2;
        }
        

        /* レスポンシブ対応 */
        @media (max-width: 768px) {
            .form-container-wrapper {
                flex-direction: column;
                gap: 15px;
            }
            
            .form-container {
                min-height: auto;
            }
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

        // プロジェクト人員詳細表示（弹出窗口）
        function showProjectMemberDetailPopup() {
            var projectId = document.getElementById('projectSelect').value;
            var month = document.getElementById('monthSelect').value;
            
            if (projectId === '' || month === '') {
                alert('プロジェクトと月を選択してください');
                return;
            }
            
            // 新しいウィンドウでプロジェクト人員詳細を表示
            var url = '<%= request.getContextPath() %>/ProjectBudgetReportServlet?action=getProjectMembers&projectId=' + projectId + '&month=' + month;
            window.open(url, 'projectMemberDetail', 'width=1000,height=600,scrollbars=yes,resizable=yes');
        }
        
        // プロジェクト一覧表示（弹出窗口）
        function showProjectListPopup() {
            var url = '<%= request.getContextPath() %>/projectManage?action=showProjectList';
            window.open(url, 'projectList', 'width=800,height=500,scrollbars=yes,resizable=yes');
        }


        // プロジェクト詳細情報表示
        function showProjectInfo() {
            var select = document.getElementById('projectSelect');
            var infoArea = document.getElementById('projectInfoArea');
            
            if (select.value === '') {
                infoArea.style.display = 'none';
                return;
            }
            
            var selectedOption = select.options[select.selectedIndex];
            var projectName = selectedOption.getAttribute('data-name');
            var budget = selectedOption.getAttribute('data-budget');
            var startDate = selectedOption.getAttribute('data-start');
            var endDate = selectedOption.getAttribute('data-end');
            
            document.getElementById('selectedProjectName').textContent = projectName;
            document.getElementById('selectedProjectBudget').textContent = budget ? '¥' + Number(budget).toLocaleString() : '-';
            
            var period = '';
            if (startDate && endDate) {
                period = startDate + ' ～ ' + endDate;
            } else if (startDate) {
                period = startDate + ' ～';
            } else if (endDate) {
                period = '～ ' + endDate;
            } else {
                period = '-';
            }
            document.getElementById('selectedProjectPeriod').textContent = period;
            
            infoArea.style.display = 'block';
        }

        // ページ読み込み時の初期化
        window.onload = function() {
            var projectSelect = document.getElementById('projectSelect');
            
            // 第一個項目を選択
            if (projectSelect.options.length > 1) {
                projectSelect.selectedIndex = 1; // 最初の項目（選択してくださいを除く）
                showProjectInfo(); // プロジェクト情報を表示
            }
        }
    </script>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="user-info">
                <p>部署：管理部</p>
                <p>氏名：<%= user.getName() %></p>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/logout" style="margin: 0;">
                <input type="submit" value="ログアウト" class="logout-button">
            </form>
        </div>
        
        <h1>プロジェクト管理</h1>
        
        <%-- メッセージ表示 --%>
        <% if (message != null && !message.isEmpty()) { %>
            <div class="message <%= (success != null && success) ? "success-message" : "error-message" %>">
                <%= message %>
            </div>
        <% } %>
        
        <%-- 並排配置のフォームコンテナ --%>
        <div class="form-container-wrapper">
            <%-- 新規追加フォーム --%>
            <div class="add-form form-container">
                <h2>新規プロジェクト追加</h2>
                <form method="post" action="<%= request.getContextPath() %>/projectManage" onsubmit="return confirmAdd();">
                    <input type="hidden" name="action" value="add">

                    <div class="form-group">
                        <label for="newPrjName">プロジェクト名：</label>
                        <input type="text" id="ProjectName" name="ProjectName" maxlength="10" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="newPrjName">予算：</label>
                        <input type="number" id="BudgetAmount" name="BudgetAmount" maxlength="10">
                    </div>

                    <div class="form-group">
                        <label for="startDate">期間(開始日)：</label>
                        <input type="date" id="StartDate" name="StartDate" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(today) %>">
                    </div>

                    <div class="form-group">
                        <label for="endDate">期間(終了日)：</label>
                        <input type="date" id="EndDate" name="EndDate" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(today) %>">
                    </div>

                    <div style="margin-top: auto; padding-top: 15px;">
                        <button type="submit" class="btn btn-primary">追加</button>
                    </div>
                </form>
            </div>
            
            <%-- プロジェクト予算実績表示セクション --%>
            <div class="add-form form-container">
                <h2>プロジェクト予算実績表示</h2>
                <div class="form-inline">
                    <div class="form-group">
                        <label for="projectSelect">プロジェクト：</label>
                        <select id="projectSelect" onchange="showProjectInfo()" required>
                            <option value="">選択してください</option>
                            <% if (projectmanagelist != null && !projectmanagelist.isEmpty()) { %>
                                <% for (ProjectBean project : projectmanagelist) { %>
                                    <option value="<%= project.getProjectId() %>" 
                                            data-name="<%= project.getProjectName() %>"
                                            data-budget="<%= project.getBudget() %>"
                                            data-start="<%= project.getStartDate() != null ? project.getStartDate() : "" %>"
                                            data-end="<%= project.getEndDate() != null ? project.getEndDate() : "" %>">
                                        <%= project.getProjectName() %>
                                    </option>
                                <% } %>
                            <% } %>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="monthSelect">月：</label>
                        <select id="monthSelect" required>
                            <option value="">選択してください</option>
                            <% 
                                java.util.Calendar cal = java.util.Calendar.getInstance();
                                int currentYear = cal.get(java.util.Calendar.YEAR);
                                int currentMonth = cal.get(java.util.Calendar.MONTH) + 1;
                            %>
                            <% for (int month = 1; month <= 12; month++) { %>
                                <option value="<%= currentYear %>-<%= String.format("%02d", month) %>" 
                                        <%= (month == currentMonth) ? "selected" : "" %>>
                                    <%= month %>月
                                </option>
                            <% } %>
                        </select>
                    </div>
                    
                    <div style="margin-top: auto;">
                        <button type="button" class="btn btn-primary" onclick="showProjectMemberDetailPopup()">詳細表示</button>
                        <button type="button" class="btn btn-secondary" onclick="showProjectListPopup()">プロジェクト一覧</button>
                    </div>
                </div>
                
                <%-- プロジェクト詳細情報表示エリア --%>
                <div id="projectInfoArea" class="project-info-display" style="display: none; margin-top: 15px; padding: 10px; background-color: #f1f3f4; border-radius: 4px;">
                    <h4 style="margin: 0 0 8px 0; color: #495057;">選択プロジェクト詳細</h4>
                    <div class="info-grid">
                        <div><strong>プロジェクト名:</strong> <span id="selectedProjectName">-</span></div>
                        <div><strong>予算:</strong> <span id="selectedProjectBudget">-</span></div>
                        <div><strong>期間:</strong> <span id="selectedProjectPeriod">-</span></div>
                    </div>
                </div>
            </div>
        </div>

        
        <a href="<%= request.getContextPath() %>/web/admin_menu.jsp" class="back-link">管理部基本メニューへ戻る</a>
    </div>
</body>
</html>

