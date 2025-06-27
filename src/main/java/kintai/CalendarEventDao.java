package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * calendar_eventテーブルへのデータアクセスを担当するクラス (DAO)。
 * カレンダーイベント情報の検索、追加、更新、削除を行う。
 * 新しいER図のcalendar_eventテーブルに準拠し、REPEAT_RULE_IDに対応。
 */
public class CalendarEventDao {

    private DBAccess db = new DBAccess();

    /**
     * すべてのカレンダーイベント情報を取得する
     * @return カレンダーイベント情報のリスト
     */
    public List<CalendarEventBean> findAll() {
        List<CalendarEventBean> eventList = new ArrayList<>();
        // SELECT文にREPEAT_RULE_IDを追加
        String sql = "SELECT EVENT_DATE, EVENT_NAME, IS_WORK FROM calendar_event ORDER BY EVENT_DATE DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CalendarEventBean event = new CalendarEventBean();
                event.setEventDate(rs.getDate("EVENT_DATE").toLocalDate());
                event.setEventName(rs.getString("EVENT_NAME"));
                event.setWork(rs.getBoolean("IS_WORK"));
                // REPEAT_RULE_IDは暫時的に無効化（データベースに列が存在しないため）
                // Integer repeatRuleId = rs.getObject("REPEAT_RULE_ID", Integer.class); // nullの場合も対応
                // event.setRepeatRuleId(repeatRuleId);
                eventList.add(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventList;
    }

    /**
     * イベント日付でカレンダーイベント情報を検索する
     * @param eventDate イベント日付
     * @return カレンダーイベント情報。見つからない場合はnull
     */
    public CalendarEventBean findByEventDate(LocalDate eventDate) {
        CalendarEventBean event = null;
        // SELECT文にREPEAT_RULE_IDを追加
        String sql = "SELECT EVENT_DATE, EVENT_NAME, IS_WORK, REPEAT_RULE_ID FROM calendar_event WHERE EVENT_DATE = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(eventDate));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    event = new CalendarEventBean();
                    event.setEventDate(rs.getDate("EVENT_DATE").toLocalDate());
                    event.setEventName(rs.getString("EVENT_NAME"));
                    event.setWork(rs.getBoolean("IS_WORK"));
                    // REPEAT_RULE_IDを取得し、Beanにセット
                    Integer repeatRuleId = rs.getObject("REPEAT_RULE_ID", Integer.class); // nullの場合も対応
                    event.setRepeatRuleId(repeatRuleId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    /**
     * 新しいカレンダーイベントを追加する
     * @param event 追加するカレンダーイベント情報
     * @return 追加に成功した場合true、失敗した場合false
     */
    public boolean insert(CalendarEventBean event) {
        // INSERT文にREPEAT_RULE_IDを追加
        String sql = "INSERT INTO calendar_event (EVENT_DATE, EVENT_NAME, IS_WORK, REPEAT_RULE_ID) VALUES (?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(event.getEventDate()));
            ps.setString(2, event.getEventName());
            ps.setBoolean(3, event.isWork());
            // REPEAT_RULE_IDがnullの場合も対応
            if (event.getRepeatRuleId() != null) {
                ps.setInt(4, event.getRepeatRuleId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            int count = ps.executeUpdate();
            return count > 0;

        } catch (SQLException e) {
            // 主キー重複エラーの場合
            if (e.getSQLState().equals("23000")) {
                System.err.println("イベント日付が既に存在します: " + event.getEventDate());
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * カレンダーイベント情報を更新する
     * @param event 更新するカレンダーイベント情報
     * @return 更新に成功した場合true、失敗した場合false
     */
    public boolean update(CalendarEventBean event) {
        // UPDATE文にREPEAT_RULE_IDを追加
        String sql = "UPDATE calendar_event SET EVENT_NAME = ?, IS_WORK = ?, REPEAT_RULE_ID = ? WHERE EVENT_DATE = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, event.getEventName());
            ps.setBoolean(2, event.isWork());
            // REPEAT_RULE_IDがnullの場合も対応
            if (event.getRepeatRuleId() != null) {
                ps.setInt(3, event.getRepeatRuleId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setDate(4, Date.valueOf(event.getEventDate()));

            int count = ps.executeUpdate();
            return count > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * カレンダーイベントを削除する
     * @param eventDate 削除するイベント日付
     * @return 削除に成功した場合true、失敗した場合false
     */
    public boolean delete(LocalDate eventDate) {
        String sql = "DELETE FROM calendar_event WHERE EVENT_DATE = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(eventDate));

            int count = ps.executeUpdate();
            return count > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * イベント日付の重複をチェックする
     * @param eventDate チェックするイベント日付
     * @return 既に存在する場合true、存在しない場合false
     */
    public boolean exists(LocalDate eventDate) {
        return findByEventDate(eventDate) != null;
    }
}
