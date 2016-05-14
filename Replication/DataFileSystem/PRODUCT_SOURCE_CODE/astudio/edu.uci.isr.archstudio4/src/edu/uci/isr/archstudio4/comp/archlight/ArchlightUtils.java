package edu.uci.isr.archstudio4.comp.archlight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import edu.uci.isr.archstudio4.Archstudio4Activator;
import edu.uci.isr.archstudio4.archlight.ArchlightTest;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ArchlightUtils{
	
	public static final String ARCHLIGHT_ISSUE_VIEW_ECLIPSE_ID = 
		"edu.uci.isr.archstudio4.comp.archlight.issueview.ArchlightIssueView";
	public static final int APPLIED = 1;
	public static final int DISABLED = 2;
	public static final int NOT_APPLIED = 3;

	public static final String IMAGE_OVERLAY_CHECKBOX_CHECKED = "archlight:checkbox/checked";
	public static final String IMAGE_OVERLAY_STOPSIGN = "archlight:stopsign";
	public static final String IMAGE_OVERLAY_CHECKBOX_UNCHECKED = "archlight:checkbox/unchecked";
	
	public static final String IMAGE_FOLDER_APPLIED = "archlight:folder/enabled";
	public static final String IMAGE_FOLDER_DISABLED = "archlight:folder/disabled";
	public static final String IMAGE_FOLDER_NOTAPPLIED = "archlight:folder/notapplied";
	
	public static final String IMAGE_TEST_APPLIED = "archlight:test/enabled";
	public static final String IMAGE_TEST_DISABLED = "archlight:test/disabled";
	public static final String IMAGE_TEST_NOTAPPLIED = "archlight:test/notapplied";
	
	public static final String IMAGE_RUN_TESTS = "archlight:runtests";
	public static final String IMAGE_RELOAD_TESTS = "archlight:refreshtests";
	
	private ArchlightUtils(){}
	
	public static void initResources(IResources resources){
		resources.createImage(IMAGE_RUN_TESTS, Archstudio4Activator.class.getResourceAsStream("res/icon-go.gif"));
		resources.createImage(IMAGE_RELOAD_TESTS, Archstudio4Activator.class.getResourceAsStream("res/icon-refresh.gif"));
		
		resources.createImage(IMAGE_OVERLAY_CHECKBOX_CHECKED, Archstudio4Activator.class.getResourceAsStream("res/decorator-checkbox-checked.gif"));
		resources.createImage(IMAGE_OVERLAY_CHECKBOX_UNCHECKED, Archstudio4Activator.class.getResourceAsStream("res/decorator-checkbox-unchecked.gif"));
		resources.createImage(IMAGE_OVERLAY_STOPSIGN, Archstudio4Activator.class.getResourceAsStream("res/decorator-stopsign.gif"));
		
		resources.createOverlayImage(IMAGE_FOLDER_APPLIED, 
			resources.getPlatformImage(ISharedImages.IMG_OBJ_FOLDER), 
			new Image[]{resources.getImage(IMAGE_OVERLAY_CHECKBOX_CHECKED)}, 
			new int[]{IResources.BOTTOM_RIGHT});
			
		resources.createOverlayImage(IMAGE_FOLDER_DISABLED, 
			resources.getPlatformImage(ISharedImages.IMG_OBJ_FOLDER), 
			new Image[]{resources.getImage(IMAGE_OVERLAY_STOPSIGN)}, 
			new int[]{IResources.BOTTOM_RIGHT});
		
		resources.createOverlayImage(IMAGE_FOLDER_NOTAPPLIED, 
			resources.getPlatformImage(ISharedImages.IMG_OBJ_FOLDER), 
			new Image[]{resources.getImage(IMAGE_OVERLAY_CHECKBOX_UNCHECKED)}, 
			new int[]{IResources.BOTTOM_RIGHT});
		
		resources.createOverlayImage(IMAGE_TEST_APPLIED, 
			resources.getPlatformImage(ISharedImages.IMG_OBJ_FILE), 
			new Image[]{resources.getImage(IMAGE_OVERLAY_CHECKBOX_CHECKED)}, 
			new int[]{IResources.BOTTOM_RIGHT});
			
		resources.createOverlayImage(IMAGE_TEST_DISABLED, 
			resources.getPlatformImage(ISharedImages.IMG_OBJ_FILE), 
			new Image[]{resources.getImage(IMAGE_OVERLAY_STOPSIGN)}, 
			new int[]{IResources.BOTTOM_RIGHT});
		
		resources.createOverlayImage(IMAGE_TEST_NOTAPPLIED, 
			resources.getPlatformImage(ISharedImages.IMG_OBJ_FILE), 
			new Image[]{resources.getImage(IMAGE_OVERLAY_CHECKBOX_UNCHECKED)}, 
			new int[]{IResources.BOTTOM_RIGHT});
	}

	public static ArchlightDocTest[] loadDocTests(XArchFlatInterface xarch, ObjRef xArchRef){
		List docTestList = new ArrayList();
		try{
			ObjRef analysisContext = xarch.createContext(xArchRef, "analysis");
			ObjRef tronAnalysisContext = xarch.createContext(xArchRef, "tronanalysis");

			ObjRef archAnalysisRef = xarch.getElement(analysisContext, "archAnalysis", xArchRef);
			if(archAnalysisRef != null){
				ObjRef[] analysisRefs = xarch.getAll(archAnalysisRef, "analysis");
				if(analysisRefs != null){
					for(int i = 0; i < analysisRefs.length; i++){
						if(xarch.isInstanceOf(analysisRefs[i], "tronanalysis#TronAnalysis")){
							//It's an ArchLight analysis
							ObjRef[] testRefs = xarch.getAll(analysisRefs[i], "test");
							if(testRefs != null){
								for(int j = 0; j < testRefs.length; j++){
									String testUID = (String)xarch.get(testRefs[j], "id");
									String testDescription = XadlUtils.getDescription(xarch, testRefs[j]);
									if(testDescription == null) testDescription = "[no data]";
									String enabled = (String)xarch.get(testRefs[j], "enabled");
									if(enabled == null) enabled = "true";
									ArchlightDocTest newDocTest = new ArchlightDocTest(testUID, testDescription, enabled.equals("true"));
									docTestList.add(newDocTest);
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e){
		}
		return (ArchlightDocTest[])docTestList.toArray(new ArchlightDocTest[docTestList.size()]);
	}
	
	public static void makeDocTestApplied(XArchFlatInterface xarch, ObjRef xArchRef, ArchlightTest testToUpdate, Boolean isEnabled){
		try{
			ObjRef analysisContext = xarch.createContext(xArchRef, "analysis");
			ObjRef tronAnalysisContext = xarch.createContext(xArchRef, "tronanalysis");

			//First see if we can find the test and just update it.
			ObjRef archAnalysisRef = null;
			ObjRef tronAnalysisRef = null;
			
			archAnalysisRef = xarch.getElement(analysisContext, "archAnalysis", xArchRef);
			if(archAnalysisRef != null){
				ObjRef[] analysisRefs = xarch.getAll(archAnalysisRef, "analysis");
				if(analysisRefs != null){
					if(analysisRefs.length > 0){
						tronAnalysisRef = analysisRefs[0];
					}
					for(int i = 0; i < analysisRefs.length; i++){
						if(xarch.isInstanceOf(analysisRefs[i], "tronanalysis#TronAnalysis")){
							//It's a tron analysis
							ObjRef[] testRefs = xarch.getAll(analysisRefs[i], "test");
							if(testRefs != null){
								for(int j = 0; j < testRefs.length; j++){
									String testUID = (String)xarch.get(testRefs[j], "id");
									if(testUID.equals(testToUpdate.getUID())){
										//Found it.  Just update it with the new enabled status.
										if(isEnabled != null){
											if(isEnabled.booleanValue()){
												xarch.set(testRefs[j], "enabled", "true");
												return;
											}
											else{
												xarch.set(testRefs[j], "enabled", "false");
												return;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			//We didn't find it. Create ancestor elements if necessary.
			if(archAnalysisRef == null){
				archAnalysisRef = xarch.createElement(analysisContext, "archAnalysis");
				xarch.add(xArchRef, "Object", archAnalysisRef);
			}
			if(tronAnalysisRef == null){
				tronAnalysisRef = xarch.create(tronAnalysisContext, "tronAnalysis");
				xarch.set(tronAnalysisRef, "id", UIDGenerator.generateUID());
				XadlUtils.setDescription(xarch, tronAnalysisRef, "Archlight Analysis Tests");
				xarch.add(archAnalysisRef, "Analysis", tronAnalysisRef);
			}
			//Okay, we have the ancestor elements, let's create the test.
			ObjRef newTestRef = xarch.create(tronAnalysisContext, "test");
			xarch.set(newTestRef, "id", testToUpdate.getUID());
			String descString = "Tool: " + testToUpdate.getToolID() + "; Category: " + testToUpdate.getCategory();
			XadlUtils.setDescription(xarch, newTestRef, descString);

			if((isEnabled == null) || (isEnabled.booleanValue())){
				xarch.set(newTestRef, "enabled", "true");
			}
			else{
				xarch.set(newTestRef, "enabled", "false");
			}
			xarch.add(tronAnalysisRef, "test", newTestRef);
		}
		catch(Exception e){
		}
	}

	public static void makeDocTestNotApplied(XArchFlatInterface xarch, ObjRef xArchRef, ArchlightTest testToUpdate){
		try{
			ObjRef analysisContext = xarch.createContext(xArchRef, "analysis");
			ObjRef tronAnalysisContext = xarch.createContext(xArchRef, "tronanalysis");

			//First see if we can find the test and just update it.
			ObjRef archAnalysisRef = null;
			ObjRef tronAnalysisRef = null;
			
			archAnalysisRef = xarch.getElement(analysisContext, "archAnalysis", xArchRef);
			if(archAnalysisRef != null){
				ObjRef[] analysisRefs = xarch.getAll(archAnalysisRef, "analysis");
				if(analysisRefs != null){
					if(analysisRefs.length > 0){
						tronAnalysisRef = analysisRefs[0];
					}
					for(int i = 0; i < analysisRefs.length; i++){
						if(xarch.isInstanceOf(analysisRefs[i], "tronanalysis#TronAnalysis")){
							//It's a tron analysis
							ObjRef[] testRefs = xarch.getAll(analysisRefs[i], "test");
							if(testRefs != null){
								for(int j = 0; j < testRefs.length; j++){
									String testUID = (String)xarch.get(testRefs[j], "id");
									if(testUID.equals(testToUpdate.getUID())){
										//Found it.  Remove it.
										xarch.remove(analysisRefs[i], "test", testRefs[j]);
										return;
									}
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e){
		}
	}
	
	public static void changeTestState(XArchFlatInterface xarch, ObjRef xArchRef, 
	ArchlightTest[] tests, Object element, int newState){
		List testsToChange = new ArrayList();
		
		if(element instanceof ObjRef){
			testsToChange.addAll(Arrays.asList(tests));
		}
		else if(element instanceof FolderNode){
			String[] basePathSegments = ((FolderNode)element).getPathSegments();
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < basePathSegments.length; i++){
				sb.append(basePathSegments[i]);
				if(i != (basePathSegments.length - 1)){
					sb.append("/");
				}
			}
			String basePath = sb.toString();
			for(int i = 0; i < tests.length; i++){
				if(tests[i].getCategory().startsWith(basePath)){
					testsToChange.add(tests[i]);
				}
			}
		}
		else if(element instanceof ArchlightTest){
			testsToChange.add((ArchlightTest)element);
		}
		
		ArchlightTest[] ts = (ArchlightTest[])testsToChange.toArray(new ArchlightTest[testsToChange.size()]);
		for(int i = 0; i < ts.length; i++){
			if((newState == ArchlightUtils.APPLIED) || (newState == ArchlightUtils.DISABLED)){
				ArchlightUtils.makeDocTestApplied(xarch, xArchRef, ts[i], new Boolean(newState == ArchlightUtils.APPLIED));
			}
			else if(newState == ArchlightUtils.NOT_APPLIED){
				ArchlightUtils.makeDocTestNotApplied(xarch, xArchRef, ts[i]);
			}
		}
	}
	
	public static IAction[] createTestMenuActions(XArchFlatInterface xarch, ObjRef xArchRef, 
	ArchlightTest[] tests, IResources resources, Object element){
		final XArchFlatInterface fxarch = xarch;
		final ObjRef fxArchRef = xArchRef;
		final ArchlightTest[] ftests = tests;
		
		List actions = new ArrayList();
		boolean multi = !(element instanceof ArchlightTest);
		
		String kindOfThing = multi ? "All Tests" : "Test";
		Image applyIcon = resources.getImage(multi ? IMAGE_FOLDER_APPLIED : IMAGE_TEST_APPLIED);
		Image disableIcon = resources.getImage(multi ? IMAGE_FOLDER_DISABLED : IMAGE_TEST_DISABLED);
		Image unapplyIcon = resources.getImage(multi ? IMAGE_FOLDER_NOTAPPLIED : IMAGE_TEST_NOTAPPLIED);

		final Object felement = element;
		
		Action applyAction = new Action("Make " + kindOfThing + " Applied/Enabled", ImageDescriptor.createFromImage(applyIcon)){
			public void run(){
				changeTestState(fxarch, fxArchRef, ftests, felement, APPLIED);
			}
		};
		actions.add(applyAction);
		
		Action disableAction = new Action("Make " + kindOfThing + " Applied/Disabled", ImageDescriptor.createFromImage(disableIcon)){
			public void run(){
				changeTestState(fxarch, fxArchRef, ftests, felement, DISABLED);
			}
		};
		actions.add(disableAction);

		Action unapplyAction = new Action("Make " + kindOfThing + " Unapplied", ImageDescriptor.createFromImage(unapplyIcon)){
			public void run(){
				changeTestState(fxarch, fxArchRef, ftests, felement, NOT_APPLIED);
			}
		};
		actions.add(unapplyAction);
		
		return (IAction[])actions.toArray(new IAction[actions.size()]);
	}


}
