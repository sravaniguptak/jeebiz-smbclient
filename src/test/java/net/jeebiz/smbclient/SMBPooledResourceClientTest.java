package net.jeebiz.smbclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.smb.SmbFile;
import net.jeebiz.smbclient.client.ISMBClient;
import net.jeebiz.smbclient.client.SMBPooledResourceClient;
import net.jeebiz.smbclient.pool.SMBClientPool;
import net.jeebiz.smbclient.pool.SMBClientPoolConfig;
import net.jeebiz.smbclient.pool.SMBPooledClientFactory;
public class SMBPooledResourceClientTest {
	
	// 要写入的文件内容
	String fileContent = "hello world，你好世界";
	protected static Logger LOG = LoggerFactory.getLogger(SMBResourceClientTest.class);
	ISMBClient smbClient = null;
	
	@Before
	public void setUp() {
		
		
		SMBClientConfig clientConfig = new SMBClientConfig();
		
		/*
		 * #===============================================================================
		 * #=============SMBClient参数配置  ===================================================
		 * #===============================================================================
		 */
		
		// smbclient启用或禁用是否允许用户交互（例如弹出一个验证对话框）的上下文中对此 URL 进行检查 
		clientConfig.setAllowUserInteraction(false);
		// smbclient启用或禁用数据流方式上传/下载时是否在缓冲发送/接收完成自动刷新缓存区；大文件上传下载时比较有用;默认false
		clientConfig.setAutoFlush(true);
		// smbclient数据流方式上传/下载时缓存区达到可自动刷新的最小阀值；仅当 autoflush 为true 时才有效； 2M
		clientConfig.setAutoFlushBlockSize(2097152);
		// smbclient为缓冲数据流而设置内部缓冲器区大小;默认 10M
		clientConfig.setBufferSize(10485760);
		// smbclient文件通道读取缓冲区大小;默认 2M
		clientConfig.setChannelReadBufferSize(2097152);
		// smbclient文件通道写出缓冲区大小;默认 2M
		clientConfig.setChannelWriteBufferSize(2097152);
		// smbclient连接超时时间，单位为毫秒，默认30000毫秒
		clientConfig.setConnectTimeout(30000);
		// smbclientTCP进行存储时/检索操作时数据处理进度监听对象类路径
		clientConfig.setCopyStreamProcessListener(null);
		clientConfig.setCopyStreamProcessListenerName("net.jeebiz.smbclient.io.PrintCopyStreamProcessListener");
		/*
		 * smbclient读取的共享文件的目录结构及IP地址，如果需要用户权限的话，那么你就要知道用户名和密码是多少。
		 * smbclient例1：smb://userName:password@ip/filePath（这种情况是需要用户名密码的情况下输入的条件）
		 * smbclient例2：smb://ip/filePath（这种情况是不需要用户名和密码的）
		 * smbclient【共享文件服务器】域名 
		 */
		clientConfig.setDomain("");
		// smbclient【文件共享服务器】地址
		clientConfig.setHost("192.168.31.100");
		// smbclient【文件共享服务器】用户名
		clientConfig.setUsername("");
		// smbclient【文件共享服务器】密码；注意事项：登陆服务器的密码不支持强密码（如密码含有 &……%￥smbclient 等字符，都当成分隔符处理）
		clientConfig.setPassword("");
		// smbclient是否使用Log4j记录命令信息,默认打印出命令，如果开启日志则关闭打印;默认 false
		clientConfig.setLogDebug(false);
		// smbclient从数据连接读取数据的 超时时间，单位（毫秒）；默认 30000 毫秒
		clientConfig.setReadTimeout(30000);
		// smbclient【共享文件服务器】根共享目录
		clientConfig.setSharedDir("test");
		// smbclient启用或禁用在条件允许情况下允许协议使用缓存
		clientConfig.setUsecaches(false);

		SMBClientBuilder builder = new SMBClientBuilder(clientConfig);
		
		/*
		 * #===============================================================================
		 * #=============SMBClient对象池配置==================================================
		 * #===============================================================================
		 * maxActive" -> "maxTotal" and "maxWait" -> "maxWaitMillis
		 */
		SMBClientPoolConfig poolConfig = new SMBClientPoolConfig();
		
		// 在对象池耗尽时是否阻塞，默认true。false的话超时就没有作用了
		poolConfig.setBlockWhenExhausted(true);
		//设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
		poolConfig.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
		// 驱逐线程关闭的超时时间，默认10秒。
		poolConfig.setEvictorShutdownTimeoutMillis(10000);
		// 是否使用公平锁，默认false(公平锁是线程安全中的概念，true的含义是谁先等待获取锁，谁先在锁释放的时候获取锁，如非必要，一般不使用公平锁，会影响性能)
		poolConfig.setFairness(false);
		// 是否启用pool的jmx管理功能, 默认true
		poolConfig.setJmxEnabled(true);
		// jmx默认的base name，默认为null，意味着池提供一个名称
		poolConfig.setJmxNameBase(null);
		// jmx默认的前缀名，默认为pool
		// MBean ObjectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + "pool" + i)
		poolConfig.setJmxNamePrefix("smbclient.pool");
		//borrowObject返回对象时，是采用DEFAULT_LIFO（last in first out，即类似cache的最频繁使用队列），如果为False，则表示FIFO队列；是否启用后进先出, 默认true
		poolConfig.setLifo(true);
		//最大能够保持idel状态的对象数；控制一个pool最多有多少个状态为idle的smbclient实例；
		poolConfig.setMaxIdle(8);
		//控制一个pool可分配多少个smbclient实例，通过pool.getResource()来获取；  
		//如果赋值为-1，则表示不限制；如果pool已经分配了maxTotal个smbclient实例，则此时pool的状态为exhausted(耗尽)。
		poolConfig.setMaxTotal(500);
		//当池内没有返回对象时，最大等待时间获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
		//表示当borrow一个smbclient实例时，最大的等待时间，如果超过等待时间，则直接抛出smbclientConnectionException；
		poolConfig.setMaxWaitMillis(-1);
		//逐出连接的最小空闲时间, 默认1800000毫秒(30分钟)，达到此值后空闲连接将可能会被移除。负值(-1)表示不移除。
		//表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；
		poolConfig.setMinEvictableIdleTimeMillis(-1);
		//连接池中最少空闲的连接数,默认为0.
		poolConfig.setMinIdle(0);
		//每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3 . 表示idle object evitor每次扫描的最多的对象数；
		poolConfig.setNumTestsPerEvictionRun(3);
		//连接空闲的最小时间，达到此值后空闲链接将会被移除，且保留“minIdle”个空闲连接数。默认为-1.
		//对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
		//在minEvictableIdleTimeMillis基础上，加入了至少 minIdle个对象已经在pool里面了。
		//如果为-1，evicted不会根据idle time驱逐任何对象。
		//如果minEvictableIdleTimeMillis>0，则此项设置无意义，且只有在 timeBetweenEvictionRunsMillis大于0时才有意义；
		poolConfig.setSoftMinEvictableIdleTimeMillis(-1);
		//向调用者输出“链接”资源时，是否检测是有有效，如果无效则从连接池中移除，并尝试获取继续获取。默认为false。建议保持默认值.
		//在borrow一个smbclient实例时，是否提前进行alidate操作；如果为true，则得到的smbclient实例均是可用的；
		poolConfig.setTestOnBorrow(true);
		//向连接池“获取”链接时，是否检测“链接”对象的有效性。默认为false。建议保持默认值.
		poolConfig.setTestOnCreate(true);
		//向连接池“归还”链接时，是否检测“链接”对象的有效性。默认为false。建议保持默认值.
		poolConfig.setTestOnReturn(true);
		//向调用者输出“链接”对象时，是否检测它的空闲超时；默认为false。如果“链接”空闲超时，将会被移除。建议保持默认值.
		//如果为true，表示有一个idle object evitor线程对idle object进行扫描，如果validate失败，此object会被从pool中drop掉；这一项只有在 timeBetweenEvictionRunsMillis大于0时才有意义；
		poolConfig.setTestWhileIdle(true);
		//“空闲链接”检测线程，检测的周期，毫秒数。如果为负值，表示不运行“检测线程”。默认为-1. 表示idle object evitor两次扫描之间要sleep的毫秒数；
		poolConfig.setTimeBetweenEvictionRunsMillis(-1);
		
		SMBPooledClientFactory factory = new SMBPooledClientFactory(builder);
		
		SMBClientPool clientPool = new SMBClientPool(factory, poolConfig);
		
		
 		smbClient = new SMBPooledResourceClient(clientPool, clientConfig);
	}
	
