package org.epiclouds.bean;

import io.netty.channel.Channel;


public class ResponseBean {
	private Channel ch;
	private ResponseType type;
	private byte[] data;
	
	
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public ResponseType getType() {
		return type;
	}
	public void setType(ResponseType type) {
		this.type = type;
	}

	public Channel getCh() {
		return ch;
	}
	public void setCh(Channel ch) {
		this.ch = ch;
	}

	public static enum ResponseType{
		RequestVoteResponse,
		AppendEntriesResponse
	}
}
