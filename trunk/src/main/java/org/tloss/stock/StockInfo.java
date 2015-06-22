package org.tloss.stock;

public class StockInfo {
	@Override
	public String toString() {
		return "StockInfo [id=" + id + ", price=" + price + ", hightPrice="
				+ hightPrice + ", lowPrice=" + lowPrice + "]";
	}
	private String id;
	private int price;
	private int hightPrice;
	private int lowPrice;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getHightPrice() {
		return hightPrice;
	}
	public void setHightPrice(int hightPrice) {
		this.hightPrice = hightPrice;
	}
	public int getLowPrice() {
		return lowPrice;
	}
	public void setLowPrice(int lowPrice) {
		this.lowPrice = lowPrice;
	}
}
