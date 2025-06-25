package kintai;

import java.sql.Connection;
import java.sql.Date; // java.sql.Date をインポート
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * empテーブルへのデータアクセスを担当するクラス (DAO)。
 * 社員情報の検索、追加、更新、削除を行う。
 * 新しいER図のempテーブルの構造に合わせて修正。
 */
public class EmpDao {
    
    private DBAccess db = new DBAccess();
    
    /**
     * すべての社員情報を取得する（部署名、役職名、ロール名、等級名も含む）
     * @return 社員情報のリスト
     */
    public List<EmpBean> findAll() {
        List<EmpBean> empList = new ArrayList<>();
        // 新しいER図のempテーブルの列と結合するテーブルに合わせてSQLを修正
        String sql = "SELECT e.EMPNO, e.EMPNAME, e.DEPTNO, e.POSTNO, e.ROLEID, e.GRADENO, " +
                     "e.PASS, e.MAIL, e.EMPDATE, " +
                     "d.DEPTNAME, p.POSTNAME, r.ROLENAME, g.GRADENAME " +
                     "FROM emp e " +
                     "LEFT JOIN dept d ON e.DEPTNO = d.DEPTNO " +
                     "LEFT JOIN post p ON e.POSTNO = p.POSTNO " +
                     "LEFT JOIN role r ON e.ROLEID = r.ROLEID " +
                     "LEFT JOIN grade g ON e.GRADENO = g.GRADENO " +
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
                emp.setRoleId(rs.getInt("ROLEID"));
                emp.setGradeNo(rs.getInt("GRADENO"));
                emp.setPass(rs.getString("PASS"));
                emp.setMail(rs.getString("MAIL"));
                
                // EMPDATEはNULLの場合もあるので、nullチェック
                Date empDateSql = rs.getDate("EMPDATE");
                if (empDateSql != null) {
                    emp.setEmpDate(empDateSql.toLocalDate());
                } else {
                    emp.setEmpDate(null);
                }
                
                emp.setDeptName(rs.getString("DEPTNAME"));
                emp.setPostName(rs.getString("POSTNAME"));
                emp.setRoleName(rs.getString("ROLENAME")); // 新規追加
                emp.setGradeName(rs.getString("GRADENAME")); // 新規追加
                empList.add(emp);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return empList;
    }
    
    /**
     * 社員番号で社員情報を検索する
     * 新しいER図のempテーブルの構造に合わせて修正。
     * @param empNo 社員番号
     * @return 社員情報。見つからない場合はnull
     */
    public EmpBean findByEmpNo(String empNo) {
        EmpBean emp = null;
        String sql = "SELECT e.EMPNO, e.EMPNAME, e.DEPTNO, e.POSTNO, e.ROLEID, e.GRADENO, " +
                     "e.PASS, e.MAIL, e.EMPDATE, " +
                     "d.DEPTNAME, p.POSTNAME, r.ROLENAME, g.GRADENAME " +
                     "FROM emp e " +
                     "LEFT JOIN dept d ON e.DEPTNO = d.DEPTNO " +
                     "LEFT JOIN post p ON e.POSTNO = p.POSTNO " +
                     "LEFT JOIN role r ON e.ROLEID = r.ROLEID " +
                     "LEFT JOIN grade g ON e.GRADENO = g.GRADENO " +
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
                    emp.setRoleId(rs.getInt("ROLEID"));
                    emp.setGradeNo(rs.getInt("GRADENO"));
                    emp.setPass(rs.getString("PASS"));
                    emp.setMail(rs.getString("MAIL"));
                    
                    // EMPDATEはNULLの場合もあるので、nullチェック
                    Date empDateSql = rs.getDate("EMPDATE");
                    if (empDateSql != null) {
                        emp.setEmpDate(empDateSql.toLocalDate());
                    } else {
                        emp.setEmpDate(null);
                    }

                    emp.setDeptName(rs.getString("DEPTNAME"));
                    emp.setPostName(rs.getString("POSTNAME"));
                    emp.setRoleName(rs.getString("ROLENAME")); // 新規追加
                    emp.setGradeName(rs.getString("GRADENAME")); // 新規追加
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emp;
    }
    
