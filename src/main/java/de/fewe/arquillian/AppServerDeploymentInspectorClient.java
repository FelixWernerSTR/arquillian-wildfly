package de.fewe.arquillian;
/*
 * Copyright (c) COR&FJA AG. All Rights Reserved.
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;


/**
 * 
 * Example Client for Invoking the Remote EJB: AppServerDeploymentInspectorBean
 * 
 */
public class AppServerDeploymentInspectorClient {

	private static final Logger logger = Logger.getLogger(AppServerDeploymentInspectorClient.class);

	static String jndiLookupWebsphere = "ejb/ipl-deployment-inspector/deployment-inspector-ejb.jar/AppServerDeploymentInspectorBean#com.fja.ipl.bsf.util.jee.inspector.AppServerDeploymentInspector";
	static String jndiLookupJBoss = "ejb:ipl-deployment-inspector/deployment-inspector-ejb//AppServerDeploymentInspectorBean!com.fja.ipl.bsf.util.jee.inspector.AppServerDeploymentInspector";

	String jndiLookup = null;

	Properties props = null;

	String serverInfo = null;

	private static String REG_EX_IP_PORT = "\\b([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\b:(?<port>[0-9]+)";
	private static String REG_EX_HOST_PORT = "[a-zA-Z0-9.-]+:(?<port>[0-9]+)";

	AppServerDeploymentInspector appServerDeploymentInspector;

	AppServerDeploymentInspectorClient(Properties props) {
		this.props = props;
	}


	public String getJndiLookup() {
		return jndiLookup;
	}

	public void setJndiLookup(String jndiLookup) {
		this.jndiLookup = jndiLookup;
	}

