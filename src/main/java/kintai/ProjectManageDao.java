package kintai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProjectManageDao {
	private DBAccess db = new DBAccess();

    /**
     * すべてのプロジェクト情報を取得する
     * @return プロジェクト情報のリスト
     */
    public List<ProjectManageBean> findAll() {
        List<ProjectManageBean> projectmanageList = new ArrayList<>();
        String sql = "SELECT PROJECT_ID, PROJECT_NAME BUDGET_AMOUNT FROM project ORDER BY PROJECT_ID";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProjectManageBean project = new ProjectManageBean();
                project.setProjectManageId(rs.getInt("PROJECT_ID")); // PROJECTNOからPROJECT_IDへ変更
                project.setProjectManageName(rs.getString("PROJECT_NAME"));
                project.setProjectBudget(rs.getInt("BUDGET_AMOUNT"));
                
                projectmanageList.add(project);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return projectmanageList;
    }

    /**
     * プロジェクトIDでプロジェクト情報を検索する
     * @param projectId プロジェクトID
     * @return プロジェクト情報。見つからない場合はnull
     */
//    public ProjectManageBean findByProjectId(int projectId) { // projectNoからprojectIdへ変更
//        ProjectBean project = null;
//        String sql = "SELECT PROJECT_ID, PROJECT_NAME FROM project WHERE PROJECT_ID = ?"; // PROJECTNOからPROJECT_IDへ変更
//
//        try (Connection conn = db.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setInt(1, projectId); // projectNoからprojectIdへ変更
//
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    project = new ProjectBean();
//                    project.setProjectId(rs.getInt("PROJECT_ID")); // PROJECTNOからPROJECT_IDへ変更
//                    project.setProjectName(rs.getString("PROJECT_NAME"));
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return project;
//    }
}
