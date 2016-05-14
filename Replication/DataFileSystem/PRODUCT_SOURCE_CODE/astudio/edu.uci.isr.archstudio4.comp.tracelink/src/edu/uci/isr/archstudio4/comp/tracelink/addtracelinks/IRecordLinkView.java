/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.addtracelinks;

import java.util.Collection;

import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.XADLFacade;
import edu.uci.isr.archstudio4.comp.tracelink.models.ISelectionModel;

/**
 * @author Hazel
 *
 */
public interface IRecordLinkView {

	public void invokeRecordView(Shell shell, Collection<ISelectionModel> selectionList, String archSelectedID);
	public void serializeLinks(Collection<ISelectionModel> selectionList);
}