	/**
 	 * 向smb写文件(数据)
 	 */
	//@Test
 	public void uploadFile() {
 		try {
 			File localFile = new File("E:\\test.mp4");
 			
 			smbClient.upload("E:\\test.mp4","tst1.mp4");
 			smbClient.upload(localFile, "tst.mp4");
 			smbClient.upload(localFile,"test/tst.mp4");
 			smbClient.upload(localFile ,"test/20160118","tst.mp4");
 			
 			smbClient.upload(new FileInputStream(localFile),"tst2.mp4");
 			smbClient.upload(new FileInputStream(localFile),"test","tst2.mp4" );
 			smbClient.upload(new FileInputStream(localFile),"test/20160118","tst2.mp4");
 			
 			smbClient.upload(new StringBuilder(fileContent),"tst1.txt");
 			smbClient.upload(new StringBuilder(fileContent),"test","tst2.txt");
 			smbClient.upload(new StringBuilder(fileContent),"test/20160118","tst3.txt");
 			
 			smbClient.upload("E:\\第九套广播体操.mp4","tst3.mp4");
 			smbClient.upload("E:\\第九套广播体操.mp4","test","tst4.mp4");
 			smbClient.upload("E:\\第九套广播体操.mp4","test/20160118","tst5.mp4");
 			smbClient.uploadByChannel(localFile, "tst-c.mp4");
 			smbClient.uploadByChannel(localFile,"test/20160118","tst-c.mp4");
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}  
 	}
 	
