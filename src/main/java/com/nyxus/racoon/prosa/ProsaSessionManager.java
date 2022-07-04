package com.nyxus.racoon.prosa;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nyxus.broker.socket.SocketClient;
import com.nyxus.mensaje.bean.MessagePROSA;
import com.nyxus.mensaje.exceptions.NyxusSwitchException;
import com.nyxus.mensaje.services.coder.CoderMessage;
import com.nyxus.racoon.config.JsonMain;
import com.nyxus.racoon.dao.SessionInfo;
import com.nyxus.racoon.init.SocketBuilder;
import com.nyxus.racoon.util.Utils;

@Component
public class ProsaSessionManager {

	@Autowired
	private CoderMessage coderMessage;

	@Autowired
	private SocketBuilder socketBuilder;

	@PreDestroy
	private void LogOut() {
		System.out.println("INICIO TERMINANDO WILDFLY");

		System.out.println("Ejecucion de echoTestManager cada " + jsonMain.getProsa().getEchoInterval());
		for (SocketClient sc : socketBuilder.getSocketList()) {
			send800AndWaitResponse(sc, true, Type800.LOG_OUT);
		}

		System.out.println("FIN TERMINANDO WILDFLY");

	}

	@Autowired
	private JsonMain jsonMain;

	public static enum SessionResult {
		SUCCESS, RETRY, RESET,
	}

	public static enum Type800 {
		LOGIN(1), ECHO_TEST(301), LOG_OUT(2);

		private final int type;

		private Type800(int levelCode) {
			this.type = levelCode;
		}

		public int getType() {
			return type;

		}

	}

	Integer auditNumber = 0;
	private final int MILLION = 1000000;
	private final String PATTERN_GREENWICH = "MMddHHmmss";
//	private boolean isLogged;
	private Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<String, SessionInfo>();

	private ExecutorService executorService;

	public void init(Integer socketClients) {
		executorService = Executors.newFixedThreadPool(socketClients);
	}

	public void send800AndWaitResponse(SocketClient client, boolean isFirst, Type800 type800) {
		MessagePROSA message800 = build800Message(type800);
		try {
			ByteBuffer bb800 = coderMessage.code(message800);
			client.sendData(bb800);

			if (type800 == Type800.LOGIN)
				login(client, message800, isFirst);
			if (type800 == Type800.ECHO_TEST)
				echoTest(client, message800, isFirst);
			if (type800 == Type800.LOG_OUT)
				logOut(client, message800, isFirst);
			System.out.println("Mandamos mensaje 800 a PROSA PARA Operacion del typo : " + type800.getType()+
					"Msg number: "+message800.getCampo(11)
			);
		} catch (NyxusSwitchException e2) {
			e2.printStackTrace();
		}

	}

	public SessionResult validateSessionInfo(MessagePROSA mp, SocketClient sc) {
		SessionInfo si = sessionInfoMap.get(sc.getClientId());
		if (si != null) {
			SessionResult res = si.validate(mp.getCampo(70), mp.getCampo(11), sc);
			if (res == SessionResult.SUCCESS) {
				System.out.println("Cancelando el futuro de sucess");
				si.getFuture().cancel(true);
				si.setFuture(null);
				//TOFO
				
				si.setCounter(1);
			}
			return res;
		}
		return null;
	}
	
	
	public void login(SocketClient sc, MessagePROSA mp, boolean isFirst) {
		String field11 = mp.getCampo(11);
		SessionInfo newSessionInfo=null;
		
		if (isFirst) {
			newSessionInfo = new SessionInfo();
			newSessionInfo.setStatus(SessionInfo.StatusInfo.WAITING_RESPONSE_LOG_IN);
			newSessionInfo.setMessageId(field11);
			newSessionInfo.setCounter(1);
		} else {
			String clientId = sc.getClientId();
			newSessionInfo = sessionInfoMap.get(clientId);
			Integer counter = newSessionInfo.getCounter();
			newSessionInfo.setCounter(++counter);
			if (counter >= sc.getRetryNumber()) {
				System.out.println("No se recibio respuesta despues de " + counter
						+ " intentos , Reiniciando Cliente : " + clientId);
				sc.restart();
			}
		}
		newSessionInfo.setStatus(SessionInfo.StatusInfo.WAITING_RESPONSE_LOG_IN);
		newSessionInfo.setMessageId(field11);
		sessionInfoMap.put(sc.getClientId(), newSessionInfo);
		final SessionInfo si = newSessionInfo;

		Future<Void> future = executorService.submit(() -> {
			try {
				Thread.sleep(sc.getResponseTime());
				// ESPERANDO QUE SE CANCELE CANCELE HILO ; CUANDO LLEGA 810

			} catch (InterruptedException e) {
				System.out.println("RECIBIMOS RESPUESTA EXITOSA DEL LOG IN - PRIMERO");
				return null;
			}
			si.setStatus(SessionInfo.StatusInfo.NO_SESSION);
			// TODO CUANDO NO LLEGSA EL MENSAJE 810 DE PROSA
			System.out.println("No se Recibio respuesta a login " + sc.getClientId() + " con msgId: " + field11);
			try {
				Thread.sleep(sc.getRetryTime());
			} catch (InterruptedException e) {
				System.out.println("Espera de " + sc.getRetryTime() + " para enviar log in");
			}

			send800AndWaitResponse(sc, false, Type800.LOGIN);
			return null;
		});
		newSessionInfo.setFuture(future);

	}

