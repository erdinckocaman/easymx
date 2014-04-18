package net.sourceforge.easymx.plugin.dlg;

import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.controller.RootController;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



public class ServerDefinitionDialog extends Dialog {
	
	private Text nameField;
	private Text addressField;
	
	RootController rootController;
	
	private ServerDefinition serverDefinition;
	private boolean editMode;
	

	public ServerDefinitionDialog(Shell parentShell) {
		super(parentShell);
		
		serverDefinition = new ServerDefinition();
		serverDefinition.setName("");
		serverDefinition.setAddress("http://");
		
	}
	
	public void setRootController(RootController rootController) {
		this.rootController = rootController;
	}
	
	public void setServerDefinition(ServerDefinition serverDefinition) {
		this.serverDefinition = serverDefinition;
		editMode = true;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite)super.createDialogArea(parent);
		GridLayout layout = (GridLayout)comp.getLayout();
		
		
		layout.numColumns = 2;
		
		Label usernameLabel = new Label(comp, SWT.RIGHT);
		usernameLabel.setText("Server name: ");
		nameField = new Text(comp, SWT.SINGLE|SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		nameField.setText(serverDefinition.getName());
		
		nameField.setLayoutData(data);
		Label passwordLabel = new Label(comp, SWT.RIGHT);
		passwordLabel.setText("Address: ");
		addressField = new Text(comp, SWT.SINGLE|SWT.BORDER);
		addressField.setText(serverDefinition.getAddress());
		
	
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.minimumWidth = 300;
		addressField.setLayoutData(data);
		
		return comp;
	}
	
	@Override
	protected void okPressed() {
		if ( !editMode ) {
			rootController.onAddServer(nameField.getText(), addressField.getText());
		}else {
			String oldServerName = serverDefinition.getName();
			
			serverDefinition.setName(nameField.getText());
			serverDefinition.setAddress(addressField.getText());
			
			rootController.onEditServer(oldServerName, serverDefinition);
		}
		close();
	}
	
	
	
	

}
