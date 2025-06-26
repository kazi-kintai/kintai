-- ---------------------------------------------------
-- 新規テーブルの作成（最新ER図に基づく）
-- ---------------------------------------------------

-- dept テーブル
CREATE TABLE IF NOT EXISTS dept (
    DEPTNO VARCHAR(5) NOT NULL PRIMARY KEY COMMENT '部署番号',
    DEPTNAME VARCHAR(50) NOT NULL COMMENT '部署名'
);

-- post テーブル
CREATE TABLE IF NOT EXISTS post (
    POSTNO VARCHAR(5) NOT NULL PRIMARY KEY COMMENT '役職番号',
    POSTNAME VARCHAR(50) NOT NULL COMMENT '役職名'
);

-- role テーブル
CREATE TABLE IF NOT EXISTS role (
    ROLEID INT NOT NULL PRIMARY KEY COMMENT 'ロールID',
    ROLENAME VARCHAR(50) NOT NULL COMMENT 'ロール名'
);

-- grade テーブル
CREATE TABLE IF NOT EXISTS grade (
    GRADENO INT NOT NULL PRIMARY KEY COMMENT '等級番号',
    GRADENAME VARCHAR(50) NOT NULL COMMENT '等級名'
);

-- emp テーブル
CREATE TABLE IF NOT EXISTS emp (
    EMPNO VARCHAR(10) NOT NULL PRIMARY KEY COMMENT '従業員番号',
    EMPNAME VARCHAR(50) NOT NULL COMMENT '従業員名',
    DEPTNO VARCHAR(5) COMMENT '部署番号',
    POSTNO VARCHAR(5) COMMENT '役職番号',
    ROLEID INT COMMENT 'ロールID',
    GRADENO INT COMMENT '等級番号',
    PASS VARCHAR(64) NOT NULL COMMENT 'パスワード（ハッシュ化を想定し長めに）',
    MAIL VARCHAR(100) COMMENT 'メールアドレス',
    EMPDATE DATE COMMENT '入社年月日',
    FOREIGN KEY (DEPTNO) REFERENCES dept(DEPTNO) ON UPDATE CASCADE ON DELETE SET NULL,
    FOREIGN KEY (POSTNO) REFERENCES post(POSTNO) ON UPDATE CASCADE ON DELETE SET NULL,
    FOREIGN KEY (ROLEID) REFERENCES role(ROLEID) ON UPDATE CASCADE ON DELETE SET NULL,
    FOREIGN KEY (GRADENO) REFERENCES grade(GRADENO) ON UPDATE CASCADE ON DELETE SET NULL
);

-- salary テーブル
CREATE TABLE IF NOT EXISTS salary (
    GRADE_SALARY_ID INT AUTO_INCREMENT PRIMARY KEY COMMENT '等級給与ID',
    GRADENO INT NOT NULL COMMENT '等級番号',
    EFFECTIVE_FROM DATE NOT NULL COMMENT '適用開始日',
    EFFECTIVE_TO DATE COMMENT '適用終了日',
    MIN_YEARS INT COMMENT '最小勤続年数',
    MAX_YEARS INT COMMENT '最大勤続年数',
    HOURLY_RATE DECIMAL(10, 2) NOT NULL COMMENT '時間給',
    FOREIGN KEY (GRADENO) REFERENCES grade(GRADENO) ON UPDATE CASCADE ON DELETE CASCADE
);

-- kintai テーブル
CREATE TABLE IF NOT EXISTS kintai (
    RECID INT AUTO_INCREMENT PRIMARY KEY COMMENT '勤怠記録ID',
    KINTAIDATE DATE NOT NULL COMMENT '勤怠日付',
    EMPNO VARCHAR(10) NOT NULL COMMENT '従業員番号',
    CLOCKIN TIME COMMENT '出勤時刻',
    CLOCKOUT TIME COMMENT '退勤時刻',
    WORKING_HOURS DECIMAL(4, 2) COMMENT '実働時間 (例: 8.00)',
    OVERTIME_HOURS DECIMAL(4, 2) COMMENT '残業時間 (例: 1.50)',
    NIGHT_HOURS DECIMAL(4, 2) COMMENT '深夜時間 (例: 0.50)',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE (EMPNO, KINTAIDATE), -- 同一従業員の同日記録を許可しない (外部キー制約の参照元として必要)
    FOREIGN KEY (EMPNO) REFERENCES emp(EMPNO) ON UPDATE CASCADE ON DELETE CASCADE
);