 	/**
 	 * smb列举文件
 	 */
 	//@Test
 	public void listFile() {
 		try {
 			
 			for(SmbFile smbFile : smbClient.listFiles("test")){
 				LOG.info("File:" + smbFile.getName());
 			}
 			
 			LOG.info("Files:" +StringUtils.join(smbClient.listNames("test"),","));
 			
 			SmbFile smbFile1 = smbClient.getFile("test", "tst1.txt");
 			LOG.info("File1:" + smbFile1.getName());
 			SmbFile smbFile2 = smbClient.getFile("test/tst1.txt");
 			LOG.info("File2:" + smbFile2.getName());
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	/**
 	 * smb下载数据
 	 */
 	//@Test
 	public void downFile() {
 		try {
 			
 			smbClient.downloadToFile("tst1.mp4", "E:\\test\\第九套广播体操1.mp4");
 			smbClient.downloadToFile("tst.mp4", new File("E:\\test\\第九套广播体操.mp4"));
 			smbClient.downloadToFile("test/20160118","tst.mp4", "E:\\test\\test\\第九套广播体操1.mp4");
 			smbClient.downloadToFile("test/20160118","tst.mp4", new File("E:\\test\\test\\第九套广播体操2.mp4"));
 			smbClient.downloadToStream("tst2.mp4", new FileOutputStream(new File("E:\\test\\第九套广播体操2.mp4")));
 			smbClient.downloadToStream("test","tst2.mp4", new FileOutputStream(new File("E:\\test\\test\\第九套广播体操3.mp4")));
 			smbClient.downloadToDir("test/20160118", "E:\\test\\test\\dir01");
 			smbClient.downloadToDir("test/20160118", new File("E:\\test\\test\\dir02"));
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	/**
 	 * smb移动和创建目录
 	 */
 	//@Test
 	public void moveFile() {
 		try {
 			smbClient.makeDir("test");
 			smbClient.makeDir("test/dddd");
 			smbClient.makeDir("test/ss/sdsd");
 			
 			smbClient.rename("tst2-sss9.mp4","tst1.mp4");
 			smbClient.rename("tst1.mp4", "tst2-sss9.mp4");
 			smbClient.renameTo("tst2-sss9.mp4", "test//rename.mp4");
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
	
	/**
 	 * smb删除文件
 	 */
 	//@Test
 	public void removeFile() {
 		try {
 			smbClient.remove("tst.mp4");
 			smbClient.remove(new String[]{"tst2.mp4","tst1.mp4"});
 			smbClient.remove("test", "tst.mp4");
 			smbClient.remove("test",new String[]{"tst1.txt","tst2.mp4","tst1.mp4"});
 			smbClient.removeDir("test");
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 }
 