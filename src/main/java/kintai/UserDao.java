package kintai; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * empテーブルへのデータアクセスを担当するクラス (DAO)。
 * ※重要：このコードは、「テーブル定義書」に準拠しています。
 */
public class UserDao {

    private DBAccess db = new DBAccess();

    /**
     * 従業員番号とパスワードを基にデータベースを検索し、ユーザー情報を取得する。
     * @param empno ログイン画面で入力された従業員番号
     * @param password ログイン画面で入力されたパスワード
     * @return ユーザーが見つかった場合はUserBeanオブジェクト、見つからない、またはエラーの場合はnull
     */
    public UserBean findByLoginInfo(String empno, String password) {
        UserBean user = null;
        
        // --- SQL文 ---
        // テーブル名: emp
        // 検索列: EMPNO, PASS
        // 取得列: EMPNO, EMPNAME, DEPTNO, POSTINO
        String sql = "SELECT EMPNO, EMPNAME, DEPTNO, POSTNO FROM emp WHERE EMPNO = ? AND PASS = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // プレースホルダに値をセット
            ps.setString(1, empno);    // 1番目の「?」は EMPNO に対応
            ps.setString(2, password); // 2番目の「?」は PASS に対応

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new UserBean();
                    
                    // --- ResultSetからUserBeanへのマッピング ---
                    user.setEmpno(rs.getString("EMPNO"));
                    user.setName(rs.getString("EMPNAME"));
                    user.setDeptId(rs.getString("DEPTNO"));
                    user.setPostId(rs.getString("POSTNO"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
 //           return null;
        }
        return user;
    }
}
