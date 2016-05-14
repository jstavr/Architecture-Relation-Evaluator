/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.Date;

/**
 * @author Hazel
 *
 */
public interface IMozillaBrowserHistoryModel {
	
	public void setVisitCount(int visitCount);
	public int getVisitCount();
	public void setFirstVisitDate(Date visitDate);
	public Date getFirstVisitDate();
	public void setLastVisitDate(Date visitDate);
	public Date getLastVisitDate();
	public void setUrl(String url);
	public String getUrl();

}
