/**
 * @author Administrator
 * @created 2014 2014年8月27日 下午3:04:28
 * @version 1.0
 */
package org.epiclouds.netty;

/**
 * @author Administrator
 *
 */
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.Executors;

import org.epiclouds.client.netty.handler.NettyServerHandler;
import org.epiclouds.client.netty.handler.RequestBeanDecoder;
import org.epiclouds.client.netty.handler.ResponseBeanEncoder;
import org.epiclouds.handlers.util.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discards any incoming data.
 */
public class NettyServer {

	public  static Logger mainlogger = LoggerFactory.getLogger(NettyServer.class);
	

    private EventLoopGroup workers=new NioEventLoopGroup(Runtime.getRuntime()
			.availableProcessors(), Executors.newFixedThreadPool(Runtime
			.getRuntime().availableProcessors()));
    private EventLoopGroup boss=new NioEventLoopGroup(2);

    public  NettyServer(int port,final ChannelManager manager) throws Exception {   
    	ServerBootstrap sb=new ServerBootstrap();
	        sb.group(boss,workers).channel(NioServerSocketChannel.class).
	        option(ChannelOption.SO_KEEPALIVE, true).
	        option(ChannelOption.TCP_NODELAY, true).
	        childOption(ChannelOption.TCP_NODELAY, true) 
	        .childOption(ChannelOption.SO_KEEPALIVE, true)
	        .option(ChannelOption.SO_BACKLOG, 1580)
	        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000).
	        childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					
					 ChannelPipeline pipeline = ch.pipeline();

				        pipeline.addLast( new RequestBeanDecoder());

				       
				        pipeline.addLast(new ResponseBeanEncoder());

				       pipeline.addLast(new NettyServerHandler(manager));
				       
				        
				}	
	        });
	        ChannelFuture f = sb.bind(port).sync(); // (7)
	        System.out.println("server started in port:"+port);
    }
    
   
    
    public void close(){
    	workers.shutdownGracefully();
    }


}
