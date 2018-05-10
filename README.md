#ftpclient
=========

基于jcifs的smb协议扩展，实现连接的复用，减少smb连接的消耗
1、基于ThreadLocal机制实现的SMBClient扩展，实现当前线程的SMBClient 对象复用
2、基于jcifs 和 commons-pool2实现的SMBClient扩展；实现SMBClient对象池复用
3、扩展SmbFile文件过滤实现
4、扩展SMBClient工具函数


### Maven Dependency

``` xml
<dependency>
	<groupId>net.jeebiz</groupId>
	<artifactId>jeebiz-ftpclient</artifactId>
	<version>${project.version}</version>
</dependency>
```

### Usage
------------
``` 
```