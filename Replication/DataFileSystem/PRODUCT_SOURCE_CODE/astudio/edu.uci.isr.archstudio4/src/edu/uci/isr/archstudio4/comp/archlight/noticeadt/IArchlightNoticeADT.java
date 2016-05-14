package edu.uci.isr.archstudio4.comp.archlight.noticeadt;

import edu.uci.isr.archstudio4.archlight.ArchlightNotice;

public interface IArchlightNoticeADT{

	public void addNotices(String toolID, String[] messages);

	public void addNotice(String toolID, String message);

	public void addNotice(String toolID, String message, String additionalDetail);

	public void addNotice(String toolID, String message, Throwable error);

	public void addNotice(String toolID, String message, String additionalDetail,
		Throwable error);

	public void addNotice(ArchlightNotice ttn);

	public void addNotices(ArchlightNotice[] ttns);

	public ArchlightNotice[] getAllNotices();

}