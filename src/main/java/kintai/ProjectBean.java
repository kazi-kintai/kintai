package kintai;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * プロジェクト情報（projectテーブルのレコード）を保持するJavaBean。
 * 新しいER図のprojectテーブルに準拠。
 */
public class ProjectBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int projectId;      // projectテーブルの「PROJECT_ID」列に対応
    private String projectName; // projectテーブルの「PROJECT_NAME」列に対応
	private int projectBudget;
	private LocalDate startDate;
	private LocalDate endDate;

    /**
     * デフォルトコンストラクタ
     */
    public ProjectBean() {
    }

    /**
     * 全フィールドを初期化するコンストラクタ
     * @param projectId プロジェクトID
     * @param projectName プロジェクト名
     */
    public ProjectBean(int projectId, String projectName, int projectBudget, LocalDate startDate, LocalDate endDate) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectBudget = projectBudget;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) --

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public int getProjectBudget() {
        return projectBudget;
    }

    public void setProjectBudget(int projectBudget) {
        this.projectBudget = projectBudget;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
}
