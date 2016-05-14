/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.preferences;

import edu.uci.isr.sysutils.SystemUtils;

/**
 * @author Hazel
 */
public class PreferencesConstants{

//	public static final String PROP_BASE_URL = "/res";
//	public static final String PROP_URL = "/tracelink.properties";

	public static final String DEFAULT_CONFIG_BUNDLE_PATH = "/res/defaultconfig/";
	public static final String PROP_BUNDLE_STATE_PATH = "/tracelink.properties";

	//default urls for reports and rules
	public static final String DEFAULT_REPORT_PATH = "/report";
	public static final String DEFAULT_RULE_PATH = "/rule";

	public static final String DEFAULT_REPORT_TEMPLATE_URL = "/template.html";
	public static final String DEFAULT_REPORT_URL = "/report.html";
	public static final String DEFAULT_RULE_SESSION_URL = "/session.xml";
	public static final String DEFAULT_RULE_SESSION_OUT_URL = "/sessionOut.xml";

	//default entries for the System properties
	public static final String DEFAULT_PROP_REPORT_TEMPLATE = "traceDefaultReportTemplatePath";
	public static final String DEFAULT_PROP_REPORT = "traceDefaultReportPath";
	public static final String DEFAULT_PROP_RULE_SESSION = "traceDefaultRuleSessionPath";
	public static final String DEFAULT_PROP_RULE_SESSION_OUT = "traceDefaultRuleSessionOutPath";
	public static final String PROP_AS_INSTALL_PATH = "traceASInstallPath";

	//entries used for user defined System properties
	public static final String PROP_REPORT = "traceUserReportPath";
	public static final String PROP_RULE = "traceUserRulePath";
	public static final String PROP_BROWSER = "traceBrowserUserPath";

	/**
	 * Method determines the fileSeparator based on the given filePath
	 * 
	 * @param filePath
	 * @return the file separator for the current OS
	 */
	//TODO: move this to SystemUtils?
	public static String getFileSeparator(String filePath){
		if(filePath.length() == 0){
			return null;
		}
		else{
			if(filePath.contains("\\")){
				return "\\";
			}
			else if(filePath.contains("/")){
				return "/";
			}
			else{
				//on windows, this is a single backslash
				return SystemUtils.fileSeparator;
			}

		}
	}

	/**
	 * Method extracts the filename in the given path
	 * 
	 * @param filePath
	 * @return the filename without file separators
	 */
	public static String getFileName(String filePath){
		String fileSeparator = getFileSeparator(filePath);
		int location = filePath.lastIndexOf(fileSeparator);
		return filePath.substring(location + 1);
	}

	/**
	 * Method extracts everything except the filename in the given path
	 * 
	 * @param filePath
	 * @return the filename without file separators
	 */
	public static String getPathOnly(String filePath){
		String fileSeparator = getFileSeparator(filePath);
		int location = filePath.lastIndexOf(fileSeparator);
		return filePath.substring(0, location + 1);
	}
}
