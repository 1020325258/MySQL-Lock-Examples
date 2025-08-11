package lock;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseClass {

    // 修改为你的连接串/账号
    static final String URL = "jdbc:mysql://127.0.0.1:3306/mysql_demo?useSSL=false&serverTimezone=UTC&allowMultiQueries=true";
    static final String USER = "root";
    static final String PASS = "123456";

    static  List<Point> primaryIndex = new ArrayList<>();
    static  List<Point> cIndex = new ArrayList<>();

    protected static void verifyLock(String lockSQL, List<String> primaryIndexSQL, List<String> cIndexSQL) throws SQLException {
        // 连接1：持锁
        Connection conn1 = DriverManager.getConnection(URL, USER, PASS);
        conn1.setAutoCommit(false);
        try (Statement s1 = conn1.createStatement()) {
            // 降低锁等待时间，超过时间后 JDBC 会抛出 SQLTransactionRollbackException 异常
            s1.execute("set innodb_lock_wait_timeout=3");
            System.out.println("[C1] 执行: " + lockSQL + "（保持未提交）");
            s1.execute(lockSQL);
        }

        // 连接2：发起各种操作来验证锁范围
        try (Connection conn2 = DriverManager.getConnection(URL, USER, PASS)) {
            conn2.setAutoCommit(false);
            try (Statement s2 = conn2.createStatement()) {
                s2.execute("SET innodb_lock_wait_timeout=3");
                for (String sql : primaryIndexSQL) {
                    runWithTimeout(conn2, sql, 4000, 1);
                }
                for (String sql : cIndexSQL) {
                    runWithTimeout(conn2, sql, 4000, 2);
                }
                conn2.rollback(); // 不保留变更
            }
        } finally {
            // 释放连接1的锁，便于再次实验
            conn1.rollback();
            conn1.close();
        }
        paint();
    }

    protected static void paint() {
        // 创建 JFrame
        JFrame frame = new JFrame("加锁情况");
        LineWithArrowAndXExample panel = new LineWithArrowAndXExample(primaryIndex, cIndex);

        // 设置 JFrame 属性（增加窗口宽度以适应更长的线段）
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setBackground(Color.WHITE);
        // 将自定义的 JPanel 添加到 JFrame
        frame.add(panel);

        // 显示窗口
        frame.setVisible(true);
    }

    /** 在独立线程执行 SQL，超时即视为被锁阻塞 */
    protected static void runWithTimeout(Connection conn, String sql, long timeoutMs, Integer indexType) {
        List<Point> tmp;
        if (indexType == 1) {
            tmp = primaryIndex;
        } else if (indexType == 2) {
            tmp = cIndex;
        } else {
            tmp = new ArrayList<>();
        }
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<?> f = es.submit(() -> {
            try (Statement st = conn.createStatement()) {
                long t0 = System.currentTimeMillis();
                st.execute(sql);
                long cost = System.currentTimeMillis() - t0;
                System.out.printf("[C2] %-20s 立即成功（%d ms） -> %s%n",
                        tag(sql), cost, strip(sql));
                tmp.add(new Point(getX(sql, indexType), false));
            } catch (SQLTransactionRollbackException e) {
                // 遇到锁等待超时（受 innodb_lock_wait_timeout 影响）、或死锁
                System.out.printf("[C2] %-20s 被锁阻塞 -> %s%n", tag(sql), strip(sql));
                tmp.add(new Point(getX(sql, indexType), true));
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

    private static int getX(String sql, int indexType) {
        // 正则表达式：匹配 "id = " 后的数字
        Pattern pattern;
        if (indexType == 1) {
            pattern = Pattern.compile("id = (\\d+)");
        } else {
            pattern = Pattern.compile("c = (\\d+)");
        }
        Matcher matcher = pattern.matcher(sql);

        // 查找匹配的内容
        if (matcher.find()) {
            // 提取数字部分
            String x = matcher.group(1);  // group(1) 是第一个捕获组
            return Integer.valueOf(x);
        }
        return -1;
    }

    private static String tag(String sql) {
        int i = sql.indexOf("*/");
        return (i > 0 ? sql.substring(0, i + 2) : "").replace("/*", "").replace("*/", "").trim();
    }
    private static String strip(String sql) {
        return sql.replaceAll("/\\*.*?\\*/", "").trim();
    }
}