-- break テーブル
CREATE TABLE IF NOT EXISTS break (
    BREAKID INT AUTO_INCREMENT PRIMARY KEY COMMENT '休憩ID',
    RECID INT NOT NULL COMMENT '勤怠記録ID',
    KINTAIDATE DATE NOT NULL COMMENT '休憩日付', -- 参照用
    EMPNO VARCHAR(10) NOT NULL COMMENT '従業員番号', -- 参照用
    BREAKSTART TIME COMMENT '休憩開始時刻',
    BREAKEND TIME COMMENT '休憩終了時刻',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    FOREIGN KEY (RECID) REFERENCES kintai(RECID) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (EMPNO, KINTAIDATE) REFERENCES kintai(EMPNO, KINTAIDATE) ON UPDATE CASCADE ON DELETE CASCADE
);

-- leave_type テーブル（休暇タイプ）
CREATE TABLE IF NOT EXISTS `leave_type` ( -- `leave` は予約語なので leave_type に変更
    LEAVE_TYPE_ID INT NOT NULL PRIMARY KEY COMMENT '休暇タイプID',
    LEAVE_TYPE_NAME VARCHAR(50) NOT NULL COMMENT '休暇タイプ名',
    IS_PAID BOOLEAN NOT NULL COMMENT '有給休暇かどうか (1:有給, 0:無給)'
);

-- leave_rec テーブル（休暇記録）
CREATE TABLE IF NOT EXISTS leave_rec (
    LEAVE_ID INT AUTO_INCREMENT PRIMARY KEY COMMENT '休暇記録ID',
    EMPNO VARCHAR(10) NOT NULL COMMENT '従業員番号',
    LEAVE_TYPE_ID INT NOT NULL COMMENT '休暇タイプID',
    STARTDATE DATE NOT NULL COMMENT '休暇開始日',
    ENDDATE DATE NOT NULL COMMENT '休暇終了日',
    REASON VARCHAR(200) COMMENT '理由',
    APPROVED_BY VARCHAR(10) COMMENT '承認者（従業員番号）',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    FOREIGN KEY (EMPNO) REFERENCES emp(EMPNO) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (LEAVE_TYPE_ID) REFERENCES `leave_type`(LEAVE_TYPE_ID) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (APPROVED_BY) REFERENCES emp(EMPNO) ON UPDATE CASCADE ON DELETE SET NULL
);

-- calendar_event テーブル
CREATE TABLE IF NOT EXISTS calendar_event (
    EVENT_DATE DATE NOT NULL PRIMARY KEY COMMENT 'イベント日付',
    EVENT_NAME VARCHAR(100) NOT NULL COMMENT 'イベント名',
    IS_WORK BOOLEAN NOT NULL COMMENT '出勤日かどうか (1:出勤日, 0:休日)'
);

-- project テーブル
CREATE TABLE IF NOT EXISTS project (
    PROJECT_ID INT AUTO_INCREMENT PRIMARY KEY COMMENT 'プロジェクトID',
    PROJECT_NAME VARCHAR(100) NOT NULL COMMENT 'プロジェクト名',
    BUDGET_AMOUNT INT COMMENT '予算額',
    ACTUAL_COST INT COMMENT '実績費用',
    ACTUAL_LABOR_COST INT COMMENT '実績人件費',
    START_DATE DATE COMMENT '開始日',
    END_DATE DATE COMMENT '終了日',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時'
);

-- work_alloc テーブル
CREATE TABLE IF NOT EXISTS work_alloc (
    ALLOCATION_ID INT AUTO_INCREMENT PRIMARY KEY COMMENT '割り当てID',
    EMPNO VARCHAR(10) NOT NULL COMMENT '従業員番号',
    PROJECT_ID INT NOT NULL COMMENT 'プロジェクトID',
    WORK_DATE DATE NOT NULL COMMENT '作業日',
    WORK_HOURS DECIMAL(4, 2) NOT NULL COMMENT '作業時間（例: 8.00）',
    UNIQUE (EMPNO, PROJECT_ID, WORK_DATE), -- 同一従業員が同日に同一プロジェクトに複数割り当てできない
    FOREIGN KEY (EMPNO) REFERENCES emp(EMPNO) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (PROJECT_ID) REFERENCES project(PROJECT_ID) ON UPDATE CASCADE ON DELETE CASCADE
);

