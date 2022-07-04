package com.nyxus.racoon.init;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nyxus.broker.socket.SocketClient;
import com.nyxus.racoon.config.JsonMain;
import com.nyxus.racoon.config.Socket;
import com.nyxus.racoon.prosa.ProsaConnector;

@Component
public class SocketBuilder {
	
	@Autowired
	private ProsaConnector prosaConnector;
	
//	@Autowired
//	private List<Socket> lineConfiguration;
	
	@Autowired
	private JsonMain jsonMain;
	
	
	private List<SocketClient> socketList;
	
	

//	public  void createSocketClientsFromJson(){
//		List<SocketClient> sl=new ArrayList<SocketClient>();
//		
//		if(lineConfiguration ==null || lineConfiguration.size() <= 0) {
//			System.out.println("Custom Socket List has 0 ");
//			setSocketList(null);
//			return ;
//		}else {
//			for(Socket lc:lineConfiguration) {
//				SocketClient s= new SocketClient();
//				s.setDestinationIP(lc.getIp());
//				s.setLocalPort(lc.getPortLocal());
//				s.setDestinationPort(lc.getPort());
//				s.setClientId(lc.getDescription());
//				s.setRetryNumber(lc.getRetryNumber());
//				s.setClientId(lc.getId());
//				s.setRetryTime(lc.getRetryTime());
//				s.setResponseTime(lc.getResponseTime());
//				System.out.println("createSocketClientsFromJson - Socket Client: "+s.getClientId());
//				sl.add(s);
//			}
//			socketList=sl;
//			
//		}
//	}
	
	public List<SocketClient> getSocketList() {
		return socketList;
	}

	public  void createSocketClientsFromJsonAdv(){
		List<SocketClient> sl=new ArrayList<SocketClient>();
		if(jsonMain== null|| jsonMain.getProsa() ==null|| jsonMain.getProsa().getSockets()==null || jsonMain.getProsa().getSockets().size() <= 0) {
			System.out.println("Custom Socket List has 0 ");
			setSocketList(null);
			return ;
		}else {
			for(Socket lc:jsonMain.getProsa().getSockets()) {
				SocketClient s= new SocketClient();
				s.setDestinationIP(lc.getIp());
				s.setLocalPort(lc.getPortLocal());
				s.setDestinationPort(lc.getPort());
				s.setClientId(lc.getDescription());
				s.setRetryNumber(lc.getRetryNumber());
				s.setClientId(lc.getId());
				s.setRetryTime(lc.getRetryTime());
				s.setResponseTime(lc.getResponseTime());
				System.out.println("createSocketClientsFromJson - Socket Client: "+s.getClientId());
				sl.add(s);
			}
			
			socketList=sl;
			
		}
	}
	
	@PostConstruct
	public void startSockets() {
		createSocketClientsFromJsonAdv();
		System.out.println("Antes del for "+socketList.size());
		for(SocketClient sc :socketList) {
			sc.setClientEventManager(prosaConnector);
			sc.init();
		}
		prosaConnector.init(socketList.size());
	}

//	
//	public List<SocketClient> getSocketList() {
//		return socketList;
//	}


//
	public void setSocketList(List<SocketClient> socketList) {
		this.socketList = socketList;
	}


}
