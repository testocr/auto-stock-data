package org.tloss.ws;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class Application {
	VelocityEngine ve;
	VelocityContext vecontext;
	private Logger logger = LoggerFactory.getLogger(Application.class);
	DataSource dataSource;

	public DataSource initDataSource() throws Exception {

		Properties dbProps = new Properties();
		try {
			InputStream is = Application.class
					.getResourceAsStream("/jdbc.properties");
			dbProps.load(is);
			is.close();
		} catch (Exception e) {
			throw new IOException("Could not read properties file");
		}
		try {
			ComboPooledDataSource cpds = new ComboPooledDataSource();
			cpds.setDriverClass(dbProps.getProperty("db.driver"));
			cpds.setJdbcUrl(dbProps.getProperty("db.url"));
			cpds.setUser(dbProps.getProperty("db.user"));
			String pass = dbProps.getProperty("db.password", null);
			if (pass != null) {
				cpds.setPassword(pass);
			}
			return cpds;
		} catch (Exception e) {
			throw e;
		}

	}

	public void init() throws Exception {
		logger.info("Init application");
		ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		ve.init();
		vecontext = new VelocityContext();
		WSHelper helper = new WSHelper();
		helper.init();
		vecontext.put("helper", helper);
		dataSource = initDataSource();
		vecontext.put("dataSource", dataSource);
		vecontext.put("logger", logger);
		logger.info("Init application successfully");
	}

	public void execute() throws ResourceNotFoundException,
			ParseErrorException, MethodInvocationException, IOException {
		logger.info("Execute application");
		Template template = ve.getTemplate("/templates/app.vm", "UTF-8");
		FileWriter writer = new FileWriter("out.log");
		template.merge(vecontext, writer);
		writer.flush();
		writer.close();
		logger.info("Execute application successfully");
	}

	public static void main(String[] args) throws Exception {
		Application application = new Application();
		application.init();
		application.execute();
	}

}
