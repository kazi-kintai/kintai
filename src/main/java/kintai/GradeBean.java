package kintai;

import java.io.Serializable;

/**
 * 等級情報（gradeテーブルのレコード）を保持するJavaBean。
 * 従業員の給与等級を定義します。
 */
public class GradeBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int gradeNo;        // gradeテーブルの「GRADENO」列に対応
    private String gradeName;   // gradeテーブルの「GRADENAME」列に対応

    /**
     * デフォルトコンストラクタ
     */
    public GradeBean() {
    }

    /**
     * 全フィールドを初期化するコンストラクタ
     * @param gradeNo 等級番号
     * @param gradeName 等級名
     */
    public GradeBean(int gradeNo, String gradeName) {
        this.gradeNo = gradeNo;
        this.gradeName = gradeName;
    }

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

    public int getGradeNo() {
        return gradeNo;
    }

    public void setGradeNo(int gradeNo) {
        this.gradeNo = gradeNo;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    // JSPとの互換性のための追加メソッド
    /**
     * 等級IDの別名ゲッター（gradeId形式）
     * JSPでの使用を考慮した後方互換性メソッド
     * @return 等級番号
     */
    public int getGradeId() {
        return gradeNo;
    }

    /**
     * 等級IDの別名セッター（gradeId形式）
     * JSPでの使用を考慮した後方互換性メソッド
     * @param gradeId 等級番号
     */
    public void setGradeId(int gradeId) {
        this.gradeNo = gradeId;
    }
}
