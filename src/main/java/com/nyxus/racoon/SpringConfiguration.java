package com.nyxus.racoon;

import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.nyxus.broker.socket.SocketClient;
import com.nyxus.mensaje.services.coder.CoderMessage;
import com.nyxus.mensaje.services.decoder.DecoderMessage;
import com.nyxus.racoon.config.Socket;
import com.nyxus.racoon.config.JsonConfigurationProcess;
import com.nyxus.racoon.config.JsonMain;

import con.nyxus.sw.processor.DecoderTokens;

@Configuration
@EnableLoadTimeWeaving
@EnableWebMvc
@EnableJpaRepositories
@EnableTransactionManagement
@EnableScheduling
@ComponentScan("com.nyxus")
@PropertySource(value = "file:${config.starter.nyxus}/config/config.properties", ignoreResourceNotFound = false)
public class SpringConfiguration {

	@Value("${config.datasource}")
	private String jndiName;
	
	@Value("${socket.configuration}")
	private String fileLocation;
	
	
	@Bean
	public DataSource dataSource() {
		JndiDataSourceLookup lookup = new JndiDataSourceLookup();
		return lookup.getDataSource(jndiName);
	}

	@Bean("entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

		em.setPersistenceUnitName("RacoonPU");
		em.setDataSource(dataSource());
		em.setPersistenceXmlLocation("/WEB-INF/persistenceConfig.xml");

		JpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());
		return em;
	}

	private Properties additionalProperties() {
		Properties props = new Properties();
		props.setProperty("eclipselink.cache.shared.default", "true");
		return props;
	}

	private JpaDialect jpaDialect() {
		return new EclipseLinkJpaDialect();
	}

	@Bean
	JpaVendorAdapter vendorAdapter() {
		EclipseLinkJpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();

		vendorAdapter.setDatabasePlatform("org.eclipse.persistence.platform.database.PostgreSQLPlatform");
		return vendorAdapter;
	}
	
	@Bean
	@DependsOn("entityManagerFactory")
	public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        transactionManager.setDataSource(dataSource());
        transactionManager.setJpaDialect(jpaDialect());
        return transactionManager;
    }
	
//	PASAR DEL JSON A LISTA DE OBJETOS
//	@Bean
//	public List<Socket> jsonConfiguration() {
//		JsonConfigurationProcess lc= new JsonConfigurationProcess();
//		return lc.config(fileLocation);
//	}
	
	@Bean(name="jsonMain")
	public JsonMain jsonConfigurationAdvanced() {
		JsonConfigurationProcess lc= new JsonConfigurationProcess();
		return lc.configAdvanced(fileLocation);
	}
	
	
	@Bean
	public CoderMessage coderMessage() {
		return new CoderMessage();
	}
	
	@Bean
	public DecoderMessage decoderMessage() {
		return new DecoderMessage();
	}
	
	@Bean
	public DecoderTokens decoderTokens() {
		return new DecoderTokens();
	}
	
	@Bean
	public SocketClient socketClient() {
		return new SocketClient();
	}
	
//	@Bean
//	public ProsaConnector ProsaConnector() {
//		SocketBuilder sb =new SocketBuilder();
//		sb.createSocketClients(getLineConfiguration());
//		return sb;
//	}
	
	
	
	//PASA DE LISTA DE OBJETOS A OBJETO MAESTRO SOCKET
//	@Bean
//	public SocketBuilder socketFactory() {
//		SocketBuilder sb =new SocketBuilder();
//		sb.createSocketClients(getLineConfiguration());
//		return sb;
//	}
	
	
	
//	@Bean(initMethod = "connectSockets")
//	public SocketBuilder connectSockets() throws IOException {
//		SocketBuilder sb =new SocketBuilder();
//		sb.connectSockets();
//		return sb;
//	}
}
