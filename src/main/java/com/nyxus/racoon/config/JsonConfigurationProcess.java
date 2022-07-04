package com.nyxus.racoon.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonConfigurationProcess  {

//	final static String FILE_LOCATION_LINE_CONFIG="src/main/java/com/nyxus/racoon/resources/socketConfiguration.json";

	public String location;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<Socket> config(String fileLocation) {
		List<Socket> customSocketList = null;
		try {
//		    System.out.println("Working Directory = " + System.getProperty("user.dir"));
			ObjectMapper mapper = new ObjectMapper();
			customSocketList = Arrays.asList(mapper.readValue(new File(fileLocation), Socket[].class));
			System.out.println("Custom Json Configuration Created: " + customSocketList.size());
			customSocketList.forEach(System.out::println);

		} catch (Exception ex) {
			System.out.println("Error Fatal en Archivo Json ANTIGUO, Se Apagara servidor");
			System.out.println("Resultado de Shutdown: "+programaticShutdown());
			
			
			ex.printStackTrace();
			return null;
		}
		return customSocketList;
	}

	public JsonMain configAdvanced(String fileLocation) {
		JsonMain jsonMain = null;
		try {
//		    System.out.println("Working Directory = " + System.getProperty("user.dir"));
			ObjectMapper mapper = new ObjectMapper();

			jsonMain = mapper.readValue(new File(fileLocation), JsonMain.class);
//		    System.out.println("Custom Json Configuration Created: "+customSocketList.size());
			jsonMain.getProsa().getSockets().forEach(System.out::println);

		} catch (Exception ex) {
			System.out.println("Error Fatal en Archivo Json, Se Apagara servidor");
			System.out.println("Resultado de Shutdown: "+programaticShutdown());
			ex.printStackTrace();
			return null;
		}
		return jsonMain;
	}

	public static void main(String[] args) {
		new JsonConfigurationProcess().configAdvanced("/Users/rrodriguez/racoon/config/socketConfiguration.json");

	}

	public Integer programaticShutdown() {
		Runtime.getRuntime().halt(0);
		return 1;
		}

}
