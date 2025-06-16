package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * empテーブルへのデータアクセスを担当するクラス (DAO)。
 * 社員情報の検索、追加、更新、削除を行う。
 */
public class EmpDao {
    
    private DBAccess db = new DBAccess();
    
    /**
     * すべての社員情報を取得する（部署名、役職名も含む）
     * @return 社員情報のリスト
     */
    public List<EmpBean> findAll() {
        List<EmpBean> empList = new ArrayList<>();
        String sql = "SELECT e.EMPNO, e.EMPNAME, e.DEPTNO, e.POSTNO, e.PASS, e.ROLE, " +
                     "d.DEPTNAME, p.POSTNAME " +
                     "FROM emp e " +
                     "LEFT JOIN dept d ON e.DEPTNO = d.DEPTNO " +
                     "LEFT JOIN post p ON e.POSTNO = p.POSTNO " +
                     "ORDER BY e.EMPNO";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                EmpBean emp = new EmpBean();
                emp.setEmpNo(rs.getString("EMPNO"));
                emp.setEmpName(rs.getString("EMPNAME"));
                emp.setDeptNo(rs.getString("DEPTNO"));
                emp.setPostNo(rs.getString("POSTNO"));
                emp.setPass(rs.getString("PASS"));
                emp.setRole(rs.getInt("ROLE"));
                emp.setDeptName(rs.getString("DEPTNAME"));
                emp.setPostName(rs.getString("POSTNAME"));
                empList.add(emp);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return empList;
    }
    
    /**
     * 社員番号で社員情報を検索する
     * @param empNo 社員番号
     * @return 社員情報。見つからない場合はnull
     */
    public EmpBean findByEmpNo(String empNo) {
        EmpBean emp = null;
        String sql = "SELECT e.EMPNO, e.EMPNAME, e.DEPTNO, e.POSTNO, e.PASS, e.ROLE, " +
                     "d.DEPTNAME, p.POSTNAME " +
                     "FROM emp e " +
                     "LEFT JOIN dept d ON e.DEPTNO = d.DEPTNO " +
                     "LEFT JOIN post p ON e.POSTNO = p.POSTNO " +
                     "WHERE e.EMPNO = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, empNo);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    emp = new EmpBean();
                    emp.setEmpNo(rs.getString("EMPNO"));
                    emp.setEmpName(rs.getString("EMPNAME"));
                    emp.setDeptNo(rs.getString("DEPTNO"));
                    emp.setPostNo(rs.getString("POSTNO"));
                    emp.setPass(rs.getString("PASS"));
                    emp.setRole(rs.getInt("ROLE"));
                    emp.setDeptName(rs.getString("DEPTNAME"));
                    emp.setPostName(rs.getString("POSTNAME"));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return emp;
    }
    
    /**
     * 新しい社員を追加する
     * @param emp 追加する社員情報
     * @return 追加に成功した場合true、失敗した場合false
     */
    public boolean insert(EmpBean emp) {
        String sql = "INSERT INTO emp (EMPNO, EMPNAME, DEPTNO, POSTNO, PASS, ROLE) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, emp.getEmpNo());
            ps.setString(2, emp.getEmpName());
            ps.setString(3, emp.getDeptNo());
            ps.setString(4, emp.getPostNo());
            ps.setString(5, emp.getPass());
            ps.setInt(6, emp.getRole());
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (SQLException e) {
            // 主キー重複エラーの場合
            if (e.getSQLState().equals("23000")) {
                System.err.println("社員番号が既に存在します: " + emp.getEmpNo());
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 社員情報を更新する
     * @param emp 更新する社員情報
     * @return 更新に成功した場合true、失敗した場合false
     */
    public boolean update(EmpBean emp) {
        String sql = "UPDATE emp SET EMPNAME = ?, DEPTNO = ?, POSTNO = ?, PASS = ?, ROLE = ? " +
                     "WHERE EMPNO = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, emp.getEmpName());
            ps.setString(2, emp.getDeptNo());
            ps.setString(3, emp.getPostNo());
            ps.setString(4, emp.getPass());
            ps.setInt(5, emp.getRole());
            ps.setString(6, emp.getEmpNo());
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 社員を削除する
     * @param empNo 削除する社員番号
     * @return 削除に成功した場合true、失敗した場合false
     */
    public boolean delete(String empNo) {
        String sql = "DELETE FROM emp WHERE EMPNO = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, empNo);
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (SQLException e) {
            // 外部キー制約エラーの場合（この社員に関連する勤怠データがある場合）
            if (e.getSQLState().startsWith("23")) {
                System.err.println("この社員の勤怠データが存在するため削除できません: " + empNo);
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 社員番号の重複をチェックする
     * @param empNo チェックする社員番号
     * @return 既に存在する場合true、存在しない場合false
     */
    public boolean exists(String empNo) {
        return findByEmpNo(empNo) != null;
    }
}