package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * leave_type テーブル（論理削除対応）へのデータアクセスクラス。
 */
public class LeaveTypeDao {

    private DBAccess db = new DBAccess();

    /** 有効な休暇種別一覧を取得（IS_DELETED = false） */
    public List<LeaveTypeBean> findAll() {
        List<LeaveTypeBean> list = new ArrayList<>();
        String sql = "SELECT LEAVE_TYPE_ID, LEAVE_TYPE_NAME, IS_PAID FROM leave_type WHERE IS_DELETED = FALSE ORDER BY LEAVE_TYPE_ID";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LeaveTypeBean bean = new LeaveTypeBean();
                bean.setLeaveTypeId(rs.getInt("LEAVE_TYPE_ID"));
                bean.setLeaveTypeName(rs.getString("LEAVE_TYPE_NAME"));
                bean.setPaid(rs.getBoolean("IS_PAID"));
                bean.setDeleted(false);
                list.add(bean);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return list;
    }

    /** IDで休暇種別を取得（削除済みも含む） */
    public LeaveTypeBean findById(int leaveTypeId) {
        String sql = "SELECT LEAVE_TYPE_ID, LEAVE_TYPE_NAME, IS_PAID, IS_DELETED FROM leave_type WHERE LEAVE_TYPE_ID = ?";
        LeaveTypeBean bean = null;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, leaveTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bean = new LeaveTypeBean();
                    bean.setLeaveTypeId(rs.getInt("LEAVE_TYPE_ID"));
                    bean.setLeaveTypeName(rs.getString("LEAVE_TYPE_NAME"));
                    bean.setPaid(rs.getBoolean("IS_PAID"));
                    bean.setDeleted(rs.getBoolean("IS_DELETED"));
                }
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return bean;
    }

    /** 新規登録（IS_DELETED = false で追加） */
    public boolean insert(LeaveTypeBean bean) {
        String sql = "INSERT INTO leave_type (LEAVE_TYPE_ID, LEAVE_TYPE_NAME, IS_PAID, IS_DELETED) VALUES (?, ?, ?, FALSE)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bean.getLeaveTypeId());
            ps.setString(2, bean.getLeaveTypeName());
            ps.setBoolean(3, bean.isPaid());

            return ps.executeUpdate() > 0;

        } catch (SQLException | ClassNotFoundException e) {
            if ("23000".equals(((SQLException) e).getSQLState())) {
                System.err.println("休暇種別IDが既に存在しています: " + bean.getLeaveTypeId());
            } else {
                e.printStackTrace();
            }
        }

        return false;
    }

    /** 更新処理（IDは変更可能） */
    public boolean update(int originalId, LeaveTypeBean bean) {
        String sql = "UPDATE leave_type SET LEAVE_TYPE_ID = ?, LEAVE_TYPE_NAME = ?, IS_PAID = ? WHERE LEAVE_TYPE_ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bean.getLeaveTypeId());
            ps.setString(2, bean.getLeaveTypeName());
            ps.setBoolean(3, bean.isPaid());
            ps.setInt(4, originalId);

            return ps.executeUpdate() > 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    /** 論理削除（IS_DELETED = true に更新） */
    public boolean delete(int leaveTypeId) {
        String sql = "UPDATE leave_type SET IS_DELETED = TRUE WHERE LEAVE_TYPE_ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, leaveTypeId);
            return ps.executeUpdate() > 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    /** 復元（論理削除フラグを戻す） */
    public boolean restore(int leaveTypeId) {
        String sql = "UPDATE leave_type SET IS_DELETED = FALSE WHERE LEAVE_TYPE_ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, leaveTypeId);
            return ps.executeUpdate() > 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    /** 論理削除済み一覧を取得 */
    public List<LeaveTypeBean> findDeleted() {
        List<LeaveTypeBean> list = new ArrayList<>();
        String sql = "SELECT LEAVE_TYPE_ID, LEAVE_TYPE_NAME, IS_PAID FROM leave_type WHERE IS_DELETED = TRUE ORDER BY LEAVE_TYPE_ID";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LeaveTypeBean bean = new LeaveTypeBean();
                bean.setLeaveTypeId(rs.getInt("LEAVE_TYPE_ID"));
                bean.setLeaveTypeName(rs.getString("LEAVE_TYPE_NAME"));
                bean.setPaid(rs.getBoolean("IS_PAID"));
                bean.setDeleted(true);
                list.add(bean);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return list;
    }

    /** ID存在チェック（削除済みも含む） */
    public boolean exists(int leaveTypeId) {
        return findById(leaveTypeId) != null;
    }
}
