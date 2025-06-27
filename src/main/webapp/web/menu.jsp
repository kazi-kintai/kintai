<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kintai.UserBean" %>
<%@ page import="kintai.AnnouncementBean" %>
<%@ page import="kintai.AnnouncementDao" %>
<%@ page import="java.util.List" %>
<%
    UserBean user = (UserBean) session.getAttribute("user");
    // ログインチェック
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/web/login.jsp");
        return;
    }
    
    String deptname = (String)session.getAttribute("deptname");
    
    // アナウンス情報を取得
    AnnouncementDao announcementDao = new AnnouncementDao();
    List<AnnouncementBean> announcements = announcementDao.findActiveAnnouncements();
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
      
      /* アナウンス横幅バナーのスタイル */
      .announcement-banner {
        background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(0, 123, 255, 0.3);
        margin-bottom: 0;
      }
      
      .banner-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 20px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.2);
      }
      
      .banner-title {
        margin: 0;
        font-size: 1.3em;
        display: flex;
        align-items: center;
        gap: 10px;
      }
      
      .banner-icon {
        font-size: 1.2em;
      }
      
      .banner-toggle {
        background: none;
        border: none;
        color: white;
        font-size: 1.2em;
        cursor: pointer;
        padding: 5px;
        margin-left: 10px;
        transition: transform 0.3s;
      }
      
      .banner-toggle:hover {
        transform: scale(1.1);
      }
      
      .banner-content {
        padding: 20px;
        transition: all 0.3s ease;
        overflow: hidden;
      }
      
      .banner-content.collapsed {
        padding: 0;
        max-height: 0;
        opacity: 0;
      }
      
      .announcement-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 15px;
        margin-bottom: 15px;
      }
      
      .announcement-card {
        background: rgba(255, 255, 255, 0.95);
        border-radius: 8px;
        padding: 15px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        transition: transform 0.2s, box-shadow 0.2s;
      }
      
      .announcement-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
      }
      
      .announcement-title {
        margin: 0 0 10px 0;
        color: #007bff;
        font-size: 1.1em;
        cursor: pointer;
        transition: color 0.3s;
        padding: 5px;
        border-radius: 4px;
      }
      
      .announcement-title:hover {
        background: #f8f9fa;
        color: #0056b3;
      }
      
      .announcement-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .announcement-date {
        font-size: 0.85em;
        color: #6c757d;
        font-weight: normal;
      }
      
      .more-section {
        text-align: center;
        margin-top: 15px;
      }
      
      .more-announcements-btn, .less-announcements-btn {
        background: rgba(255, 255, 255, 0.9);
        color: #007bff;
        border: 1px solid rgba(255, 255, 255, 0.3);
        border-radius: 20px;
        padding: 8px 16px;
        cursor: pointer;
        font-size: 0.9em;
        transition: all 0.3s;
      }
      
      .more-announcements-btn:hover, .less-announcements-btn:hover {
        background: white;
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      
      .no-announcement {
        color: rgba(255, 255, 255, 0.8);
        font-style: italic;
        text-align: center;
        padding: 20px;
      }
      
      /* モーダルダイアログのスタイル */
      .modal {
        display: none;
        position: fixed;
        z-index: 1000;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0,0,0,0.5);
      }
      
      .modal-content {
        background-color: #fefefe;
        margin: 15% auto;
        padding: 20px;
        border: 1px solid #888;
        border-radius: 8px;
        width: 50%;
        max-width: 600px;
        position: relative;
      }
      
      .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
        border-bottom: 1px solid #dee2e6;
        padding-bottom: 10px;
      }
      
      .modal-title {
        margin: 0;
        color: #007bff;
        font-size: 1.2em;
      }
      
      .close {
        color: #aaa;
        font-size: 28px;
        font-weight: bold;
        cursor: pointer;
      }
      
      .close:hover {
        color: #000;
      }
      
      .modal-body {
        line-height: 1.6;
        color: #333;
      }
      
      .modal-date {
        color: #6c757d;
        font-size: 0.9em;
        margin-bottom: 10px;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <%-- アナウンス横幅バナー --%>
      <div class="announcement-banner">
        <div class="banner-header">
          <h2 class="banner-title">
            <i class="banner-icon">📢</i> アナウンス
            <button class="banner-toggle" onclick="toggleBanner()" id="bannerToggle">▶ <span id="bannerToggleText">展開</span></button>
          </h2>
        </div>
        
        <div class="banner-content collapsed" id="bannerContent">
          <% if (announcements != null && !announcements.isEmpty()) { %>
            <div class="announcement-grid">
              <% 
              int displayCount = Math.min(3, announcements.size());
              for (int i = 0; i < displayCount; i++) { 
                  AnnouncementBean announcement = announcements.get(i);
                  String dateStr = "";
                  if (announcement.getCreatedAt() != null) {
                      dateStr = announcement.getCreatedAt().toLocalDate().toString();
                  }
              %>
                <div class="announcement-card">
                  <h3 class="announcement-title" onclick="showAnnouncementDetail(<%= announcement.getAnnouncementId() %>, '<%= announcement.getTitle().replace("'", "\\'") %>', '<%= announcement.getContent().replace("'", "\\'").replace("\n", "\\n") %>', '<%= dateStr %>')">
                    <%= announcement.getTitle() %>
                  </h3>
                  <div class="announcement-meta">
                    <span class="announcement-date"><%= dateStr %></span>
                  </div>
                </div>
              <% } %>
            </div>
            
            <% if (announcements.size() > 3) { %>
              <div class="more-section">
                <button class="more-announcements-btn" onclick="showAllAnnouncements()">さらに表示 (<%= announcements.size() - 3 %>件)</button>
              </div>
            <% } %>
            
            <!-- 隠された全アナウンス表示エリア -->
            <div id="allAnnouncementsArea" style="display: none;">
              <div class="announcement-grid">
                <% for (int i = 3; i < announcements.size(); i++) { 
                    AnnouncementBean announcement = announcements.get(i);
                    String dateStr = "";
                    if (announcement.getCreatedAt() != null) {
                        dateStr = announcement.getCreatedAt().toLocalDate().toString();
                    }
                %>
                  <div class="announcement-card">
                    <h3 class="announcement-title" onclick="showAnnouncementDetail(<%= announcement.getAnnouncementId() %>, '<%= announcement.getTitle().replace("'", "\\'") %>', '<%= announcement.getContent().replace("'", "\\'").replace("\n", "\\n") %>', '<%= dateStr %>')">
                      <%= announcement.getTitle() %>
                    </h3>
                    <div class="announcement-meta">
                      <span class="announcement-date"><%= dateStr %></span>
                    </div>
                  </div>
                <% } %>
              </div>
              <div class="more-section">
                <button class="less-announcements-btn" onclick="hideExtraAnnouncements()">折りたたむ</button>
              </div>
            </div>
          <% } else { %>
            <div class="no-announcement">
              現在、アナウンスはありません
            </div>
          <% } %>
        </div>
      </div>
      
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

    <!-- アナウンス詳細モーダル -->
    <div id="announcementModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title" id="modalTitle">アナウンス詳細</h2>
          <span class="close" onclick="closeModal()">&times;</span>
        </div>
        <div class="modal-body">
          <div class="modal-date" id="modalDate"></div>
          <div id="modalContent"></div>
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
      
      // アナウンス関連のJavaScript
      function showAnnouncementDetail(id, title, content, date) {
        document.getElementById('modalTitle').textContent = title;
        document.getElementById('modalDate').textContent = '投稿日: ' + date;
        document.getElementById('modalContent').innerHTML = content.replace(/\n/g, '<br/>');
        document.getElementById('announcementModal').style.display = 'block';
      }
      
      function closeModal() {
        document.getElementById('announcementModal').style.display = 'none';
      }
      
      // モーダルの外側をクリックしたら閉じる
      window.onclick = function(event) {
        var modal = document.getElementById('announcementModal');
        if (event.target == modal) {
          closeModal();
        }
      }
      
      // more/lessボタンの機能
      function showAllAnnouncements() {
        document.getElementById('allAnnouncementsArea').style.display = 'block';
        // moreボタンを隠す
        var moreBtn = document.querySelector('.more-announcements-btn');
        if (moreBtn) moreBtn.style.display = 'none';
      }
      
      function hideExtraAnnouncements() {
        document.getElementById('allAnnouncementsArea').style.display = 'none';
        // moreボタンを表示
        var moreBtn = document.querySelector('.more-announcements-btn');
        if (moreBtn) moreBtn.style.display = 'inline-block';
      }
      
      // 横幅バナーの折りたたみ機能
      function toggleBanner() {
        var content = document.getElementById('bannerContent');
        var toggle = document.getElementById('bannerToggle');
        var toggleText = document.getElementById('bannerToggleText');
        
        if (content.classList.contains('collapsed')) {
          content.classList.remove('collapsed');
          toggle.innerHTML = '▼ <span id="bannerToggleText">収納</span>';
        } else {
          content.classList.add('collapsed');
          toggle.innerHTML = '▶ <span id="bannerToggleText">展開</span>';
        }
      }
    </script>
  </body>
</html>
