/**
 * 
 */
package com.google.gwt.sample.stockwatcher.client;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * @author shai
 * 
 */
public class StockPrice extends BaseModel {
	
	private static final long serialVersionUID = -3690155438484559052L;
	
	public StockPrice() {
		super();
	}

	public StockPrice(String symbol, double price, double change) {
		super();
		set("change", change);
	    set("symbol", symbol);
	    set("price", price);
	}

	/**
	 * @return the symbol
	 */
	public String getSymbol() {
		return get("symbol");
	}

	/**
	 * @param symbol the symbol to set
	 */
	public void setSymbol(String symbol) {
		set("symbol", symbol);
	}

	/**
	 * @return the price
	 */
	public double getPrice() {
		return get("price");
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(double price) {
		set("price", price);
	}

	/**
	 * @return the change
	 */
	public double getChange() {
		return get("change");
	}

	/**
	 * @param change the change to set
	 */
	public void setChange(double change) {
		set("change", change);
	}

	public double getChangePercent() {
		return 100.0 * this.getChange() / this.getPrice();
	}
	
	public int hashCode() {
		if (getSymbol() != null)
			return this.getSymbol().hashCode();
		else
			return -1;
	}
	
	public boolean equals(Object o) {
		if (o != null && (o instanceof StockPrice)) {
			if (((StockPrice)o).getSymbol().equals(this.getSymbol()))
				return true;
		}
		return false;
	}
}
