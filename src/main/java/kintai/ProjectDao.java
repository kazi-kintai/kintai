package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * projectテーブルへのデータアクセスを担当するクラス (DAO)。
 * プロジェクト情報の検索を行う。
 * 新しいER図のprojectテーブルに準拠。
 */
public class ProjectDao {

    private DBAccess db = new DBAccess();

    /**
     * すべてのプロジェクト情報を取得する
     * @return プロジェクト情報のリスト
     */
    public List<ProjectBean> findAll() {
        List<ProjectBean> projectList = new ArrayList<>();
        String sql = "SELECT PROJECT_ID, PROJECT_NAME FROM project ORDER BY PROJECT_ID";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProjectBean project = new ProjectBean();
                project.setProjectId(rs.getInt("PROJECT_ID")); // PROJECTNOからPROJECT_IDへ変更
                project.setProjectName(rs.getString("PROJECT_NAME"));
                projectList.add(project);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return projectList;
    }

    /**
     * プロジェクトIDでプロジェクト情報を検索する
     * @param projectId プロジェクトID
     * @return プロジェクト情報。見つからない場合はnull
     */
    public ProjectBean findByProjectId(int projectId) { // projectNoからprojectIdへ変更
        ProjectBean project = null;
        String sql = "SELECT PROJECT_ID, PROJECT_NAME FROM project WHERE PROJECT_ID = ?"; // PROJECTNOからPROJECT_IDへ変更

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId); // projectNoからprojectIdへ変更

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    project = new ProjectBean();
                    project.setProjectId(rs.getInt("PROJECT_ID")); // PROJECTNOからPROJECT_IDへ変更
                    project.setProjectName(rs.getString("PROJECT_NAME"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }
}
