/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.io.Serializable;
import java.util.Date;

import org.eclipse.jface.viewers.ISelection;

/**
 * @author Hazel
 *
 */
public interface ISelectionModel extends Serializable {
	
	public String getView();
	public void setView(String view);
	public String getElement();
	public void setElement(String element);
	public Date getTimeStamp();
	public void setTimeStamp(Date timeStamp);
	public String getProperty();
	public void setProperty(String property);
	public ISelection getSelectedItem();
	public void setSelectedItem(ISelection selectedItem);
	public Integer getGroupNum();
	public void setGroupNum(Integer groupNum);
	public String getRelationship();
	public void setRelationship(String relationship);
}
