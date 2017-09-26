package org.epiclouds.handlers.util;

import io.netty.channel.Channel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.epiclouds.bean.AppendEntries;
import org.epiclouds.bean.AppendEntriesResponse;
import org.epiclouds.bean.Log;
import org.epiclouds.bean.RequestBean;
import org.epiclouds.bean.RequestBean.RequestType;
import org.epiclouds.bean.RequestVote;
import org.epiclouds.bean.RequestVoteResponse;
import org.epiclouds.bean.ResponseBean;
import org.epiclouds.bean.ResponseBean.ResponseType;
import org.epiclouds.bean.ServerInfo;
import org.epiclouds.client.main.MainRun;
import org.epiclouds.netty.NettyClient;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
/**
 * the manager to manage the channel
 * @author xianglong
 *
 */
public class ChannelManager implements Runnable{


	private Kryo kryo = new Kryo();

	/**
	 * Persistent state on all servers:
		(Updated on stable storage before responding to RPCs)
	 */
	private int currentTerm=0;
	private int votedFor=0;
	private int votedNum=0;
	private List<Log> logs=new LinkedList<Log>();
	
	private State state=State.Follower;
	/**
	 * index of highest log entry known to be committed (initialized to 0, increases monotonically)
	 */
	private int commitIndex;
	/**
	 * index of highest log entry applied to state machine (initialized to 0, increases monotonically)
	 */
	private int lastApplied;
	
	private int leaderId;
	
	//bytebuffer
	private byte[] buffer=new byte[4096];
	
	private int election_timeout=new Random().nextInt(Paras.MAX_ELECTION_TIMEOUT-Paras.MIN_ELECTION_TIMEOUT)+Paras.MIN_ELECTION_TIMEOUT;
	private long currentTime=System.currentTimeMillis();
	
	private NettyClient client;
	/**
	 * the queue to contain the requests
	 */
	private LinkedBlockingQueue<RequestBean> requestQue=new LinkedBlockingQueue<RequestBean>(5000);
	private LinkedBlockingQueue<ResponseBean> responseQue=new LinkedBlockingQueue<ResponseBean>(5000);
	

	
	public void start(){
		Executors.newSingleThreadExecutor().execute(this);
	}
	
