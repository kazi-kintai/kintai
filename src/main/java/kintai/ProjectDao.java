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
        String sql = "SELECT PROJECT_ID, PROJECT_NAME, START_DATE, END_DATE, BUDGET_AMOUNT FROM project ORDER BY PROJECT_ID";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProjectBean project = new ProjectBean();
                project.setProjectId(rs.getInt("PROJECT_ID"));
                project.setProjectName(rs.getString("PROJECT_NAME"));
                project.setBudget(rs.getInt("BUDGET_AMOUNT"));
                
                // 日付フィールドの処理（nullの場合も考慮）
                java.sql.Date startDate = rs.getDate("START_DATE");
                if (startDate != null) {
                    project.setStartDate(startDate.toLocalDate());
                }
                
                java.sql.Date endDate = rs.getDate("END_DATE");
                if (endDate != null) {
                    project.setEndDate(endDate.toLocalDate());
                }
                
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
    public ProjectBean findByProjectId(int projectId) {
        ProjectBean project = null;
        String sql = "SELECT PROJECT_ID, PROJECT_NAME, START_DATE, END_DATE, BUDGET_AMOUNT FROM project WHERE PROJECT_ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    project = new ProjectBean();
                    project.setProjectId(rs.getInt("PROJECT_ID"));
                    project.setProjectName(rs.getString("PROJECT_NAME"));
                    project.setBudget(rs.getInt("BUDGET_AMOUNT"));
                    
                    // 日付フィールドの処理（nullの場合も考慮）
                    java.sql.Date startDate = rs.getDate("START_DATE");
                    if (startDate != null) {
                        project.setStartDate(startDate.toLocalDate());
                    }
                    
                    java.sql.Date endDate = rs.getDate("END_DATE");
                    if (endDate != null) {
                        project.setEndDate(endDate.toLocalDate());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }

    /**
     * 指定されたプロジェクトIDが既に存在するかチェックする
     * @param projectId チェックするプロジェクトID
     * @return 存在する場合true、存在しない場合false
     */
    public static boolean exists(int projectId) {
        boolean exists = false;
        String sql = "SELECT COUNT(*) FROM project WHERE PROJECT_ID = ?";
        DBAccess db = new DBAccess();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    /**
     * 新しいプロジェクトを追加する
     * @param project 追加するプロジェクト情報
     * @return 追加に成功した場合true、失敗した場合false
     */
    public boolean insert(ProjectBean project) {
        String sql = "INSERT INTO project (PROJECT_NAME, START_DATE, END_DATE, BUDGET_AMOUNT) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, project.getProjectName());
            ps.setObject(2, project.getStartDate());
            ps.setObject(3, project.getEndDate());
            ps.setInt(4, project.getBudget());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * プロジェクト情報を更新する
     * @param project 更新するプロジェクト情報
     * @return 更新に成功した場合true、失敗した場合false
     */
    public boolean update(ProjectBean project) {
        String sql = "UPDATE project SET PROJECT_NAME = ?, START_DATE = ?, END_DATE = ?, BUDGET_AMOUNT = ? WHERE PROJECT_ID = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, project.getProjectName());
            ps.setObject(2, project.getStartDate());
            ps.setObject(3, project.getEndDate());
            ps.setInt(4, project.getBudget());
            ps.setInt(5, project.getProjectId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * プロジェクトを削除する
     * @param projectId 削除するプロジェクトID
     * @return 削除に成功した場合true、失敗した場合false
     */
    public boolean delete(String projectId) {
        String sql = "DELETE FROM project WHERE PROJECT_ID = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(projectId));

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * プロジェクトIDでプロジェクト情報を検索する (findById用)
     * @param projectId プロジェクトID
     * @return プロジェクト情報。見つからない場合はnull
     */
    public ProjectBean findById(int projectId) {
        return findByProjectId(projectId);
    }
}
