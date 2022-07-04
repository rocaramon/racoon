package com.nyxus.racoon.dao;

import java.util.concurrent.Future;

import com.nyxus.broker.socket.SocketClient;
import com.nyxus.racoon.prosa.ProsaSessionManager.SessionResult;

public class SessionInfo {
	public StatusInfo status;
	public Integer counter;
	public String messageId;
	public Future<Void> future;
	public boolean isLogged;
	
	
	
	
	
	public boolean isLogged() {
		return isLogged;
	}

	public void setLogged(boolean isLogged) {
		this.isLogged = isLogged;
	}

	public Future<Void> getFuture() {
		return future;
	}

	public void setFuture(Future<Void> future) {
		this.future = future;
	}

	public Integer addCounter() {
		counter=counter+1;
		return counter;
	}
	
	public SessionResult validate(String field70,String field11,SocketClient sc){
		System.out.println("|||| Validate |||| , Campo 11: "+field11+" Campo 70: "+field70+" Estatus: "+status);
		System.out.println("|||| Validate |||| messaId: "+messageId);
		if(status==StatusInfo.WAITING_RESPONSE_LOG_IN && field70.equals("001")) { //810 - 1 -
			if( Integer.valueOf(messageId) ==Integer.valueOf(field11) ) {
				System.out.println("SESION ESTABLECIDA, Cliente: "+sc.getClientId()+" Con IP: "+sc.getDestinationIP()
				+":"+sc.getDestinationPort());
				status=StatusInfo.LOGGED_IN;
				//TODO aqui agregamos el cambio de estado
				isLogged=true;
				return SessionResult.SUCCESS;
			}
			else {
				if(counter < sc.getRetryNumber() ) {
					counter ++;
					System.out.println("SESION REINTENTAR, Cliente: "+sc.getClientId()+" Con IP: "+sc.getDestinationIP()
					+":"+sc.getDestinationPort());
					status=StatusInfo.RETRY;
					 return SessionResult.RETRY;
				}
				else {
					 return SessionResult.RESET;
				}
			}
		}
		if(status==StatusInfo.WAITING_RESPONSE_ECHO_TEST && field70.equals("301")) {
			System.out.println("|||| Validate |||| entro a waiting echo test ");
			if( Integer.valueOf(messageId) ==Integer.valueOf(field11) ) {
				System.out.println("|||| Se recibio Respuesta Echo Test "+field11+"||||");
				status=StatusInfo.LOGGED_IN;
				return SessionResult.SUCCESS;
			}else {
			 System.out.println("|||| Error en Echo Test messageId:"+messageId+" distinto al campo 11:"+field11+" ||||");

			}
		}
		if(status==StatusInfo.WAITING_RESPONSE_LOG_OUT && field70.equals("2")) {
			if( Integer.valueOf(messageId) ==Integer.valueOf(field11) ) {
				System.out.println("|||| Se recibio Respuesta Log Out "+field11+"||||");
				status=StatusInfo.LOGGED_OUT;
				return SessionResult.SUCCESS;
			}
		}
		
		return null;
	}
	
	public static enum StatusInfo {
		  RETRY,
		  NO_SESSION,
		  LOGGED_IN,
		  WAITING_RESPONSE_LOG_IN,
		  WAITING_RESPONSE_ECHO_TEST,
		  WAITING_RESPONSE_LOG_OUT,
		  LOGGED_OUT

		}

	public StatusInfo getStatus() {
		return status;
	}

	public void setStatus(StatusInfo status) {
		this.status = status;
	}

	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer counter) {
		this.counter = counter;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	
	
}
