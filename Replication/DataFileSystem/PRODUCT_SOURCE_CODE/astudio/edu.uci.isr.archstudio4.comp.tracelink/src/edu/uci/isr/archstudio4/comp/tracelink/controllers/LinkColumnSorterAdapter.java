/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.controllers;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * @author dpurpura
 *
 */
public class LinkColumnSorterAdapter extends SelectionAdapter{
		
		private ITracelinkController controller;
		private String sortingCriteria;
		
		public LinkColumnSorterAdapter(ITracelinkController controller, String sortingCritera){
			this.controller = controller;
			this.sortingCriteria = sortingCritera;
		}
		
		
		public void widgetSelected(SelectionEvent e) {
			System.out.println(sortingCriteria + " selected");
			controller.setSortCriteria(sortingCriteria);
			controller.updateViews();
		}
		
	}
