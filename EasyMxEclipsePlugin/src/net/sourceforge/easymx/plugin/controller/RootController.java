package net.sourceforge.easymx.plugin.controller;

import java.util.Collections;
import java.util.List;

import net.sourceforge.easymx.plugin.Activator;
import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.dlg.ServerDefinitionDialog;
import net.sourceforge.easymx.plugin.views.ServerView;
import net.sourceforge.easymx.plugin.views.ServerView.TreeObject;
import net.sourceforge.easymx.plugin.views.ServerView.TreeParent;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


public class RootController extends BaseController {
	
	@Override
	public void handleAction(Viewer viewer, String action) {
		ServerDefinitionDialog dlg = new ServerDefinitionDialog(viewer.getControl().getShell());
		dlg.open();
	}
	
	@Override
	public List<Action> getPopupActions(final Viewer viewer) {
		Action actionAdd = new Action() {
			public void run() {			
				ServerDefinitionDialog dlg = new ServerDefinitionDialog(viewer.getControl().getShell());
				dlg.setRootController(RootController.this);
				dlg.open();
			}
			

		};
		actionAdd.setText("Add server");
		actionAdd.setToolTipText("Add server");
		actionAdd.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		

		

		
		return Collections.singletonList(actionAdd);
	}
	
	public void onAddServer(String name, String address) {		
		if ( name != null && name.length() >0 && address != null && address.length() > 0 ) {
			ServerDefinition serverDefinition = new ServerDefinition();
			serverDefinition.setAddress(address);
			serverDefinition.setName(name);
			
			Activator activator = Activator.getDefault();
			activator.addServerDefinition(serverDefinition);
			
			TreeParent treeParent = (TreeParent)getSelectedTreeObject(getViewer());
			
			if (  treeParent != null ) {
				//ServerView.addTreeObject(getViewer(), treeParent, serverDefinition);
				
				getViewer().refresh(treeParent, true);
			}
			
		}else {
		}
	}
	
	@Override
	public void onDoubleClicked() {
	}

	public void onEditServer(String oldServerName,
			ServerDefinition serverDefinition) {
		
		Activator activator = Activator.getDefault();
		activator.editServerDefinition(oldServerName, serverDefinition);
		
		TreeObject selected = getSelectedTreeObject(getViewer());
		if ( selected != null ) {
			selected.setName(serverDefinition.getName());
		}
		
		getViewer().refresh();
	}
}
