package kintai;

import java.io.Serializable;

/**
 * プロジェクト情報（projectテーブルのレコード）を保持するJavaBean。
 * 新しいER図のprojectテーブルに準拠。
 */
public class ProjectManageBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int projectId;      // projectテーブルの「PROJECT_ID」列に対応
    private String projectName; // projectテーブルの「PROJECT_NAME」列に対応
	private int projectBudget;

    /**
     * デフォルトコンストラクタ
     */
    public ProjectManageBean() {
    }

    /**
     * 全フィールドを初期化するコンストラクタ
     * @param projectId プロジェクトID
     * @param projectName プロジェクト名
     */
    public ProjectManageBean(int projectId, String projectName, int projectBudget) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectBudget = projectBudget;
    }

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) --

    public int getProjectManageId() {
        return projectId;
    }

    public void setProjectManageId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectManageName() {
        return projectName;
    }

    public void setProjectManageName(String projectName) {
        this.projectName = projectName;
    }
    public int getProjectBudget() {
        return projectBudget;
    }

    public void setProjectBudget(int projectBudget) {
        this.projectBudget = projectBudget;
    }
}