-- work_report テーブル
CREATE TABLE IF NOT EXISTS work_report (
    REPORT_ID INT AUTO_INCREMENT PRIMARY KEY COMMENT 'レポートID',
    EMPNO VARCHAR(10) NOT NULL COMMENT '従業員番号',
    REPORT_MONTH VARCHAR(7) NOT NULL COMMENT 'レポート対象年月 (YYYY-MM)',
    TOTAL_WORKING_HOURS DECIMAL(5, 2) COMMENT '総実働時間',
    TOTAL_OVERTIME_HOURS DECIMAL(5, 2) COMMENT '総残業時間',
    WORKING_DAYS INT COMMENT '出勤日数',
    PAID_LEAVE_DAYS INT COMMENT '有給休暇日数',
    TOTAL_BREAK_TIME DECIMAL(5, 2) COMMENT '総休憩時間',
    REMARKS VARCHAR(200) COMMENT '備考',
    GENERATED_BY VARCHAR(10) COMMENT '生成者（従業員番号）',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    UNIQUE (EMPNO, REPORT_MONTH),
    FOREIGN KEY (EMPNO) REFERENCES emp(EMPNO) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (GENERATED_BY) REFERENCES emp(EMPNO) ON UPDATE CASCADE ON DELETE SET NULL
);

