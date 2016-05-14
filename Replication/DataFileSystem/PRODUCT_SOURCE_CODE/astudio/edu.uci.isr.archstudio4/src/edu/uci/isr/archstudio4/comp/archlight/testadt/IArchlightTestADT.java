package edu.uci.isr.archstudio4.comp.archlight.testadt;

import edu.uci.isr.archstudio4.archlight.ArchlightTest;

public interface IArchlightTestADT{

	public ArchlightTest[] getAllTests();
	public ArchlightTest[] getAllTests(String toolID);
	public ArchlightTest getTest(String testUID);
	public void addTests(ArchlightTest[] tests);
	public void removeTests(ArchlightTest[] tests);

}