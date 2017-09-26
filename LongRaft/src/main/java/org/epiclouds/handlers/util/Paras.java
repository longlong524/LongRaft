package org.epiclouds.handlers.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.epiclouds.bean.ServerInfo;

public class Paras {
	public static int id;
	public static Map<Integer,ServerInfo> servers=new ConcurrentHashMap<>();
	public static final int ALL_SERVER=3;


	public static final int MIN_ELECTION_TIMEOUT=500;
	public static final int MAX_ELECTION_TIMEOUT=3000;
	public static final int HEART_TIMEOUT=50;
	public static final int REQUEST_HANDLE_NUM=100;
	public static final int RESPONSE_HANDLE_NUM=100;
}
