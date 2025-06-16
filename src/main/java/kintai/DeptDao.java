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
        String sql = "SELECT DEPTNO, DEPTNAME FROM dept ORDER BY DEPTNO";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                DeptBean dept = new DeptBean();
                dept.setDeptNo(rs.getString("DEPTNO"));
                dept.setDeptName(rs.getString("DEPTNAME"));
                deptList.add(dept);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return deptList;
    }
    
    /**
     * 部署番号で部署情報を検索する
     * @param deptNo 部署番号
     * @return 部署情報。見つからない場合はnull
     */
    public DeptBean findByDeptNo(String deptNo) {
        DeptBean dept = null;
        String sql = "SELECT DEPTNO, DEPTNAME FROM dept WHERE DEPTNO = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deptNo);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dept = new DeptBean();
                    dept.setDeptNo(rs.getString("DEPTNO"));
                    dept.setDeptName(rs.getString("DEPTNAME"));
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
        String sql = "INSERT INTO dept (DEPTNO, DEPTNAME) VALUES (?, ?)";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, dept.getDeptNo());
            ps.setString(2, dept.getDeptName());
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (SQLException e) {
            // 主キー重複エラーの場合
            if (e.getSQLState().equals("23000")) {
                System.err.println("部署番号が既に存在します: " + dept.getDeptNo());
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
        String sql = "UPDATE dept SET DEPTNAME = ? WHERE DEPTNO = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getDeptNo());
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 部署を削除する
     * @param deptNo 削除する部署番号
     * @return 削除に成功した場合true、失敗した場合false
     */
    public boolean delete(String deptNo) {
        String sql = "DELETE FROM dept WHERE DEPTNO = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deptNo);
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (SQLException e) {
            // 外部キー制約エラーの場合（この部署に所属する社員がいる場合）
            if (e.getSQLState().startsWith("23")) {
                System.err.println("この部署に所属する社員が存在するため削除できません: " + deptNo);
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
     * @param deptNo チェックする部署番号
     * @return 既に存在する場合true、存在しない場合false
     */
    public boolean exists(String deptNo) {
        return findByDeptNo(deptNo) != null;
    }
}