package net.sourceforge.easymx.plugin.controller.action;

import java.util.List;

import net.sourceforge.easymx.plugin.CombinationGenerator;
import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.ServiceDefinition;
import net.sourceforge.easymx.plugin.controller.ServerController;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class SyncServicesAction extends Action {

	private ServerController serverController;

	public SyncServicesAction(ServerController serverController) {
		this.serverController = serverController;
		
		setText("Syncronize services");
		
		setToolTipText("Syncronize services");
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
	}
	
	@Override
	public void run() {
		List<ServerDefinition> sdList = serverController.getSelectedServers();
		
		int size = sdList.size();
		//System.out.println("sd list=" + size);
		
		if ( size < 2 ) {
			return;
		}
		
		CombinationGenerator combinationGenerator = new CombinationGenerator(size, 2);
		
		int total = combinationGenerator.getTotal().intValue();
		
		for(int i=0; i < total; i++) {
			int[] next = combinationGenerator.getNext();
			
			int first = next[0];
			int second = next[1];
			
			ServerDefinition firstServer = sdList.get(first);
			ServerDefinition secondServer = sdList.get(second);
			
			List<ServiceDefinition> firstList = firstServer.findServicesNotExistingIn(secondServer);
			
			List<ServiceDefinition> secondList = secondServer.findServicesNotExistingIn(firstServer);
			
			System.out.println("first  list=" + firstList);
			System.out.println("second list=" + secondList);
			
			
			for(ServiceDefinition sd: firstList) {
				System.out.println("second server=" + secondServer.getName() + " sd=" + sd.getName());
				serverController.onAddService(secondServer, sd.getName(), sd.getAddress(), sd.getResultType());
				
			}
			
			for(ServiceDefinition sd: secondList) {
				System.out.println("first server=" + firstServer.getName() + " sd=" + sd.getName());
				serverController.onAddService(firstServer, sd.getName(), sd.getAddress(), sd.getResultType());
			}
		}
		
		serverController.refreshTree();
		
	}
	
	
	
}
