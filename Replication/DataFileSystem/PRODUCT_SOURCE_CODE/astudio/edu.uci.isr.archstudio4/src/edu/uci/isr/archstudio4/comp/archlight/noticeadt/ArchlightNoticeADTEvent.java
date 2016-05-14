package edu.uci.isr.archstudio4.comp.archlight.noticeadt;

import edu.uci.isr.archstudio4.archlight.ArchlightNotice;

public class ArchlightNoticeADTEvent implements java.io.Serializable{

	public static final int NOTICES_ADDED = 100;
	public static final int NOTICES_REMOVED = 200;
	
	protected int type;
	protected ArchlightNotice[] notices;
	
	public ArchlightNoticeADTEvent(int type, ArchlightNotice[] notices){
		this.type = type;
		this.notices = notices;
	}
	
	public ArchlightNotice[] getNotices(){
		return notices;
	}

	public int getEventType(){
		return type;
	}

}
