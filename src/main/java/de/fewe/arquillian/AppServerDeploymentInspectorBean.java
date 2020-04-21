/*
 * Copyright (c) COR&FJA AG. All Rights Reserved.
 */

package de.fewe.arquillian;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * Implementation of @AppServerDeploymentInspector as EJB
 */
@javax.ejb.Singleton
@Startup
@javax.ejb.Remote(de.fewe.arquillian.AppServerDeploymentInspector.class)
public class AppServerDeploymentInspectorBean implements AppServerDeploymentInspector {

	private static final Logger logger = Logger.getLogger(AppServerDeploymentInspectorBean.class.getName());

	Context ic = null;

	@PostConstruct
	void init() {
		try {
			ic = new InitialContext();
		}
		catch (NamingException e) {
			logger.severe("error init()" + e);
		}
	}

	public String lookupJndi(String jndiName) {
		try {
			return ic.lookup(jndiName).toString();
		}
		catch (NamingException e) {
			logger.severe("error lookupJndi for :" + jndiName + "/" + e);
			return null;
		}
	}

	public Map<String, String> getJvmConfig() {

		Map<String, String> toReturn = new HashMap<String, String>();

		for (Object key : System.getProperties().keySet()) {
			toReturn.put((String) key, System.getProperties().getProperty((String) key));
		}

		return toReturn;
	}

	public Map<String, String> getEnvConfig() {
		Map<String, String> toReturn = new HashMap<String, String>();

		// strange i've done it for aix/linux, else i got: serializable exception if i return it directly!
		for (String key : System.getenv().keySet()) {
			toReturn.put(key, System.getenv().get(key));
		}
		return toReturn;
	}

}
