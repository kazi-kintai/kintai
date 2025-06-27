<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>
<%
    UserBean user = (UserBean) session.getAttribute("user");
    // ログインチェック
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    
    String deptname = (String)session.getAttribute("deptname");
    
    // 現在の日付と曜日を取得（サーバー側）
    LocalDate today = LocalDate.now();
    int month = today.getMonthValue();
    int day = today.getDayOfMonth();
    String[] weekdays = {"日", "月", "火", "水", "木", "金", "土"};
    String weekday = weekdays[today.getDayOfWeek().getValue() % 7];
    String dateString = "今日は" + month + "月" + day + "日です<br/>" + weekday + "曜日";
%>
<html>
<head>
    <title>勤怠管理システムメニュー（管理部）</title>
    
    <style>
        body {
            margin: 0;
            font-family: 'メイリオ', sans-serif;
            background: #f5f5f5;
            font-size: 14px;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
            background-color: white;
            min-height: 100vh;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            padding: 15px 20px;
            background: #fff;
            border-bottom: 2px solid #dc3545; /* 管理者用は赤色 */
        }
        .user-info {
            display: flex;
            flex-direction: column;
            line-height: 1.5;
        }
        .user-info p {
            margin: 2px 0;
            color: #333;
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
            transition: background-color 0.3s;
        }
        .logout-button:hover {
            background-color: #c82333;
            border-color: #bd2130;
        }
        
        /* ダッシュボードレイアウト */
        .dashboard {
            padding: 20px;
            display: grid;
            grid-template-columns: 1fr 1fr 1fr; /* 3列レイアウト */
            grid-template-rows: auto auto auto;
            gap: 20px;
            height: calc(100vh - 100px);
        }
        
        .dashboard h1 {
            grid-column: 1 / -1;
            text-align: center;
            color: #333;
            margin: 0 0 20px 0;
            font-size: 1.8em;
            border-bottom: 2px solid #dc3545; /* 管理者用は赤色 */
            padding-bottom: 10px;
        }
        
        /* ウィジェットの共通スタイル */
        .widget {
            background: white;
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            transition: box-shadow 0.3s;
            display: flex;
            flex-direction: column;
        }
        
        .widget:hover {
            box-shadow: 0 4px 8px rgba(0,0,0,0.15);
        }
        
        .widget h2 {
            margin: 0 0 15px 0;
            color: #dc3545; /* 管理者用は赤色 */
            font-size: 1.2em;
            border-bottom: 1px solid #eee;
            padding-bottom: 8px;
        }
        
        /* 基本機能ウィジェット */
        .basic-widget {
            border-left: 4px solid #007bff;
        }
        
        .basic-widget h2 {
            color: #007bff;
        }
        
        /* 管理機能ウィジェット */
        .admin-widget {
            border-left: 4px solid #dc3545;
        }
        
        /* その他管理機能ウィジェット */
        .other-admin-widget {
            border-left: 4px solid #fd7e14;
        }
        
        .other-admin-widget h2 {
            color: #fd7e14;
        }
        
        /* 機能ボタンの共通スタイル */
        .function-btn {
            width: 100%;
            padding: 10px;
            margin-bottom: 8px;
            background: #6c757d;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            display: block;
            text-align: center;
            transition: background-color 0.3s;
            font-size: 0.9em;
        }
        
        .function-btn:hover {
            background: #545b62;
        }
        
        .function-btn.basic {
            background: #007bff;
        }
        
        .function-btn.basic:hover {
            background: #0056b3;
        }
        
        .function-btn.admin {
            background: #dc3545;
        }
        
        .function-btn.admin:hover {
            background: #c82333;
        }
        
        /* システム概要ウィジェット */
        .system-widget {
            grid-column: 1 / -1;
            border-left: 4px solid #ffc107;
        }
        
        .system-widget h2 {
            color: #ffc107;
        }
        
        .system-summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 15px;
            margin-bottom: 15px;
        }
        
        .summary-item {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            text-align: center;
            border-left: 4px solid #ffc107;
        }
        
        .summary-item .label {
            font-size: 0.9em;
            color: #666;
            margin-bottom: 5px;
        }
        
        .summary-item .value {
            font-size: 1.3em;
            font-weight: bold;
            color: #333;
        }
        
        /* レスポンシブ対応 */
        @media (max-width: 1024px) {
            .dashboard {
                grid-template-columns: 1fr 1fr;
            }
        }
        
        @media (max-width: 768px) {
            .dashboard {
                grid-template-columns: 1fr;
                padding: 15px;
            }
            
            .system-widget {
                grid-column: 1;
            }
        }
    </style>
    
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="user-info">
                <%-- 部署名と氏名を表示 --%>
                <p>部署：管理部<%-- <%= deptname %> --%></p>
                <p>氏名：<%= user.getName() %> <span style="color: #dc3545; font-weight: bold;">[管理者]</span></p>
            </div>
            <%-- ログアウトボタン --%>
            <form method="post" action="<%= request.getContextPath() %>/logout" style="margin: 0;">
                <input type="submit" value="ログアウト" class="logout-button">
            </form>
        </div>
        
        <div class="dashboard">
            <h1>管理者メニュー</h1>
            
            
            <!-- 基本機能ウィジェット -->
            <div class="widget basic-widget">
                <h2>基本機能</h2>
                <a href="<%= request.getContextPath() %>/showWorkPunchForm" class="function-btn basic">本日分の打刻</a>
                <a href="<%= request.getContextPath() %>/KintaiRecServlet" class="function-btn basic">従業員別勤怠記録表示</a>
                <a href="<%= request.getContextPath() %>/KinmuManageServlet" class="function-btn basic">勤務時間管理</a>
                <a href="<%= request.getContextPath() %>/PasswordChangeServlet" class="function-btn basic">パスワード変更</a>
            </div>
            
            <!-- 業務管理ウィジェット -->
            <div class="widget admin-widget">
                <h2>業務管理</h2>
                <a href="<%= request.getContextPath() %>/projectManage" class="function-btn admin">プロジェクト管理</a>
                <a href="#" class="function-btn" style="background: #6c757d;">休暇申請管理 (準備中)</a>
                <a href="#" class="function-btn" style="background: #6c757d;">休暇付与管理 (準備中)</a>
            </div>
            
            <!-- マスタ管理ウィジェット -->
            <div class="widget other-admin-widget">
                <h2>マスタ管理</h2>
                <a href="<%= request.getContextPath() %>/empManage" class="function-btn" style="background: #fd7e14;">従業員管理</a>
                <a href="<%= request.getContextPath() %>/deptManage" class="function-btn" style="background: #fd7e14;">部署管理</a>
                <a href="<%= request.getContextPath() %>/postManage" class="function-btn" style="background: #fd7e14;">役職管理</a>
                <a href="<%= request.getContextPath() %>/CalendarManageServlet" class="function-btn" style="background: #fd7e14;">カレンダー・イベント管理</a>
                <a href="#" class="function-btn" style="background: #6c757d;">休日種別管理 (準備中)</a>
            </div>
            
            <!-- システム概要ウィジェット -->
            <div class="widget system-widget">
                <h2>システム概要</h2>
                <div class="system-summary">
                    <div class="summary-item" style="grid-column: span 2;">
                        <div class="value" id="currentDate"><%= dateString %></div>
                    </div>
                    <div class="summary-item">
                        <div class="label">今日の出勤者</div>
                        <div class="value" id="todayAttendance">-</div>
                    </div>
                    <div class="summary-item">
                        <div class="label">総従業員数</div>
                        <div class="value" id="totalEmployees">-</div>
                    </div>
                    <div class="summary-item">
                        <div class="label">部署数</div>
                        <div class="value" id="totalDepts">-</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        
        // サーバーから取得したリアルデータを表示
        <% if (request.getAttribute("totalEmployees") != null) { %>
            document.getElementById('totalEmployees').textContent = '<%= request.getAttribute("totalEmployees") %>名';
        <% } %>
        <% if (request.getAttribute("todayAttendance") != null) { %>
            document.getElementById('todayAttendance').textContent = '<%= request.getAttribute("todayAttendance") %>名';
        <% } %>
        <% if (request.getAttribute("totalDepts") != null) { %>
            document.getElementById('totalDepts').textContent = '<%= request.getAttribute("totalDepts") %>部署';
        <% } %>
    </script>
</body>
</html>