# LongRaft
raft工程实现，现在只实现了选举功能。

# 使用

## 1. 修改配置文件

配置文件如下：
id: 当前server的id
servers： 所有服务器,以逗号分隔，每个服务器格式：id:host:port

config文件例子
id=1
servers=1:127.0.0.1:10020,2:127.0.0.1:10021,3:127.0.0.1:10022

## 2. 运行MainRun文件或者打包运行
