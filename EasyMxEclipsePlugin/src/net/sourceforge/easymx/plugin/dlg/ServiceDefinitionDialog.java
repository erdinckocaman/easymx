package net.sourceforge.easymx.plugin.dlg;

import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.ServiceDefinition;
import net.sourceforge.easymx.plugin.controller.ServerController;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ServiceDefinitionDialog extends Dialog {
	
	private ServerController serverDefinitionController;
	
	private Text nameField;
	private Text addressField;
	private Combo resultTypeCombo;
	
	private ServerDefinition serverDefinition;
	private ServiceDefinition serviceDefinition;
	private boolean editMode;

	public ServiceDefinitionDialog(Shell parentShell) {
		super(parentShell);
		
		serviceDefinition = new ServiceDefinition();
		serviceDefinition.setAddress("/");
		serviceDefinition.setName("");
		
	}
	
	public void setServerDefinitionController(
			ServerController serverDefinitionController) {
		this.serverDefinitionController = serverDefinitionController;
	}
	
	public void setParentServerDefinition(ServerDefinition serverDefinition) {
		this.serverDefinition = serverDefinition;
	}
	
	public void setEditedServiceDefinition(ServiceDefinition serviceDefinition) {
		if ( serviceDefinition != null ) {
			this.serviceDefinition = serviceDefinition;
			editMode = true;
		}
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite)super.createDialogArea(parent);
		GridLayout layout = (GridLayout)comp.getLayout();
		
		layout.numColumns = 2;
		
		Label usernameLabel = new Label(comp, SWT.RIGHT);
		usernameLabel.setText("Service name: ");
		
		nameField = new Text(comp, SWT.SINGLE|SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.minimumWidth = 150;
		
		nameField.setLayoutData(data);
		nameField.setText(serviceDefinition.getName());
		
		Label passwordLabel = new Label(comp, SWT.RIGHT);
		passwordLabel.setText("Context URI: ");
		addressField = new Text(comp, SWT.SINGLE|SWT.BORDER);
		addressField.setText(serviceDefinition.getAddress());
		data = new GridData(GridData.FILL_HORIZONTAL);
		addressField.setLayoutData(data);
		
		Label lblResultType = new Label(comp, SWT.RIGHT);
		lblResultType.setText("Result type:");
		
		resultTypeCombo = new Combo(comp, SWT.READ_ONLY);
		resultTypeCombo.add("text");
		resultTypeCombo.add("tabular");
		
		if (serviceDefinition.isTabular() ) {
			resultTypeCombo.select(1);
		}else {
			resultTypeCombo.select(0);
		}
		
		
		return comp;
	}
	
	@Override
	protected void okPressed() {
		String resultType = resultTypeCombo.getItem(resultTypeCombo.getSelectionIndex());
		
		if ( !editMode ) {
			serverDefinitionController.onAddService(serverDefinition, 
				nameField.getText(), addressField.getText(), resultType);
		}else {
			String oldServiceName = serviceDefinition.getName();
			
			serviceDefinition.setAddress(addressField.getText());
			serviceDefinition.setName(nameField.getText());
			serviceDefinition.setResultType(resultType);
			serverDefinitionController.onEditService(serverDefinition, oldServiceName, serviceDefinition);
		}
		
		close();
	}
	
	
	
	

}
