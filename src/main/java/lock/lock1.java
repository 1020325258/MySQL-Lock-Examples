package lock;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class lock1 extends BaseClass{

    public static void main(String[] args) throws Exception {

        // 连接1：执行加锁 SQL
        /**
         * 加锁流程：
         * 首先访问了 c 索引树，因此会在 (0, 5] 添加锁，由于 c 索引不是唯一索引，会继续向后遍历，发现 c=10 不满足 where 条件，因此会先在 (5, 10] 上添加 next-key lock，之后发现 10 不满足条件，退化为间隙锁(5, 10)
         * 因此，在 c 索引树上添加的锁为：(0, 5]、(5, 10)。
         * select ... for update 会认为后续想要更新对应的数据，因此除了在 c 索引树上加锁之外，还会在主键索引上加锁。
         * 同样，先在 (0, 5] 添加 next-key lock，由于主键索引也是唯一索引，next-key lock 会退化为行锁，因此主键索引只针对了 id = 5 添加了行锁。
         * 总结：select id from t where c=5 for update 在 c 索引上添加了 (0, 5] 和 (5, 10) 的锁，在住建索引上添加了 id = 5 的行锁。
         */
        String lockSQL = "select id from t where c=5 for update";


        // 连接2：发起各种操作来验证锁范围
        // 主键索引加锁
        List<String> primaryIndexSQL = Arrays.asList(
                "/* 尝试获取主键索引 id = 5 的行锁*/ update t set d = d+1 where id = 5",
                "/* 向主键索引的间隙(5, 10)插入数据:id = 8 */ insert into t values(8, 99, 99)"
        );
        // c 索引加锁
        List<String> cIndexSQL = Arrays.asList(
                "/* 尝试获取 c 索引 c = 5 的行锁 */ update t set d = d+1 where c=5",
                "/* 向 c 索引的间隙(0, 5)插入数据:c = 4 */ insert into t values(99, 4, 99)",
                "/* 向 c 索引的间隙(5, 10)插入数据:c = 8 */ insert into t values(99, 8, 99)",
                "/* 向 c 索引的间隙(10, 15)插入数据:c = 11 */insert into t values(99, 11, 99)"
        );

        verifyLock(lockSQL, primaryIndexSQL, cIndexSQL);
    }


}
