package edu.uci.isr.archstudio4.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class ArchStudio4Nature implements IProjectNature{

	public static final String ID = "edu.uci.isr.archstudio4.ArchStudioNature";
	
	public IProject theProject = null;
	
	public ArchStudio4Nature(){
	}

	public void configure() throws CoreException{
	}

	public void deconfigure() throws CoreException{
	}

	public IProject getProject(){
		return theProject;
	}

	public void setProject(IProject project){
		theProject = project;
	}

}
