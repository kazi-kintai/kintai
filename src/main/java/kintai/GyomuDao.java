package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * gyomuテーブルへのデータアクセスを担当するクラス (DAO)。
 * 業務情報の検索を行う。
 */
public class GyomuDao {

    private DBAccess db = new DBAccess();

    /**
     * すべての業務情報を取得する
     * @return 業務情報のリスト
     */
    public List<GyomuBean> findAll() {
        List<GyomuBean> gyomuList = new ArrayList<>();
        String sql = "SELECT GYOMUNO, GYOMUNAME FROM gyomu ORDER BY GYOMUNO";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                GyomuBean gyomu = new GyomuBean();
                gyomu.setGyomuNo(rs.getString("GYOMUNO"));
                gyomu.setGyomuName(rs.getString("GYOMUNAME"));
                gyomuList.add(gyomu);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return gyomuList;
    }

    /**
     * 業務番号で業務情報を検索する
     * @param gyomuNo 業務番号
     * @return 業務情報。見つからない場合はnull
     */
    public GyomuBean findByGyomuNo(String gyomuNo) {
        GyomuBean gyomu = null;
        String sql = "SELECT GYOMUNO, GYOMUNAME FROM gyomu WHERE GYOMUNO = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, gyomuNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    gyomu = new GyomuBean();
                    gyomu.setGyomuNo(rs.getString("GYOMUNO"));
                    gyomu.setGyomuName(rs.getString("GYOMUNAME"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return gyomu;
    }
}
