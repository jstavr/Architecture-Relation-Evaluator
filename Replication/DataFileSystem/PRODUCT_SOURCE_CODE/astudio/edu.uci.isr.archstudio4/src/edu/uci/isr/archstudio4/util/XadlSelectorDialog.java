package edu.uci.isr.archstudio4.util;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class XadlSelectorDialog{

	public static ObjRef showSelectorDialog(Shell parentShell, String text, XArchFlatInterface xarch, IResources resources, ObjRef rootRef, int showFlags, int selectionFlags){
		ObjRef[] results = showSelectorDialog(parentShell, text, xarch, resources, rootRef, showFlags, selectionFlags, false);
		if(results == null){
			return null;
		}
		return results[0];
	}

	public static ObjRef[] showSelectorDialog(Shell parentShell, String text, XArchFlatInterface xarch, IResources resources, ObjRef rootRef, int showFlags, int selectionFlags, final boolean allowMultipleSelections){
		final Shell dialog = new Shell(parentShell, SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		dialog.setText(text);

		dialog.setLayout(new GridLayout(1, false));

		int treeViewerFlags = allowMultipleSelections ? SWT.MULTI | SWT.BORDER : SWT.SINGLE | SWT.BORDER;

		final TreeViewer treeViewer = new TreeViewer(dialog, treeViewerFlags);
		treeViewer.setContentProvider(new XadlTreeContentProvider(xarch, rootRef, showFlags));
		treeViewer.setLabelProvider(new XadlTreeLabelProvider(xarch, resources));
		treeViewer.setSorter(new ViewerSorter());
		treeViewer.setInput(new XadlTreeContentProvider.XadlTreeInput());
		treeViewer.expandAll();

		GridData treeData = new GridData();
		treeData.horizontalAlignment = GridData.FILL;
		treeData.grabExcessHorizontalSpace = true;
		treeData.verticalAlignment = GridData.FILL;
		treeData.grabExcessVerticalSpace = true;
		treeData.widthHint = 400;
		treeData.heightHint = 300;
		treeViewer.getControl().setLayoutData(treeData);

		Composite cButtons = new Composite(dialog, SWT.NONE);
		GridLayout cButtonsLayout = new GridLayout(2, false);
		cButtonsLayout.horizontalSpacing = 5;
		cButtonsLayout.marginTop = 5;
		cButtonsLayout.marginBottom = 5;
		cButtonsLayout.marginLeft = 5;
		cButtonsLayout.marginRight = 5;
		cButtons.setLayout(cButtonsLayout);

		cButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		Button bOK = new Button(cButtons, SWT.PUSH);
		bOK.setText("OK");
		GridData bOKData = new GridData();
		bOKData.horizontalAlignment = GridData.FILL;
		//bOKData.grabExcessHorizontalSpace = true;
		bOKData.widthHint = 100;
		bOK.setLayoutData(bOKData);

		final java.util.List<ObjRef> results = new ArrayList<ObjRef>();

		final XArchFlatInterface fxarch = xarch;
		final int fselectionFlags = selectionFlags;
		final Listener okListener = new Listener(){

			public void handleEvent(Event event){
				IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
				if(!validateSelection(fxarch, selection, fselectionFlags)){
					MessageBox messageBox = new MessageBox(dialog, SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("Error - Invalid Selection");
					messageBox.setMessage("Please select one " + (allowMultipleSelections ? "or more " : "") + "of the following: " + getSelectionString(fselectionFlags) + ".");
					messageBox.open();
				}
				else{
					for(Iterator it = selection.iterator(); it.hasNext();){
						results.add((ObjRef)it.next());
					}
					dialog.close();
				}
			}
		};
		bOK.addListener(SWT.Selection, okListener);

		treeViewer.addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event){
				okListener.handleEvent(null);
			}
		});

		Button bCancel = new Button(cButtons, SWT.PUSH);
		bCancel.setText("Cancel");
		bCancel.setSize(bCancel.computeSize(100, SWT.DEFAULT));
		GridData bCancelData = new GridData();
		bCancelData.horizontalAlignment = GridData.FILL;
		//bCancelData.grabExcessHorizontalSpace = true;
		bCancelData.widthHint = 100;
		bCancel.setLayoutData(bCancelData);

		Listener cancelListener = new Listener(){

			public void handleEvent(Event event){
				dialog.close();
			}
		};
		bCancel.addListener(SWT.Selection, cancelListener);
		dialog.pack();
		dialog.open();

		while(!dialog.isDisposed()){
			if(!dialog.getDisplay().readAndDispatch()){
				dialog.getDisplay().sleep();
			}
		}

		//IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if(results.size() == 0){
			return null;
		}
		else{
			ObjRef[] resultArray = results.toArray(new ObjRef[results.size()]);
			return resultArray;
		}
	}

	protected static String getSelectionString(int flags){
		String[] strings = XadlTreeUtils.typesToStrings(flags);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < strings.length; i++){
			if(i != 0){
				if(i == strings.length - 1){
					sb.append(" or ");
				}
				else{
					sb.append(", ");
				}
			}
			sb.append(strings[i]);
		}
		return sb.toString();
	}

	protected static boolean validateSelection(XArchFlatInterface xarch, IStructuredSelection selection, int selectionFlags){
		Object[] selectedObjects = selection.toArray();
		if(selectedObjects.length == 0){
			return false;
		}
		for(Object element: selectedObjects){
			if(element instanceof ObjRef){
				ObjRef ref = (ObjRef)element;
				int typeOfRef = XadlTreeUtils.getType(xarch, ref);
				if((typeOfRef & selectionFlags) == 0){
					return false;
				}
			}
		}
		return true;
	}
}
