package org.tloss.stock;

import java.util.ArrayList;
import java.util.List;

public class StockHold {
	List<StockHoldItem> holdItems = new ArrayList<StockHoldItem>();
	int pageNumber;
	int totalRecords;

	public List<StockHoldItem> getHoldItems() {
		return holdItems;
	}

	public void setHoldItems(List<StockHoldItem> holdItems) {
		this.holdItems = holdItems;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

}
