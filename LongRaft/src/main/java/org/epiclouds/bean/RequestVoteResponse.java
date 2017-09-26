package org.epiclouds.bean;



public class RequestVoteResponse {
	private int term;
	private boolean voteGranted;
	public int getTerm() {
		return term;
	}
	public void setTerm(int term) {
		this.term = term;
	}
	public boolean isVoteGranted() {
		return voteGranted;
	}
	public void setVoteGranted(boolean voteGranted) {
		this.voteGranted = voteGranted;
	}
	
}
