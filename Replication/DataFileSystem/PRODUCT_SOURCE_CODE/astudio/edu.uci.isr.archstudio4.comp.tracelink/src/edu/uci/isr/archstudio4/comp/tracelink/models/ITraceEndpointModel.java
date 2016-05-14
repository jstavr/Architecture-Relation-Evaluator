/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.Date;

/**
 * @author dpurpura
 *
 */
public interface ITraceEndpointModel extends ITracelinkElement {
	public String getAuthor();
	public void setAuthor(String author);
	public String getCaptureMode();
	public void setCaptureMode(String captureMode);
	public String getLocationType();
	public void setLocationType(String locationType);
	public String getLocationHref();
	public void setLocationHref(String locationHref);
	public Date getTimestamp();
	public void setTimestamp(Date timestamp);
	public String getStatus();
	public void setStatus(String status);
	public String getRelationship();
	public void setRelationship(String relationship);
	public String getDescription();
	public void setDescription(String description);
	public boolean equals(Object o);
	public boolean hasKey(String key);
	

}