	@Value("${config.datasource}")
	private String jndiName;

	@Scheduled(fixedRateString = "#{ (jsonMain.getProsa().getEchoInterval()) }", initialDelay = 30000)
	public void echoTestManager() {
		System.out.println("|||| Scheduled |||| Ejecucion de echoTestManager cada " + jsonMain.getProsa().getEchoInterval());
		
		for (SocketClient sc : socketBuilder.getSocketList()) {
			String clientId = sc.getClientId();
			if (clientId == null )return;
			SessionInfo newSessionInfo = sessionInfoMap.get(clientId);
			if(newSessionInfo == null) return; 
			if(! (newSessionInfo.getStatus().equals(SessionInfo.StatusInfo.LOGGED_IN))) {
				System.out.println("|||| Scheduled |||| No se lanzo tarea programada ya que estamos en: "+newSessionInfo.getStatus()) ;
				return ;
			}else {
				System.out.println("|||| Scheduled |||| Si se lanzo tarea programada ya que estamos en: "+newSessionInfo.getStatus()) ;
				send800AndWaitResponse(sc, true, Type800.ECHO_TEST);
			}
		}
	}

	public void echoTest(SocketClient sc, MessagePROSA mp, boolean isFirst) {
		SessionInfo newSessionInfo = null;
		String clientId = sc.getClientId();
		// 1.- si esta logeado lanzamos chec
		if (clientId != null ) {
			newSessionInfo = sessionInfoMap.get(clientId);
			if(newSessionInfo.isLogged) {
				String field11 = mp.getCampo(11);
	
				//RESETEAR CONTADOR 
				// CREAADO POR LOG IN
				Integer counter = newSessionInfo.getCounter();
				if (isFirst) {
					newSessionInfo.setCounter(1);
					newSessionInfo.setStatus(SessionInfo.StatusInfo.WAITING_RESPONSE_ECHO_TEST);
					newSessionInfo.setMessageId(field11);
				} else {
					
					newSessionInfo.setCounter(++counter);
					newSessionInfo.setMessageId(field11);
				}
	
				if (counter >= sc.getRetryNumber()) {
					System.out.println("No se recibio respuesta de Echo Test despues de " + counter
							+ " intentos , Reiniciando Cliente : " + clientId);
					sc.restart();
				}
	//			newSessionInfo.setStatus(SessionInfo.StatusInfo.WAITING_RESPONSE_ECHO_TEST);
				sessionInfoMap.put(sc.getClientId(), newSessionInfo);
				final SessionInfo si = newSessionInfo;
				Future<Void> future =executorService.submit(() -> {
					try {
						Thread.sleep(sc.getResponseTime());
					} catch (InterruptedException e) {
						System.out.println("Interrupcion en EchoTest Sleep");
						return null;
					}
					si.setStatus(SessionInfo.StatusInfo.WAITING_RESPONSE_ECHO_TEST);
					System.out.println("|||| No se Recibio respuesta a echotest Client id: " + sc.getClientId() + " con msgId: " + field11+
							" intento: "+si.getCounter()+" de "+sc.getRetryNumber()+" ||||");
					try {
						Thread.sleep(sc.getRetryTime());
					} catch (InterruptedException e) {
						System.out.println("Espera de " + sc.getRetryTime() + " para enviar Echo Test");

					}
					send800AndWaitResponse(sc, false, Type800.ECHO_TEST);
					return null;
				});
				
				newSessionInfo.setFuture(future);

			}
		}
	}

