/**
 * @author Administrator
 * @created 2014 2014年8月27日 下午3:04:28
 * @version 1.0
 */
package org.epiclouds.client.main;

/*
 //
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               佛祖保佑         永无BUG
//
//
//
 
 */

/**
 * @author Administrator
 *
 */
import java.io.FileInputStream;
import java.util.Properties;

import org.epiclouds.bean.ServerInfo;
import org.epiclouds.handlers.util.ChannelManager;
import org.epiclouds.handlers.util.Paras;
import org.epiclouds.netty.NettyClient;
import org.epiclouds.netty.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discards any incoming data.
 */
public class MainRun {

	public  static Logger mainlogger = LoggerFactory.getLogger(MainRun.class);

   public static void main(String[] args) throws Exception{
	   //initProxyFromFile(Constants.PROXYFILE);
	   try{
		   getConfig();
		   ChannelManager manager=new ChannelManager();
		   NettyClient client=new NettyClient(manager);
		   manager.setClient(client);
		   NettyServer server=new NettyServer(Paras.servers.get(Paras.id).getPort(),manager); 
		   manager.start();
		   
	   }catch(Exception e){
		   MainRun.mainlogger.error(e.getLocalizedMessage(), e);
		   throw e;
	   }
	   
   }


   /**
    * get config.inner from file
 * @throws Exception 
    */
	private static void getConfig() throws Exception{
		Properties pros=new Properties();
		pros.load(new FileInputStream("config"));
		Paras.id=(Integer.parseInt(pros.getProperty("id")));
		String str=pros.getProperty("servers");
		String [] ss=str.split(",");
		for(String s:ss){
			String[] args=s.split(":");
			ServerInfo si=new ServerInfo();
			si.setHost(args[1]);
			si.setId(Integer.parseInt(args[0]));
			si.setPort(Integer.parseInt(args[2]));
			Paras.servers.put(si.getId(), si);
		}
		
	}


	

}
