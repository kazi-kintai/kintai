package kintai;

import java.io.Serializable;

/**
 * 業務情報（gyomuテーブルのレコード）を保持するJavaBean。
 */
public class GyomuBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gyomuNo;   // gyomuテーブルの「GYOMUNO」列に対応
    private String gyomuName; // gyomuテーブルの「GYOMUNAME」列に対応

    /**
     * デフォルトコンストラクタ
     */
    public GyomuBean() {
    }

    /**
     * 全フィールドを初期化するコンストラクタ
     * @param gyomuNo 業務番号
     * @param gyomuName 業務名
     */
    public GyomuBean(String gyomuNo, String gyomuName) {
        this.gyomuNo = gyomuNo;
        this.gyomuName = gyomuName;
    }

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

    public String getGyomuNo() {
        return gyomuNo;
    }

    public void setGyomuNo(String gyomuNo) {
        this.gyomuNo = gyomuNo;
    }

    public String getGyomuName() {
        return gyomuName;
    }

    public void setGyomuName(String gyomuName) {
        this.gyomuName = gyomuName;
    }
}
