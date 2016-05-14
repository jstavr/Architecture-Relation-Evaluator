package edu.uci.isr.archstudio4.comp.archedit;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditor;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.sysutils.SystemUtils;
import edu.uci.isr.widgets.swt.AutoResizeTableLayout;
import edu.uci.isr.xarchflat.IXArchInstanceMetadata;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchMetadataUtils;

public class ArchEditEditor
	extends AbstractArchstudioEditor{

	public static final String[] COLUMN_NAMES = new String[]{"Name", "Value"};

	protected XArchChangeSetInterface xarch;

	public ArchEditEditor(){
		super(ArchEditMyxComponent.class, ArchEditMyxComponent.EDITOR_NAME);
		this.xarch = (XArchChangeSetInterface)comp.getXArchADT(); 
		setBannerInfo(((ArchEditMyxComponent)comp).getIcon(), "Syntax-Directed Architecture Editor");
		setHasBanner(true);
	}

	@Override
	protected AbstractArchstudioOutlinePage createOutlinePage(){
		return new ArchEditOutlinePage(xarch, xArchRef, resources,explicit);
	}

	@Override
	public void createEditorContents(Composite parent){
		ObjRef[] selectedRefs = null;
		if(outlinePage != null){
			selectedRefs = ((ArchEditOutlinePage)outlinePage).getSelectedRefs();
			if(selectedRefs != null){
				//System.err.println("Number of selected refs: "+selectedRefs.length);
				try{
					int ifaces = 0;
					for(ObjRef o : selectedRefs){
						ObjRef[] ifaceRefs = xarch.getAll(o, "interface");
						ifaces += ifaceRefs.length;
					}
					//System.err.println("Number of ifaces: "+ifaces);
				}catch(Exception e){
				}
			}
		}

		if(selectedRefs == null || selectedRefs.length == 0){
			Label lNothingSelected = new Label(parent, SWT.LEFT);
			lNothingSelected.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lNothingSelected.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			lNothingSelected.setText("Select one or more elements in the outline view.");
		}
		else{
			for(ObjRef element: selectedRefs){
				Label lElement = new Label(parent, SWT.LEFT);
				lElement.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				lElement.setFont(resources.getPlatformFont(IResources.PLATFORM_HEADER_FONT_ID));

				IXArchInstanceMetadata instanceMetadata = xarch.getInstanceMetadata(element);
				IXArchTypeMetadata typeMetadata = xarch.getTypeMetadata(element);
				StringBuffer headerLine = new StringBuffer();
				headerLine.append(SystemUtils.capFirst(xarch.getElementName(element)));
				headerLine.append(" (");
				headerLine.append(instanceMetadata.getCurrentContext());
				//headerLine.append("/");
				//headerLine.append(typeMetadata.getTypePackage());
				headerLine.append(":");
				headerLine.append(XArchMetadataUtils.getTypeName(typeMetadata.getType()));
				headerLine.append(")");
				lElement.setText(headerLine.toString());

				if(xarch.isInstanceOf(element, "instance#XMLLink")){
					createDragSourceComposite(parent, element);
				}

				boolean hasAttribute = false;
				for(IXArchPropertyMetadata property: typeMetadata.getProperties()){
					if(property.getMetadataType() == IXArchPropertyMetadata.ATTRIBUTE){
						hasAttribute = true;
						break;
					}
				}

				if(!hasAttribute){
					//No attributes.
					Label lNoAttributes = new Label(parent, SWT.LEFT);
					lNoAttributes.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					lNoAttributes.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
					lNoAttributes.setText("This element has no attributes.");
				}
				else{
					TableViewer tv = new TableViewer(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.NO_FOCUS);
					tv.setContentProvider(new ViewContentProvider());
					tv.setLabelProvider(new ViewLabelProvider());
					tv.setInput(element);

					Table table = tv.getTable();

					TableColumn column = new TableColumn(table, SWT.LEFT);
					column.setText(COLUMN_NAMES[0]);

					TableColumn column2 = new TableColumn(table, SWT.LEFT);
					column2.setText(COLUMN_NAMES[1]);

					TableLayout tableLayout = new AutoResizeTableLayout(table);
					tableLayout.addColumnData(new ColumnWeightData(30, true));
					tableLayout.addColumnData(new ColumnWeightData(70, true));
					table.setLayout(tableLayout);

					table.setHeaderVisible(true);
					table.setLinesVisible(true);
					table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

					tv.setColumnProperties(COLUMN_NAMES);
					CellEditor tce = new TextCellEditor(table);
					tv.setCellEditors(new CellEditor[]{null, tce});
					tv.setCellModifier(new ViewCellModifier(element));

					tv.refresh();
				}
			}
		}

		/*
		 * FIXME: selection of many elements causes severe slowdown. Probably
		 * what needs to be done is to add a toggle switch to turn tracking of
		 * selections in ArchEdit on/off. As an example, see the bidirectional
		 * arrows on the package explorer view which synchronizes the outline
		 * with the current editor -- but can be turned off.
		 */
		//getEditorSite().getPage().addPostSelectionListener((ArchEditOutlinePage)this.outlinePage);
	}

	protected class NameValuePair{

		public String name;
		public String value;
	}

	protected class ViewContentProvider
		implements IStructuredContentProvider{

		public ViewContentProvider(){
		}

		public Object[] getElements(Object inputElement){
			java.util.List l = new ArrayList();
			IXArchTypeMetadata typeMetadata = xarch.getTypeMetadata((ObjRef)inputElement);
			for(IXArchPropertyMetadata propMetadata: typeMetadata.getProperties()){
				if(propMetadata.getMetadataType() == IXArchPropertyMetadata.ATTRIBUTE){
					l.add(new String[]{propMetadata.getName(), (String)xarch.get((ObjRef)inputElement, propMetadata.getName())});
				}
			}
			return l.toArray();
		}

		public void dispose(){
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		}
	}

	class ViewLabelProvider
		extends LabelProvider
		implements ITableLabelProvider{

		public String getColumnText(Object obj, int index){
			return ((String[])obj)[index];
		}

		public Image getColumnImage(Object obj, int index){
			return null;
		}

		@Override
		public Image getImage(Object obj){
			return null;
		}
	}

	class ViewCellModifier
		implements ICellModifier{

		protected ObjRef ref;

		public ViewCellModifier(ObjRef ref){
			this.ref = ref;
		}

		public boolean canModify(Object element, String property){
			return true;
		}

		public Object getValue(Object element, String property){
			if(element instanceof String[]){
				String[] elts = (String[])element;
				if(elts == null){
					return "";
				}
				if(elts[1] == null){
					return "";
				}
				return elts[1].toString();
			}
			return null;
		}

		public void modify(Object element, String property, Object value){
			//SWT bug workaround
			if(element instanceof Item){
				element = ((Item)element).getData();
			}
			if(element instanceof String[]){
				String[] elts = (String[])element;
				String propertyName = elts[0].toString();

				String oldValue = null;
				if(elts[1] != null){
					oldValue = elts[1].toString();
				}

				String newValue = null;
				if(value != null){
					newValue = value.toString();
				}

				if(oldValue == null && newValue == null){
					//Do nothing
				}
				else if(oldValue != null && newValue == null){
					xarch.clear(ref, propertyName);
				}
				else if(oldValue == null && newValue != null){
					xarch.set(ref, propertyName, newValue);
				}
				else{
					//Both non-null:
					if(!oldValue.equals(newValue)){
						xarch.set(ref, propertyName, newValue);
					}
				}
			}
		}
	}

	public void createDragSourceComposite(Composite parent, ObjRef ref){
		//Composite c = new Composite(parent, SWT.BORDER);
		Group c = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout gl = new GridLayout(2, false);
		gl.marginTop = 1;
		gl.marginBottom = 5;
		gl.marginLeft = 1;
		gl.marginRight = 1;
		gl.marginHeight = 1;
		gl.marginWidth = 1;
		gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;

		c.setLayout(gl);

		c.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		Label il = new Label(c, SWT.LEFT | SWT.NO_FOCUS);
		il.setImage(resources.getPlatformImage(ISharedImages.IMG_OBJS_INFO_TSK));
		il.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		Label l = new Label(c, SWT.LEFT | SWT.NO_FOCUS);
		l.setText("Drag this area to a target in the tree to quick-link.");
		l.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		c.pack();

		DragSource[] sources = new DragSource[]{
		//new DragSource(il, DND.DROP_MOVE | DND.DROP_COPY),
		new DragSource(l, DND.DROP_MOVE | DND.DROP_COPY), new DragSource(c, DND.DROP_MOVE | DND.DROP_COPY),};

		final ObjRef fref = ref;

		for(DragSource source: sources){
			Transfer[] types = new Transfer[]{TextTransfer.getInstance()};
			source.setTransfer(types);

			source.addDragListener(new DragSourceListener(){

				public void dragStart(DragSourceEvent event){
					// Only start the drag if there is actually text in the
					// label - this text will be what is dropped on the target.
					event.doit = true;
				}

				public void dragSetData(DragSourceEvent event){
					if(TextTransfer.getInstance().isSupportedType(event.dataType)){
						event.data = "$$OBJREF$$" + fref.getUID();
					}
				}

				public void dragFinished(DragSourceEvent event){
					if(event.detail == DND.DROP_MOVE){
					}
				}
			});
		}
	}

	@Override
	public void focusEditor(String editorName, ObjRef[] refs){
		super.focusEditor(editorName, refs);
	}
	
	@Override
	public void handleExplicitEvent(ExplicitADTEvent evt){
		if(outlinePage != null){
			((ArchEditOutlinePage)outlinePage).handleExplicitEvent(evt);
		}
	}
}
