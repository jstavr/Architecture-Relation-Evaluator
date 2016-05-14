package edu.uci.isr.archstudio4.comp.archlight.noticeadt;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.uci.isr.archstudio4.archlight.ArchlightNotice;

public class ArchlightNoticeADT implements IArchlightNoticeADT{
	public static final int MAX_NOTICE_SIZE = 250;

	protected List toolNoticeList = new ArrayList();

	public ArchlightNoticeADT(){
		super();
	}
	
	public void addNotices(String toolID, String[] messages){
		ArchlightNotice[] ttns = new ArchlightNotice[messages.length];
		for(int i = 0; i < messages.length; i++){
			ttns[i] = new ArchlightNotice(toolID, messages[i], null, null);
		}
		addNotices(ttns);
	}
	
	public void addNotice(String toolID, String message){
		ArchlightNotice ttn = new ArchlightNotice(toolID, message, null, null);
		addNotice(ttn);
	}
	
	public void addNotice(String toolID, String message, String additionalDetail){
		ArchlightNotice ttn = new ArchlightNotice(toolID, message, additionalDetail, null);
		addNotice(ttn);
	}
	
	public void addNotice(String toolID, String message, Throwable error){
		ArchlightNotice ttn = new ArchlightNotice(toolID, message, null, error);
		addNotice(ttn);
	}
	
	public void addNotice(String toolID, String message, String additionalDetail, Throwable error){
		ArchlightNotice ttn = new ArchlightNotice(toolID, message, additionalDetail, error);
		addNotice(ttn);
	}
	
	public synchronized void addNotice(ArchlightNotice ttn){
		toolNoticeList.add(ttn);
		fireNoticesAdded(new ArchlightNotice[]{ttn});
		if(toolNoticeList.size() > MAX_NOTICE_SIZE){
			ArchlightNotice removedNotice = (ArchlightNotice)toolNoticeList.remove(0);
			fireNoticesRemoved(new ArchlightNotice[]{removedNotice});
		}
	}
	
	public synchronized void addNotices(ArchlightNotice[] ttns){
		for(int i = 0; i < ttns.length; i++){
			toolNoticeList.add(ttns[i]);
			if(toolNoticeList.size() > MAX_NOTICE_SIZE){
				ArchlightNotice removedNotice = (ArchlightNotice)toolNoticeList.remove(0);
				fireNoticesRemoved(new ArchlightNotice[]{removedNotice});
			}
		}
		fireNoticesAdded(ttns);
	}
	
	public ArchlightNotice[] getAllNotices(){
		return (ArchlightNotice[])toolNoticeList.toArray(new ArchlightNotice[toolNoticeList.size()]);
	}
	
	protected Vector listeners = new Vector();
	
	public void addArchlightNoticeADTListener(ArchlightNoticeADTListener l){
		listeners.add(l);
	}
	
	public void removeArchlightNoticeADTListener(ArchlightNoticeADTListener l){
		listeners.remove(l);
	}
	
	protected void fireNoticesAdded(ArchlightNotice[] notices){
		fireEvent(ArchlightNoticeADTEvent.NOTICES_ADDED, notices);
	}
	
	protected void fireNoticesRemoved(ArchlightNotice[] notices){
		fireEvent(ArchlightNoticeADTEvent.NOTICES_REMOVED, notices);
	}
	
	protected void fireEvent(int type, ArchlightNotice[] issues){
		ArchlightNoticeADTListener[] ls = (ArchlightNoticeADTListener[])listeners.toArray(new ArchlightNoticeADTListener[listeners.size()]);
		ArchlightNoticeADTEvent evt = new ArchlightNoticeADTEvent(type, issues);
		for(int i = 0; i < ls.length; i++){
			((ArchlightNoticeADTListener)ls[i]).noticeADTChanged(evt);
		}
	}

}
