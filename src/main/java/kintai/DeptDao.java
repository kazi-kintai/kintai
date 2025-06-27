package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * deptテーブルへのデータアクセスを担当するクラス (DAO)。
 * 部署情報の検索、追加、更新、削除を行う。
 */
public class DeptDao {
    
    private DBAccess db = new DBAccess();
    
    /**
     * すべての部署情報を取得する
     * @return 部署情報のリスト
     */
    public List<DeptBean> findAll() {
        List<DeptBean> deptList = new ArrayList<>();
        String sql = "SELECT DEPT_ID, DEPT_NAME FROM dept WHERE IS_DELETED = false ORDER BY DEPT_ID";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                DeptBean dept = new DeptBean();
                dept.setDeptId(rs.getString("DEPT_ID"));
                dept.setDeptName(rs.getString("DEPT_NAME"));
                deptList.add(dept);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return deptList;
    }
    
    /**
     * 部署番号で部署情報を検索する
     * @param deptId 部署番号
     * @return 部署情報。見つからない場合はnull
     */
    public DeptBean findByDeptId(String deptId) {
        DeptBean dept = null;
        String sql = "SELECT DEPT_ID, DEPT_NAME FROM dept WHERE DEPT_ID = ? AND IS_DELETED = false";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deptId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dept = new DeptBean();
                    dept.setDeptId(rs.getString("DEPT_ID"));
                    dept.setDeptName(rs.getString("DEPT_NAME"));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return dept;
    }
    
    /**
     * 新しい部署を追加する
     * @param dept 追加する部署情報
     * @return 追加に成功した場合true、失敗した場合false
     */
    public boolean insert(DeptBean dept) {
        String sql = "INSERT INTO dept (DEPT_ID, DEPT_NAME, IS_DELETED, CREATED_AT, CREATED_BY) VALUES (?, ?, false, NOW(), 'system')";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, dept.getDeptId());
            ps.setString(2, dept.getDeptName());
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (SQLException e) {
            // 主キー重複エラーの場合
            if (e.getSQLState().equals("23000")) {
                System.err.println("部署番号が既に存在します: " + dept.getDeptId());
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 部署情報を更新する
     * @param dept 更新する部署情報
     * @return 更新に成功した場合true、失敗した場合false
     */
    public boolean update(DeptBean dept) {
        String sql = "UPDATE dept SET DEPT_NAME = ?, UPDATED_AT = NOW(), UPDATED_BY = 'system' WHERE DEPT_ID = ? AND IS_DELETED = false";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getDeptId());
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 部署を削除する
     * @param deptId 削除する部署番号
     * @return 削除に成功した場合true、失敗した場合false
     */
    public boolean delete(String deptId) {
        String sql = "UPDATE dept SET IS_DELETED = true, DELETED_AT = NOW(), DELETED_BY = 'system' WHERE DEPT_ID = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deptId);
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (SQLException e) {
            // 外部キー制約エラーの場合（この部署に所属する社員がいる場合）
            if (e.getSQLState().startsWith("23")) {
                System.err.println("この部署に所属する社員が存在するため削除できません: " + deptId);
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 部署番号の重複をチェックする
     * @param deptId チェックする部署番号
     * @return 既に存在する場合true、存在しない場合false
     */
    public boolean exists(String deptId) {
        return findByDeptId(deptId) != null;
    }
}