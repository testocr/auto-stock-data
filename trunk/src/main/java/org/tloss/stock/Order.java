package org.tloss.stock;

public class Order {

	public static final int WAITTING = 0;
	public static final int A_HAFT_MATCHED = 1;
	public static final int MATCHED = 2;
	public static final int CANCEL = 3;

	public static final String _STATUS_STRING[] = { "WAITTING",
			"A_HAFT_MATCHED", "MATCHED", "CANCEL" };

	private String acctno;
	private Boolean cancelable;
	private String custodycd;
	private String execType;
	private String execTypeDesc;
	private String feedbackMsg;
	private Boolean isDisposal;
	private Integer limitPrice;
	private Boolean modifiable;
	private String orderFrom;
	private String orderID;
	private String orderIDDesc;
	private String orderType;
	private String orderer;
	private Integer priceMatched;
	private Integer priceOrder;
	private Integer qtyCancel;
	private Integer qtyMatched;
	private Integer qtyModified;
	private Integer qtyOrder;
	private Integer qtyRemain;
	private Integer quoteQtty;
	private String rootOrderID;
	private String sessionCd;
	private String status;
	private String statusValue;
	private String symbol;
	private String time;
	private String timeType;
	private String timeTypeValue;

	public int getStatusInt() {
		if (qtyMatched.equals(qtyOrder))
			return MATCHED;
		if (qtyMatched > 0)
			return A_HAFT_MATCHED;
		if (qtyCancel > 0)
			return CANCEL;
		return WAITTING;
	}

	public String getStatusString() {
		return _STATUS_STRING[getStatusInt()];
	}

	/**
	 * 
	 * @return The acctno
	 */
	public String getAcctno() {
		return acctno;
	}

	/**
	 * 
	 * @param acctno
	 *            The acctno
	 */
	public void setAcctno(String acctno) {
		this.acctno = acctno;
	}

	/**
	 * 
	 * @return The cancelable
	 */
	public Boolean getCancelable() {
		return cancelable;
	}

	/**
	 * 
	 * @param cancelable
	 *            The cancelable
	 */
	public void setCancelable(Boolean cancelable) {
		this.cancelable = cancelable;
	}

	/**
	 * 
	 * @return The custodycd
	 */
	public String getCustodycd() {
		return custodycd;
	}

	/**
	 * 
	 * @param custodycd
	 *            The custodycd
	 */
	public void setCustodycd(String custodycd) {
		this.custodycd = custodycd;
	}

	/**
	 * 
	 * @return The execType
	 */
	public String getExecType() {
		return execType;
	}

	/**
	 * 
	 * @param execType
	 *            The execType
	 */
	public void setExecType(String execType) {
		this.execType = execType;
	}

	/**
	 * 
	 * @return The execTypeDesc
	 */
	public String getExecTypeDesc() {
		return execTypeDesc;
	}

	/**
	 * 
	 * @param execTypeDesc
	 *            The execTypeDesc
	 */
	public void setExecTypeDesc(String execTypeDesc) {
		this.execTypeDesc = execTypeDesc;
	}

	/**
	 * 
	 * @return The feedbackMsg
	 */
	public String getFeedbackMsg() {
		return feedbackMsg;
	}

	/**
	 * 
	 * @param feedbackMsg
	 *            The feedbackMsg
	 */
	public void setFeedbackMsg(String feedbackMsg) {
		this.feedbackMsg = feedbackMsg;
	}

	/**
	 * 
	 * @return The isDisposal
	 */
	public Boolean getIsDisposal() {
		return isDisposal;
	}

	/**
	 * 
	 * @param isDisposal
	 *            The isDisposal
	 */
	public void setIsDisposal(Boolean isDisposal) {
		this.isDisposal = isDisposal;
	}

	/**
	 * 
	 * @return The limitPrice
	 */
	public Integer getLimitPrice() {
		return limitPrice;
	}

	/**
	 * 
	 * @param limitPrice
	 *            The limitPrice
	 */
	public void setLimitPrice(Integer limitPrice) {
		this.limitPrice = limitPrice;
	}

	/**
	 * 
	 * @return The modifiable
	 */
	public Boolean getModifiable() {
		return modifiable;
	}

	/**
	 * 
	 * @param modifiable
	 *            The modifiable
	 */
	public void setModifiable(Boolean modifiable) {
		this.modifiable = modifiable;
	}

	/**
	 * 
	 * @return The orderFrom
	 */
	public String getOrderFrom() {
		return orderFrom;
	}

	/**
	 * 
	 * @param orderFrom
	 *            The orderFrom
	 */
	public void setOrderFrom(String orderFrom) {
		this.orderFrom = orderFrom;
	}

	/**
	 * 
	 * @return The orderID
	 */
	public String getOrderID() {
		return orderID;
	}

	/**
	 * 
	 * @param orderID
	 *            The orderID
	 */
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	/**
	 * 
	 * @return The orderIDDesc
	 */
	public String getOrderIDDesc() {
		return orderIDDesc;
	}

	/**
	 * 
	 * @param orderIDDesc
	 *            The orderIDDesc
	 */
	public void setOrderIDDesc(String orderIDDesc) {
		this.orderIDDesc = orderIDDesc;
	}

	/**
	 * 
	 * @return The orderType
	 */
	public String getOrderType() {
		return orderType;
	}

	/**
	 * 
	 * @param orderType
	 *            The orderType
	 */
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	/**
	 * 
	 * @return The orderer
	 */
	public String getOrderer() {
		return orderer;
	}

	/**
	 * 
	 * @param orderer
	 *            The orderer
	 */
	public void setOrderer(String orderer) {
		this.orderer = orderer;
	}

