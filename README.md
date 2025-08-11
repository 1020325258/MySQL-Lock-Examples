# MySQL 加锁机制

实验环境：

- MySQL 版本：8.0.39。
- 事务隔离级别：可重复读（RR）。

## 建表语句
```sql
CREATE TABLE `t`
(
    `id` int(11) NOT NULL,
    `c`  int(11) DEFAULT NULL,
    `d`  int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `c` (`c`)
) ENGINE = InnoDB;
insert into t values(0,0,0),(5,5,5),(10,10,10),(15,15,15),(20,20,20),(25,25,25);
```

加锁验证语句：

主要通过两种类型的 SQL 对加锁（如果已有数据，则使用 update；如果未存在数据，则使用 insert）：
- `insert into t values(3, 99, 99)`：用于验证主键索引在区间 (0, 5) 上是否存在锁。
  插入一条记录 id = 3，其中 b 和 c 的值都为 99，由于表中已有的 b 和 c 最大值只有 20，因此插入 99 可以保证不会被 c 和 d 列上的锁影响。
- `update t set d = d + 1 where id = 5`：用于验证主键索引是否存在 id = 5 的行锁。

上边介绍的是对主键索引加锁的验证，对于 c 索引也是同理。

## 自定义语句
如果想要自己定义 SQL 语句，需要参照以下写法。

**自定义 insert 语句：**
```java
"/* 向主键索引的间隙(0, 5)插入数据:id = 3 */ insert into t values(3, 99, 99)"
```
例如，想要将上边的语句改为向主键索引的 (10, 15) 间隙插入数据 id = 8，则需要修改如下:
```java
"/* 向主键索引的间隙(10, 15)插入数据:id = 8 */ insert into t values(8, 99, 99)"
```


**自定义 update 语句：**

```java
"/* 尝试获取主键索引 id = 5 的行锁*/ update t set d = d+1 where id = 5"
```
update 语句修改同理，如果想要验证 id = 10 是否存在行锁，则修改如下：
```java
"/* 尝试获取主键索引 id = 10 的行锁*/ update t set d = d+1 where id = 10"
```

注意：除了要修改 SQL 语句，还要修改 id = {10}, c = {8} 花括号中的值（用于画图定位坐标）