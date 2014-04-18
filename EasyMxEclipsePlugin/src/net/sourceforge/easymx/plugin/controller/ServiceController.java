package net.sourceforge.easymx.plugin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.easymx.plugin.Activator;
import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.ServiceDefinition;
import net.sourceforge.easymx.plugin.dlg.ServiceDefinitionDialog;
import net.sourceforge.easymx.plugin.dlg.ServiceInvokerDialog;
import net.sourceforge.easymx.plugin.views.ServerView.TreeObject;
import net.sourceforge.easymx.plugin.views.ServerView.TreeParent;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class ServiceController extends BaseController {

	@Override
	public List<Action> getPopupActions(final Viewer viewer) {
		
		Action actionEdit = new Action() {
			@Override
			public void run() {
				super.run();
				
				onEditServiceDefinition();
			}


		};
		
		actionEdit.setText("Edit service");
		actionEdit.setToolTipText("Edit service");
		actionEdit.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		
		Action actionRemove = new Action() {
			@Override
			public void run() {
				onRemoveServiceDefinition();
			}


		};
		
		actionRemove.setText("Remove service");
		actionRemove.setToolTipText("Remove service");
		actionRemove.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(actionEdit);
		actionList.add(actionRemove);
		
		
		return actionList;
	}
	
	private void onEditServiceDefinition() {
		ServiceDefinition sd = getServiceDefinition();
		
		if ( sd == null ) {
			System.out.println("No Service Definition selected !!");
		}else {
			ServiceDefinitionDialog dlg = new ServiceDefinitionDialog(getViewer().getControl().getShell());
			dlg.setEditedServiceDefinition(sd);
			dlg.setParentServerDefinition(getServerDefinition());
			
			ServerController sc = new ServerController();
			sc.setViewer(getViewer());
			dlg.setServerDefinitionController(sc);
			
			dlg.open();
		}

		
	}
	
	private ServiceDefinition getServiceDefinition() {
		TreeObject treeObject = getSelectedTreeObject(getViewer());
		
		if ( treeObject != null ) {
			return (ServiceDefinition) getSelectedServiceDefinition(treeObject);
		}else {
			return null;
		}
	}
	
	private ServerDefinition getServerDefinition() {
		TreeObject treeObject = getSelectedTreeObject(getViewer());
		if ( treeObject != null ) {
			TreeParent parent = treeObject.getParent();
			return (ServerDefinition)getSelectedServerDefinition(parent);
		}
		
		return null;
	}
	
	private void onRemoveServiceDefinition() {
		TreeObject treeObject = getSelectedTreeObject(getViewer());
		
		if ( treeObject != null ) {
			Activator activator = Activator.getDefault();
			
			TreeParent parent = treeObject.getParent();
			
			activator.removeService((ServerDefinition)getSelectedServerDefinition(parent),
					(ServiceDefinition)getSelectedServiceDefinition(treeObject));
			
			getViewer().refresh(parent, true);
		}
	}
	
	@Override
	public void onDoubleClicked() {
		ServiceInvokerDialog serviceInvokerDialog = 
			new ServiceInvokerDialog(getViewer().getControl().getShell());
		
		serviceInvokerDialog.setServiceController(this);
		serviceInvokerDialog.open();
	}

	public String invokeService(String address, Map<String, String> params, Text txtLog) {
		String sep = System.getProperty("line.separator");
		
		try {			
			txtLog.append(address);
			txtLog.append(sep);
			txtLog.append("" + params);
			txtLog.append(sep);
			
			Activator activator = Activator.getDefault();
			
			Client client = activator.getRestfulClient();
			WebResource r = client.resource(address);
			
			
			if ( params != null ) {
				for(Map.Entry<String, String> entry : params.entrySet()) {
					r = r.queryParam(entry.getKey(), entry.getValue());
				}
			}
			
			txtLog.append("" + r);
			txtLog.append(sep);
		
		
			long start = System.currentTimeMillis();
			String result = r.get(String.class);
			long end = System.currentTimeMillis();
		
			txtLog.append("Result returned successfully in " + (end - start) + " ms");
			
			return result;
			
		}catch(Exception e) {
			txtLog.append("ERROR>");
			txtLog.append(sep);
			txtLog.append(e.getMessage());
			txtLog.append(sep);
			return "";
		}

	}

	public void saveServiceParams(ServerDefinition serverDefinition,
			ServiceDefinition sd, Map<String, String> params) {
		if ( serverDefinition == null || sd == null ) {
			return;
		}
		
		Activator activator = Activator.getDefault();
		activator.saveServiceParams(serverDefinition, sd, params);
		
		sd.reloadParams(params);
	}
}
