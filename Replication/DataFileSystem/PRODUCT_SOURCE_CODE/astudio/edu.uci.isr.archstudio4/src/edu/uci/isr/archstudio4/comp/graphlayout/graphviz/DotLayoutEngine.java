package edu.uci.isr.archstudio4.comp.graphlayout.graphviz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.isr.archstudio4.graphlayout.GraphLayoutConstants;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutException;
import edu.uci.isr.sysutils.NativeProcess;
import edu.uci.isr.sysutils.SystemUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class DotLayoutEngine extends AbstractGraphvizLayoutEngine{

	public static final String LAYOUT_ENGINE_ID = "dot";
	public static final String LAYOUT_ENGINE_DESCRIPTION = "GraphViz DOT Engine";
	
	public String getID(){
		return LAYOUT_ENGINE_ID;
	}
	
	public String getDescription(){
		return LAYOUT_ENGINE_DESCRIPTION;
	}
	
	protected String runLayoutTool(XArchFlatInterface xarch, IPreferenceStore prefs, String toolInput) throws GraphLayoutException{
		int os = SystemUtils.guessOperatingSystem();
		String toolFilename = "dot";
		if(os == SystemUtils.OS_WINDOWS){
			toolFilename = toolFilename + ".exe";
		}
		
		String graphvizPath = prefs.getString(GraphLayoutConstants.PREF_GRAPHVIZ_PATH);
		
		String dotPath = graphvizPath;
		if(!dotPath.endsWith(File.separator)){
			dotPath += File.separator;
		}
		if(!dotPath.endsWith("bin" + File.separator)){
			dotPath += "bin" + File.separator;
		}
		dotPath += toolFilename;
		
		File dotExecutable = new File(dotPath);
		if(!dotExecutable.exists()){
			throw new GraphLayoutException("Can't find GraphViz dot.  Check Graph Layout Preferences.");
		}
		if(!dotExecutable.canRead()){
			throw new GraphLayoutException("No read permission on GraphViz dot.");
		}
		
		//We have to flip Y so we don't get a mathematical (i.e. 0,0 at the lower-left corner)
		//coordinate system.
		List<String> commandLineEltList = new ArrayList<String>();
		commandLineEltList.add(dotPath);
		commandLineEltList.add("-Tplain-ext");
		commandLineEltList.add("-y");

		try{
			String[] commandline = (String[])commandLineEltList.toArray(new String[0]);
			Process p = Runtime.getRuntime().exec(commandline);
			
			NativeProcess np = new NativeProcess(p, toolInput);
			np.start();
			np.waitForCompletion();
		
			String outputData = np.getStdout().trim();
			return outputData;
		}
		catch(IOException ioe){
			throw new GraphLayoutException("I/O Error Running Graphviz dot", ioe);
		}
	}
}
