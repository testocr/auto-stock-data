package org.tloss.stock;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.tloss.stock.bvsc.BVSC;
import org.tloss.stock.strade.Strade;

public class StockManager {
	private static JavaSender defaultJavaSender = new SenderClient.Builder(
			"https://www.runningchild.com:28443/ag-push/").build();

	protected static void sendErrorNotification(String info, Exception e) {
		UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
				.pushApplicationId("d7ebb4ad-1cda-40a8-8bbb-8fe958d636f3")
				.masterSecret("68dc7315-410e-447f-b242-6e7c90b04944")
				.alert(info + " : " + e.getMessage()).build();
		defaultJavaSender.send(unifiedMessage, new MessageResponseCallback() {

			public void onComplete(int statusCode) {
				// do cool stuff
			}

			public void onError(Throwable throwable) {
				// bring out the bad news
				throwable.fillInStackTrace();
			}
		});
	}

	BVSC bvsc = new BVSC();
	Strade strade = new Strade();

	public boolean init() {
		return bvsc.login("001C502292",
				RSAUtils.getPassword("/password.properties"))
				&& strade.login("017C102285",
						RSAUtils.getPassword("/strade.properties"));
	}

	public void buy() {
		try {
			DataSource dataSource = DataSource.getInstance();
			Connection connection = null;
			Statement statement = null;
			ResultSet resultSet = null;
			try {
				connection = dataSource.getConnection();
				statement = connection.createStatement();
				resultSet = statement
						.executeQuery("SELECT * FROM stock WHERE symbol='HQC' AND type=1 ");
				while (resultSet.next()) {
					bvsc.buyStock("HQC", resultSet.getInt("price") - 100,
							resultSet.getInt("volume"),
							RSAUtils.getPin("/password.properties"));
				}
			} catch (SQLException e) {
				sendErrorNotification("Error at Main", e);
				e.printStackTrace();
			} finally {
				if (resultSet != null)
					try {
						resultSet.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				if (statement != null)
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				if (connection != null)
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			}
		} catch (IOException e) {
			sendErrorNotification("Error at Main", e);
			e.printStackTrace();
		} catch (SQLException e) {
			sendErrorNotification("Error at Main", e);
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			sendErrorNotification("Error at Main", e);
			e.printStackTrace();
		}
	}

	public void buyAndSell() {
		StockInfo info = bvsc.getStockInfo("HQC");
		if (info != null) {
			bvsc.buyStock("HQC", info.getPrice() - 100, 150,
					RSAUtils.getPin("/password.properties"));
			strade.sellStock("HQC", info.getPrice() + 100, 150, "");
		}

	}

	public void sell() {
		try {
			DataSource dataSource = DataSource.getInstance();
			Connection connection = null;
			Statement statement = null;
			ResultSet resultSet = null;
			try {
				connection = dataSource.getConnection();
				statement = connection.createStatement();
				resultSet = statement
						.executeQuery("SELECT * FROM stock WHERE symbol='HQC' AND type=0 ");
				while (resultSet.next()) {
					strade.sellStock("HQC", resultSet.getInt("price") + 100,
							resultSet.getInt("volume"), "");
				}
			} catch (SQLException e) {
				sendErrorNotification("Error at Main", e);
				e.printStackTrace();
			} finally {
				if (resultSet != null)
					try {
						resultSet.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				if (statement != null)
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				if (connection != null)
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			}
		} catch (IOException e) {
			sendErrorNotification("Error at Main", e);
			e.printStackTrace();
		} catch (SQLException e) {
			sendErrorNotification("Error at Main", e);
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			sendErrorNotification("Error at Main", e);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		StockManager manager = new StockManager();
		if (manager.init()) {
			manager.buy();
			manager.sell();
			manager.buyAndSell();
		}
	}
}
