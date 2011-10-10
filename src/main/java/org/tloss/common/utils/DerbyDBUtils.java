package org.tloss.common.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DerbyDBUtils {
	public static final String URL = "jdbc:derby:d:/work/article/article";

	protected static Connection getConnection() throws SQLException {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			Connection conn = null;
			Properties connectionProps = new Properties();
			connectionProps.put("user", "");
			connectionProps.put("password", "");
			conn = DriverManager.getConnection(URL, connectionProps);
			return conn;
		} catch (ClassNotFoundException e) {

		}
		return null;

	}

	public static void save(String url, String source) throws SQLException {
		Connection con = getConnection();
		if (con != null) {
			PreparedStatement stmt = null;
			String query = "insert into article(url,source) values(?,?)";
			try {
				stmt = con.prepareStatement(query);
				stmt.setString(1, url);
				stmt.setString(2, source);
				stmt.executeUpdate();
				con.commit();

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			}
		}
	}

	public static boolean checkExisted(String url) throws SQLException {
		Connection con = getConnection();
		if (con != null) {
			PreparedStatement stmt = null;
			String query = "select * from article where url =?";
			try {
				stmt = con.prepareStatement(query);
				stmt.setString(1, url);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					return true;
				}
			} catch (SQLException e) {

			} finally {
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
			}
		}
		return false;
	}

	public static void main(String[] args) throws SQLException {
		System.out
				.println(checkExisted("http://download.oracle.com/javase/tutorial/jdbc/basics/retrieving.html"));
		save("http://download.oracle.com/javase/tutorial/jdbc/basics/retrieving.html",
				"oracle");
		System.out
				.println(checkExisted("http://download.oracle.com/javase/tutorial/jdbc/basics/retrieving.html"));

	}
}
