package lock;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class lock3 extends BaseClass {

    public static void main(String[] args) throws Exception {

        // 连接1：执行加锁 SQL
        /**
         * 加锁流程：
         * c >= 10，因此先在 c 索引的 (5, 10] 加锁，满足 c < 11，向后继续查找，在 (10, 15] 加锁，这里 c < 11 是范围查询，不是等值查询，因此 next-key lock 不会退化为间隙锁。
         * select ... for update 会认为想要更新对应的数据，因此会在主键索引 id = 10 的记录上添加行锁。
         * 总结：
         * 1、主键索引：id = 10 的行锁。
         * 2、c 索引：(5, 10]、(10, 15] 的锁。
         */
         String lockSQL = "select * from t where c >= 10 and c < 11 for update;";

        // 连接2：发起各种操作来验证锁范围
        List<String> primaryIndexSQL = Arrays.asList(
                "/* 尝试获取主键索引 id = 5 的行锁 */ update t set d = d + 1 where id = 5",
                "/* 尝试获取主键索引 id = 10 的行锁 */ update t set d = d + 1 where id = 10",
                "/* 尝试获取主键索引 id = 15 的行锁 */ update t set d = d + 1 where id = 15",
                "/* 向主键索引的间隙(5, 10)插入数据:id = 8 */ insert into t values(8, 99, 99)",
                "/* 向主键索引的间隙(10, 15)插入数据:id = 12 */ insert into t values(12, 99, 99)"
        );
        // c 索引加锁
        List<String> cIndexSQL = Arrays.asList(
                "/* 尝试获取 c 索引树 c = 5 的行锁*/ update t set d = d + 1 where c = 5",
                "/* 尝试获取 c 索引树 c = 10 的行锁*/ update t set d = d + 1 where c = 10",
                "/* 尝试获取 c 索引树 c = 15 的行锁*/ update t set d = d + 1 where c = 15",
                "/* 向 c 索引树的间隙(5, 10)插入数据:c = 8 */ insert into t values(99, 8, 99)",
                "/* 向 c 索引树的间隙(10, 15)插入数据:c = 12 */ insert into t values(99, 12, 99)",
                "/* 向 c 索引树的间隙(15, 20)插入数据:c = 17 */ insert into t values(99, 17, 99)"
        );

        verifyLock(lockSQL, primaryIndexSQL, cIndexSQL);
    }

}