	public String getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(String serverInfo) {
		this.serverInfo = serverInfo;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args != null && args.length > 0) {

			AppServerDeploymentInspectorClient appServerDeploymentTest = new AppServerDeploymentInspectorClient(
					readProperties(args[0]));

			if (isJBoss()) {
				appServerDeploymentTest.setServerInfo(readProperties("jboss-ejb-client.properties")
						.getProperty("remote.connection.default.host") + "/"
						+ readProperties("jboss-ejb-client.properties").getProperty("remote.connection.default.port"));

				logger.info("DIAGNOSTIC FOR JBOSS APPLICATION SERVER: " + appServerDeploymentTest.getServerInfo());
			}
			else {
				appServerDeploymentTest
						.setServerInfo(readProperties("jndi.properties").getProperty("java.naming.provider.url"));
				logger.info("DIAGNOSTIC FOR WEBSPHERE APPLICATION SERVER: " + appServerDeploymentTest.getServerInfo());
			}


			if (args.length == 2) {
				appServerDeploymentTest
						.setJndiLookup(readProperties(args[1]).getProperty("AppServerDeploymentInspector"));
			}

			appServerDeploymentTest.doTest();

			if (logger.isDebugEnabled()) {
				appServerDeploymentTest.getJvmConf();
				appServerDeploymentTest.getEnvConf();
			}

		}
		else {
			logger.info("Usage: some-test.properties is mandatory parameter");
		}

	}

	private void getJvmConf() {
		logger.debug("JVM-CONFIGURATION OF THE APPLICATION SERVER:");
		Map<String, String> mapJvm = getAppServerDeploymentInspector().getJvmConfig();
		logger.debug(mapJvm);

		if (logger.isTraceEnabled()) {
			Properties propsJvm = new Properties();

			propsJvm.putAll(mapJvm);

			try {
				propsJvm.store(new FileOutputStream("jvm.properties"),
					"jvm properties of the application server: " + getServerInfo());
			}
			catch (IOException e) {
				logger.info("error getJvmConf() " + e);
			}
		}

	}

	private void getEnvConf() {
		logger.debug("ENVIRONMENT-CONFIGURATION OF THE APPLICATION SERVER:");
		logger.debug(getAppServerDeploymentInspector().getEnvConfig());

		Map<String, String> mapEnv = getAppServerDeploymentInspector().getEnvConfig();
		logger.debug(mapEnv);

		if (logger.isTraceEnabled()) {
			Properties propsEnv = new Properties();

			propsEnv.putAll(mapEnv);

			try {
				propsEnv.store(new FileOutputStream("env.properties"),
					"environment variables of the application server: " + getServerInfo());
			}
			catch (IOException e) {
				logger.info("error getEnvConf() " + e);
			}
		}
	}


	private void doTest() {

		for (Entry<Object, Object> entry : props.entrySet()) {


			if (((String) entry.getValue()).matches(REG_EX_IP_PORT)
					|| ((String) entry.getValue()).matches(REG_EX_HOST_PORT)) {
				doReachableConnectTest((String) entry.getValue());
			}
			else {
				doJndiTest((String) entry.getValue());
			}

		}
	}


	private void doJndiTest(String entryValue) {

		logger.debug("lookup for : " + entryValue);

		String valueForJndiLookup = null;

		valueForJndiLookup = getAppServerDeploymentInspector().lookupJndi(entryValue);

		String failed = "";

		if (valueForJndiLookup == null) {
			failed = "FAILED ";
		}

		logger.info(failed + "JNDI LOOKUP FOR: " + entryValue + " " + valueForJndiLookup);
	}

	private static void doReachableConnectTest(String entryValue) {


		logger.debug("reachable connect for : " + entryValue);

		String test = null;

		test = setUpConnection(entryValue.split(":")[0], Integer.parseInt(entryValue.split(":")[1]));

		String failed = "";

		if (test == null) {
			failed = "FAILED ";
		}

		logger.info(failed + "REACHABLE CONNECT FOR: " + entryValue + " " + test);

	}

	private static String setUpConnection(String host, int port) {

		InetAddress address;

		try {
			address = InetAddress.getByName(host);
			boolean reachable = address.isReachable(port);

			logger.debug("Is host reachable? " + reachable);

			logger.debug("Name: " + address.getHostName());
			logger.debug("Addr: " + address.getHostAddress());
			logger.debug("Reach: " + address.isReachable(3000));
		}
		catch (IOException e) {
			logger.debug("Error testing reachable " + host + " " + e);
			return null;
		}

		Socket client = null;
		try {
			client = new Socket();

			client.setKeepAlive(true);
			client.setSoTimeout(5 * 1000);
			client.setSoLinger(true, 1000);
			client.connect(new InetSocketAddress(host, port), 5 * 1000);

			logger.trace("socket inputstream: " + client.getInputStream());

			client.close();

			logger.debug("socket client connected: " + host + ":" + port);
			return "REACHABLE,CONNECTED " + host + ":" + port;

		}
		catch (UnknownHostException e) {
			logger.debug("Error setting up socket connection: unknown host at " + port + " " + e);
			logger.debug("host: " + host + " port: " + port);
			return "REACHABLE,CONNECTION FAILED " + host + ":" + port;
		}
		catch (IOException e) {
			logger.debug("Error setting up socket connection: " + e);
			logger.debug("host: " + host + " port:" + port);
			return "REACHABLE,CONNECTION FAILED " + host + ":" + port;

		}
		finally {
			try {
				if (client != null) {
					client.close();
				}
			}
			catch (IOException e) {
				logger.debug("Error closing socket connection: " + e);
			}
			finally {
				client = null;
			}
		}
	}

	private static Properties readProperties(String propertiesFile) {

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesFile));
		}
		catch (IOException e) {
			logger.info("error getEnvConf() " + e);
		}
		return properties;
	}


	public AppServerDeploymentInspector getAppServerDeploymentInspector() {

		if (appServerDeploymentInspector != null) {
			return appServerDeploymentInspector;
		}

		if (jndiLookup == null) {
			if (isJBoss()) {
				jndiLookup = jndiLookupJBoss;
			}
			else {
				jndiLookup = jndiLookupWebsphere;
			}
		}

		Properties jndiProperties = readProperties("jndi.properties");

		Context ctx = null;
		AppServerDeploymentInspector o = null;

		try {

			if (jndiProperties.size() > 0) {
				ctx = new InitialContext(jndiProperties);
			}
			else {
				ctx = new InitialContext();
			}

			logger.debug("GETTING AppServerDeploymentInspector with: " + jndiLookup);
			o = (AppServerDeploymentInspector) ctx.lookup(jndiLookup);
		}
		catch (NamingException e) {
			throw new RuntimeException("ERROR GETTING AppServerDeploymentConfigurer!", e);
		}

		return o;
	}

	/**
	 * Check if <em>JBoss</em> is running.
	 * 
	 * @return {@code true}, <em>JBoss</em> is used as the application server; {@code false} if another application
	 *         server is running.
	 */
	public static boolean isJBoss() {
		Properties jndiProperties = readProperties("jndi.properties");
		String pkgs = jndiProperties.getProperty("java.naming.factory.url.pkgs");
		return pkgs != null && pkgs.toUpperCase().contains("JBOSS");
	}


}