	/**
	 * 
	 * @return The priceMatched
	 */
	public Integer getPriceMatched() {
		return priceMatched;
	}

	/**
	 * 
	 * @param priceMatched
	 *            The priceMatched
	 */
	public void setPriceMatched(Integer priceMatched) {
		this.priceMatched = priceMatched;
	}

	/**
	 * 
	 * @return The priceOrder
	 */
	public Integer getPriceOrder() {
		return priceOrder;
	}

	/**
	 * 
	 * @param priceOrder
	 *            The priceOrder
	 */
	public void setPriceOrder(Integer priceOrder) {
		this.priceOrder = priceOrder;
	}

	/**
	 * 
	 * @return The qtyCancel
	 */
	public Integer getQtyCancel() {
		return qtyCancel;
	}

	/**
	 * 
	 * @param qtyCancel
	 *            The qtyCancel
	 */
	public void setQtyCancel(Integer qtyCancel) {
		this.qtyCancel = qtyCancel;
	}

	/**
	 * 
	 * @return The qtyMatched
	 */
	public Integer getQtyMatched() {
		return qtyMatched;
	}

	/**
	 * 
	 * @param qtyMatched
	 *            The qtyMatched
	 */
	public void setQtyMatched(Integer qtyMatched) {
		this.qtyMatched = qtyMatched;
	}

	/**
	 * 
	 * @return The qtyModified
	 */
	public Integer getQtyModified() {
		return qtyModified;
	}

	/**
	 * 
	 * @param qtyModified
	 *            The qtyModified
	 */
	public void setQtyModified(Integer qtyModified) {
		this.qtyModified = qtyModified;
	}

	/**
	 * 
	 * @return The qtyOrder
	 */
	public Integer getQtyOrder() {
		return qtyOrder;
	}

	/**
	 * 
	 * @param qtyOrder
	 *            The qtyOrder
	 */
	public void setQtyOrder(Integer qtyOrder) {
		this.qtyOrder = qtyOrder;
	}

	/**
	 * 
	 * @return The qtyRemain
	 */
	public Integer getQtyRemain() {
		return qtyRemain;
	}

	/**
	 * 
	 * @param qtyRemain
	 *            The qtyRemain
	 */
	public void setQtyRemain(Integer qtyRemain) {
		this.qtyRemain = qtyRemain;
	}

	/**
	 * 
	 * @return The quoteQtty
	 */
	public Integer getQuoteQtty() {
		return quoteQtty;
	}

	/**
	 * 
	 * @param quoteQtty
	 *            The quoteQtty
	 */
	public void setQuoteQtty(Integer quoteQtty) {
		this.quoteQtty = quoteQtty;
	}

	/**
	 * 
	 * @return The rootOrderID
	 */
	public String getRootOrderID() {
		return rootOrderID;
	}

	/**
	 * 
	 * @param rootOrderID
	 *            The rootOrderID
	 */
	public void setRootOrderID(String rootOrderID) {
		this.rootOrderID = rootOrderID;
	}

	/**
	 * 
	 * @return The sessionCd
	 */
	public String getSessionCd() {
		return sessionCd;
	}

	/**
	 * 
	 * @param sessionCd
	 *            The sessionCd
	 */
	public void setSessionCd(String sessionCd) {
		this.sessionCd = sessionCd;
	}

	/**
	 * 
	 * @return The status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * 
	 * @param status
	 *            The status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 
	 * @return The statusValue
	 */
	public String getStatusValue() {
		return statusValue;
	}

	/**
	 * 
	 * @param statusValue
	 *            The statusValue
	 */
	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
	}

	/**
	 * 
	 * @return The symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * 
	 * @param symbol
	 *            The symbol
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * 
	 * @return The time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * 
	 * @param time
	 *            The time
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * 
	 * @return The timeType
	 */
	public String getTimeType() {
		return timeType;
	}

	/**
	 * 
	 * @param timeType
	 *            The timeType
	 */
	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}

	/**
	 * 
	 * @return The timeTypeValue
	 */
	public String getTimeTypeValue() {
		return timeTypeValue;
	}

	/**
	 * 
	 * @param timeTypeValue
	 *            The timeTypeValue
	 */
	public void setTimeTypeValue(String timeTypeValue) {
		this.timeTypeValue = timeTypeValue;
	}

	@Override
	public String toString() {
		return "Order [acctno=" + acctno + ", cancelable=" + cancelable
				+ ", custodycd=" + custodycd + ", execType=" + execType
				+ ", execTypeDesc=" + execTypeDesc + ", feedbackMsg="
				+ feedbackMsg + ", isDisposal=" + isDisposal + ", limitPrice="
				+ limitPrice + ", modifiable=" + modifiable + ", orderFrom="
				+ orderFrom + ", orderID=" + orderID + ", orderIDDesc="
				+ orderIDDesc + ", orderType=" + orderType + ", orderer="
				+ orderer + ", priceMatched=" + priceMatched + ", priceOrder="
				+ priceOrder + ", qtyCancel=" + qtyCancel + ", qtyMatched="
				+ qtyMatched + ", qtyModified=" + qtyModified + ", qtyOrder="
				+ qtyOrder + ", qtyRemain=" + qtyRemain + ", quoteQtty="
				+ quoteQtty + ", rootOrderID=" + rootOrderID + ", sessionCd="
				+ sessionCd + ", status=" + status + ", statusValue="
				+ statusValue + ", symbol=" + symbol + ", time=" + time
				+ ", timeType=" + timeType + ", timeTypeValue=" + timeTypeValue
				+ ", statusString=" + getStatusString() + ", statusInt="
				+ getStatusInt() + "]";
	}

}