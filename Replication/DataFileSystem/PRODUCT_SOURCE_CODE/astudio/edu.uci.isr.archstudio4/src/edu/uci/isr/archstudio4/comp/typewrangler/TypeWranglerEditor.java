package edu.uci.isr.archstudio4.comp.typewrangler;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;

import edu.uci.isr.archstudio4.Archstudio4Activator;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditor;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.archstudio4.util.XadlSelectorDialog;
//import edu.uci.isr.archstudio4.util.XadlTreeContentProvider;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.widgets.swt.AutoResizeTableLayout;
import edu.uci.isr.xadlutils.XadlInterfaceConsistencyChecker;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class TypeWranglerEditor extends AbstractArchstudioEditor{
	public static final String FONT_TABLE = "typewrangler:font/table";
	public static final String IMAGE_OK_ICON = "typewrangler:icon/ok";
	private static final String IMAGE_TABLE_SHIM = "typewrangler:shim/table";
	
	private static final String[] DIRECTIONS = new String[]{"none", "in", "out", "inout"};
	
	private static final int MAPPED_COLUMN_INDEX_STATUS = 0;
	private static final int MAPPED_COLUMN_INDEX_INTERFACE = 1;
	private static final int MAPPED_COLUMN_INDEX_INTERFACE_DIRECTION = 2;
	private static final int MAPPED_COLUMN_INDEX_INTERFACE_TYPE = 3;
	private static final int MAPPED_COLUMN_INDEX_SIGNATURE = 4;
	private static final int MAPPED_COLUMN_INDEX_SIGNATURE_DIRECTION = 5;
	private static final int MAPPED_COLUMN_INDEX_SIGNATURE_TYPE = 6;
	
	private static final String[] MAPPED_COLUMN_NAMES = new String[]{
		" " /* Status */, "Interface", "Direction", "Type", "Signature", "Direction", "Type"
	};
	private static final String[] MAPPED_COLUMN_PROPERTY_NAMES = new String[]{
		"Status", "Interface", "iDirection", "iType", "Signature", "sDirection", "sType"
	};

	private static final int UNMAPPED_COLUMN_INDEX_DESCRIPTION = 0;
	private static final int UNMAPPED_COLUMN_INDEX_DIRECTION = 1;
	private static final int UNMAPPED_COLUMN_INDEX_TYPE = 2;
	
	private static final String[] IFACE_COLUMN_NAMES = new String[]{
		"Interface", "Direction", "Type"
	};
	private static final String[] SIG_COLUMN_NAMES = new String[]{
		"Signature", "Direction", "Type"
	};
	private static final String[] UNMAPPED_COLUMN_PROPERTY_NAMES = new String[]{
		"description", "direction", "type"
	};
	
	private ObjRef[] EMPTY_REF_ARRAY = new ObjRef[0];
	
	protected ObjRef[] mappedInterfaces = EMPTY_REF_ARRAY;
	protected ObjRef[] unmappedInterfaces = EMPTY_REF_ARRAY;
	protected ObjRef[] unmappedSignatures = EMPTY_REF_ARRAY;
	
	public TypeWranglerEditor(){
		super(TypeWranglerMyxComponent.class, TypeWranglerMyxComponent.EDITOR_NAME);
		
		setBannerInfo(((TypeWranglerMyxComponent)comp).getIcon(), "Type Consistency Manager");
		setHasBanner(true);
		
		resources.createImage(IMAGE_OK_ICON, Archstudio4Activator.class.getResourceAsStream("res/icon-ok.gif"));
		resources.createImage(IMAGE_TABLE_SHIM, Archstudio4Activator.class.getResourceAsStream("res/trans-1x20.gif"));
		
		Font defaultFont = resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID);
		int newHeight = 7;
		FontData[] fds = defaultFont.getFontData();
		if((fds != null) && (fds.length > 0)){
			newHeight = fds[0].getHeight() - 1;
		}
		resources.createDerivedFont(FONT_TABLE, defaultFont.getFontData(), newHeight, 0);
		ArchstudioResources.init(resources);
		
	}
	
	protected AbstractArchstudioOutlinePage createOutlinePage(){
		return new TypeWranglerOutlinePage(xarch, xArchRef, resources);
	}
	
	private void clearLists(){
		mappedInterfaces = EMPTY_REF_ARRAY;
		unmappedInterfaces = EMPTY_REF_ARRAY;
		unmappedSignatures = EMPTY_REF_ARRAY;
	}
	
	private void updateLists(ObjRef brickRef, ObjRef typeRef){
		clearLists();
		java.util.List<ObjRef> mappedInterfaceList = new ArrayList<ObjRef>();
		java.util.List<ObjRef> unmappedInterfaceList = new ArrayList<ObjRef>();
		java.util.List<ObjRef> unmappedSignatureList = new ArrayList<ObjRef>();
		if(brickRef != null){
			ObjRef[] interfaceRefs = xarch.getAll(brickRef, "interface");
			for(int i = 0; i < interfaceRefs.length; i++){
				int status = XadlInterfaceConsistencyChecker.check(xarch, interfaceRefs[i]);
				if(((status & XadlInterfaceConsistencyChecker.INTERFACE_MISSING_SIGNATURE) != 0) ||
					((status & XadlInterfaceConsistencyChecker.INTERFACE_INVALID_SIGNATURE) != 0)){
					unmappedInterfaceList.add(interfaceRefs[i]);
				}
				else{
					mappedInterfaceList.add(interfaceRefs[i]);
				}
			}
		}
		if(typeRef != null){
			ObjRef[] signatureRefs = xarch.getAll(typeRef, "signature");
			for(int i = 0; i < signatureRefs.length; i++){
				//See if it's mapped
				boolean found = false;
				for(Iterator it = mappedInterfaceList.iterator(); it.hasNext(); ){
					ObjRef interfaceRef = (ObjRef)it.next();
					ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
					if(signatureRef != null){
						if(signatureRef.equals(signatureRefs[i])){
							found = true; break;
						}
						else if(xarch.isEqual(signatureRef, signatureRefs[i])){
							found = true; break;
						}
					}
				}
				if(!found){
					//It was not mapped
					unmappedSignatureList.add(signatureRefs[i]);
				}
			}
		}
		mappedInterfaces = (ObjRef[])mappedInterfaceList.toArray(new ObjRef[mappedInterfaceList.size()]);
		unmappedInterfaces = (ObjRef[])unmappedInterfaceList.toArray(new ObjRef[unmappedInterfaceList.size()]);
		unmappedSignatures = (ObjRef[])unmappedSignatureList.toArray(new ObjRef[unmappedSignatureList.size()]);
	}
	
	public void createEditorContents(Composite parent){
		ObjRef[] selectedRefs = null;
		if(outlinePage != null){
			Object[] selectedNodes = outlinePage.getSelectedObjects();
			selectedRefs = new ObjRef[selectedNodes.length];
			System.arraycopy(selectedNodes, 0, selectedRefs, 0, selectedNodes.length);
		}
		
		if((selectedRefs == null) || (selectedRefs.length != 1)){
			clearLists();
			Label lInvalidSelection = new Label(parent, SWT.LEFT);
			lInvalidSelection.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lInvalidSelection.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			lInvalidSelection.setText("Select a brick or brick type in the outline view.");
		}
		else{
			ObjRef selectedRef = selectedRefs[0];
			int selectedRefType = XadlTreeUtils.getType(xarch, selectedRef);

			ObjRef brickRef = null;
			int brickType = 0;
			
			ObjRef typeRef = null;
			int typeType = 0;
			
			if((selectedRefType & (XadlTreeUtils.COMPONENT | XadlTreeUtils.CONNECTOR)) != 0){
				brickRef = selectedRef;
				brickType = selectedRefType;
				typeRef = XadlUtils.resolveXLink(xarch, brickRef, "type");
				if(typeRef != null){
					typeType = XadlTreeUtils.getType(xarch, typeRef);
				}
			}
			else if((selectedRefType & (XadlTreeUtils.COMPONENT_TYPE | XadlTreeUtils.CONNECTOR_TYPE)) != 0){
				typeRef = selectedRef;
				typeType = selectedRefType;
			}
			
			updateLists(brickRef, typeRef);
			
			if((brickRef == null) && (typeRef == null)){
				Label lInvalidSelection = new Label(parent, SWT.LEFT);
				lInvalidSelection.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				lInvalidSelection.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
				lInvalidSelection.setText("Select a brick or brick type in the outline view.");
			}
			else{
				createSelectionComposite(parent, brickRef, brickType, typeRef, typeType);
				/*
				Label sep1 = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
				sep1.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
				*/
				createMappingComposite(parent, brickRef, brickType, typeRef, typeType);
			}
		}
	}
	
	private void createSelectionComposite(Composite parent, ObjRef brickRef, int brickType, ObjRef typeRef, int typeType){
		Composite cSelection = new Composite(parent, SWT.NONE);
		int cols = 0;
		if(brickRef != null) cols += 5;
		if(typeRef != null) cols += 3;
		
		cSelection.setLayout(new GridLayout(cols, false));
		cSelection.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		if(brickRef != null){
			Label lBrickLabel = new Label(cSelection, SWT.LEFT);
			lBrickLabel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lBrickLabel.setFont(resources.getBoldPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			lBrickLabel.setText("Brick: ");
			
			Label lBrickIcon = new Label(cSelection, SWT.LEFT);
			lBrickIcon.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lBrickIcon.setImage(XadlTreeUtils.getIconForType(resources, brickType));
			
			Label lBrickDescription = new Label(cSelection, SWT.LEFT);
			lBrickDescription.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lBrickDescription.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			String brickDescription = XadlUtils.getDescription(xarch, brickRef);
			if(brickDescription == null){
				brickDescription = "[No Description]";
			}
			lBrickDescription.setText(brickDescription);
		}
		if(typeRef != null){
			Label lTypeLabel = new Label(cSelection, SWT.LEFT);
			lTypeLabel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lTypeLabel.setFont(resources.getBoldPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			lTypeLabel.setText("Type: ");
			
			if(brickRef != null){
				GridData lTypeLabelData = new GridData();
				lTypeLabelData.horizontalIndent = 20;
				lTypeLabel.setLayoutData(lTypeLabelData);
			}
			
			Label lTypeIcon = new Label(cSelection, SWT.LEFT);
			lTypeIcon.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lTypeIcon.setImage(XadlTreeUtils.getIconForType(resources, typeType));
			
			Label lTypeDescription = new Label(cSelection, SWT.LEFT);
			lTypeDescription.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lTypeDescription.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			String typeDescription = XadlUtils.getDescription(xarch, typeRef);
			if(typeDescription == null){
				typeDescription = "[No Description]";
			}
			lTypeDescription.setText(typeDescription);
		}
		if(brickRef != null){
			Button bAssignType = new Button(cSelection, SWT.PUSH);
			bAssignType.setText("Assign Type...");
			final ObjRef fbrickRef = brickRef;
			final int t = (brickType == XadlTreeUtils.COMPONENT) ? XadlTreeUtils.COMPONENT_TYPE : XadlTreeUtils.CONNECTOR_TYPE;
			bAssignType.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event event){
					ObjRef selectedRef = XadlSelectorDialog.showSelectorDialog(getSite().getShell(), "Select New Type", 
						xarch, resources, xArchRef, t, t);
					if(selectedRef != null){
						setType(fbrickRef, selectedRef);
					}
				}
			});
			
			Button bClearType = new Button(cSelection, SWT.PUSH);
			bClearType.setText("Clear Type");
			bClearType.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event event){
					xarch.clear(fbrickRef, "type");
				}
			});

		}
	}
	
	private void createMappingComposite(Composite parent, ObjRef brickRef, int brickType, ObjRef typeRef, int typeType){
		final ObjRef fbrickRef = brickRef;
		final int fbrickType = brickType;
		final ObjRef ftypeRef = typeRef;
		final int ftypeType = typeType;
		
		Group cMapping = new Group(parent, SWT.NONE);
		cMapping.setLayout(new GridLayout(1, false));
		cMapping.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
			GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
		cMapping.setText("Mapped Interfaces and Signatures");
		cMapping.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		final TableViewer mappedViewer = createMappedTableViewer(cMapping, brickRef, brickType, typeRef, typeType);
		mappedViewer.getControl().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		Composite cMappingButtons = new Composite(cMapping, SWT.NONE);
		cMappingButtons.setLayout(new GridLayout(2, false));
		cMappingButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		cMappingButtons.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		final Button bUnmap = new Button(cMappingButtons, SWT.PUSH);
		bUnmap.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		bUnmap.setText("Unmap");
		bUnmap.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				ObjRef interfaceRef = (ObjRef)((IStructuredSelection)mappedViewer.getSelection()).getFirstElement();
				if(interfaceRef != null){
					xarch.clear(interfaceRef, "signature");
				}
			}
		});
		bUnmap.setEnabled(false);
		
		final Label lStatus = new Label(cMappingButtons, SWT.RIGHT | SWT.WRAP);
		lStatus.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
		lStatus.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		lStatus.setText("Status: N/A");
		lStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,	1));
		
		ISelectionChangedListener statusListener = new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection selection = (IStructuredSelection)mappedViewer.getSelection();
				ObjRef interfaceRef = (ObjRef)selection.getFirstElement();
				bUnmap.setEnabled(interfaceRef != null);
				if(interfaceRef == null){
					lStatus.setText("Status: N/A");
				}
				else{
					int status = XadlInterfaceConsistencyChecker.check(xarch, interfaceRef);
					if(status == XadlInterfaceConsistencyChecker.OK){
						lStatus.setText("Status: OK");
					}
					else{
						String[] errors = XadlInterfaceConsistencyChecker.statusToStrings(status);
						StringBuffer sb = new StringBuffer("Status: ");
						for(int i = 0; i < errors.length; i++){
							if(i != 0) sb.append(" ");
							sb.append(errors[i]);
						}
						lStatus.setText(sb.toString());
					}
				}
			}
		};
		
		mappedViewer.addSelectionChangedListener(statusListener);
		
		//enablement
		if((brickRef == null) || (typeRef == null)){
			mappedViewer.getTable().setEnabled(false);
			cMapping.setEnabled(false);
		}

		// bottom half
		
		Composite cBottomHalf = new Composite(parent, SWT.NONE);
		cBottomHalf.setLayout(new GridLayout(3, false));
		cBottomHalf.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
			GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
		cBottomHalf.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		// unmapped ifaces
		
		Group cIface = new Group(cBottomHalf, SWT.NONE);
		cIface.setLayout(new GridLayout(1, false));
		cIface.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		cIface.setText("Unmapped Interfaces");
		cIface.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		final TableViewer ifaceViewer = createUnmappedTableViewer(cIface, 
			IFACE_COLUMN_NAMES, new InterfaceContentProvider(), brickRef, brickType, typeRef, typeType);
		ifaceViewer.getControl().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		Composite cIfaceButtons = new Composite(cIface, SWT.NONE);
		cIfaceButtons.setLayout(new GridLayout(2, false));
		cIfaceButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		cIfaceButtons.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		Button bNewInterface = new Button(cIfaceButtons, SWT.PUSH);
		bNewInterface.setText("New Interface");
		bNewInterface.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				createInterface(fbrickRef, "[New Interface]", "none");
			}
		});
		
		final Button bRemoveInterface = new Button(cIfaceButtons, SWT.PUSH);
		bRemoveInterface.setText("Remove Interface");
		bRemoveInterface.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				Object selectedObject = ((IStructuredSelection)ifaceViewer.getSelection()).getFirstElement();
				if(selectedObject instanceof ObjRef){
					xarch.remove(fbrickRef, "interface", (ObjRef)selectedObject);
				}
			}
		});
		bRemoveInterface.setEnabled(false);
		
		if(brickRef == null){
			ifaceViewer.getTable().setEnabled(false);
			bNewInterface.setEnabled(false);
			bRemoveInterface.setEnabled(false);
			cIface.setEnabled(false);
		}
		
		//Middle Buttons
		Composite cMiddleButtons = new Composite(cBottomHalf, SWT.NONE);
		cMiddleButtons.setLayout(new GridLayout(1, false));
		cMiddleButtons.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		//finish here
		final Button bMap = new Button(cMiddleButtons, SWT.PUSH);
		bMap.setText("< Map >");
		bMap.setEnabled(false);
		
		final Button bCreateInterface = new Button(cMiddleButtons, SWT.PUSH);
		bCreateInterface.setText("< Create");
		bCreateInterface.setEnabled(false);
		
		final Button bCreateSignature = new Button(cMiddleButtons, SWT.PUSH);
		bCreateSignature.setText("Create >");
		bCreateSignature.setEnabled(false);
		
		// unmapped sigs
		
		Group cSig = new Group(cBottomHalf, SWT.NONE);
		cSig.setLayout(new GridLayout(1, false));
		cSig.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		cSig.setText("Unmapped Signatures");
		cSig.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		final TableViewer sigViewer = createUnmappedTableViewer(cSig, 
			SIG_COLUMN_NAMES, new SignatureContentProvider(), brickRef, brickType, typeRef, typeType);
		sigViewer.getControl().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		
		Composite cSigButtons = new Composite(cSig, SWT.NONE);
		cSigButtons.setLayout(new GridLayout(2, false));
		cSigButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		cSigButtons.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		Button bNewSignature = new Button(cSigButtons, SWT.PUSH);
		bNewSignature.setText("New Signature");
		bNewSignature.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				createSignature(ftypeRef, "[New Signature]", "none");
			}
		});
		
		final Button bRemoveSignature = new Button(cSigButtons, SWT.PUSH);
		bRemoveSignature.setText("Remove Signature");
		bRemoveSignature.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				Object selectedObject = ((IStructuredSelection)sigViewer.getSelection()).getFirstElement();
				if(selectedObject instanceof ObjRef){
					xarch.remove(ftypeRef, "signature", (ObjRef)selectedObject);
				}
			}
		});
		bRemoveSignature.setEnabled(false);
		
		if(typeRef == null){
			sigViewer.getTable().setEnabled(false);
			bNewSignature.setEnabled(false);
			bRemoveSignature.setEnabled(false);
			cSig.setEnabled(false);
		}
		
		//Button enablement
		ISelectionChangedListener buttonEnableListener = new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event){
				boolean ifaceSelected = !ifaceViewer.getSelection().isEmpty();
				boolean sigSelected = !sigViewer.getSelection().isEmpty();
				bRemoveInterface.setEnabled(ifaceSelected && (fbrickRef != null));
				bRemoveSignature.setEnabled(sigSelected && (ftypeRef != null));
				bMap.setEnabled(ifaceSelected && sigSelected && (fbrickRef != null) && (ftypeRef != null));
				bCreateInterface.setEnabled(sigSelected && (fbrickRef != null));
				bCreateSignature.setEnabled(ifaceSelected && (ftypeRef != null));
			}
		};
		ifaceViewer.addSelectionChangedListener(buttonEnableListener);
		sigViewer.addSelectionChangedListener(buttonEnableListener);
		
		//Middle Buttons
		bMap.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				ObjRef interfaceRef = (ObjRef)((IStructuredSelection)ifaceViewer.getSelection()).getFirstElement();
				ObjRef signatureRef = (ObjRef)((IStructuredSelection)sigViewer.getSelection()).getFirstElement();
				String signatureID = XadlUtils.getID(xarch, signatureRef);
				if(signatureID != null){
					XadlUtils.setXLink(xarch, interfaceRef, "signature", signatureID);
				}
			}
		});
		
		bCreateInterface.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				ObjRef signatureRef = (ObjRef)((IStructuredSelection)sigViewer.getSelection()).getFirstElement();
				String signatureID = XadlUtils.getID(xarch, signatureRef);
				if(signatureID != null){
					String direction = XadlUtils.getDirection(xarch, signatureRef);
					if(direction == null) direction = "none";
					ObjRef interfaceRef = createInterface(fbrickRef, 
						XadlUtils.guessGoodInterfaceDescription(XadlUtils.getDescription(xarch, signatureRef)), 
						direction);
					XadlUtils.setXLink(xarch, interfaceRef, "signature", signatureID);
					
					ObjRef itypeRef = XadlUtils.resolveXLink(xarch, signatureRef, "type");
					if(itypeRef != null){
						String iTypeID = XadlUtils.getID(xarch, itypeRef);
						if(iTypeID != null){
							XadlUtils.setXLink(xarch, interfaceRef, "type", iTypeID);
						}
					}
				}
			}
		});

		bCreateSignature.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				ObjRef interfaceRef = (ObjRef)((IStructuredSelection)ifaceViewer.getSelection()).getFirstElement();
				String direction = XadlUtils.getDirection(xarch, interfaceRef);
				if(direction == null) direction = "none";
				ObjRef signatureRef = createSignature(ftypeRef, 
					XadlUtils.guessGoodSignatureDescription(XadlUtils.getDescription(xarch, interfaceRef)),
					direction);
				String signatureID = XadlUtils.getID(xarch, signatureRef);
				XadlUtils.setXLink(xarch, interfaceRef, "signature", signatureID);
				
				ObjRef itypeRef = XadlUtils.resolveXLink(xarch, interfaceRef, "type");
				if(itypeRef != null){
					String iTypeID = XadlUtils.getID(xarch, itypeRef);
					if(iTypeID != null){
						XadlUtils.setXLink(xarch, signatureRef, "type", iTypeID);
					}
				}
			}
		});
	}

	private TableViewer createMappedTableViewer(Composite parent, ObjRef brickRef, int brickType, ObjRef typeRef, int typeType){
		TableViewer tv = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tv.setContentProvider(new MappedContentProvider());
		tv.setLabelProvider(new MappedLabelProvider());
		
		tv.setInput(brickRef);
		
		Table table = tv.getTable();
		
		for(int i = 0; i < MAPPED_COLUMN_NAMES.length; i++){
			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setText(MAPPED_COLUMN_NAMES[i]);
		}
		
		TableLayout tableLayout = new AutoResizeTableLayout(table);
		tableLayout.addColumnData(new ColumnWeightData(4, false));
		tableLayout.addColumnData(new ColumnWeightData(20, true));
		tableLayout.addColumnData(new ColumnWeightData(8, false));
		tableLayout.addColumnData(new ColumnWeightData(20, true));
		tableLayout.addColumnData(new ColumnWeightData(20, true));
		tableLayout.addColumnData(new ColumnWeightData(8, false));
		tableLayout.addColumnData(new ColumnWeightData(20, true));
		table.setLayout(tableLayout);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		//table.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		tv.setColumnProperties(MAPPED_COLUMN_PROPERTY_NAMES);

		CellEditor descriptionEditor = new TextCellEditor(table);
		CellEditor directionEditor = new ComboBoxCellEditor(table, new String[]{"none", "in", "out", "inout"});
		CellEditor typeEditor = new TypeDialogCellEditor(table);
		tv.setCellEditors(new CellEditor[]{null, 
			descriptionEditor, directionEditor, typeEditor, 
			descriptionEditor, directionEditor, typeEditor});
		tv.setCellModifier(new MappedCellModifier());

		tv.refresh(true);
		return tv;
	}
	
	class MappedContentProvider implements IStructuredContentProvider{
		private Object[] EMPTY_ARRAY = new Object[0];
		
		public Object[] getElements(Object inputElement){
			if(inputElement instanceof ObjRef){
				return mappedInterfaces;
			}
			return EMPTY_ARRAY;
		}
		
		public void dispose(){}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
	}
	
	class MappedLabelProvider extends LabelProvider implements ITableLabelProvider, ITableFontProvider{
		public Font getFont(Object element, int columnIndex){
			return resources.getFont(FONT_TABLE);
		}

		public String getColumnText(Object element, int columnIndex){
			if(element instanceof ObjRef){
				ObjRef interfaceRef = (ObjRef)element;
				switch(columnIndex){
					case MAPPED_COLUMN_INDEX_INTERFACE:
						return getDescriptionDisplayString(interfaceRef);
					case MAPPED_COLUMN_INDEX_INTERFACE_DIRECTION:
						return getDirectionDisplayString(interfaceRef);
					case MAPPED_COLUMN_INDEX_INTERFACE_TYPE:
						return getTypeDisplayString(interfaceRef);
					case MAPPED_COLUMN_INDEX_SIGNATURE:
						ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
						if(signatureRef != null){
							return getDescriptionDisplayString(signatureRef);
						}
						return null;
					case MAPPED_COLUMN_INDEX_SIGNATURE_DIRECTION:
						ObjRef signatureRef2 = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
						if(signatureRef2 != null){
							return getDirectionDisplayString(signatureRef2);
						}
						return null;
					case MAPPED_COLUMN_INDEX_SIGNATURE_TYPE:
						ObjRef signatureRef3 = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
						if(signatureRef3 != null){
							return getTypeDisplayString(signatureRef3);
						}
						return null;
					default:
						return null;
				}
			}
			return null;
		}
		
		public Image getImage(Object obj){
			return null;
		}
		
		public Image getColumnImage(Object element, int columnIndex){
			if(element instanceof ObjRef){
				ObjRef interfaceRef = (ObjRef)element;
				
				if(columnIndex == MAPPED_COLUMN_INDEX_STATUS){
					int status = XadlInterfaceConsistencyChecker.check(xarch, interfaceRef);
					if(status != XadlInterfaceConsistencyChecker.OK){
						return resources.getPlatformImage(ISharedImages.IMG_OBJS_ERROR_TSK);
					}
					else{
						return resources.getImage(IMAGE_OK_ICON);
					}
				}
			}
			return null;
		}
	}
	
	class MappedCellModifier implements ICellModifier{
		public boolean canModify(Object element, String property){
			if((element != null) && (element instanceof ObjRef)){
				ObjRef interfaceRef = (ObjRef)element;
				//Can always edit the interface columns
				if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE]) ||
					property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE_DIRECTION]) ||
					property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE_TYPE])){
					return true;
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE]) ||
					property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE_DIRECTION]) || 
					property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE_TYPE])){
					
					int status = XadlInterfaceConsistencyChecker.check(xarch, interfaceRef);
					if(((status & XadlInterfaceConsistencyChecker.INTERFACE_MISSING_SIGNATURE) == 0) &&
						((status & XadlInterfaceConsistencyChecker.INTERFACE_INVALID_SIGNATURE) == 0)){
						return true;
					}
				}
			}
			return false;
		}
		
		public Object getValue(Object element, String property){
			if(element instanceof ObjRef){
				ObjRef interfaceRef = (ObjRef)element;
				if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE])){
					String text = XadlUtils.getDescription(xarch, interfaceRef);
					if(text == null){
						text = "";
					}
					return text;
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE_DIRECTION])){
					String text = XadlUtils.getDirection(xarch, interfaceRef);
					return new Integer(getDirectionIndex(text));
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE_TYPE])){
					ObjRef interfaceTypeRef = XadlUtils.resolveXLink(xarch, interfaceRef, "type");
					return interfaceTypeRef;
				}
				if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE])){
					ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
					if(signatureRef != null){
						String text = XadlUtils.getDescription(xarch, signatureRef);
						if(text == null){
							text = "";
						}
						return text;
					}
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE_DIRECTION])){
					ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
					if(signatureRef != null){
						String text = XadlUtils.getDirection(xarch, signatureRef);
						return new Integer(getDirectionIndex(text));
					}
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE_TYPE])){
					ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
					if(signatureRef != null){
						ObjRef interfaceTypeRef = XadlUtils.resolveXLink(xarch, signatureRef, "type");
						return interfaceTypeRef;
					}
				}
			}
			return null;
		}
		
		public void modify(Object element, String property, Object value){
			//SWT bug workaround
			if(element instanceof Item) {
				element = ((Item)element).getData();
			}
			Object formerValue = getValue(element, property);
			if((formerValue != null) && formerValue.equals(value)) return;
			
			if(element instanceof ObjRef){
				ObjRef interfaceRef = (ObjRef)element;
				if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE])){
					String newDescription = (String)value;
					if(newDescription != null){
						XadlUtils.setDescription(xarch, interfaceRef, newDescription);
					}
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE_DIRECTION])){
					String newDirection = DIRECTIONS[((Integer)value).intValue()];
					if(newDirection!= null){
						XadlUtils.setDirection(xarch, interfaceRef, newDirection);
					}
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_INTERFACE_TYPE])){
					ObjRef newInterfaceTypeRef = (ObjRef)value;
					String id = XadlUtils.getID(xarch, newInterfaceTypeRef);
					if(id != null){
						XadlUtils.setXLink(xarch, interfaceRef, "type", id);
					}
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE])){
					ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
					if(signatureRef != null){
						String newDescription = (String)value;
						if(newDescription != null){
							XadlUtils.setDescription(xarch, signatureRef, newDescription);
						}
					}
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE_DIRECTION])){
					ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
					if(signatureRef != null){
						String newDirection = DIRECTIONS[((Integer)value).intValue()];
						if(newDirection!= null){
							XadlUtils.setDirection(xarch, signatureRef, newDirection);
						}
					}
				}
				else if(property.equals(MAPPED_COLUMN_PROPERTY_NAMES[MAPPED_COLUMN_INDEX_SIGNATURE_TYPE])){
					ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
					if(signatureRef != null){
						ObjRef newInterfaceTypeRef = (ObjRef)value;
						String id = XadlUtils.getID(xarch, newInterfaceTypeRef);
						if(id != null){
							XadlUtils.setXLink(xarch, signatureRef, "type", id);
						}
					}
				}
			}
		}
	}
	
	private TableViewer createUnmappedTableViewer(Composite parent, 
	String[] columnNames, IStructuredContentProvider contentProvider,
	ObjRef brickRef, int brickType, ObjRef typeRef, int typeType){
		TableViewer tv = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tv.setContentProvider(contentProvider);
		tv.setLabelProvider(new UnmappedLabelProvider());
		
		tv.setInput(brickRef);
		
		Table table = tv.getTable();

		for(int i = 0; i < columnNames.length; i++){
			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setText(columnNames[i]);
		}
		
		TableLayout tableLayout = new AutoResizeTableLayout(table);
		tableLayout.addColumnData(new ColumnWeightData(40, true));
		tableLayout.addColumnData(new ColumnWeightData(20, false));
		tableLayout.addColumnData(new ColumnWeightData(40, true));
		table.setLayout(tableLayout);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tv.setColumnProperties(UNMAPPED_COLUMN_PROPERTY_NAMES);

		CellEditor descriptionEditor = new TextCellEditor(table);
		CellEditor directionEditor = new ComboBoxCellEditor(table, new String[]{"none", "in", "out", "inout"});
		CellEditor typeEditor = new TypeDialogCellEditor(table);
		tv.setCellEditors(new CellEditor[]{descriptionEditor, directionEditor, typeEditor}); 
		tv.setCellModifier(new UnmappedCellModifier());

		tv.refresh(true);
		return tv;
	}

	class InterfaceContentProvider implements IStructuredContentProvider{
		private Object[] EMPTY_ARRAY = new Object[0];
		
		public Object[] getElements(Object inputElement){
			if(inputElement instanceof ObjRef){
				return unmappedInterfaces;
			}
			return EMPTY_ARRAY;
		}
		
		public void dispose(){}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
	}
	
	class UnmappedLabelProvider extends LabelProvider implements ITableLabelProvider, ITableFontProvider{
		public Font getFont(Object element, int columnIndex){
			return resources.getFont(FONT_TABLE);
		}
		
		public String getColumnText(Object element, int columnIndex){
			if(element instanceof ObjRef){
				ObjRef eltRef = (ObjRef)element;
				switch(columnIndex){
					case UNMAPPED_COLUMN_INDEX_DESCRIPTION:
						return getDescriptionDisplayString(eltRef);
					case UNMAPPED_COLUMN_INDEX_DIRECTION:
						return getDirectionDisplayString(eltRef);
					case UNMAPPED_COLUMN_INDEX_TYPE:
						return getTypeDisplayString(eltRef);
					default:
						return null;
				}
			}
			return null;
		}
		
		public Image getImage(Object obj){
			return null;
		}
		
		public Image getColumnImage(Object element, int columnIndex){
			if(columnIndex == 0){
				return resources.getImage(IMAGE_TABLE_SHIM);
			}
			return null;
		}
	}

	class UnmappedCellModifier implements ICellModifier{
		public boolean canModify(Object element, String property){
			return true;
		}
		
		public Object getValue(Object element, String property){
			if(element instanceof ObjRef){
				ObjRef eltRef = (ObjRef)element;
				if(property.equals(UNMAPPED_COLUMN_PROPERTY_NAMES[UNMAPPED_COLUMN_INDEX_DESCRIPTION])){
					String text = XadlUtils.getDescription(xarch, eltRef);
					if(text == null){
						text = "";
					}
					return text;
				}
				else if(property.equals(UNMAPPED_COLUMN_PROPERTY_NAMES[UNMAPPED_COLUMN_INDEX_DIRECTION])){
					String text = XadlUtils.getDirection(xarch, eltRef);
					return new Integer(getDirectionIndex(text));
				}
				else if(property.equals(UNMAPPED_COLUMN_PROPERTY_NAMES[UNMAPPED_COLUMN_INDEX_TYPE])){
					ObjRef eltTypeRef = XadlUtils.resolveXLink(xarch, eltRef, "type");
					return eltTypeRef;
				}
			}
			return null;
		}
		
		public void modify(Object element, String property, Object value){
			//SWT bug workaround
			if(element instanceof Item) {
				element = ((Item)element).getData();
			}
			Object formerValue = getValue(element, property);
			if((formerValue != null) && formerValue.equals(value)) return;
			
			if(element instanceof ObjRef){
				ObjRef eltRef = (ObjRef)element;
				if(property.equals(UNMAPPED_COLUMN_PROPERTY_NAMES[UNMAPPED_COLUMN_INDEX_DESCRIPTION])){
					String newDescription = (String)value;
					if(newDescription != null){
						XadlUtils.setDescription(xarch, eltRef, newDescription);
					}
				}
				else if(property.equals(UNMAPPED_COLUMN_PROPERTY_NAMES[UNMAPPED_COLUMN_INDEX_DIRECTION])){
					String newDirection = DIRECTIONS[((Integer)value).intValue()];
					if(newDirection!= null){
						XadlUtils.setDirection(xarch, eltRef, newDirection);
					}
				}
				else if(property.equals(UNMAPPED_COLUMN_PROPERTY_NAMES[UNMAPPED_COLUMN_INDEX_TYPE])){
					ObjRef newInterfaceTypeRef = (ObjRef)value;
					String id = XadlUtils.getID(xarch, newInterfaceTypeRef);
					if(id != null){
						XadlUtils.setXLink(xarch, eltRef, "type", id);
					}
				}
			}
		}
	}

	class SignatureContentProvider implements IStructuredContentProvider{
		private Object[] EMPTY_ARRAY = new Object[0];
		
		public Object[] getElements(Object inputElement){
			if(inputElement instanceof ObjRef){
				return unmappedSignatures;
			}
			return EMPTY_ARRAY;
		}
		
		public void dispose(){}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
	}
	
	protected String getDescriptionDisplayString(ObjRef ref){
		String description = XadlUtils.getDescription(xarch, ref);
		if(description == null) return "[No Description]";
		return description;
	}
	
	protected String getDirectionDisplayString(ObjRef ref){
		String direction = XadlUtils.getDirection(xarch, ref);
		if(direction == null) return "[No Direction]";
		return direction;
	}
	
	protected String getTypeDisplayString(ObjRef ref){
		ObjRef typeRef = XadlUtils.resolveXLink(xarch, ref, "type");
		if(typeRef == null) return "[No Type]";
		return getDescriptionDisplayString(typeRef);
	}
	
	class TypeDialogCellEditor extends DialogCellEditor{
		public TypeDialogCellEditor(Composite parent){
			super(parent);
		}
		
		protected Button createButton(Composite parent){
			Button b = new Button(parent, SWT.PUSH);
			b.setText("Change");
			return b;
		}
		
		protected Object openDialogBox(Control cellEditorWindow){
			ObjRef result = XadlSelectorDialog.showSelectorDialog(cellEditorWindow.getShell(), 
				"Select Type", xarch, resources, xArchRef, 
				XadlTreeUtils.INTERFACE_TYPE, XadlTreeUtils.INTERFACE_TYPE);
			return result;
		}
		
		protected void updateContents(Object value){
			if(value == null) return;
			if(value instanceof ObjRef){
				ObjRef interfaceTypeRef = (ObjRef)value;
				String description = XadlUtils.getDescription(xarch, interfaceTypeRef);
				if(description == null){
					description = "[No Description]";
				}
				getDefaultLabel().setText(description);
			}
		}
	}

	public void setFocus(){
		parent.getChildren()[0].setFocus();
	}
	
	protected void setType(ObjRef element, ObjRef typeRef){
		String id = XadlUtils.getID(xarch, typeRef);
		if(id == null){
			MessageBox messageBox = new MessageBox(getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
      messageBox.setText("Error");
      messageBox.setMessage("Target type has no ID and thus can't be linked.");
      messageBox.open();
		}
		else{
			XadlUtils.setXLink(xarch, element, "type", id);
		}
	}
	
	protected ObjRef createInterface(ObjRef brickRef, String description, String direction){
		ObjRef xArchRef = xarch.getXArch(brickRef);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		
		ObjRef interfaceRef = xarch.create(typesContextRef, "interface");
		xarch.set(interfaceRef, "id", UIDGenerator.generateUID());
		XadlUtils.setDescription(xarch, interfaceRef, description);
		XadlUtils.setDirection(xarch, interfaceRef, direction);
		
		xarch.add(brickRef, "interface", interfaceRef);
		return interfaceRef;
	}
	
	protected ObjRef createSignature(ObjRef brickTypeRef, String description, String direction){
		ObjRef xArchRef = xarch.getXArch(brickTypeRef);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		
		ObjRef signatureRef = xarch.create(typesContextRef, "signature");
		xarch.set(signatureRef, "id", UIDGenerator.generateUID());
		XadlUtils.setDescription(xarch, signatureRef, description);
		XadlUtils.setDirection(xarch, signatureRef, direction);
		
		xarch.add(brickTypeRef, "signature", signatureRef);
		return signatureRef;
	}

	private int getDirectionIndex(String direction){
		if(direction == null) return 0;
		for(int i = 0; i < DIRECTIONS.length; i++){
			if(direction.equals(DIRECTIONS[i])){
				return i;
			}
		}
		return 0;
	}

}