	public void logOut(SocketClient sc, MessagePROSA mp, boolean isFirst) {
		// Lanzamos echo Test y esperamos que responda , si no responde hacemos retry,
				// si no funciona el Rety cerramos sesion
				SessionInfo newSessionInfo = null;
				String clientId = sc.getClientId();
				// 1.- si esta logeado lanzamos chec
				if (clientId != null ) {
					newSessionInfo = sessionInfoMap.get(clientId);
					if(newSessionInfo.isLogged) {
						String field11 = mp.getCampo(11);
			
						// CREAADO POR LOG IN
						Integer counter = newSessionInfo.getCounter();
						if (isFirst) {
							newSessionInfo.setCounter(1);
							newSessionInfo.setStatus(SessionInfo.StatusInfo.WAITING_RESPONSE_LOG_OUT);
							newSessionInfo.setMessageId(field11);
						} else {
							newSessionInfo.setCounter(++counter);
						}
			
						if (counter >= sc.getRetryNumber()) {
							System.out.println("No se recibio respuesta de LogOut despues de " + counter
									+ " intentos , TERMINANDO SERVIDOR : " + clientId);
							return;
						}
			//			newSessionInfo.setStatus(SessionInfo.StatusInfo.WAITING_RESPONSE_ECHO_TEST);
						sessionInfoMap.put(sc.getClientId(), newSessionInfo);
						final SessionInfo si = newSessionInfo;
						
						
						Future<Void> future =executorService.submit(() -> {
							try {
								Thread.sleep(sc.getResponseTime());
							} catch (InterruptedException e) {
								System.out.println("Interrupcion en LogOut Sleep");
								return null;
							}
							si.setStatus(SessionInfo.StatusInfo.WAITING_RESPONSE_ECHO_TEST);
							// TODO CUANDO NO LLEGSA EL MENSAJE 810 DE PROSA
							System.out.println("No se Recibio respuesta a LogOut " + sc.getClientId() + " con msgId: " + field11);
							try {
								Thread.sleep(sc.getRetryTime());
							} catch (InterruptedException e) {
								System.out.println("Espera de " + sc.getRetryTime() + " para enviar LogOut");

							}
							send800AndWaitResponse(sc, false, Type800.LOG_OUT);
							return null;
						});
						newSessionInfo.setFuture(future);
						try {
							future.get(sc.getResponseTime(), TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
	}

//	public MessagePROSA isControlMessage(MessagePROSA mp) {
//		// solicitud de log in , log out , o echo test /810 /
//		int mti = mp.getMti();
//		if (mti == 800) {
//			MessagePROSA mpr = build810Message(mp);
//			return mpr;
//		}
//		if (mti == 810) { // Echo test log in y log out (Campo 70)
//			String field70 = mp.getCampo(70);
//			if (field70.equals("1") || field70.equals("001")) { // TODO
//				isLogged = true;
//				return null;
//			}
//		}
//
//		return null;
//	}

	String getAuditNumber() {
		auditNumber++;
		return Integer.valueOf(auditNumber % MILLION).toString();
	}

	MessagePROSA build800Message(Type800 t800) {
		MessagePROSA message = new MessagePROSA();
		message.setHeaderObject("006000055");
		message.setMti(800);
		message.setCampo(7, Utils.getGMTDate(PATTERN_GREENWICH)); // hora greendwich 10 int MMddHHmmss
		message.setCampo(11, getAuditNumber());
		message.setCampo(70, String.valueOf(t800.getType())); // log in (1), Log out (2), ecxho tech (301)
		return message;

	}

	MessagePROSA build810Message(MessagePROSA mp) {
		MessagePROSA message = new MessagePROSA();
		message.setHeaderObject("006000055");
		message.setMti(810);
		message.setCampo(7, Utils.getGMTDate(PATTERN_GREENWICH)); // hora greendwich 10 int MMddHHmmss
		message.setCampo(11, mp.getCampo(11));
		message.setCampo(39, "00");
		message.setCampo(70, mp.getCampo(70)); // log in (1), Log out (2), ecxho tech (301)
		return message;
	}

//	public boolean isLogged() {
//		return isLogged;
//	}

//	public void setLogged(boolean isLogged) {
//		this.isLogged = isLogged;
//	}
}