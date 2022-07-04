package com.nyxus.racoon.config;

public class Socket {
	private String id;
	private String ip;
	private Integer port;
	private String description;
	private Integer portLocal;
	private Integer retryTime;
	private Integer retryNumber;
	private Integer responseTime;

	
	public Integer getResponseTime() {
		return responseTime;
	}


	public void setResponseTime(Integer responseTime) {
		this.responseTime = responseTime;
	}


	public String toString() { 
	    return "Description: '" + this.description + "', Local Port: '" + this.portLocal + "', Port: '" + this.port + "'";
	} 
	
	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public Integer getPortLocal() {
		return portLocal;
	}
	public void setPortLocal(Integer portLocal) {
		this.portLocal = portLocal;
	}
	public Integer getRetryTime() {
		return retryTime;
	}
	public void setRetryTime(Integer retryTime) {
		this.retryTime = retryTime;
	}
	public Integer getRetryNumber() {
		return retryNumber;
	}
	public void setRetryNumber(Integer retryNumber) {
		this.retryNumber = retryNumber;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	

}
