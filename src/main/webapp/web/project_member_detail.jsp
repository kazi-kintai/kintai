<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="kintai.UserBean" %>
<%@ page import="kintai.ProjectBean" %>
<%@ page import="kintai.ProjectMemberReportBean" %>

<%
    // ログインチェック
    UserBean user = (UserBean) session.getAttribute("user");
    if (user == null || user.getRoleId() != 1) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    
    ProjectBean project = (ProjectBean) request.getAttribute("project");
    List<ProjectMemberReportBean> memberReports = (List<ProjectMemberReportBean>) request.getAttribute("memberReports");
    BigDecimal totalActual = (BigDecimal) request.getAttribute("totalActual");
    BigDecimal budgetVariance = (BigDecimal) request.getAttribute("budgetVariance");
    String selectedMonth = (String) request.getAttribute("selectedMonth");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>項目人員詳細</title>
    <style>
        body {
            font-family: 'メイリオ', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        
        .container {
            max-width: 1000px;
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
        
        .project-info {
            background-color: #f8f9fa;
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 4px;
            border: 1px solid #dee2e6;
        }
        
        .project-info h2 {
            margin-top: 0;
            color: #495057;
        }
        
        .info-row {
            margin-bottom: 8px;
        }
        
        .info-label {
            font-weight: bold;
            display: inline-block;
            width: 120px;
        }
        
        .member-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }
        
        .member-table th, .member-table td {
            border: 1px solid #dee2e6;
            padding: 10px;
            text-align: left;
            vertical-align: middle;
        }
        
        .member-table th {
            background-color: #f8f9fa;
            font-weight: bold;
            color: #495057;
        }
        
        .member-table tr:nth-child(even) {
            background-color: #f8f9fa;
        }
        
        .member-table tr:hover {
            background-color: #e9ecef;
        }
        
        .text-right {
            text-align: right;
        }
        
        .summary-section {
            background-color: #e3f2fd;
            padding: 15px;
            margin-top: 20px;
            border-radius: 4px;
            border: 1px solid #bbdefb;
        }
        
        .summary-row {
            margin-bottom: 10px;
            font-size: 16px;
        }
        
        .summary-label {
            font-weight: bold;
            display: inline-block;
            width: 150px;
        }
        
        .summary-value {
            font-weight: bold;
            color: #1976d2;
        }
        
        .variance-positive {
            color: #2e7d32;
        }
        
        .variance-negative {
            color: #d32f2f;
        }
        
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            margin-right: 10px;
        }
        
        .btn-primary {
            background-color: #007bff;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #0056b3;
        }
        
        .btn-secondary {
            background-color: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background-color: #545b62;
        }
        
        .calculation-section {
            margin-top: 20px;
            text-align: center;
        }
        
        .no-data {
            text-align: center;
            color: #6c757d;
            font-style: italic;
        }
    </style>
    <script>
        function calculateResults() {
            // 集計ボタンが押された後の表示制御
            var summarySection = document.getElementById('summarySection');
            summarySection.style.display = 'block';
            
            // ボタンを無効化
            var calcButton = document.getElementById('calculateButton');
            calcButton.disabled = true;
            calcButton.textContent = '集計済み';
        }
        
        function closeWindow() {
            window.close();
        }
    </script>
</head>
<body>
    <div class="container">
        <h1>項目人員詳細</h1>
        
        <% if (project != null) { %>
            <div class="project-info">
                <h2>プロジェクト情報</h2>
                <div class="info-row">
                    <span class="info-label">プロジェクト名：</span>
                    <%= project.getProjectName() %>
                </div>
                <div class="info-row">
                    <span class="info-label">対象月：</span>
                    <%= selectedMonth %>
                </div>
                <div class="info-row">
                    <span class="info-label">予算：</span>
                    ¥<%= String.format("%,d", project.getBudget()) %>
                </div>
            </div>
            
            <% if (memberReports != null && !memberReports.isEmpty()) { %>
                <table class="member-table">
                    <thead>
                        <tr>
                            <th>従業員番号</th>
                            <th>従業員名</th>
                            <th class="text-right">月総工作時間</th>
                            <th class="text-right">時給</th>
                            <th class="text-right">実績</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (ProjectMemberReportBean member : memberReports) { %>
                            <tr>
                                <td><%= member.getEmpNo() %></td>
                                <td><%= member.getEmpName() %></td>
                                <td class="text-right">
                                    <%= member.getTotalHours() != null ? String.format("%.2f", member.getTotalHours()) : "0.00" %>時間
                                </td>
                                <td class="text-right">
                                    ¥<%= member.getHourlyRate() != null ? String.format("%,.0f", member.getHourlyRate()) : "0" %>
                                </td>
                                <td class="text-right">
                                    ¥<%= member.getActualAmount() != null ? String.format("%,.0f", member.getActualAmount()) : "0" %>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
                
                <div class="calculation-section">
                    <button id="calculateButton" class="btn btn-primary" onclick="calculateResults()">集計</button>
                    <button class="btn btn-secondary" onclick="closeWindow()">閉じる</button>
                </div>
                
                <div id="summarySection" class="summary-section" style="display: none;">
                    <div class="summary-row">
                        <span class="summary-label">項目総集計：</span>
                        <span class="summary-value">¥<%= totalActual != null ? String.format("%,.0f", totalActual) : "0" %></span>
                    </div>
                    <div class="summary-row">
                        <span class="summary-label">予算実績差異：</span>
                        <% if (budgetVariance != null) { %>
                            <span class="summary-value <%= budgetVariance.compareTo(BigDecimal.ZERO) >= 0 ? "variance-positive" : "variance-negative" %>">
                                ¥<%= String.format("%,.0f", budgetVariance) %>
                            </span>
                        <% } else { %>
                            <span class="summary-value">計算不可</span>
                        <% } %>
                    </div>
                </div>
                
            <% } else { %>
                <div class="no-data">
                    <p>指定された月にこのプロジェクトの作業記録がありません。</p>
                </div>
                <div class="calculation-section">
                    <button class="btn btn-secondary" onclick="closeWindow()">閉じる</button>
                </div>
            <% } %>
            
        <% } else { %>
            <div class="no-data">
                <p>プロジェクト情報が見つかりません。</p>
            </div>
            <div class="calculation-section">
                <button class="btn btn-secondary" onclick="closeWindow()">閉じる</button>
            </div>
        <% } %>
    </div>
</body>
</html>