package kintai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao {
	private DBAccess db = new DBAccess();

    /**
     * すべてのプロジェクト情報を取得する
     * @return プロジェクト情報のリスト
     */
    public List<ProjectBean> findAll() {
        List<ProjectBean> projectmanageList = new ArrayList<>();
        String sql = "SELECT PROJECT_ID, PROJECT_NAME, BUDGET_AMOUNT,START_DATE, END_DATE FROM project ORDER BY PROJECT_ID";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProjectBean project = new ProjectBean();
                project.setProjectId(rs.getInt("PROJECT_ID")); // PROJECTNOからPROJECT_IDへ変更
                project.setProjectName(rs.getString("PROJECT_NAME"));
                project.setProjectBudget(rs.getInt("BUDGET_AMOUNT"));
//                project.setStartDate(rs.getDate("START_DATE"));
//                project.setEndDate(rs.getDate("END_DATE"));
                
                Date start = rs.getDate("START_DATE");
                Date end = rs.getDate("END_DATE");
                project.setStartDate(start != null ? start.toLocalDate() : null);
                project.setEndDate(end != null ? end.toLocalDate() : null);
                
                projectmanageList.add(project);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return projectmanageList;
    }

    /**
     * 新しいプロジェクトを追加する
     * @param projectNo 追加するプロジェクト情報
     * @return 追加に成功した場合true、失敗した場合false
     */
    public boolean insert(ProjectBean projectNo) {
        // 新しいER図のprojectテーブルの列に合わせてSQLを修正
        String sql = "INSERT INTO project (PROJECT_NAME, BUDGET_AMOUNT,START_DATE, END_DATE) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, projectNo.getProjectName());
            if (projectNo.getProjectBudget() != null) {
                ps.setInt(2, projectNo.getProjectBudget());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }//            ps.setDate(4, projectNo.getStartDate());
//            ps.setDate(5, projectNo.getEndDate());
//            
//            // EMPDATEはNULL許容
//            if (projectNo.getStartDate() != null) {
//                ps.setDate(5, Date.valueOf(projectNo.getStartDate()));
//            } else {
//                ps.setNull(5, java.sql.Types.DATE); // nullの場合はSQLのDATE型でnullをセット
//            }
            
            if (projectNo.getStartDate() != null) {
                ps.setDate(3,java.sql.Date.valueOf(projectNo.getStartDate()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            if (projectNo.getEndDate() != null) {
                ps.setDate(4, java.sql.Date.valueOf(projectNo.getEndDate()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (SQLException e) {
            // 主キー重複エラーの場合
            if (e.getSQLState().equals("23000")) {
                System.err.println("プロジェクトが既に存在します: " + projectNo.getProjectId());
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * プロジェクト情報を更新する
     * 新しいER図のempテーブルの構造に合わせて修正。
     * @param updateProject 更新するプロジェクト情報
     * @return 更新に成功した場合true、失敗した場合false
     */
    public boolean update(ProjectBean updateProject) {
        String sql = "UPDATE project SET PROJECT_NAME=?, BUDGET_AMOUNT=?, START_DATE=?, END_DATE=? WHERE PROJECT_ID = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, updateProject.getProjectName());
            if (updateProject.getProjectBudget() != null) {
                ps.setInt(2, updateProject.getProjectBudget());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }//            ps.setDate(4, updateProject.getStartDate());
//            ps.setDate(5, updateProject.getEndDate());
            
            
//            // EMPDATEはNULL許容
//            if (updateProject.getStartDate() != null) {
//                ps.setDate(5, Date.valueOf(updateProject.getStartDate()));
//            } else {
//                ps.setNull(5, java.sql.Types.DATE); // nullの場合はSQLのDATE型でnullをセット
//            }
            
//            ps.setString(9, updateProject.getEmpNo());
            
            if (updateProject.getStartDate() != null) {
                ps.setDate(3,java.sql.Date.valueOf(updateProject.getStartDate()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            if (updateProject.getEndDate() != null) {
                ps.setDate(4, java.sql.Date.valueOf(updateProject.getEndDate()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            
            ps.setInt(5, updateProject.getProjectId());
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * プロジェクトを削除する
     * @param projectId 削除する社員番号
     * @return 削除に成功した場合true、失敗した場合false
     */
    public boolean delete(String projectNo) {
        String sql = "DELETE FROM project WHERE PROJECT_ID = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, projectNo);
            
            int count = ps.executeUpdate();
            return count > 0;
            
        } catch (SQLException e) {
            // 外部キー制約エラーの場合（このプロジェクトに関連する勤怠データなどがある場合）
            if (e.getSQLState().startsWith("23")) {
                System.err.println("このプロジェクトに関連するデータが存在するため削除できません: " + projectNo);
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * プロジェクト番号の重複をチェックする
     * @param projectId チェックするプロジェクトID
     * @return 既に存在する場合true、存在しない場合false
     */
//    public boolean exists(String projectNo) {
//        return findByProjectId (String projectNo) != null;
//    }
    
    public ProjectBean findByProjectId(int projectId) {
        ProjectBean project = null;
        String sql = "SELECT PROJECT_ID, PROJECT_NAME, BUDGET_AMOUNT, START_DATE, END_DATE FROM project WHERE PROJECT_ID = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    project = new ProjectBean();
                    project.setProjectId(rs.getInt("PROJECT_ID"));
                    project.setProjectName(rs.getString("PROJECT_NAME"));
                    project.setProjectBudget(rs.getInt("BUDGET_AMOUNT"));
//                    project.setStartDate(rs.getDate("START_DATE"));
//                    project.setEndDate(rs.getDate("END_DATE"));
                }
                
                Date start = rs.getDate("START_DATE");
                Date end = rs.getDate("END_DATE");
                project.setStartDate(start != null ? start.toLocalDate() : null);
                project.setEndDate(end != null ? end.toLocalDate() : null);
            }

            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return project;
    }

    public boolean exists(String projectId) {
        try {
            int id = Integer.parseInt(projectId);
            return findByProjectId(id) != null;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
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

