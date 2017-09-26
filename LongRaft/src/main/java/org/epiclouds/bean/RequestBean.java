package org.epiclouds.bean;

import io.netty.channel.Channel;


public class RequestBean {
	private Channel ch;
	private RequestType type;
	private byte[] data;
	
	
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public RequestType getType() {
		return type;
	}
	public void setType(RequestType type) {
		this.type = type;
	}

	public Channel getCh() {
		return ch;
	}
	public void setCh(Channel ch) {
		this.ch = ch;
	}

	public static enum RequestType{
		RequestVote,
		AppendEntries
	}
	
}
