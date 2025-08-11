package lock;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class lock2 extends BaseClass {


    public static void main(String[] args) throws Exception {
        // 连接1：执行加锁 SQL
        /**
         * 加锁流程：
         * 根据主键 id 查找 > 10 的元素，因此会先在主键添加 next-key lock：(10, 15]。
         * 找到 id = 15 的数据之后，由于主键是唯一索引，不会继续向后查找。
         * 总结：
         * 1、主键索引：(10, 15] 的锁。
         */
        String lockSQL = "select * from t where id > 10 and id <= 15 for update;";

        // 连接2：发起各种操作来验证锁范围
        List<String> primaryIndexSQL = Arrays.asList(
                "/* 尝试获取主键索引 id = 10 的行锁*/ update t set d = d + 1 where id = 10",
                "/* 尝试获取主键索引 id = 15 的行锁*/ update t set d = d + 1 where id = 15",
                "/* 向主键索引的间隙(10, 15)插入数据:id = 11 */ insert into t values(11, 99, 99);",
                "/* 向主键索引的间隙(15, 20)插入数据:id = 17 */ insert into t values(17, 99, 99);"
        );
        List<String> cIndexSQL = Arrays.asList();

        verifyLock(lockSQL, primaryIndexSQL, cIndexSQL);


    }


}
