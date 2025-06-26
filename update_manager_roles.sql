-- 修正ROLEID分配規則
-- 1. 管理部の全員をROLEID=1に設定
UPDATE emp SET ROLEID = 1 WHERE DEPTNO = 'D001';

-- 2. 全ての部長をROLEID=2に設定
UPDATE emp SET ROLEID = 2 WHERE POSTNO = 'P004';

-- 3. その他の従業員をROLEID=0に設定
UPDATE emp SET ROLEID = 0 WHERE DEPTNO != 'D001' AND POSTNO != 'P004';

-- 確認用クエリ（実行結果を確認するため）
SELECT EMPNO, EMPNAME, DEPTNO, POSTNO, ROLEID, 
       CASE 
           WHEN DEPTNO = 'D001' THEN '管理部'
           WHEN POSTNO = 'P004' THEN '部長'
           ELSE 'その他'
       END AS 分類
FROM emp 
ORDER BY ROLEID, DEPTNO, POSTNO;