-- project_monthly テーブル
CREATE TABLE IF NOT EXISTS project_monthly (
    PRJMONTH_ID INT AUTO_INCREMENT PRIMARY KEY COMMENT 'プロジェクト月次ID',
    PROJECT_ID INT NOT NULL COMMENT 'プロジェクトID',
    REPORT_MONTH VARCHAR(7) NOT NULL COMMENT 'レポート対象年月 (YYYY-MM)',
    ACTUAL_LABOR_COST INT COMMENT '実績人件費',
    ACTUAL_EXPENSE INT COMMENT '実績経費',
    ACTUAL_HOURS DECIMAL(6, 2) COMMENT '実績時間（工数）',
    REMARKS VARCHAR(200) COMMENT '備考',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE (PROJECT_ID, REPORT_MONTH),
    FOREIGN KEY (PROJECT_ID) REFERENCES project(PROJECT_ID) ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------
-- 初期データの挿入
-- ---------------------------------------------------

-- dept データ
INSERT IGNORE INTO dept (DEPTNO, DEPTNAME) VALUES
('D001', '管理部'),
('D002', '営業部'),
('D003', '開発部'),
('D004', '人事部'),
('D005', '経理部'),
('D006', 'DX推進部');

-- post データ
INSERT IGNORE INTO post (POSTNO, POSTNAME) VALUES
('P001', '一般社員'),
('P002', '主任'),
('P003', '課長'),
('P004', '部長'),
('P005', '役員');

-- role データ
INSERT IGNORE INTO role (ROLEID, ROLENAME) VALUES
(0, '一般社員'),
(1, '管理者'),
(2, '承認者（リーダー/主任）');

-- grade データ（人件費.xlsx - Sheet1.csv から）
INSERT IGNORE INTO grade (GRADENO, GRADENAME) VALUES
(1, '一般社員'),
(2, '主任'),
(3, '課長'),
(4, '部長'),
(5, '役員');

-- salary データ（人件費.xlsx - Sheet1.csv から）
INSERT IGNORE INTO salary (GRADENO, EFFECTIVE_FROM, EFFECTIVE_TO, MIN_YEARS, MAX_YEARS, HOURLY_RATE) VALUES
(1, '2023-01-01', NULL, 0, 3, 1500.00),
(2, '2023-01-01', NULL, 3, 7, 2000.00),
(3, '2023-01-01', NULL, 7, 15, 3000.00),
(4, '2023-01-01', NULL, 15, 25, 4000.00),
(5, '2023-01-01', NULL, 25, 40, 5000.00);

-- leave_type データ
INSERT IGNORE INTO `leave_type` (LEAVE_TYPE_ID, LEAVE_TYPE_NAME, IS_PAID) VALUES
(1, '有給休暇', TRUE),
(2, '特別休暇', TRUE),
(3, '欠勤', FALSE),
(4, '代休', TRUE),
(5, '慶弔休暇', TRUE);

-- emp データ（人件費.xlsx - Sheet1.csv のスクリーンショットに基づく）
-- パスワードは要求に応じてシンプルな形式に変更（テスト目的のみ）
-- DEPTNO, POSTNO, ROLEID は推測に基づくマッピングのため、必要に応じて調整してください。
INSERT IGNORE INTO emp (EMPNO, EMPNAME, EMPDATE, DEPTNO, POSTNO, ROLEID, GRADENO, PASS, MAIL) VALUES
('S0001', '石井 和也', '2015-10-29', 'D002', 'P004', 1, 5, 'passS0001', 'ishii.k@example.com'), -- 営業部 部長, 管理者
('S0002', '山崎 淳', '2017-04-21', 'D002', 'P002', 2, 3, 'passS0002', 'yamazaki.j@example.com'), -- 営業部 主任, 承認者
('S0003', '鈴木 七夏', '2015-10-14', 'D002', 'P002', 2, 3, 'passS0003', 'suzuki.n@example.com'), -- 営業部 主任, 承認者
('S0004', '山下 京助', '2019-01-27', 'D002', 'P002', 2, 3, 'passS0004', 'yamashita.k@example.com'), -- 営業部 主任, 承認者
('S0005', '山本 あすか', '2019-11-13', 'D002', 'P002', 2, 3, 'passS0005', 'yamamoto.a@example.com'), -- 営業部 主任, 承認者
('S0006', '佐藤 和也', '2024-06-30', 'D002', 'P002', 2, 3, 'passS0006', 'sato.k@example.com'), -- 営業部 主任, 承認者
('S0007', '阿部 七夏', '2021-03-01', 'D002', 'P001', 0, 2, 'passS0007', 'abe.n@example.com'), -- 営業部 一般社員
('S0008', '松本 加奈', '2007-11-10', 'D002', 'P001', 0, 2, 'passS0008', 'matsumoto.k@example.com'), -- 営業部 一般社員
('S0009', '中川 陽一', '2012-08-29', 'D002', 'P001', 0, 2, 'passS0009', 'nakagawa.y@example.com'), -- 営業部 一般社員
('S0010', '山本 千代', '2015-09-21', 'D001', 'P004', 1, 5, 'passS0010', 'yamamoto.c@example.com'), -- 管理部 部長, 管理者
('S0011', '藤原 さゆり', '2015-03-05', 'D002', 'P001', 0, 2, 'passS0011', 'fujiwara.s@example.com'), -- 営業部 一般社員
('S0012', '山口 直樹', '2019-10-10', 'D002', 'P001', 0, 2, 'passS0012', 'yamaguchi.n@example.com'), -- 営業部 一般社員
('S0013', '松田 明美', '2015-07-12', 'D002', 'P001', 0, 2, 'passS0013', 'matsuda.a@example.com'), -- 営業部 一般社員
('S0014', '山本 花子', '2024-07-27', 'D002', 'P001', 0, 2, 'passS0014', 'yamamoto.h@example.com'), -- 営業部 一般社員
('S0015', '森 里佳', '2006-11-27', 'D002', 'P001', 0, 1, 'passS0015', 'mori.r@example.com'), -- 営業部 一般社員
('S0016', '山下 淳', '2013-07-09', 'D002', 'P001', 0, 1, 'passS0016', 'yamashita.a@example.com'), -- 営業部 一般社員
('S0017', '井上 直樹', '2020-02-08', 'D002', 'P001', 0, 2, 'passS0017', 'inoue.n@example.com'), -- 営業部 一般社員
('S0018', '伊藤 学', '2016-05-04', 'D002', 'P001', 0, 1, 'passS0018', 'ito.m@example.com'), -- 営業部 一般社員
('S0019', '森 加奈', '2010-11-24', 'D002', 'P001', 0, 2, 'passS0019', 'mori.k@example.com'), -- 営業部 一般社員
('S0020', '山崎 零', '2008-04-18', 'D002', 'P001', 0, 2, 'passS0020', 'yamazaki.r@example.com'), -- 営業部 一般社員
('S0021', '山田 舞', '2003-09-03', 'D002', 'P001', 0, 2, 'passS0021', 'yamada.m@example.com'), -- 営業部 一般社員
('S0022', '高橋 翼', '2005-05-21', 'D002', 'P001', 0, 2, 'passS0022', 'takahashi.t@example.com'), -- 営業部 一般社員
('S0023', '小川 修平', '2004-01-16', 'D002', 'P001', 0, 2, 'passS0023', 'ogawa.s@example.com'), -- 営業部 一般社員
('S0024', '近藤 稔', '2022-01-11', 'D002', 'P001', 0, 2, 'passS0024', 'kondo.m@example.com'), -- 営業部 一般社員
('S0025', '小川 裕美子', '2017-03-21', 'D002', 'P001', 0, 2, 'passS0025', 'ogawa.y@example.com'), -- 営業部 一般社員
('S0026', '吉田 あすか', '2015-04-16', 'D001', 'P001', 0, 1, 'passS0026', 'yoshida.a@example.com'), -- 管理部 一般社員
('S0027', '長谷川 真綾', '2008-06-12', 'D001', 'P001', 0, 1, 'passS0027', 'hasegawa.m@example.com'), -- 管理部 一般社員
('S0028', '岡本 修平', '2022-07-12', 'D002', 'P001', 0, 1, 'passS0028', 'okamoto.s@example.com'), -- 営業部 一般社員
('S0029', '鈴木 浩', '2012-11-10', 'D002', 'P001', 0, 2, 'passS0029', 'suzuki.h@example.com'), -- 営業部 一般社員
('S0030', '渡辺 和也', '2021-07-15', 'D002', 'P001', 0, 2, 'passS0030', 'watanabe.k@example.com'), -- 営業部 一般社員
('S0031', '鈴木 春香', '2023-06-19', 'D001', 'P001', 0, 2, 'passS0031', 'suzuki.h@example.com'), -- 管理部 一般社員
('S0032', '松本 裕美子', '2001-04-28', 'D001', 'P001', 0, 2, 'passS0032', 'matsumoto.y@example.com'), -- 管理部 一般社員
('S0033', '高橋 舞', '2005-07-03', 'D002', 'P001', 0, 2, 'passS0033', 'takahashi.m@example.com'), -- 営業部 一般社員
('S0034', '松田 陽一', '2024-09-30', 'D002', 'P001', 0, 1, 'passS0034', 'matsuda.y@example.com'), -- 営業部 一般社員
('S0035', '林 春香', '2004-06-08', 'D002', 'P001', 0, 2, 'passS0035', 'hayashi.h@example.com'), -- 営業部 一般社員
('S0036', '藤井 明美', '2012-04-12', 'D002', 'P001', 0, 2, 'passS0036', 'fujii.a@example.com'), -- 営業部 一般社員
('S0037', '松田 京助', '2013-05-09', 'D002', 'P001', 0, 1, 'passS0037', 'matsuda.k@example.com'), -- 営業部 一般社員
('S0038', '伊藤 翼', '2022-05-04', 'D002', 'P001', 0, 1, 'passS0038', 'ito.t@example.com'), -- 営業部 一般社員
('S0039', '石川 直樹', '2010-09-12', 'D002', 'P001', 0, 1, 'passS0039', 'ishikawa.n@example.com'), -- 営業部 一般社員
('S0040', '山田 花子', '2016-10-20', 'D002', 'P001', 0, 2, 'passS0040', 'yamada.h@example.com'); -- 営業部 一般社員


-- Carendar

コマンドに貼り付けるといいと思います。
「--」はコメント文です。
「--」がない場合は、SQL文です。

-------　ここから貼り付ける -------

USE kintai;

-- ---------------------------------------------------
-- calendar_event テーブルの修正（event_repeat_ruleとの連携のため）
-- ---------------------------------------------------

-- まず、calendar_event_ibfk_1 のような既存の外部キー制約があれば削除します。
-- これは、もし event_repeat_rule との以前の関連付けや、他のFKがこの変更の邪魔をする場合です。
-- 通常、初回作成時には不要ですが、念のため。
-- 正しい制約名は `SHOW CREATE TABLE calendar_event;` で確認してください。
-- ALTER TABLE calendar_event DROP FOREIGN KEY your_existing_fk_name;

-- calendar_event テーブルに REPEAT_RULE_ID 列を追加
-- この列は、このイベントがどの繰り返しルールに属するかを示すために使用します。
ALTER TABLE calendar_event
ADD COLUMN REPEAT_RULE_ID INT COMMENT '繰り返しルールID (event_repeat_ruleへの外部キー)';

-- ---------------------------------------------------
-- event_repeat_rule テーブルの作成
-- ---------------------------------------------------

CREATE TABLE IF NOT EXISTS event_repeat_rule (
    RULE_ID INT AUTO_INCREMENT PRIMARY KEY COMMENT '繰り返しルールID',
    EVENT_DATE_FK DATE NOT NULL COMMENT '主イベント日付 (calendar_eventへの外部キー)',
    REPEAT_TYPE VARCHAR(20) NOT NULL COMMENT '繰り返しタイプ (DAILY, WEEKLY, MONTHLY_DAY, MONTHLY_WEEKDAY, YEARLY)',
    REPEAT_INTERVAL INT NOT NULL DEFAULT 1 COMMENT '繰り返し間隔 (例: 2週間に1回)',
    REPEAT_DAYS_OF_WEEK VARCHAR(7) COMMENT '繰り返し曜日 (例: "1,3,5" - 月水金)', -- 1=月, 7=日
    REPEAT_END_DATE DATE COMMENT '繰り返し終了日',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',

    -- calendar_event の EVENT_DATE への外部キー制約
    FOREIGN KEY (EVENT_DATE_FK) REFERENCES calendar_event(EVENT_DATE) ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------
-- calendar_event に event_repeat_rule への外部キー制約を追加
-- ---------------------------------------------------

-- ADD CONSTRAINT はもし以前のALTER TABLEで既にRULE_IDを追加していれば、このステップをスキップしてください。
-- 必要であれば、ON UPDATE CASCADE ON DELETE SET NULL などのポリシーも追加できます。
ALTER TABLE calendar_event
ADD CONSTRAINT fk_repeat_rule_id FOREIGN KEY (REPEAT_RULE_ID) REFERENCES event_repeat_rule(RULE_ID) ON UPDATE CASCADE ON DELETE SET NULL;

-- ---------------------------------------------------
-- 初期データの例（イベントと繰り返しルール）
-- ---------------------------------------------------

-- 例1: 毎週月曜日に繰り返す「定例ミーティング」
-- まず、主イベント（例えば最初の月曜日）を calendar_event に挿入
INSERT IGNORE INTO calendar_event (EVENT_DATE, EVENT_NAME, IS_WORK) VALUES
('2025-07-07', '定例ミーティング', TRUE);

-- 次に、このイベントの繰り返しルールを event_repeat_rule に挿入
INSERT IGNORE INTO event_repeat_rule (EVENT_DATE_FK, REPEAT_TYPE, REPEAT_INTERVAL, REPEAT_DAYS_OF_WEEK, REPEAT_END_DATE) VALUES
('2025-07-07', 'WEEKLY', 1, '1', '2025-12-31'); -- '1'は月曜日

-- 挿入したルールのRULE_IDを calendar_event の REPEAT_RULE_ID に紐付け
UPDATE calendar_event
SET REPEAT_RULE_ID = (SELECT RULE_ID FROM event_repeat_rule WHERE EVENT_DATE_FK = '2025-07-07' AND REPEAT_TYPE = 'WEEKLY')
WHERE EVENT_DATE = '2025-07-07';

-- 例2: 毎月15日に繰り返す「給与締め日」
INSERT IGNORE INTO calendar_event (EVENT_DATE, EVENT_NAME, IS_WORK) VALUES
('2025-07-15', '給与締め日', TRUE);

INSERT IGNORE INTO event_repeat_rule (EVENT_DATE_FK, REPEAT_TYPE, REPEAT_INTERVAL, REPEAT_DAYS_OF_WEEK, REPEAT_END_DATE) VALUES
('2025-07-15', 'MONTHLY_DAY', 1, NULL, NULL); -- NULLは無限に繰り返す

UPDATE calendar_event
SET REPEAT_RULE_ID = (SELECT RULE_ID FROM event_repeat_rule WHERE EVENT_DATE_FK = '2025-07-15' AND REPEAT_TYPE = 'MONTHLY_DAY')
WHERE EVENT_DATE = '2025-07-15';

-- 例3: 単一イベント「会社創立記念日」
INSERT IGNORE INTO calendar_event (EVENT_DATE, EVENT_NAME, IS_WORK) VALUES
('2025-08-01', '会社創立記念日', FALSE);
-- 単一イベントの場合、event_repeat_rule には関連付けない（REPEAT_RULE_ID は NULL のまま）

-------　ここまで貼り付ける -------
