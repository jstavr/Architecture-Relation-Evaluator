package edu.uci.isr.archstudio4.comp.xarchcs.changesetstatusview;

import java.util.List;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT.IChangeReference;
import edu.uci.isr.xarchflat.ObjRef;


/**
 * Interface for ChangeSetStatysView.
 * 
 * @author Kari
 * 
 */
public interface IChangeSetStatusView {	
	
	class ChangeSetStatusData{

		Boolean initialized;
		
		IChangeReference reference;

		/** Keeps track of all change sets that create particular change reference */
		public List<ObjRef> creators;

		/** Keeps track of all change sets that modify particular change reference */
		public List<ObjRef> modifiers;
		
		/** Keeps track of all change sets that remove particular change reference */
		public List<ObjRef> removers;

		
		/**
		 * Constructor
		 */
		public ChangeSetStatusData(IChangeReference reference,
				                   List<ObjRef> creators,
				                   List<ObjRef> modifiers,
				                   List<ObjRef> removers) {
			this.reference = reference;
			this.creators = creators;
			this.modifiers = modifiers;
			this.removers = removers;
		}
	}
	
	
	public ChangeSetStatusData getChangeSetStatusData(ObjRef xArchRef, ObjRef[] changeSetRefs, IChangeReference changeReference);
}