    /**
     * 新しい社員を追加する
     * 新しいER図のempテーブルの構造に合わせて修正。
     * @param emp 追加する社員情報
     * @return 追加に成功した場合true、失敗した場合false
     */
    public boolean insert(EmpBean emp) {
        // 新しいER図のempテーブルの列に合わせてSQLを修正
        String sql = "INSERT INTO emp (EMPNO, EMPNAME, DEPTNO, POSTNO, ROLEID, GRADENO, PASS, MAIL, EMPDATE) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, emp.getEmpNo());
            ps.setString(2, emp.getEmpName());
            ps.setString(3, emp.getDeptNo());
            ps.setString(4, emp.getPostNo());
            ps.setInt(5, emp.getRoleId());
            ps.setInt(6, emp.getGradeNo());
            ps.setString(7, emp.getPass());
            ps.setString(8, emp.getMail());
            
            // EMPDATEはNULL許容
            if (emp.getEmpDate() != null) {
                ps.setDate(9, Date.valueOf(emp.getEmpDate()));
            } else {
                ps.setNull(9, java.sql.Types.DATE); // nullの場合はSQLのDATE型でnullをセット
            }
            
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
     * 新しいER図のempテーブルの構造に合わせて修正。
     * @param emp 更新する社員情報
     * @return 更新に成功した場合true、失敗した場合false
     */
    public boolean update(EmpBean emp) {
        String sql = "UPDATE emp SET EMPNAME = ?, DEPTNO = ?, POSTNO = ?, ROLEID = ?, GRADENO = ?, PASS = ?, MAIL = ?, EMPDATE = ? " +
                     "WHERE EMPNO = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, emp.getEmpName());
            ps.setString(2, emp.getDeptNo());
            ps.setString(3, emp.getPostNo());
            ps.setInt(4, emp.getRoleId());
            ps.setInt(5, emp.getGradeNo());
            ps.setString(6, emp.getPass());
            ps.setString(7, emp.getMail());
            
            // EMPDATEはNULL許容
            if (emp.getEmpDate() != null) {
                ps.setDate(8, Date.valueOf(emp.getEmpDate()));
            } else {
                ps.setNull(8, java.sql.Types.DATE); // nullの場合はSQLのDATE型でnullをセット
            }
            
            ps.setString(9, emp.getEmpNo());
            
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
            // 外部キー制約エラーの場合（この社員に関連する勤怠データなどがある場合）
            if (e.getSQLState().startsWith("23")) {
                System.err.println("この社員に関連するデータが存在するため削除できません: " + empNo);
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
    
    /**
     * 個人レポート用に社員の詳細情報を取得する
     * @param empNo 社員番号
     * @return PersonalReportBean用の社員情報、見つからない場合はnull
     */
    public PersonalReportBean getEmployeeForReport(String empNo) {
        String sql = "SELECT e.EMPNO, e.EMPNAME, e.DEPTNO, e.POSTNO, " +
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
                    PersonalReportBean report = new PersonalReportBean();
                    report.setEmpno(rs.getString("EMPNO"));
                    report.setEmpName(rs.getString("EMPNAME"));
                    report.setDeptName(rs.getString("DEPTNAME") != null ? rs.getString("DEPTNAME") : "未設定");
                    report.setPostName(rs.getString("POSTNAME") != null ? rs.getString("POSTNAME") : "未設定");
                    return report;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 部署・役職フィルターに基づいて従業員一覧を取得する
     * @param deptNo 部署番号（nullまたは空文字の場合は全部署）
     * @param postNo 役職番号（nullまたは空文字の場合は全役職）
     * @return フィルター条件に一致する従業員のリスト
     */
    public List<EmpBean> findByFilters(String deptNo, String postNo) {
        List<EmpBean> empList = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT e.EMPNO, e.EMPNAME, e.DEPTNO, e.POSTNO, e.ROLEID, e.GRADENO, " +
            "e.PASS, e.MAIL, e.EMPDATE, " +
            "d.DEPTNAME, p.POSTNAME, r.ROLENAME, g.GRADENAME " +
            "FROM emp e " +
            "LEFT JOIN dept d ON e.DEPTNO = d.DEPTNO " +
            "LEFT JOIN post p ON e.POSTNO = p.POSTNO " +
            "LEFT JOIN role r ON e.ROLEID = r.ROLEID " +
            "LEFT JOIN grade g ON e.GRADENO = g.GRADENO " +
            "WHERE 1=1 "
        );
        
        // フィルター条件を動的に追加
        List<String> params = new ArrayList<>();
        if (deptNo != null && !deptNo.trim().isEmpty()) {
            sql.append("AND e.DEPTNO = ? ");
            params.add(deptNo);
        }
        if (postNo != null && !postNo.trim().isEmpty()) {
            sql.append("AND e.POSTNO = ? ");
            params.add(postNo);
        }
        
        sql.append("ORDER BY e.EMPNO");
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            // パラメータを設定
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmpBean emp = new EmpBean();
                    emp.setEmpNo(rs.getString("EMPNO"));
                    emp.setEmpName(rs.getString("EMPNAME"));
                    emp.setDeptNo(rs.getString("DEPTNO"));
                    emp.setPostNo(rs.getString("POSTNO"));
                    emp.setRoleId(rs.getInt("ROLEID"));
                    emp.setGradeNo(rs.getInt("GRADENO"));
                    emp.setPass(rs.getString("PASS"));
                    emp.setMail(rs.getString("MAIL"));
                    
                    // 日付のnullチェック
                    Date empDate = rs.getDate("EMPDATE");
                    if (empDate != null) {
                        emp.setEmpDate(empDate.toLocalDate());
                    }
                    
                    emp.setDeptName(rs.getString("DEPTNAME"));
                    emp.setPostName(rs.getString("POSTNAME"));
                    emp.setRoleName(rs.getString("ROLENAME"));
                    emp.setGradeName(rs.getString("GRADENAME"));
                    
                    empList.add(emp);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return empList;
    }
}
