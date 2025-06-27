package kintai;

import java.io.Serializable;
import java.sql.Time;

/**
 * 休憩情報（breakテーブルのレコード）を保持するJavaBean。
 */
public class BreakBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int breakId;         // breakテーブルの「BREAK_ID」列に対応
    private int kintaiRecId;     // breakテーブルの「KINTAI_REC_ID」列に対応
    private Time breakStart;     // breakテーブルの「BREAK_START」列に対応
    private Time breakEnd;       // breakテーブルの「BREAK_END」列に対応

    // --- 以下、各フィールドのアクセサメソッド (getter/setter) ---

    public int getBreakId() {
        return breakId;
    }

    public void setBreakId(int breakId) {
        this.breakId = breakId;
    }

    public int getKintaiRecId() {
        return kintaiRecId;
    }

    public void setKintaiRecId(int kintaiRecId) {
        this.kintaiRecId = kintaiRecId;
    }

    // Backward compatibility
    public int getRecId() {
        return kintaiRecId;
    }

    public void setRecId(int recId) {
        this.kintaiRecId = recId;
    }

    public Time getBreakStart() {
        return breakStart;
    }

    public void setBreakStart(Time breakStart) {
        this.breakStart = breakStart;
    }

    public Time getBreakEnd() {
        return breakEnd;
    }

    public void setBreakEnd(Time breakEnd) {
        this.breakEnd = breakEnd;
    }

    // JSPとの互換性のための追加メソッド
    /**
     * 休憩ID取得メソッド（id形式）
     * JSPでの使用を考慮した後方互換性メソッド
     * @return 休憩ID
     */
    public int getId() {
        return breakId;
    }

    /**
     * 休憩ID設定メソッド（id形式）
     * JSPでの使用を考慮した後方互換性メソッド
     * @param id 休憩ID
     */
    public void setId(int id) {
        this.breakId = id;
    }
}