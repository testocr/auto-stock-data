package org.tloss.stock;


/**
 * 
 * @author tungt
 * 
 */
public interface IStock {
	public boolean login(String userName, String password);

	public int getAmount();

	public int getFeePercent();

	public StockOrderPage getNormalOrderList(int page,int numberItems);

	public StockInfo getStockInfo(String id);

	public boolean buyStock(String id, int amount, int volume, String pin);

	public boolean cancelOder(String id, String orderId, String pin);

	public boolean sellStock(String id, int amount, int volume, String pin);

	public boolean logout();
}
