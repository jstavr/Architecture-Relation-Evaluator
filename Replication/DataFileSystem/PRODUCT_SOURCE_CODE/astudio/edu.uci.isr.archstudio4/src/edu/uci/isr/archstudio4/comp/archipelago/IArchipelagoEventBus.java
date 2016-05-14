package edu.uci.isr.archstudio4.comp.archipelago;

public interface IArchipelagoEventBus{

	public void addArchipelagoEventListener(IArchipelagoEventListener l);
	public void removeArchipelagoEventListener(IArchipelagoEventListener l);

	public void fireArchipelagoEvent(IArchipelagoEvent evt);
}