package org.epiclouds.client.netty.handler;

import java.util.List;

import org.epiclouds.bean.ResponseBean;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ResponseBeanDecoder extends ByteToMessageDecoder{
	
	private static ThreadLocal<Kryo> kryos=new ThreadLocal<Kryo>(){

		@Override
		protected Kryo initialValue() {
			return new Kryo();
		}
		
	};
	private Input buffer=new Input(16000);
	
	private int len=0;
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		if(len==0){
			if(in.readableBytes()>=4){
				len=in.readInt();
			}
		}
		
		if(len>0){
			if(in.readableBytes()>=len){
				in.readBytes(buffer.getBuffer(), 0, len);
				buffer.setPosition(0);
				buffer.setLimit(len);
				Kryo k=kryos.get();
				ResponseBean r=k.readObject(buffer, ResponseBean.class);
				out.add(r);
				len=0;
			}
		}
		
	}

	

}
