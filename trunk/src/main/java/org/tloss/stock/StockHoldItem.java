package org.tloss.stock;

public class StockHoldItem {
	private int avlqtty;
	private int securities_receiving_t0;
	private int securities_receiving_t1;
	private int securities_receiving_t2;
	private int securities_receiving_t3;
	private int securities_sending_t0;
	private int securities_sending_t1;
	private int securities_sending_t2;
	private int securities_sending_t3;
	private String afacctno;
	private String symbol;

	public int getAvlqtty() {
		return avlqtty;
	}

	public void setAvlqtty(int avlqtty) {
		this.avlqtty = avlqtty;
	}

	public int getSecurities_receiving_t0() {
		return securities_receiving_t0;
	}

	public void setSecurities_receiving_t0(int securities_receiving_t0) {
		this.securities_receiving_t0 = securities_receiving_t0;
	}

	public int getSecurities_receiving_t1() {
		return securities_receiving_t1;
	}

	public void setSecurities_receiving_t1(int securities_receiving_t1) {
		this.securities_receiving_t1 = securities_receiving_t1;
	}

	public int getSecurities_receiving_t2() {
		return securities_receiving_t2;
	}

	public void setSecurities_receiving_t2(int securities_receiving_t2) {
		this.securities_receiving_t2 = securities_receiving_t2;
	}

	public int getSecurities_receiving_t3() {
		return securities_receiving_t3;
	}

	public void setSecurities_receiving_t3(int securities_receiving_t3) {
		this.securities_receiving_t3 = securities_receiving_t3;
	}

	public int getSecurities_sending_t0() {
		return securities_sending_t0;
	}

	public void setSecurities_sending_t0(int securities_sending_t0) {
		this.securities_sending_t0 = securities_sending_t0;
	}

	public int getSecurities_sending_t1() {
		return securities_sending_t1;
	}

	public void setSecurities_sending_t1(int securities_sending_t1) {
		this.securities_sending_t1 = securities_sending_t1;
	}

	public int getSecurities_sending_t2() {
		return securities_sending_t2;
	}

	public void setSecurities_sending_t2(int securities_sending_t2) {
		this.securities_sending_t2 = securities_sending_t2;
	}

	public int getSecurities_sending_t3() {
		return securities_sending_t3;
	}

	public void setSecurities_sending_t3(int securities_sending_t3) {
		this.securities_sending_t3 = securities_sending_t3;
	}

	public String getAfacctno() {
		return afacctno;
	}

	public void setAfacctno(String afacctno) {
		this.afacctno = afacctno;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return "StockHoldItem [avlqtty=" + avlqtty
				+ ", securities_receiving_t0=" + securities_receiving_t0
				+ ", securities_receiving_t1=" + securities_receiving_t1
				+ ", securities_receiving_t2=" + securities_receiving_t2
				+ ", securities_receiving_t3=" + securities_receiving_t3
				+ ", securities_sending_t0=" + securities_sending_t0
				+ ", securities_sending_t1=" + securities_sending_t1
				+ ", securities_sending_t2=" + securities_sending_t2
				+ ", securities_sending_t3=" + securities_sending_t3
				+ ", afacctno=" + afacctno + ", symbol=" + symbol + "]";
	}

}
