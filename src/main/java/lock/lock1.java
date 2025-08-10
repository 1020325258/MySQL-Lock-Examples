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
