package net.jeebiz.smbclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.smb.SmbFile;
import net.jeebiz.smbclient.client.ISMBClient;
import net.jeebiz.smbclient.client.SMBResourceClient;
@FixMethodOrder(MethodSorters.JVM) 
public class SMBResourceClientTest {
	
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
		
 		smbClient = new SMBResourceClient(builder);
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
 			smbClient.upload(localFile ,"test/test","tst.mp4");
 			
 			smbClient.upload(new FileInputStream(localFile),"tst2.mp4");
 			smbClient.upload(new FileInputStream(localFile),"test","tst2.mp4" );
 			smbClient.upload(new FileInputStream(localFile),"test/test","tst2.mp4");
 			
 			smbClient.upload(new StringBuilder(fileContent),"tst1.txt");
 			smbClient.upload(new StringBuilder(fileContent),"test","tst2.txt");
 			smbClient.upload(new StringBuilder(fileContent),"test/test","tst3.txt");
 			
 			smbClient.upload("E:\\test.mp4","tst3.mp4");
 			smbClient.upload("E:\\test.mp4","test","tst4.mp4");
 			smbClient.upload("E:\\test.mp4","test/test","tst5.mp4");
 			smbClient.uploadByChannel(localFile, "tst-c.mp4");
 			smbClient.uploadByChannel(localFile,"test/test","tst-c.mp4");
 			
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
 			
 			smbClient.downloadToFile("tst1.mp4", "E:\\test\\test1.mp4");
 			smbClient.downloadToFile("tst.mp4", new File("E:\\test\\test.mp4"));
 			smbClient.downloadToFile("test/test","tst.mp4", "E:\\test\\test\\test1.mp4");
 			smbClient.downloadToFile("test/test","tst.mp4", new File("E:\\test\\test\\test2.mp4"));
 			smbClient.downloadToStream("tst2.mp4", new FileOutputStream(new File("E:\\test\\test2.mp4")));
 			smbClient.downloadToStream("test","tst2.mp4", new FileOutputStream(new File("E:\\test\\test\\test3.mp4")));
 			smbClient.downloadToDir("test/test", "E:\\test\\test\\dir01");
 			smbClient.downloadToDir("test/test", new File("E:\\test\\test\\dir02"));
 			
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
 	@Test
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
 