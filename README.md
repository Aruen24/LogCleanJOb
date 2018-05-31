# LogCleanJOb

## 日志的文件在data.7z中（原文件数据+清洗后数据）

## 日志路径
```ruby
/usr/apache_logs
access_2013_05_30.log
access_2013_05_31.log
日志格式：
27.19.74.143 - - [30/May/2013:17:38:20 +0800] "GET /static/image/common/faq.gif HTTP/1.1" 200 1127
```
  
## 用linux shell导入
```ruby
hdfs dfs -put /usr/apache_logs /techbbs
hdfs中   /techbbs/access_2013_05_30.log
	 /techbbs/access_2013_05_31.log
```
       
##  运行：
```ruby
hadoop jar logclean.jar demo.LogCleanJobMain /techbbs/access_2013_05_30.log /techbbs/cleaned/2013_05_30	
hadoop jar logclean.jar demo.LogCleanJobMain /techbbs/access_2013_05_31.log /techbbs/cleaned/2013_05_31
```

## 清洗后的数据存放hdfs中路径：
```ruby
/techbbs/cleaned/access_2013_05_30.log
/techbbs/cleaned/access_2013_05_31.log
```

## 将清洗后的数据存入Hive中，那么我们需要先建立一张表。这里我们选择分区表，以日期作为分区的指标，建表语句如下：
（这里关键之处就在于确定映射的HDFS位置，我这里是/project/techbbs/cleaned即清洗后的数据存放的位置）
```ruby
hive>CREATE EXTERNAL TABLE techbbs(ip string, atime string, url string) PARTITIONED BY (logdate string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LOCATION '/techbbs/cleaned';
```

## 建立了分区表之后，就需要增加一个分区，增加分区的语句如下：（这里主要针对20150425这一天的日志进行分区）
```ruby
hive>ALTER TABLE techbbs ADD PARTITION(logdate='2013_05_30') LOCATION '/techbbs/cleaned/2013_05_30';
```

# 使用HQL统计关键指标：
## 关键指标之一：PV量

页面浏览量即为PV(Page View)，是指所有用户浏览页面的总和，一个独立用户每打开一个页面就被记录1 次。
这里，我们只需要统计日志中的记录个数即可，HQL代码如下：
```ruby
hive>CREATE TABLE techbbs_pv_2013_05_30 AS SELECT COUNT(1) AS PV FROM techbbs WHERE logdate='2013_05_30';
hive> select * from techbbs_pv_2013_05_30;
```
  
## 关键指标之二：注册用户数

该论坛的用户注册页面为member.php，而当用户点击注册时请求的又是member.php?mod=register的url。
因此，这里我们只需要统计出日志中访问的URL是member.php?mod=register的即可，HQL代码如下：
```ruby
hive>CREATE TABLE techbbs_reguser_2013_05_30 AS SELECT COUNT(1) AS REGUSER FROM techbbs WHERE logdate='2013_05_30' AND INSTR(url,'member.php?mod=register')>0;　
hive> select * from techbbs_reguser_2013_05_30;
```
  
## 关键指标之三：独立IP数

一天之内，访问网站的不同独立 IP 个数加和。其中同一IP无论访问了几个页面，独立IP 数均为1。
因此，这里我们只需要统计日志中处理的独立IP数即可，在SQL中我们可以通过DISTINCT关键字，在HQL中也是通过这个关键字：
```ruby
hive>CREATE TABLE techbbs_ip_2013_05_30 AS SELECT COUNT(DISTINCT ip) AS IP FROM techbbs WHERE logdate='2013_05_30';
hive> select * from techbbs_ip_2013_05_30;
```
  
## 关键指标之四：跳出用户数

只浏览了一个页面便离开了网站的访问次数，即只浏览了一个页面便不再访问的访问次数。
这里，我们可以通过用户的IP进行分组，如果分组后的记录数只有一条，那么即为跳出用户。将这些用户的数量相加，就得出了跳出用户数，HQL代码如下：
```ruby
hive>CREATE TABLE techbbs_jumper_2013_05_30 AS SELECT COUNT(1) AS jumper FROM (SELECT COUNT(ip) AS times FROM techbbs WHERE logdate='2013_05_30' GROUP BY ip HAVING times=1) e;
hive> select * from techbbs_jumper_2013_05_30;
```
  
# 将所有关键指标放入一张汇总表中以便于通过Sqoop导出到MySQL
## 为了方便通过Sqoop统一导出到MySQL，这里我们借助一张汇总表将刚刚统计到的结果整合起来，通过表连接结合，HQL代码如下：
```ruby
hive>CREATE TABLE techbbs_2013_05_30 AS SELECT '2013_05_30', a.pv, b.reguser, c.ip, d.jumper FROM techbbs_pv_2013_05_30 a JOIN techbbs_reguser_2013_05_30 b ON 1=1 JOIN techbbs_ip_2013_05_30 c ON 1=1 JOIN techbbs_jumper_2013_05_30 d ON 1=1;
报错：FAILED: SemanticException Cartesian products are disabled for safety reasons. If you know what you are doing, please sethive.strict.checks.cartesian.product to false and that hive.mapred.mode is not st to 'strict' to proceed. 
Note that if you may get errors or incorrect results if you make a mistake while using some of the unsafe features.
	
解决：在SQL前面加上如下： 
hive> set hive.mapred.mode=nonstrict;
hive> CREATE TABLE techbbs_2013_05_30 AS SELECT '2013_05_30', a.pv, b.reguser, c.ip, d.jumper FROM techbbs_pv_2013_05_30 a JOIN techbbs_reguser_2013_05_30 b ON 1=1 JOIN techbbs_ip_2013_05_30 c ON 1=1 JOIN techbbs_jumper_2013_05_30 d ON 1=1;
 ```
## 将hive中数据通过sqoop导入到mysql中
```ruby
sqoop export --connect jdbc:mysql://172.30.86.231:3306/techbbs --username root --password 921027 --table techbbs_logs_stat --fields-terminated-by '\001' --export-dir '/user/hive/warehouse/techbbs_2013_05_30'
```

## 查看hive 表在hdfs上的存储路径
```ruby
hive>show create table techbbs_2013_05_30;
LOCATION
'hdfs://bigdata11:9000/user/hive/warehouse/techbbs_2013_05_30'
```
