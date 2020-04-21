/*
 * Copyright (c) COR&FJA AG. All Rights Reserved.
 */

package de.fewe.arquillian;

import java.util.Map;

/**
 * 
 * Remote Interface for Getting Intern Information from App Server
 */
public interface AppServerDeploymentInspector {

	/**
	 * JVM/System-Parameter of an Application Server.
	 * 
	 * @return map of key values.
	 */
	public Map<String, String> getJvmConfig();

	/**
	 * lookup from EJB.
	 * 
	 * @param jndiName
	 * @return value for jndiName.
	 */
	public String lookupJndi(String jndiName);

	/**
	 * Environment-Variables of an Application Server.
	 * 
	 * @return map of key values.
	 */
	public Map<String, String> getEnvConfig();

}
