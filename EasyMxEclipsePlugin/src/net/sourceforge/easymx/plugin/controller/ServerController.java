package net.sourceforge.easymx.plugin.controller;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.easymx.plugin.Activator;
import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.ServiceDefinition;
import net.sourceforge.easymx.plugin.controller.action.SyncServicesAction;
import net.sourceforge.easymx.plugin.dlg.ServerDefinitionDialog;
import net.sourceforge.easymx.plugin.dlg.ServiceDefinitionDialog;
import net.sourceforge.easymx.plugin.views.ServerView;
import net.sourceforge.easymx.plugin.views.ServerView.ServerDefinitionNode;
import net.sourceforge.easymx.plugin.views.ServerView.TreeObject;
import net.sourceforge.easymx.plugin.views.ServerView.TreeParent;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;




public class ServerController extends BaseController {

	public ServerController() {
	}
	
	@Override
	public void onDoubleClicked() {
		ServerDefinitionNode selectedTreeObject = (ServerDefinitionNode)getSelectedTreeObject(getViewer());
		
		if ( selectedTreeObject == null ) {
			return;
		}
		
		ServerDefinition sd = selectedTreeObject.getServerDefinition();
		
		ServerDefinitionDialog dlg = new ServerDefinitionDialog(getViewer().getControl().getShell());
		
		RootController rc = new RootController();
		rc.setViewer(getViewer());
		dlg.setRootController(rc);
		dlg.setServerDefinition(sd);
		dlg.open();
	}
	

	
	@Override
	public List<Action> getPopupActions(final Viewer viewer) {
		Action actionAddService = new Action() {
			public void run() {
				ServerDefinition serverDefinition = ((ServerDefinitionNode) getSelectedTreeObject(viewer)).getServerDefinition();
				ServiceDefinitionDialog dlg = new ServiceDefinitionDialog(viewer.getControl().getShell());
				
				dlg.setServerDefinitionController(ServerController.this);
				dlg.setParentServerDefinition(serverDefinition);
				
				dlg.open();
			}
		};
		actionAddService.setText("Add service");
		actionAddService.setToolTipText("Add service");
		actionAddService.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));	
		
		Action actionRemove = new Action() {
			public void run() {			
				onRemoveServer();
			}

		};
		actionRemove.setText("Remove server");
		actionRemove.setToolTipText("Remove server");
		actionRemove.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(actionAddService);
		
		
		actionList.add(actionRemove);
		
		actionList.add(new SyncServicesAction(this));
		
		return actionList;
	}
	
	public void onAddService(ServerDefinition serverDefinition, String name, String address, String resultType) {
		Activator activator = Activator.getDefault();
		
		ServiceDefinition sd = new ServiceDefinition();
		sd.setAddress(address);
		sd.setName(name);
		sd.setResultType(resultType);
		
		activator.addServiceDefinition(serverDefinition, sd);
		
		TreeParent treeParent = (TreeParent)getSelectedTreeObject(getViewer());
		
		if (  treeParent != null ) {
			getViewer().refresh(treeParent, true);
		}
		
	}
	
	private void onRemoveServer() {
		TreeParent treeParent = (TreeParent)getSelectedTreeObject(getViewer());
		
		if ( treeParent != null ) {
			Activator activator = Activator.getDefault();
			activator.removeServer( getSelectedServerDefinition(treeParent));
			
//			TreeParent parent = treeParent.getParent();
//			parent.removeChild(treeParent);
			
			getViewer().refresh();
		}

	}
	

	public void onEditService(ServerDefinition serverDefinition,
			String oldServiceName, ServiceDefinition serviceDefinition) {
		
		Activator activator = Activator.getDefault();
		activator.editServiceDefinition(serverDefinition, oldServiceName, serviceDefinition);
		
		TreeObject selected = getSelectedTreeObject(getViewer());
		if ( selected != null ) {
			selected.setName(serviceDefinition.getName());
			
			getViewer().refresh();
		}
	}

	public List<ServerDefinition> getSelectedServers() {
		List<ServerDefinition> sdList = new ArrayList<ServerDefinition>();
		
		for(Object node: getSelectedTreeValues(getViewer()) ) {
			if ( node instanceof ServerDefinitionNode ) {
				sdList.add(((ServerDefinitionNode) node).getServerDefinition());
			}
		}
		
		return sdList;
	}

	public void refreshTree() {
		getViewer().refresh();
	}

}
