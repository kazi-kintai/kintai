package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 等級情報（gradeテーブルのレコード）へのデータアクセスを担当するクラス (DAO)。
 * 等級情報の検索を行う。
 */
public class GradeDao {

    private DBAccess db = new DBAccess();

    /**
     * すべての等級情報を取得する
     * @return 等級情報のリスト
     */
    public List<GradeBean> findAll() {
        List<GradeBean> gradeList = new ArrayList<>();
        String sql = "SELECT GRADENO, GRADENAME FROM grade ORDER BY GRADENO";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                GradeBean grade = new GradeBean();
                grade.setGradeNo(rs.getInt("GRADENO"));
                grade.setGradeName(rs.getString("GRADENAME"));
                gradeList.add(grade);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return gradeList;
    }

    /**
     * 等級番号で等級情報を検索する
     * @param gradeNo 等級番号
     * @return 等級情報。見つからない場合はnull
     */
    public GradeBean findByGradeNo(int gradeNo) {
        GradeBean grade = null;
        String sql = "SELECT GRADENO, GRADENAME FROM grade WHERE GRADENO = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, gradeNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    grade = new GradeBean();
                    grade.setGradeNo(rs.getInt("GRADENO"));
                    grade.setGradeName(rs.getString("GRADENAME"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return grade;
    }
}
