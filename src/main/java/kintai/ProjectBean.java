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
    private LocalDate startDate; // プロジェクト開始日
    private LocalDate endDate;   // プロジェクト終了日
    private int budget;         // プロジェクト予算

    /**
     * デフォルトコンストラクタ
     */
    public ProjectBean() {
    }

    /**
     * 全フィールドを初期化するコンストラクタ
     * @param projectId プロジェクトID
     * @param projectName プロジェクト名
     * @param startDate 開始日
     * @param endDate 終了日
     * @param budget 予算
     */
    public ProjectBean(int projectId, String projectName, LocalDate startDate, LocalDate endDate, int budget) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
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

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }
}
