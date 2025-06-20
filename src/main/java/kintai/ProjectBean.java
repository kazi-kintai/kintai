package kintai;

import java.io.Serializable;

/**
 * プロジェクト情報（projectテーブルのレコード）を保持するJavaBean。
 * 新しいER図のprojectテーブルに準拠。
 */
public class ProjectBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int projectId;      // projectテーブルの「PROJECT_ID」列に対応
    private String projectName; // projectテーブルの「PROJECT_NAME」列に対応

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
    public ProjectBean(int projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
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
}