	public void addRequest(RequestBean bean){
		try {
			this.requestQue.put(bean);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addResponse(ResponseBean bean){
		try {
			this.responseQue.put(bean);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {		
		while(true){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handleTimeOut();
			/**
			 * handle request
			 */
			for(int i=0;i<Paras.REQUEST_HANDLE_NUM;i++){
				RequestBean bean=this.requestQue.poll();
				if(bean==null){
					break;
				}
				handleRequestBean(bean);
				
			}
			/**
			 * handle response
			 */
			for(int i=0;i<Paras.RESPONSE_HANDLE_NUM;i++){
				ResponseBean bean=this.responseQue.poll();
				if(bean==null){
					break;
				}
				handleResponseBean(bean);
			}
		}
	}
	
	private void handleTimeOut() {
		if(this.state==State.Leader){
			if(System.currentTimeMillis()-this.currentTime>=Paras.HEART_TIMEOUT){
				MainRun.mainlogger.info("leader "+Paras.id+" send heartbeat:currentTerm:"+this.currentTerm);
				this.currentTime=System.currentTimeMillis();
				sendHeartBeats();
			}
		}else{
			if(this.state==State.Follower){
				if(System.currentTimeMillis()-this.currentTime>=this.election_timeout){
					MainRun.mainlogger.info("follower timeout "+Paras.id+" start to vote :currentTerm:"+this.currentTerm);
					startVotes();
				}
			}else{
				if(this.state==State.Candidate){
					if(System.currentTimeMillis()-this.currentTime>=this.election_timeout){
						MainRun.mainlogger.info("Candidate timeout "+Paras.id+" start to vote:currentTerm:"+this.currentTerm);
						startVotes();
					}
				}
			}
		}
	}
	
	private void adjustTerm(int term){
		MainRun.mainlogger.info("Adjust term :"+Paras.id+": return to follower");
		this.votedFor=0;
		votedNum=0;
		this.currentTime=System.currentTimeMillis();
		election_timeout=new Random().nextInt(Paras.MAX_ELECTION_TIMEOUT-Paras.MIN_ELECTION_TIMEOUT)+Paras.MIN_ELECTION_TIMEOUT;
		this.currentTerm=term;
		if(this.state!=State.Follower){
			this.state=State.Follower;
		}
	}
	
	private void startVotes() {
		this.currentTime=System.currentTimeMillis();
		election_timeout=new Random().nextInt(Paras.MAX_ELECTION_TIMEOUT-Paras.MIN_ELECTION_TIMEOUT)+Paras.MIN_ELECTION_TIMEOUT;
		this.state=State.Candidate;
		this.currentTerm++;
		this.votedFor=Paras.id;
		this.votedNum=1;
		sendRequestVotes();
	}

	/**
	 * handle response
	 * @param bean
	 */
	private void handleResponseBean(ResponseBean bean) {
		Input input = new Input(bean.getData());
		if(bean.getType()==ResponseType.AppendEntriesResponse){
			AppendEntriesResponse entry = kryo.readObject(input, AppendEntriesResponse.class);
		    input.close();
			if(entry.getTerm()<this.currentTerm){
				return;
			}
		    handleAppendEntriesResponse(bean.getCh(),entry);
		    return;
		}
		
		if(bean.getType()==ResponseType.RequestVoteResponse){
			RequestVoteResponse entry = kryo.readObject(input, RequestVoteResponse.class);
		    input.close();
		    if(entry.getTerm()<this.currentTerm){
				return;
			}
		    handleRequestVoteResponse(bean.getCh(),entry);
		    return;
		}
	}
	private void handleRequestVoteResponse(Channel ch, RequestVoteResponse entry) {
		/**
		 * If RPC request or response contains term T > currentTerm:
		 * set currentTerm = T, convert to follower
		 */
		if(this.currentTerm<entry.getTerm()){
			adjustTerm(entry.getTerm());
		}
		if(this.state==State.Candidate){
			MainRun.mainlogger.info("Candidate "+Paras.id+" receive vote response "+entry.getTerm()+":"+entry.isVoteGranted());
			if(entry.getTerm()==this.currentTerm&&entry.isVoteGranted()&&(++votedNum)>Paras.ALL_SERVER/2){
				MainRun.mainlogger.info("Candidate "+Paras.id+" become leader");
				this.state=State.Leader;
				this.currentTime=System.currentTimeMillis();
				election_timeout=new Random().nextInt(Paras.MAX_ELECTION_TIMEOUT-Paras.MIN_ELECTION_TIMEOUT)+Paras.MIN_ELECTION_TIMEOUT;
				this.votedFor=0;
				this.votedNum=0;
				/**
				 * send heartbeat
				 */
				sendHeartBeats();
			}
		}
	}

	private void sendHeartBeats() {
		for(ServerInfo server:Paras.servers.values()){
			if(server.getId()==Paras.id){
				continue;
			}
			AppendEntries ae=new AppendEntries();
			ae.setLeaderCommit(this.commitIndex);
			ae.setLeaderId(Paras.id);
			ae.setTerm(this.currentTerm);
			Output output = new Output(buffer);
		    kryo.writeObject(output, ae);
		    output.flush();
		    RequestBean request=new RequestBean();
		    request.setType(RequestType.AppendEntries);
		    request.setData(Arrays.copyOf(output.getBuffer(), (int) output.total()));
		    output.close();
		    client.sendRequest(server, request);
		}
	}
	
	private void sendRequestVotes() {
		for(ServerInfo server:Paras.servers.values()){
			if(server.getId()==Paras.id){
				continue;
			}
			RequestVote rv=new RequestVote();
			rv.setCandidateId(Paras.id);
			rv.setTerm(currentTerm);
			Output output = new Output(buffer);
		    kryo.writeObject(output, rv);
		    output.flush();
		    RequestBean request=new RequestBean();
		    request.setType(RequestType.RequestVote);
		    request.setData(Arrays.copyOf(output.getBuffer(), (int) output.total()));
		    output.close();
		    client.sendRequest(server, request);
		}
	}

	private void handleAppendEntriesResponse(Channel ch,
			AppendEntriesResponse entry) {
		/**
		 * If RPC request or response contains term T > currentTerm:
		 * set currentTerm = T, convert to follower
		 */
		if(this.currentTerm<entry.getTerm()){
			adjustTerm(entry.getTerm());
		}
		if(this.state==State.Leader){
			//todo some thing
		}
	}

	/**
	 * handle request
	 * @param bean
	 */
	private void handleRequestBean(RequestBean bean) {
		Input input = new Input(bean.getData());
		if(bean.getType()==RequestType.AppendEntries){
			AppendEntries entry = kryo.readObject(input, AppendEntries.class);
		    input.close();
		    handleAppendEntries(bean.getCh(),entry);
		    return;
		}
		
		if(bean.getType()==RequestType.RequestVote){
			RequestVote entry = kryo.readObject(input, RequestVote.class);
		    input.close();
		    handleRequestVote(bean.getCh(),entry);
		    return;
		}
	}

	private void handleRequestVote(Channel ch, RequestVote entry) {
		/**
		 * If RPC request or response contains term T > currentTerm:
		 * set currentTerm = T, convert to follower
		 */
		if(this.currentTerm<entry.getTerm()){
			adjustTerm(entry.getTerm());
		}
		/**
		 * If votedFor is null or candidateId, and candidate’s log is at
		 * least as up-to-date as receiver’s log, grant vote
		 */
		if(this.state==State.Follower){
			if(this.votedFor==0){
				votedFor=entry.getCandidateId();
			}
			RequestVoteResponse rvr=new RequestVoteResponse();
			rvr.setTerm(this.currentTerm);
			/**
			 * Reply false if term < currentTerm
			 */
			if(entry.getTerm()<this.currentTerm){
				rvr.setVoteGranted(false);
			}else{
				/**
				 * not vote for this term
				 */
				if(votedFor==0||votedFor==entry.getCandidateId()){
					/**
					 * candidate’s log is at least as up-to-date as receiver’s log
					 */
					if(this.logs.size()==0||entry.getLastLogTerm()>this.logs.get(this.logs.size()-1).getTerm()
							||((entry.getLastLogTerm()==this.logs.get(this.logs.size()-1).getTerm())
									&&entry.getLastLogIndex()>=this.logs.size())){
						rvr.setVoteGranted(true);
					}else{
						rvr.setVoteGranted(false);
					}
				/**
				 * voted for this term
				 */
				}else{
					rvr.setVoteGranted(false);
				}
			}
			Output output = new Output(buffer);
		    kryo.writeObject(output, rvr);
		    output.flush();
		    ResponseBean response=new ResponseBean();
			response.setType(ResponseType.RequestVoteResponse);
		    response.setData(Arrays.copyOf(output.getBuffer(), (int) output.total()));
		    output.close();
		    ch.writeAndFlush(response);
		/**
		 * not a follower,do nothing
		 */
		}
		
	}

	
	private void handleAppendEntries(Channel ch,AppendEntries entry) {
		/**
		 * If RPC request or response contains term T > currentTerm:
		 * set currentTerm = T, convert to follower
		 */
		if(this.currentTerm<entry.getTerm()){
			adjustTerm(entry.getTerm());
		}
		/**
		 * If the leader’s term (included in its RPC) is at least 
		 * as large as the candidate’s current term, then the candidate 
		 * recognizes the leader as legitimate and returns to follower state
		 */
		if(this.state==State.Candidate&&this.currentTerm<=entry.getTerm()){
			MainRun.mainlogger.info("candidate "+Paras.id+" find leader:"+entry.getLeaderId());
			adjustTerm(entry.getTerm());
			return;
		}
		/**
		 * heart beat
		 */
		if(entry.getEntries()==null||entry.getEntries().size()==0){
			//hearbeat
			handleHeartBeat(ch,entry);
		}else{
			// todo handle others
			if(entry.getTerm()<this.currentTerm){
				return;
			}
		}
		
	}

	private void handleHeartBeat(Channel ch,AppendEntries entry) {
		MainRun.mainlogger.info("follower "+Paras.id+" handle heartbeat from  "+entry.getLeaderId()+":"+entry.getTerm());
		if(this.state==State.Follower){
			this.currentTime=System.currentTimeMillis();
			AppendEntriesResponse aa=new AppendEntriesResponse();
			aa.setSuccess(true);
			aa.setTerm(this.currentTerm);
			Output output = new Output(buffer);
		    kryo.writeObject(output, aa);
		    output.flush();
		    ResponseBean response=new ResponseBean();
			response.setType(ResponseType.AppendEntriesResponse);
		    response.setData(Arrays.copyOf(output.getBuffer(), (int) output.total()));
		    output.close();
		    ch.writeAndFlush(response);
		}
	}




	public NettyClient getClient() {
		return client;
	}

	public void setClient(NettyClient client) {
		this.client = client;
	}




	public static enum State{
		Follower,Candidate,Leader
	}
	
	
}
