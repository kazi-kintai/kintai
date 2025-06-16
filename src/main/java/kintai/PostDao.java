package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * postテーブルへのデータアクセスを担当するクラス (DAO)。
 * 役職情報の検索を行う。
 */
public class PostDao {
    
    private DBAccess db = new DBAccess();
    
    /**
     * すべての役職情報を取得する
     * @return 役職情報のリスト
     */
    public List<PostBean> findAll() {
        List<PostBean> postList = new ArrayList<>();
        String sql = "SELECT POSTNO, POSTNAME FROM post ORDER BY POSTNO";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                PostBean post = new PostBean();
                post.setPostNo(rs.getString("POSTNO"));
                post.setPostName(rs.getString("POSTNAME"));
                postList.add(post);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return postList;
    }
}