package lock;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class lock3 {
    // 修改为你的连接串/账号
    static final String URL = "jdbc:mysql://127.0.0.1:3306/mysql_demo?useSSL=false&serverTimezone=UTC&allowMultiQueries=true";
    static final String USER = "root";
    static final String PASS = "123456";

    public static void main(String[] args) throws Exception {
        // 连接1：持锁
        Connection conn1 = DriverManager.getConnection(URL, USER, PASS);
        conn1.setAutoCommit(false);
        try (Statement s1 = conn1.createStatement()) {
            // 降低锁等待时间，超过时间后 JDBC 会抛出 SQLTransactionRollbackException 异常
            s1.execute("set innodb_lock_wait_timeout=3");
            String sql = "select * from t where c >= 10 and c < 11 for update;";
            System.out.println("[C1] 执行: " + sql + "（保持未提交）");
            s1.executeQuery(sql);

            System.out.println("[C1] 已持有锁（RR 下：c 索引树的(5, 10], (10, 15) + 主键索引的 id = 10 的行锁）");
        }



        // 连接2：发起各种操作来验证锁范围
        List<String> testLock = Arrays.asList(
                "/* 尝试获取 c 索引树 c = 5 的行锁*/ update t set d = d + 1 where c = 5",
                "/* 尝试获取 c 索引树 c = 10 的行锁*/ update t set d = d + 1 where c = 10",
                "/* 尝试获取 c 索引树 c = 15 的行锁*/ update t set d = d + 1 where c = 15",
                "/* 向 c 索引树的间隙(5, 10)插入数据:c = 8 */ insert into t values(99, 8, 99)",
                "/* 向 c 索引树的间隙(10, 15)插入数据:c = 12 */ insert into t values(99, 12, 99)",
                "/* 向主键索引的间隙(5, 10)插入数据:id = 8 */ insert into t values(8, 99, 99)",
                "/* 向主键索引的间隙(10, 15)插入数据:id = 8 */ insert into t values(12, 99, 99)",
                "/* 尝试获取主键索引 id = 5 的行锁 */ update t set d = d + 1 where id = 5",
                "/* 尝试获取主键索引 id = 10 的行锁 */ update t set d = d + 1 where id = 10",
                "/* 尝试获取主键索引 id = 15 的行锁 */ update t set d = d + 1 where id = 15"
        );
        try (Connection conn2 = DriverManager.getConnection(URL, USER, PASS)) {
            conn2.setAutoCommit(false);
            try (Statement s2 = conn2.createStatement()) {
                s2.execute("SET innodb_lock_wait_timeout=3");
                for (String sql : testLock) {
                    runWithTimeout(conn2, sql, 4000);
                }
                conn2.rollback(); // 不保留变更
            }
        } finally {
            // 释放连接1的锁，便于再次实验
            conn1.rollback();
            conn1.close();
        }
    }

    /** 在独立线程执行 SQL，超时即视为被锁阻塞 */
    private static void runWithTimeout(Connection conn, String sql, long timeoutMs) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<?> f = es.submit(() -> {
            try (Statement st = conn.createStatement()) {
                long t0 = System.currentTimeMillis();
                st.execute(sql);
                long cost = System.currentTimeMillis() - t0;
                System.out.printf("[C2] %-20s 立即成功（%d ms） -> %s%n",
                        tag(sql), cost, strip(sql));
            } catch (SQLTransactionRollbackException e) {
                // 遇到锁等待超时（受 innodb_lock_wait_timeout 影响）、或死锁
                System.out.printf("[C2] %-20s 被锁阻塞 -> %s%n", tag(sql), strip(sql));
            } catch (SQLException e) {
                System.out.printf("[C2] %-20s SQL异常: %s -> %s%n", tag(sql), e.getMessage(), strip(sql));
            }
        });
        try {
            f.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            f.cancel(true);
            System.out.printf("[C2] %-20s 被锁阻塞(超时) -> %s%n", tag(sql), strip(sql));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            es.shutdownNow();
        }
    }

    private static String tag(String sql) {
        int i = sql.indexOf("*/");
        return (i > 0 ? sql.substring(0, i + 2) : "").replace("/*", "").replace("*/", "").trim();
    }
    private static String strip(String sql) {
        return sql.replaceAll("/\\*.*?\\*/", "").trim();
    }
}
