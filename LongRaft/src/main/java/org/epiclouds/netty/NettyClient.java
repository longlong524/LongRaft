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
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.epiclouds.bean.RequestBean;
import org.epiclouds.bean.ServerInfo;
import org.epiclouds.client.netty.handler.NettyClientHandler;
import org.epiclouds.client.netty.handler.RequestBeanEncoder;
import org.epiclouds.client.netty.handler.ResponseBeanDecoder;
import org.epiclouds.handlers.util.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discards any incoming data.
 */
public class NettyClient {

	public  static Logger mainlogger = LoggerFactory.getLogger(NettyClient.class);

    private Bootstrap sb=new Bootstrap();
    private EventLoopGroup workers=new NioEventLoopGroup(Runtime.getRuntime()
			.availableProcessors());

    public  NettyClient(final ChannelManager manager) throws Exception {   
	        sb.group(workers).channel(NioSocketChannel.class).
	        option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception { 
					ChannelPipeline pipeline = ch.pipeline();
			        /**
			         * http-response解码器
			         * http服务器端对response解码
			         */
			       pipeline.addLast(new ResponseBeanDecoder());


			        /**
			         * http服务器端对request编码
			         */
			       pipeline.addLast( new RequestBeanEncoder());
			   
			       pipeline.addLast(new NettyClientHandler(manager));
				}	
	        });
	        System.out.println("client started");
    }
    
    public void sendRequest(final ServerInfo psb,final RequestBean request){
    	if(psb.getCh()!=null&&psb.getCh().isActive()){
    		psb.getCh().writeAndFlush(request);
    	}else{
	    	ChannelFuture cf=sb.connect(psb.getHost(), psb.getPort());
	    	cf.addListener(new GenericFutureListener<Future<? super Void>>() {
	
				@Override
				public void operationComplete(Future<? super Void> future)
						throws Exception {
					if(future.isSuccess()){
						Channel n=((ChannelFuture) future).channel();
						n.writeAndFlush(request);
						psb.setCh(n);
					}
				}
	    		
			});
    	}
    }
    
    public void close(){
    	workers.shutdownGracefully();
    }


}
