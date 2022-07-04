package com.nyxus.racoon.prosa;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nyxus.broker.socket.ClientEventManager;
import com.nyxus.broker.socket.SocketClient;
import com.nyxus.mensaje.bean.MessagePROSA;
import com.nyxus.mensaje.exceptions.ISOException;
import com.nyxus.mensaje.exceptions.NyxusSwitchException;
import com.nyxus.mensaje.services.coder.CoderMessage;
import com.nyxus.mensaje.services.decoder.DecoderMessage;
import com.nyxus.racoon.prosa.ProsaSessionManager.Type800;
import com.nyxus.sw.token.Token;

import con.nyxus.sw.processor.DecoderTokens;

@Component
public class ProsaConnector implements ClientEventManager {
	
	@Autowired
	private ProsaSessionManager prosaSessionManager;
	
	@Autowired
	private DecoderMessage decoderMessage;
	
	@Autowired
	private CoderMessage coderMessage;
	
	@Autowired
		 DecoderTokens decoderTokens;
	
	@Autowired
	private PROSAUtil messageProsa;
	
	@Autowired
	private ProsaSessionManager psm;
	
	private Map<Integer, Action> actionMap = new HashMap<>();

	private static final int TOKENS_ATM=126;
	private static final int TOKENS_POS=63;
	
	private ThreadLocal<Boolean> isATM; 
	
	final Integer BYTE_BUFFER_SIZE=4096;
	ByteBuffer bb= ByteBuffer.allocate(BYTE_BUFFER_SIZE);

	@Override
	public void connectionReady(SocketClient client) {
		prosaSessionManager.send800AndWaitResponse(client,true,Type800.LOGIN);
		System.out.println("connectionReady de PROSA");
	}
	
	public void tryEcho(SocketClient client) {
		
		
//		prosaSessionManager.try(client, null, Type800.ECHO_TEST);
		
	}
	

	
	@FunctionalInterface
	interface Action {
		MessagePROSA processMessage(MessagePROSA mp,SocketClient sc);
	}
	
	@PostConstruct
	private void actionMapCreator() {
		actionMap.put( 800, this::processMessage800 );
		actionMap.put( 810, this::processMessage810 );
	}
	
	@Override
	public void availableData(SocketClient sc,ByteBuffer byteBuffer) {
//		client.getExecutorService().submit(()->client.getClientEventManager().availableData(client,null));
		System.out.println("Available Data");
		MessagePROSA originalMessage= null;
		do {
			try {
			 if((originalMessage= (MessagePROSA)decoderMessage.decode(byteBuffer)) != null) {
				 
				 Action action= actionMap.get(originalMessage.getMti());
				 
				 if(action != null) {
					 MessagePROSA mpResponse=action.processMessage(originalMessage,sc);

					 if(mpResponse != null) {
						 try {
							 ByteBuffer bb=coderMessage.code(mpResponse);
							sc.sendData(bb);
						} catch (NyxusSwitchException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						 
					 }
					 
					 
				 }
			 }
			} catch (ISOException e) {
				e.printStackTrace();
			}
		}while(originalMessage!=null);
		System.out.println("availableData de PROSA");		
		
	}
	
	public void processMessage(MessagePROSA message) {
		//1.- Decodificar TOKENS
		String tokensAsString=generatorTokenString(message);
		if(tokensAsString==null) {
			//Log Motivo de rchazo(Error en Tokens)
			return;
		}
		
		Set<Token> tokensList = null;
		try {
			tokensList = decoderTokens.decode(tokensAsString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		messageProsa.validate(message,tokensList);
	}
	
	@Override
	public void connectionClose(SocketClient client) {
		System.out.println("connectionClose de PROSA");		
	}
	
	public String generatorTokenString(MessagePROSA message) {
		String s;
		if( (s=message.getCampo(TOKENS_ATM)) != null && s.startsWith("&") ) {
			isATM.set(true);
			return s;
		}
		
		if( (s=message.getCampo(TOKENS_POS)) != null && s.startsWith("&") ) {
			isATM.set(false);
			return s;
		}
		
		return null;
	}
	
	public MessagePROSA processMessage800(MessagePROSA mp,SocketClient sc) {
		System.out.println("processMessage800");
			return prosaSessionManager.build810Message(mp);
	}
	
	public MessagePROSA processMessage810(MessagePROSA mp,SocketClient sc) {
		prosaSessionManager.validateSessionInfo(mp,sc);
		return null;
	}

	public void init(Integer numberOfSockets) {
		prosaSessionManager.init(numberOfSockets);		
	}

}
