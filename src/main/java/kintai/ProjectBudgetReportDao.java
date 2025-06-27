package kintai;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectBudgetReportDao {
    
    private DBAccess db = new DBAccess();
    
    /**
     * 指定されたプロジェクトと月の参加メンバーの実績レポートを取得
     * 個人予算実績差異も計算して含める
     */
    public List<ProjectMemberReportBean> getProjectMemberReports(int projectId, String month) {
        List<ProjectMemberReportBean> reports = new ArrayList<>();
        
        // 当月の実績と全期間の工作時間を取得するSQL（初期表示では実績額を計算しない）
        String sql = "SELECT " +
                    "    e.EMPNO, " +
                    "    e.EMPNAME, " +
                    "    SUM(CASE WHEN DATE_FORMAT(wa.WORK_DATE, '%Y-%m') = ? THEN wa.WORK_HOURS ELSE 0 END) as MONTHLY_HOURS, " +
                    "    SUM(wa.WORK_HOURS) as TOTAL_PROJECT_HOURS, " +
                    "    COALESCE(s.HOURLY_RATE, 0) as HOURLY_RATE, " +
                    "    NULL as ACTUAL_AMOUNT, " +  // 初期表示では実績額を計算しない
                    "    NULL as PERSONAL_BUDGET " +  // 初期表示では個人予算を計算しない
                    "FROM work_alloc wa " +
                    "INNER JOIN emp e ON wa.EMPNO = e.EMPNO " +
                    "LEFT JOIN salary s ON e.GRADENO = s.GRADENO " +
                    "    AND s.EFFECTIVE_FROM <= CURDATE() " +
                    "    AND (s.EFFECTIVE_TO IS NULL OR s.EFFECTIVE_TO >= CURDATE()) " +
                    "WHERE wa.PROJECT_ID = ? " +
                    "GROUP BY e.EMPNO, e.EMPNAME, s.HOURLY_RATE " +
                    "HAVING SUM(CASE WHEN DATE_FORMAT(wa.WORK_DATE, '%Y-%m') = ? THEN wa.WORK_HOURS ELSE 0 END) > 0 " +
                    "ORDER BY e.EMPNO";
        
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, month);
            stmt.setString(2, month);
            stmt.setInt(3, projectId);
            stmt.setString(4, month);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProjectMemberReportBean report = new ProjectMemberReportBean();
                    report.setEmpNo(rs.getString("EMPNO"));
                    report.setEmpName(rs.getString("EMPNAME"));
                    report.setTotalHours(rs.getBigDecimal("MONTHLY_HOURS"));
                    report.setTotalProjectHours(rs.getBigDecimal("TOTAL_PROJECT_HOURS"));
                    report.setHourlyRate(rs.getBigDecimal("HOURLY_RATE"));
                    report.setActualAmount(null); // 初期表示では null
                    report.setPersonalBudget(null); // 初期表示では null
                    report.setPersonalBudgetVariance(null); // 初期表示では null
                    
                    reports.add(report);
                }
            }
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return reports;
    }
    
    /**
     * 既に保存された時給データを使用してプロジェクトメンバーレポートを取得（集計後表示用）
     */
    public List<ProjectMemberReportBean> getProjectMemberReportsWithCalculation(int projectId, String month) {
        List<ProjectMemberReportBean> reports = new ArrayList<>();
        
        // 保存された時給データを使用して実績額を計算するSQL
        String sql = "SELECT " +
                    "    e.EMPNO, " +
                    "    e.EMPNAME, " +
                    "    SUM(CASE WHEN DATE_FORMAT(wa.WORK_DATE, '%Y-%m') = ? THEN wa.WORK_HOURS ELSE 0 END) as MONTHLY_HOURS, " +
                    "    SUM(wa.WORK_HOURS) as TOTAL_PROJECT_HOURS, " +
                    "    COALESCE(s.HOURLY_RATE, 0) as HOURLY_RATE, " +
                    "    (SUM(CASE WHEN DATE_FORMAT(wa.WORK_DATE, '%Y-%m') = ? THEN wa.WORK_HOURS ELSE 0 END) * COALESCE(s.HOURLY_RATE, 0)) as ACTUAL_AMOUNT, " +
                    "    (SUM(wa.WORK_HOURS) * COALESCE(s.HOURLY_RATE, 0)) as PERSONAL_BUDGET " +
                    "FROM work_alloc wa " +
                    "INNER JOIN emp e ON wa.EMPNO = e.EMPNO " +
                    "LEFT JOIN salary s ON e.GRADENO = s.GRADENO " +
                    "    AND s.EFFECTIVE_FROM <= CURDATE() " +
                    "    AND (s.EFFECTIVE_TO IS NULL OR s.EFFECTIVE_TO >= CURDATE()) " +
                    "WHERE wa.PROJECT_ID = ? " +
                    "GROUP BY e.EMPNO, e.EMPNAME, s.HOURLY_RATE " +
                    "HAVING SUM(CASE WHEN DATE_FORMAT(wa.WORK_DATE, '%Y-%m') = ? THEN wa.WORK_HOURS ELSE 0 END) > 0 " +
                    "ORDER BY e.EMPNO";
        
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, month);
            stmt.setString(2, month);
            stmt.setInt(3, projectId);
            stmt.setString(4, month);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProjectMemberReportBean report = new ProjectMemberReportBean();
                    report.setEmpNo(rs.getString("EMPNO"));
                    report.setEmpName(rs.getString("EMPNAME"));
                    report.setTotalHours(rs.getBigDecimal("MONTHLY_HOURS"));
                    report.setTotalProjectHours(rs.getBigDecimal("TOTAL_PROJECT_HOURS"));
                    report.setHourlyRate(rs.getBigDecimal("HOURLY_RATE"));
                    report.setActualAmount(rs.getBigDecimal("ACTUAL_AMOUNT"));
                    report.setPersonalBudget(rs.getBigDecimal("PERSONAL_BUDGET"));
                    
                    // 個人予算実績差異を計算
                    BigDecimal personalBudget = rs.getBigDecimal("PERSONAL_BUDGET");
                    BigDecimal actualAmount = rs.getBigDecimal("ACTUAL_AMOUNT");
                    if (personalBudget != null && actualAmount != null) {
                        report.setPersonalBudgetVariance(personalBudget.subtract(actualAmount));
                    }
                    
                    reports.add(report);
                }
            }
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return reports;
    }
    
    /**
     * 指定された月とプロジェクトで保存済みの時給データがあるかチェック
     */
    public boolean hasCalculatedData(int projectId, String month) {
        String sql = "SELECT COUNT(*) as cnt FROM work_alloc wa " +
                    "INNER JOIN emp e ON wa.EMPNO = e.EMPNO " +
                    "INNER JOIN salary s ON e.GRADENO = s.GRADENO " +
                    "WHERE wa.PROJECT_ID = ? " +
                    "  AND DATE_FORMAT(wa.WORK_DATE, '%Y-%m') = ? " +
                    "  AND s.HOURLY_RATE > 0 " +
                    "  AND s.EFFECTIVE_FROM <= CURDATE() " +
                    "  AND (s.EFFECTIVE_TO IS NULL OR s.EFFECTIVE_TO >= CURDATE())";
        
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, projectId);
            stmt.setString(2, month);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 指定された従業員の時給を更新
     */
    public boolean updateHourlyRate(String empNo, BigDecimal hourlyRate) {
        // 従業員の等級番号を取得
        String getGradeNoSql = "SELECT GRADENO FROM emp WHERE EMPNO = ?";
        
        // 時給更新SQL（現在有効な給与レコードの時給を更新）
        String updateSql = "UPDATE salary SET HOURLY_RATE = ? " +
                          "WHERE GRADENO = (SELECT GRADENO FROM emp WHERE EMPNO = ?) " +
                          "  AND EFFECTIVE_FROM <= CURDATE() " +
                          "  AND (EFFECTIVE_TO IS NULL OR EFFECTIVE_TO >= CURDATE())";
        
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 等級番号を確認
                int gradeNo = -1;
                try (PreparedStatement checkStmt = conn.prepareStatement(getGradeNoSql)) {
                    checkStmt.setString(1, empNo);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            gradeNo = rs.getInt("GRADENO");
                        } else {
                            return false; // 従業員が見つからない
                        }
                    }
                }
                
                // 時給を更新
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setBigDecimal(1, hourlyRate);
                    updateStmt.setString(2, empNo);
                    
                    int updateCount = updateStmt.executeUpdate();
                    
                    if (updateCount > 0) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}