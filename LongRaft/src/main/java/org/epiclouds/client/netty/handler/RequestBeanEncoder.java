package org.epiclouds.client.netty.handler;

import org.epiclouds.bean.RequestBean;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RequestBeanEncoder extends MessageToByteEncoder<RequestBean>{
	
	private static ThreadLocal<Kryo> kryos=new ThreadLocal<Kryo>(){

		@Override
		protected Kryo initialValue() {
			return new Kryo();
		}
		
	};
	private Output buffer=new Output(16000);
	@Override
	protected void encode(ChannelHandlerContext ctx, RequestBean msg, ByteBuf out)
			throws Exception {
		Kryo k=kryos.get();
		k.writeObject(buffer, msg);
		out.writeInt(buffer.position());
		out.writeBytes(buffer.getBuffer(), 0, buffer.position());
		buffer.clear();
		
	}

}
