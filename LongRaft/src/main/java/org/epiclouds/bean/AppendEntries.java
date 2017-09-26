package org.epiclouds.bean;

import java.nio.ByteBuffer;
import java.util.List;

public class AppendEntries {
	private int term;
	private int leaderId;
	private int prevLogIndex;
	private int prevLogTerm;
	private List<ByteBuffer> entries;
	private int leaderCommit;
	public int getTerm() {
		return term;
	}
	public void setTerm(int term) {
		this.term = term;
	}
	public int getLeaderId() {
		return leaderId;
	}
	public void setLeaderId(int leaderId) {
		this.leaderId = leaderId;
	}
	public int getPrevLogIndex() {
		return prevLogIndex;
	}
	public void setPrevLogIndex(int prevLogIndex) {
		this.prevLogIndex = prevLogIndex;
	}
	public int getPrevLogTerm() {
		return prevLogTerm;
	}
	public void setPrevLogTerm(int prevLogTerm) {
		this.prevLogTerm = prevLogTerm;
	}
	public List<ByteBuffer> getEntries() {
		return entries;
	}
	public void setEntries(List<ByteBuffer> entries) {
		this.entries = entries;
	}
	public int getLeaderCommit() {
		return leaderCommit;
	}
	public void setLeaderCommit(int leaderCommit) {
		this.leaderCommit = leaderCommit;
	}
	
	
}
