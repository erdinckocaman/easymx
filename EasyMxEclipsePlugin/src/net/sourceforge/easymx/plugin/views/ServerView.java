package net.sourceforge.easymx.plugin.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.easymx.plugin.Activator;
import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.ServiceDefinition;
import net.sourceforge.easymx.plugin.controller.BaseController;
import net.sourceforge.easymx.plugin.controller.RootController;
import net.sourceforge.easymx.plugin.controller.ServerController;
import net.sourceforge.easymx.plugin.controller.ServiceController;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;



/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class ServerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "net.sourceforge.easymx.plugin.views.ServerView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private static Activator activator = Activator.getDefault();

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */

	public static abstract class TreeObject implements IAdaptable {
		private String name;
		private TreeParent parent;
		private BaseController controller;


		public TreeObject(String name) {
			this.name = name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setController(BaseController controller) {
			this.controller = controller;
		}

		public BaseController getController() {
			return controller;
		}
		public String getName() {
			return name;
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public String toString() {
			return getName();
		}
		public Object getAdapter(Class key) {
			if ( ServerDefinition.class.equals(key) ) {
				return new ServerController();
			}else
				return new BaseController();
		}
	}
	
	public static class ServiceDefinitionNode extends TreeObject {

		private ServiceDefinition serviceDefinition;

		public ServiceDefinitionNode(ServiceDefinition sd) {
			super(sd.getName());
			
			this.serviceDefinition= sd;
		}
		
		public ServiceDefinition getServiceDefinition() {
			return serviceDefinition;
		}
		
	}

	public static abstract class TreeParent<T extends TreeObject> extends TreeObject {
		private ArrayList<T> children;
		public TreeParent(String name) {
			super(name);
			children = new ArrayList<T>();
		}
		
		public List<T> getChildrenList() {
			return children;
		}
		
		public TreeObject [] getChildren() {
			return (TreeObject [])getChildrenList().toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return getChildrenList().size()>0;
		}
	}
	
	public static class ServerDefinitionNode extends TreeParent<ServiceDefinitionNode> {

		private ServerDefinition serverDefinition;

		public ServerDefinitionNode(ServerDefinition serverDefinition) {
			super(serverDefinition.getName());
			
			this.serverDefinition = serverDefinition;
			
		}
		
		public ServerDefinition getServerDefinition() {
			return serverDefinition;
		}
		
		@Override
		public List<ServiceDefinitionNode> getChildrenList() {
			List<ServiceDefinition> sdList = serverDefinition.getServiceDefinitionList();
			List<ServiceDefinitionNode> nodeList = new ArrayList<ServiceDefinitionNode>();
			
			for(ServiceDefinition sd: sdList) {
				ServiceDefinitionNode node = new ServiceDefinitionNode(sd);
				ServiceController serviceController = new ServiceController();
				serviceController.setViewer(getController().getViewer());
				node.setController(serviceController);
				node.setParent(this);
				
				nodeList.add(node);
				
			}
			
			return nodeList;
		}
		
		
		
	}
	
	public static class InvisibleRoot extends TreeParent<RootNode> {
		RootNode rootNode;
		
		public InvisibleRoot(Viewer viewer) {
			super("");
			
			rootNode = new RootNode();
			RootController rootController = new RootController();
			rootController.setViewer(viewer);
			rootNode.setController(rootController);
			
		}
		
		@Override
		public List<RootNode> getChildrenList() {
			return Collections.singletonList(rootNode);
		}
		
		
		
	}
	
	public static class RootNode extends TreeParent<ServerDefinitionNode> {
				

		public RootNode() {
			super("EasyMX Servers");


		}
		
		@Override
		public List<ServerDefinitionNode> getChildrenList() {
			
			List<ServerDefinition> sdList = activator.getServerDefinitions();
			
			List<ServerDefinitionNode> sdNodeList = new ArrayList<ServerView.ServerDefinitionNode>();
			
			for(ServerDefinition sd: sdList) {
				ServerDefinitionNode node = new ServerDefinitionNode(sd);
				
				ServerController serverController = new ServerController();
				serverController.setViewer(getController().getViewer());
				node.setController(serverController);
				node.setParent(this);
				
				sdNodeList.add(node);
			}
			
			return sdNodeList;
			
			
		}
		
	}

	class ViewContentProvider implements IStructuredContentProvider, 
	ITreeContentProvider {
		private TreeParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
		/*
		 * We will set up a dummy model to initialize tree heararchy.
		 * In a real code, you will connect to a real model and
		 * expose its hierarchy.
		 */
		private void initialize() {			
			invisibleRoot = new InvisibleRoot(viewer);
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof TreeParent)
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public ServerView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());


		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "ApsEclipsePlugin.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				List<Action> actions = getSelectedTreeObject().getController().getPopupActions(viewer);

				ServerView.this.fillContextMenu(manager, actions);


			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager, List<Action> actions) {
		for(Action action: actions) {
			manager.add(action);
		}
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private TreeObject getSelectedTreeObject() {
		TreeSelection treeSelection = (TreeSelection) viewer.getSelection();
		TreeObject selected = (TreeObject)treeSelection.getFirstElement();
		return selected;
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");

				TreeObject selected = getSelectedTreeObject();
				if ( selected == null ) {
					return;
				}

				selected.getController().handleAction(viewer, "action1");


			}


		};
		action1.setText("Add server");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));



		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				//ISelection selection = viewer.getSelection();
				//Object obj = ((IStructuredSelection)selection).getFirstElement();
				//showMessage("Double-click detected on "+obj.toString());

				TreeObject selectedTreeObject = getSelectedTreeObject();
				if (selectedTreeObject != null ) {
					selectedTreeObject.getController().onDoubleClicked();
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
				viewer.getControl().getShell(),
				"Sample View",
				message);
	}


	public void setFocus() {
		viewer.getControl().setFocus();
	}

}