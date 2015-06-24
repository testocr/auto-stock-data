package org.tloss.stock;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSource {

	private static DataSource datasource;
	private ComboPooledDataSource cpds;

	private DataSource() throws IOException, SQLException,
			PropertyVetoException {

		Properties props = new Properties();
		InputStream fis = null;

		fis = DBConnection.class.getResourceAsStream("/db.properties");
		props.load(fis);

		cpds = new ComboPooledDataSource();
		cpds.setDriverClass(props.getProperty("DB_DRIVER_CLASS")); // loads the
																	// jdbc
																	// driver
		cpds.setJdbcUrl(props.getProperty("DB_URL"));
		cpds.setUser(props.getProperty("DB_USERNAME"));
		if (props.getProperty("DB_PASSWORD") != null
				&& !props.getProperty("DB_PASSWORD").equals(""))
			cpds.setPassword(props.getProperty("DB_PASSWORD"));

		// the settings below are optional -- c3p0 can work with defaults
		cpds.setMinPoolSize(5);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(20);
		cpds.setMaxStatements(180);

	}

	public static DataSource getInstance() throws IOException, SQLException,
			PropertyVetoException {
		if (datasource == null) {
			datasource = new DataSource();
			return datasource;
		} else {
			return datasource;
		}
	}

	public Connection getConnection() throws SQLException {
		return this.cpds.getConnection();
	}

}
