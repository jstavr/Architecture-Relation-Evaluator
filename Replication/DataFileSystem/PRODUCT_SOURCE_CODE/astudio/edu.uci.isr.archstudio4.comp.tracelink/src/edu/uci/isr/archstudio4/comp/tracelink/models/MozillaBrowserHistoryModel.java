/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.Date;

/**
 * @author Hazel
 *
 */
public class MozillaBrowserHistoryModel implements IMozillaBrowserHistoryModel {
	
	int visitCount;
	Date firstVisitDate;
	Date lastVisitDate;
	String url;
	
	public int getVisitCount() {
		return visitCount;
	}
	public void setVisitCount(int visitCount) {
		this.visitCount = visitCount;
	}
	public Date getFirstVisitDate() {
		return firstVisitDate;
	}
	public void setFirstVisitDate(Date firstVisitDate) {
		this.firstVisitDate = firstVisitDate;
	}
	public Date getLastVisitDate() {
		return lastVisitDate;
	}
	public void setLastVisitDate(Date lastVisitDate) {
		this.lastVisitDate = lastVisitDate;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
