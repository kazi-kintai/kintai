package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; // SQLException のインポートを追加

/**
 * empテーブルへのデータアクセスを担当するクラス (DAO)。
 * ※重要：このコードは、最新の「ER図」に準拠しています。
 */
public class UserDao {

    private DBAccess db = new DBAccess();

    /**
     * 従業員番号とパスワードを基にデータベースを検索し、ユーザー情報を取得する。
     * 新しいempテーブルの構造に合わせて修正。
     * @param empno ログイン画面で入力された従業員番号
     * @param password ログイン画面で入力されたパスワード
     * @return ユーザーが見つかった場合はUserBeanオブジェクト、見つからない、またはエラーの場合はnull
     */
    public UserBean findByLoginInfo(String empno, String password) {
        UserBean user = null;

        // --- SQL文 ---
        // テーブル名: emp
        // 検索列: EMPNO, PASS
        // 取得列: EMPNO, EMPNAME, DEPTNO, POSTNO, ROLEID, GRADENO, PASS
        // ※PASS列は認証のためだけに取得し、UserBeanには格納しない（セキュリティのため）。
        String sql = "SELECT EMPNO, EMPNAME, DEPTNO, POSTNO, ROLEID, GRADENO, PASS FROM emp WHERE EMPNO = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // プレースホルダに値をセット
            ps.setString(1, empno); // 1番目の「?」は EMPNO に対応

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // データベースから取得したパスワード
                    String storedPassword = rs.getString("PASS");

                    // パスワードの直接比較 (ハッシュ化なし)
                    // !!! 注意: この直接比較はテスト目的のみで、本番環境ではハッシュ化されたパスワードを比較してください !!!
                    if (storedPassword != null && storedPassword.equals(password)) {
                        user = new UserBean();
                        // --- ResultSetからUserBeanへのマッピング ---
                        user.setEmpno(rs.getString("EMPNO"));
                        user.setName(rs.getString("EMPNAME"));
                        user.setDeptNo(rs.getString("DEPTNO"));   
                        user.setPostNo(rs.getString("POSTNO"));   
                        user.setRoleId(rs.getInt("ROLEID"));      
                        user.setGradeNo(rs.getInt("GRADENO"));    
                    }
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace();
            // データベースエラーの場合
        } catch (Exception e) {
            e.printStackTrace();
            // その他の予期せぬエラーの場合
        }
        return user;
    }
}
