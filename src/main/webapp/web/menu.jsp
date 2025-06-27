<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%
    UserBean user = (UserBean) session.getAttribute("user");
    // ログインチェック
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    
    String deptname = (String)session.getAttribute("deptname");
%>
<html>
  <head>
    <title>勤怠管理システムメニュー</title>

    <style>
      body {
        margin: 0;
        font-family: "メイリオ", sans-serif;
        background: #f5f5f5;
        font-size: 14px;
      }
      .container {
        max-width: 1400px;
        margin: 0 auto;
        background-color: white;
        min-height: 100vh;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
      }
      .header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 15px 20px;
        background: #fff;
        border-bottom: 2px solid #007bff;
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
        grid-template-columns: 1fr 1fr;
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
        border-bottom: 2px solid #007bff;
        padding-bottom: 10px;
      }

      /* ウィジェットの共通スタイル */
      .widget {
        background: white;
        border: 1px solid #ddd;
        border-radius: 8px;
        padding: 20px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        transition: box-shadow 0.3s;
      }

      .widget:hover {
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
      }

      .widget h2 {
        margin: 0 0 15px 0;
        color: #007bff;
        font-size: 1.3em;
        border-bottom: 1px solid #eee;
        padding-bottom: 8px;
      }

      /* 打刻ウィジェット */
      .punch-widget {
        display: flex;
        flex-direction: column;
        align-items: center;
      }

      .punch-buttons {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 10px;
        width: 100%;
        margin-bottom: 15px;
      }

      .punch-btn {
        padding: 15px;
        border: none;
        border-radius: 5px;
        font-size: 1.1em;
        font-weight: bold;
        cursor: pointer;
        transition: all 0.3s;
        text-decoration: none;
        text-align: center;
        display: block;
      }

      .punch-btn.start {
        background: #28a745;
        color: white;
      }
      .punch-btn.start:hover {
        background: #218838;
      }

      .punch-btn.end {
        background: #dc3545;
        color: white;
      }
      .punch-btn.end:hover {
        background: #c82333;
      }

      .current-time {
        font-size: 1.2em;
        font-weight: bold;
        color: #333;
        margin-bottom: 10px;
      }

      /* パスワード変更ウィジェット */
      .password-widget .btn {
        width: 100%;
        padding: 12px;
        background: #007bff;
        color: white;
        border: none;
        border-radius: 5px;
        font-size: 1.1em;
        cursor: pointer;
        text-decoration: none;
        display: block;
        text-align: center;
        transition: background-color 0.3s;
      }

      .password-widget .btn:hover {
        background: #0056b3;
      }

      /* 記録表示ウィジェット */
      .records-widget {
        grid-column: 1 / -1;
      }

      .records-summary {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 15px;
        margin-bottom: 15px;
      }

      .summary-item {
        background: #f8f9fa;
        padding: 15px;
        border-radius: 5px;
        text-align: center;
        border-left: 4px solid #007bff;
      }

      .summary-item .label {
        font-size: 0.9em;
        color: #666;
        margin-bottom: 5px;
      }

      .summary-item .value {
        font-size: 1.4em;
        font-weight: bold;
        color: #333;
      }

      .view-all-btn {
        width: 100%;
        padding: 10px;
        background: #6c757d;
        color: white;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        text-decoration: none;
        display: block;
        text-align: center;
        transition: background-color 0.3s;
      }

      .view-all-btn:hover {
        background: #545b62;
      }

      /* レスポンシブ対応 */
      @media (max-width: 768px) {
        .dashboard {
          grid-template-columns: 1fr;
          padding: 15px;
        }

        .records-widget {
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
          <p>部署：営業部<%-- <%= deptname %> --%></p>
          <p>氏名：<%= user.getName() %></p>
        </div>
        <%-- ログアウトボタン --%>
        <form
          method="post"
          action="<%= request.getContextPath() %>/logout"
          style="margin: 0"
        >
          <input type="submit" value="ログアウト" class="logout-button" />
        </form>
      </div>

      <div class="dashboard">
        <h1>勤怠管理ダッシュボード</h1>

        <!-- 今日の打刻ウィジェット -->
        <div class="widget punch-widget">
          <h2>今日の打刻</h2>
          <div class="current-time" id="currentTime"></div>
          <div class="punch-buttons">
            <a
              href="<%= request.getContextPath() %>/showWorkPunchForm"
              class="punch-btn start"
              >出勤</a
            >
            <a
              href="<%= request.getContextPath() %>/showWorkPunchForm"
              class="punch-btn end"
              >退勤</a
            >
          </div>
          <small style="color: #666; text-align: center"
            >※クリックして打刻画面へ</small
          >
        </div>

        <!-- パスワード変更ウィジェット -->
        <div class="widget password-widget">
          <h2>アカウント設定</h2>
          <p style="color: #666; margin-bottom: 15px; font-size: 0.9em">
            パスワードの変更や<br />
            アカウント設定を行えます
          </p>
          <a
            href="<%= request.getContextPath() %>/PasswordChangeServlet"
            class="btn"
            >パスワード変更</a
          >
        </div>

        <!-- 勤怠記録概要ウィジェット -->
        <div class="widget records-widget">
          <h2>勤怠記録概要</h2>
          <div class="records-summary">
            <div class="summary-item">
              <div class="label">今月の出勤日数</div>
              <div class="value" id="workDays">-</div>
            </div>
            <div class="summary-item">
              <div class="label">今月の総労働時間</div>
              <div class="value" id="totalHours">-</div>
            </div>
            <div class="summary-item">
              <div class="label">今週の労働時間</div>
              <div class="value" id="weekHours">-</div>
            </div>
            <div class="summary-item">
              <div class="label">平均出勤時刻</div>
              <div class="value" id="avgStartTime">-</div>
            </div>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px">
            <a
              href="<%= request.getContextPath() %>/KintaiRecServlet"
              class="view-all-btn"
              >詳細な記録を見る</a
            >
            <a
              href="<%= request.getContextPath() %>/KinmuManageServlet"
              class="view-all-btn"
              >勤務時間管理</a
            >
          </div>
        </div>
      </div>
    </div>

    <script>
      // 現在時刻の表示
      function updateCurrentTime() {
        const now = new Date();
        const timeString = now.toLocaleTimeString("ja-JP", {
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        });
        const dateString = now.toLocaleDateString("ja-JP", {
          year: "numeric",
          month: "long",
          day: "numeric",
          weekday: "long",
        });
        document.getElementById(
          "currentTime"
        ).innerHTML = `<div style="font-size: 0.8em; color: #666;">${dateString}</div>
                 <div>${timeString}</div>`;
      }

      // 1秒ごとに時刻を更新
      updateCurrentTime();
      setInterval(updateCurrentTime, 1000);

      // サンプルデータの表示（実際のデータは別途取得）
      document.getElementById("workDays").textContent = "12日";
      document.getElementById("totalHours").textContent = "96時間";
      document.getElementById("weekHours").textContent = "24時間";
      document.getElementById("avgStartTime").textContent = "09:15";
    </script>
  </body>
</html>
