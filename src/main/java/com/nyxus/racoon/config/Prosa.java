package com.nyxus.racoon.config;

import java.util.List;

public class Prosa {
	
	public List<Socket> sockets;
	
	public Integer echoInterval;

	public List<Socket> getSockets() {
		return sockets;
	}

	public void setSockets(List<Socket> sockets) {
		this.sockets = sockets;
	}

	public Integer getEchoInterval() {
		return echoInterval;
	}

	public void setEchoInterval(Integer echoInterval) {
		this.echoInterval = echoInterval;
	}
	
	
	

}
