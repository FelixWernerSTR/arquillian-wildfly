package de.fewe.arquillian;

import java.io.File;
import java.io.Serializable;

import javax.ejb.EJB;

import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.fewe.arquillian.AppServerDeploymentInspector;
import de.fewe.arquillian.AppServerDeploymentInspectorBean;


/**
 * 
 */
@RunWith(Arquillian.class)
public class ApplicationServerDeploymentInspectorEjbTest implements Serializable {

	private static final long serialVersionUID = -7386306336804828623L;

	private static final transient Logger logger = Logger
			.getLogger(ApplicationServerDeploymentInspectorEjbTest.class.getName());

	@Deployment(name = "fewe-deployment-inspector")
	public static Archive<?> createDeploymentPackage() {

		// add your ejb jar for testing
		JavaArchive deployment_configurer = ShrinkWrap.create(JavaArchive.class, "deployment-inspector-ejb.jar")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml").addClass(AppServerDeploymentInspector.class)
				.addClass(AppServerDeploymentInspectorBean.class)
				.addClass(ApplicationServerDeploymentInspectorEjbTest.class);

		final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class)
				.setApplicationXML("application.xml").addAsModule(deployment_configurer);

		File[] libs = Maven.configureResolver()
				.loadPomFromFile("src/main/resources/pom.xml")
				.importDependencies(ScopeType.COMPILE).resolve().withoutTransitivity().asFile();
		ear.addAsLibraries(libs);

		return ear;
	}
	
//  After Deployment info like that should be on console:
//	java:global/fewe-deployment-inspector/deployment-inspector-ejb/AppServerDeploymentInspectorBean!de.fewe.arquillian.wildfly.arquillian_wildfly.AppServerDeploymentInspector
//	java:app/deployment-inspector-ejb/AppServerDeploymentInspectorBean!de.fewe.arquillian.wildfly.arquillian_wildfly.AppServerDeploymentInspector
//	java:module/AppServerDeploymentInspectorBean!de.fewe.arquillian.wildfly.arquillian_wildfly.AppServerDeploymentInspector
//	java:jboss/exported/fewe-deployment-inspector/deployment-inspector-ejb/AppServerDeploymentInspectorBean!de.fewe.arquillian.wildfly.arquillian_wildfly.AppServerDeploymentInspector
//	java:global/fewe-deployment-inspector/deployment-inspector-ejb/AppServerDeploymentInspectorBean
//	java:app/deployment-inspector-ejb/AppServerDeploymentInspectorBean
//	java:module/AppServerDeploymentInspectorBean
	               
	@EJB(lookup = "java:global/fewe-deployment-inspector/deployment-inspector-ejb/AppServerDeploymentInspectorBean!de.fewe.arquillian.AppServerDeploymentInspector")
	AppServerDeploymentInspector deploymentInspector;

	@Test
	public void test() throws Exception {

		Assert.assertNotNull(deploymentInspector.lookupJndi(
			"java:global/fewe-deployment-inspector/deployment-inspector-ejb/AppServerDeploymentInspectorBean!de.fewe.arquillian.AppServerDeploymentInspector"));

		Assert.assertNotNull(deploymentInspector.getJvmConfig());

		logger.info("getJvmConfig : " + deploymentInspector.getJvmConfig());

		Assert.assertNotNull(deploymentInspector.getEnvConfig());

		logger.info("getEnvConfig : " + deploymentInspector.getEnvConfig());


	}


}
