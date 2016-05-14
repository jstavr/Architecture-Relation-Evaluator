package edu.uci.isr.archstudio4.comp.tracelink.hypermedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class CodeEditor
	implements ToolAdapter{

	private static String ENDL = System.getProperty("line.separator");

	private StyledText editor;
	private ScrolledComposite scroll;

	public CodeEditor(Composite parent){
		//scroll = 
		//	new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		editor = new StyledText(parent, SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);

		//scroll.setMinSize(editor.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public void execute(String executableURI){

	}

	public boolean isFound(String searchItem){
		return false;
	}

	public void render(String fileURI){
		File file = new File(fileURI);

		if(file.exists()){
			try{
				BufferedReader reader = new BufferedReader(new FileReader(fileURI));
				String s;
				while(reader.ready()){
					s = reader.readLine();
					editor.append(s);
					editor.append(ENDL);
				}
				editor.setLayout(new FillLayout());
				editor.setSize(200, 200);
				editor.layout();

				//					scroll.setExpandHorizontal(true);
				//					scroll.setExpandVertical(true);
				//					scroll.setContent(editor);
				//scroll.setMinSize(400, 400);

			}
			catch(FileNotFoundException e){
				e.printStackTrace();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		else{
			try{
				System.err.println("CodeEditor: no such file: " + file.getCanonicalPath());
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args){

	}

}
