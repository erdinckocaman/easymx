package net.sourceforge.easymx.plugin.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.ServiceDefinition;
import net.sourceforge.easymx.plugin.views.ServerView.ServerDefinitionNode;
import net.sourceforge.easymx.plugin.views.ServerView.ServiceDefinitionNode;
import net.sourceforge.easymx.plugin.views.ServerView.TreeObject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class BaseController {
	
	private Viewer viewer;
	
	public void setViewer(Viewer viewer) {
		this.viewer = viewer;
	}
	
	public TreeViewer getViewer() {
		return (TreeViewer)viewer;
	}

	public void handleAction(Viewer viewer, String action) {
	}
	
	public List<Action> getPopupActions(Viewer viewer) {
		return new ArrayList<Action>();
	}
	
//	public Object getSelectedTreeValue(Viewer viewer) {
//		TreeSelection treeSelection = (TreeSelection) viewer.getSelection();
//		TreeObject selected = (TreeObject)treeSelection.getFirstElement();
//		return selected.getValue();
//	}
	
//	@SuppressWarnings("unchecked")
//	public List<Object> getSelectedTreeValues(Viewer viewer) {
//		TreeSelection treeSelection = (TreeSelection) viewer.getSelection();
//		
//		List<Object> vals = new ArrayList();
//		
//		if ( treeSelection.size() > 0 ) {
//			Iterator iterator = treeSelection.iterator();
//			while ( iterator.hasNext() ) {
//				TreeObject selected = (TreeObject) iterator.next();
//				
//				vals.add(selected.getValue());
//			}
//		}
//		
//		return vals;
//	}
	
	public List<TreeObject> getSelectedTreeValues(Viewer viewer) {
		List<TreeObject> treeObjectList = new ArrayList<TreeObject>();
		
		TreeSelection treeSelection = (TreeSelection) viewer.getSelection();
		
		if ( treeSelection.size() > 0 ) {
			@SuppressWarnings("unchecked")
			Iterator<TreeObject> iterator = treeSelection.iterator();
			while ( iterator.hasNext() ) {
				TreeObject selected = iterator.next();
				treeObjectList.add(selected);
			}
		}
		
		return treeObjectList;
	}
	
	
	
	public TreeObject getSelectedTreeObject(Viewer viewer) {
		TreeSelection treeSelection = (TreeSelection) viewer.getSelection();
		TreeObject selected = (TreeObject)treeSelection.getFirstElement();
		
		return selected;
	}
	
	public ServerDefinition getSelectedServerDefinition(Viewer viewer) {
		TreeSelection treeSelection = (TreeSelection) viewer.getSelection();
		TreeObject selected = (TreeObject)treeSelection.getFirstElement();
		
		
		return getSelectedServerDefinition(selected);

	}
	
	public ServerDefinition getSelectedServerDefinition(TreeObject selected) {
		if ( selected == null ) {
			return null;
		}
		
		ServerDefinitionNode sdNode = (ServerDefinitionNode) selected;
		return sdNode.getServerDefinition();
	}
	
	public ServiceDefinition getSelectedServiceDefinition(Viewer viewer) {
		TreeSelection treeSelection = (TreeSelection) viewer.getSelection();
		TreeObject selected = (TreeObject)treeSelection.getFirstElement();
		
		return getSelectedServiceDefinition(selected);
		
	}
	
	public ServiceDefinition getSelectedServiceDefinition(TreeObject selected) {
		if ( selected == null ) {
			return null;
		}
		ServiceDefinitionNode sdNode = (ServiceDefinitionNode) selected;
		return sdNode.getServiceDefinition();
	}
	

	public void onDoubleClicked() {
		
	}
	
}
