package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * leave_typeテーブルへのデータアクセスを担当するDAOクラス。
 */
public class LeaveTypeDao {

    private DBAccess db = new DBAccess();

    /** すべての休暇種別を取得 */
    public List<LeaveTypeBean> findAll() {
        List<LeaveTypeBean> list = new ArrayList<>();
        String sql = "SELECT LEAVE_TYPE_ID, LEAVE_TYPE_NAME, IS_PAID FROM leave_type ORDER BY LEAVE_TYPE_ID";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LeaveTypeBean leaveType = new LeaveTypeBean();
                leaveType.setLeaveTypeId(rs.getInt("LEAVE_TYPE_ID"));
                leaveType.setLeaveTypeName(rs.getString("LEAVE_TYPE_NAME"));
                leaveType.setPaid(rs.getBoolean("IS_PAID"));
                list.add(leaveType);
            }

        } catch (SQLException e) {
            e.printStackTrace();}
            
            catch (Exception e) {
                e.printStackTrace();
            }
        return list;
    }

    /** IDで休暇種別を検索 */
    public LeaveTypeBean findById(int leaveTypeId) {
        LeaveTypeBean leaveType = null;
        String sql = "SELECT LEAVE_TYPE_ID, LEAVE_TYPE_NAME, IS_PAID FROM leave_type WHERE LEAVE_TYPE_ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, leaveTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    leaveType = new LeaveTypeBean();
                    leaveType.setLeaveTypeId(rs.getInt("LEAVE_TYPE_ID"));
                    leaveType.setLeaveTypeName(rs.getString("LEAVE_TYPE_NAME"));
                    leaveType.setPaid(rs.getBoolean("IS_PAID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return leaveType;
    }

    /** 新規休暇種別を追加 */
    public boolean insert(LeaveTypeBean leaveType) {
        String sql = "INSERT INTO leave_type (LEAVE_TYPE_ID, LEAVE_TYPE_NAME, IS_PAID) VALUES (?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, leaveType.getLeaveTypeId());
            ps.setString(2, leaveType.getLeaveTypeName());
            ps.setBoolean(3, leaveType.isPaid());

            int count = ps.executeUpdate();
            return count > 0;

        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                System.err.println("休暇種別IDが既に存在します: " + leaveType.getLeaveTypeId());
            } else {
                e.printStackTrace();}
          
            
        }catch (Exception e) {
                e.printStackTrace();}
        
        return false;
    }

    /** 休暇種別を更新 */
    public boolean update(LeaveTypeBean leaveType) {
        String sql = "UPDATE leave_type SET LEAVE_TYPE_NAME = ?, IS_PAID = ? WHERE LEAVE_TYPE_ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, leaveType.getLeaveTypeName());
            ps.setBoolean(2, leaveType.isPaid());
            ps.setInt(3, leaveType.getLeaveTypeId());

            int count = ps.executeUpdate();
            return count > 0;

        } catch (SQLException e) {
            e.printStackTrace();}
       catch (Exception e) {
        e.printStackTrace();}
    
        return false;
}


    /** 休暇種別を削除 */
    public boolean delete(int leaveTypeId) {
        String sql = "DELETE FROM leave_type WHERE LEAVE_TYPE_ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, leaveTypeId);

            int count = ps.executeUpdate();
            return count > 0;

        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                System.err.println("この休暇種別は使用されているため削除できません: " + leaveTypeId);
            } else {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();}    
        return false;
    }

    /** 休暇種別IDの重複チェック */
    public boolean exists(int leaveTypeId) {
        return findById(leaveTypeId) != null;
    }
}
