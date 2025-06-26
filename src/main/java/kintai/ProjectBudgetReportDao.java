package kintai;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectBudgetReportDao {
    
    /**
     * 指定されたプロジェクトと月の参加メンバーの実績レポートを取得
     */
    public List<ProjectMemberReportBean> getProjectMemberReports(int projectId, String month) {
        List<ProjectMemberReportBean> reports = new ArrayList<>();
        
        String sql = "SELECT " +
                    "    e.EMPNO, " +
                    "    e.EMPNAME, " +
                    "    SUM(wa.WORK_HOURS) as TOTAL_HOURS, " +
                    "    s.HOURLY_RATE, " +
                    "    (SUM(wa.WORK_HOURS) * s.HOURLY_RATE) as ACTUAL_AMOUNT " +
                    "FROM work_alloc wa " +
                    "INNER JOIN emp e ON wa.EMPNO = e.EMPNO " +
                    "INNER JOIN salary s ON e.GRADENO = s.GRADENO " +
                    "WHERE wa.PROJECT_ID = ? " +
                    "    AND DATE_FORMAT(wa.WORK_DATE, '%Y-%m') = ? " +
                    "    AND s.EFFECTIVE_FROM <= CURDATE() " +
                    "    AND (s.EFFECTIVE_TO IS NULL OR s.EFFECTIVE_TO >= CURDATE()) " +
                    "GROUP BY e.EMPNO, e.EMPNAME, s.HOURLY_RATE " +
                    "ORDER BY e.EMPNO";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, projectId);
            stmt.setString(2, month);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProjectMemberReportBean report = new ProjectMemberReportBean();
                    report.setEmpNo(rs.getString("EMPNO"));
                    report.setEmpName(rs.getString("EMPNAME"));
                    report.setTotalHours(rs.getBigDecimal("TOTAL_HOURS"));
                    report.setHourlyRate(rs.getBigDecimal("HOURLY_RATE"));
                    report.setActualAmount(rs.getBigDecimal("ACTUAL_AMOUNT"));
                    
                    reports.add(report);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return reports;
    }
}