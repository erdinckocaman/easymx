package net.sourceforge.easymx.plugin.dlg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.easymx.plugin.JdomParser;
import net.sourceforge.easymx.plugin.ServerDefinition;
import net.sourceforge.easymx.plugin.ServiceDefinition;
import net.sourceforge.easymx.plugin.ServiceDefinition.ServiceParam;
import net.sourceforge.easymx.plugin.controller.ServiceController;
import net.sourceforge.easymx.plugin.views.ServerView.TreeObject;
import net.sourceforge.easymx.plugin.views.ServerView.TreeParent;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jdom.Document;
import org.jdom.Element;

public class ServiceInvokerDialog extends Dialog {
	
	static class EditableTableItem{
		public String name;
		public String value;
		
		public EditableTableItem( String n, String v) {
			name = n;
			value = v;
		}
		
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	};
	
	private class NewRowAction extends Action {
	
		public NewRowAction(){
			super("Insert New Property");
		}
		public void run(){
			EditableTableItem newItem =	new EditableTableItem("Property", "Value");
			tableViewer.add(newItem);
		}
	}//end of class
	
	private TableViewer tableViewer;
	private Text txtResult;
	private ServiceController serviceController;
	private Text txtLog;
	private Table tblResult;
	private List<String> columnNames;
	private Text txtSelectedText;

	
	private static final String NAME_PROPERTY = "name";
	private static final String VALUE_PROPERTY = "value";	

	public ServiceInvokerDialog(Shell parentShell) {
		super(parentShell);
		
	}
	
	public void setServiceController(ServiceController serviceController) {
		this.serviceController = serviceController;
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		
		ServiceDefinition sd = getServiceDefinition();
		if ( sd != null ) {
			shell.setText(sd.getName());
		}
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite)super.createDialogArea(parent);
		GridLayout layout = (GridLayout)comp.getLayout();

		layout.numColumns = 1;
		
		final Table table = new Table(comp, SWT.FULL_SELECTION);
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = 250;
		
		GridData gdTable = new GridData(GridData.FILL_BOTH);
		gdTable.minimumWidth = 250;
		gdTable.minimumHeight = 100;
		table.setLayoutData(gdTable);
		
		tableViewer = buildAndLayoutTable(table);
		
		attachContentProvider(tableViewer);
		attachLabelProvider(tableViewer);
		attachCellEditors(tableViewer, table);
		MenuManager popupMenu = new MenuManager();
		IAction newRowAction = new NewRowAction();
		popupMenu.add(newRowAction);
		Menu menu = popupMenu.createContextMenu(table);
		table.setMenu(menu);

		ServiceDefinition sd = getServiceDefinition();
		
	
		if ( sd != null ) {		
			Object [] data = new Object[sd.getServiceParams().size()];
			int i=0;
			for(ServiceParam serviceParam: sd.getServiceParams()) {
				data[i] = new EditableTableItem(serviceParam.getName(), serviceParam.getValue());
				i++;
			}
			tableViewer.setInput(data);
		}
		

		
		addInvokerPanel(comp);
		
		GridData gdTxtResult = new GridData(GridData.FILL_BOTH);
		gdTxtResult.minimumWidth = 700;
		gdTxtResult.minimumHeight = 50;
		
		txtResult = new Text(comp,SWT.MULTI|SWT.BORDER);
		txtResult.setLayoutData(gdTxtResult);
		
		if ( sd.isTabular() ) {
			addResultTable(comp);
		}
		
		
		return comp;
	}
	
	private void addResultTable(Composite comp) {
		GridData gdTxtResult = new GridData(GridData.FILL_BOTH);
		gdTxtResult.minimumWidth = 450;
		gdTxtResult.minimumHeight = 200;
		
		tblResult =  new Table(comp, SWT.FULL_SELECTION);
		tblResult.setLayoutData(gdTxtResult);
		tblResult.setHeaderVisible(true);

		
		tblResult.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = tblResult.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = tblResult.getTopIndex();
				while (index < tblResult.getItemCount()) {
					boolean visible = false;
					TableItem item = tblResult.getItem(index);
					for (int i = 0; i < columnNames.size(); i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							// System.out.println("Item " + index + "-" + i +
							// "-" + item.getText(i));
							if ( item.getText(i) != null ) {
								txtSelectedText.setText(item.getText(i));
							}
							
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible)
						return;
					index++;
				}
			}
		});
		
		GridData gdTxtSelected = new GridData(GridData.FILL_BOTH);
		gdTxtSelected.minimumWidth = 450;
		gdTxtSelected.minimumHeight = 100;
		txtSelectedText = new Text(comp, SWT.MULTI|SWT.BORDER);
		txtSelectedText.setLayoutData(gdTxtSelected);
		
	}
	
	private void fillResultTable(String str) {
		tblResult.removeAll();
		
		if ( str == null ) {
			return;
		}
		
		String sep = System.getProperty("line.separator");

		JdomParser jdomParser = new JdomParser();
		Document doc = jdomParser.readFrom(str);
		
		List<Element> rows = doc.getRootElement().getChildren("row");
		
		if ( rows == null || rows.size() == 0 ) {
			txtLog.append(sep);
			txtLog.append("0 records found");
			return;
		}
		
		txtLog.append(sep);
		txtLog.append(rows.size() + " records found");
		
		if ( columnNames == null ) {
			columnNames = new ArrayList<String>();
			
			
			Element firstRow = rows.get(0);
			List<Element> propertyElements = firstRow.getChildren();
			if ( propertyElements == null ) {
				propertyElements = Collections.EMPTY_LIST;
			}
			
			for(Element propertyElement: propertyElements) {
				String name = propertyElement.getName();
				columnNames.add(name);
			}
			
			TableLayout layout = new TableLayout();
			
			
			int columnCount = columnNames.size();
			for(String columnName: columnNames) {
				TableColumn nameColumn = new TableColumn(tblResult, SWT.LEFT);
				nameColumn.setText(columnName);
				nameColumn.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent event) {
						if ( event.getSource() instanceof org.eclipse.swt.widgets.TableColumn ) {
							TableColumn col = (TableColumn) event.getSource();
							sortResultTable(col);
						}
					}
					


					@Override
					public void widgetDefaultSelected(SelectionEvent event) {
					}
				});
				
				layout.addColumnData(new ColumnWeightData(100/columnCount, 140, true));
				
			}
			tblResult.setLayout(layout);
		}
		
		
		for(Element row: rows) {			
			TableItem item = new TableItem(tblResult, SWT.NONE);

			int i=0;
			for(String column: columnNames) {				
				item.setText(i, row.getChild(column).getAttributeValue("value"));
				
				i++;
			}
		}
		
		for(TableColumn tc : tblResult.getColumns() ) {
			tc.pack();
		}
		
		tblResult.setRedraw(true);
		
		
	}
	
	private void sortResultTable(final TableColumn sortColumn) {
		TableItem[] items = tblResult.getItems();
		final int nextSortColumn = tblResult.indexOf(sortColumn);
		
		int sortCol = -1;
		
		if ( tblResult.getSortColumn() != null ) {
			sortCol = tblResult.indexOf(tblResult.getSortColumn());
		}
		
		final int currentSortColumn = sortCol;
		int dir = tblResult.getSortDirection();
		
		if (nextSortColumn == currentSortColumn) {
			dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
		} else {
			dir = SWT.UP;
		}
		
		final int finalDir = dir;
		
		Arrays.sort(items, new Comparator<TableItem>() {
			@Override
			public int compare(TableItem o1, TableItem o2) {

				
				int val = 0;
				if ( o1.getText(nextSortColumn) == null ) {
					val = -1;
				}
				
				val = o1.getText(nextSortColumn).compareTo(o2.getText(nextSortColumn));
				
				if ( finalDir == SWT.DOWN) {
					return -val;
				}else {
					return val;
				}
				
			}
		});
		
		TableColumn[] tcList = tblResult.getColumns();
		
		
		List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		for(TableItem ti: items) {
			Map<String, String> record = new HashMap<String, String>();
			
			int i=0;
			for(TableColumn column: tcList) {
				record.put(column.getText(), ti.getText(i));
				
				i++;
			}
			
			data.add(record);
		}
		
		
		tblResult.removeAll();
		
		for(Map<String, String> val: data) {
			TableItem item = new TableItem(tblResult, SWT.NONE);

			int i=0;
			for(TableColumn column: tcList) {				
				item.setText(i, val.get(column.getText()));
				
				i++;
			}
		}
		

		tblResult.setSortColumn(sortColumn);
		tblResult.setSortDirection(finalDir);
	}
	

	private void addInvokerPanel(Composite comp) {
		RowLayout layout = new RowLayout();
		Composite pnl = new Composite(comp, 0);
		pnl.setLayout(layout);
		
		
		Button buttonInvoke = new Button(pnl, SWT.PUSH);
		buttonInvoke.setText("Invoke service");
		buttonInvoke.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				actionInvoke();
			}	
		});
		
		RowData rd = new RowData();
		
		rd.height = 100;
		rd.width = 600;
		
		txtLog = new Text(pnl, SWT.MULTI|SWT.BORDER|SWT.WRAP);
		txtLog.setLayoutData(rd);
		
	}

	private TableViewer buildAndLayoutTable(final Table table){
		TableViewer tableViewer = new TableViewer(table);
		TableLayout layout = new TableLayout();
		
		layout.addColumnData(new ColumnWeightData(50, 75, true));
		layout.addColumnData(new ColumnWeightData(50, 75, true));
		table.setLayout(layout);
		
		TableColumn nameColumn = new TableColumn(table, SWT.CENTER);
		nameColumn.setText("Name");
		
		TableColumn valColumn = new TableColumn(table, SWT.CENTER);
		valColumn.setText("Value");
		table.setHeaderVisible(true);
		
		
		
		return tableViewer;
	}
	
	private void attachContentProvider(TableViewer viewer){
		viewer.setContentProvider(new IStructuredContentProvider() {
			
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
	}
	
	private void attachLabelProvider(TableViewer viewer) {
		viewer.setLabelProvider(new ITableLabelProvider() {
			
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				switch (columnIndex) {
				case 0:
					return ((EditableTableItem) element).name;
				case 1:
//					Number index = ((EditableTableItem) element).value;
//					return VALUE_SET[index.intValue()];
					return ((EditableTableItem) element).value;
				default:
					return "Invalid column: " + columnIndex;
				}
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener lpl) {
			}
		});
	}
	
	
	private void attachCellEditors(final TableViewer viewer, Composite parent) {
		viewer.setCellModifier(new ICellModifier() {
			
			public boolean canModify(Object element, String property){
				return true;
			}
			
			public Object getValue(Object element, String property) {
				if( NAME_PROPERTY.equals(property))
					return ((EditableTableItem)element).name;
				else
					return ((EditableTableItem)element).value;
				}
				
			public void modify(Object element, String property, Object value) {
				TableItem tableItem = (TableItem)element;
				EditableTableItem data = (EditableTableItem)tableItem.getData();
				
				if( NAME_PROPERTY.equals( property ) )
					data.name = value.toString();
				else
					//data.value = (Integer)value;
					data.value = (String)value;
				
					viewer.refresh(data);
			}
					
		});
		viewer.setCellEditors(new org.eclipse.jface.viewers.CellEditor[] {
					new TextCellEditor(parent),
					/*new ComboBoxCellEditor(parent, VALUE_SET )*/
					new TextCellEditor(parent)
					});
		
					viewer.setColumnProperties(new String[] {
							NAME_PROPERTY, VALUE_PROPERTY
						});
		}

	@Override
	protected void okPressed() {
		saveServiceParams();
		close();
		
	}
	
	@Override
	protected void cancelPressed() {
		//saveServiceParams();
		super.cancelPressed();
		
	}
	
	@Override
	protected void handleShellCloseEvent() {
		//saveServiceParams();
		
		super.handleShellCloseEvent();
		
	}
	
	private ServiceDefinition getServiceDefinition() {
		ServiceDefinition sd =  serviceController.getSelectedServiceDefinition(serviceController.getViewer());
		return sd;
	}
	
	private void actionInvoke() {
		TreeObject sdTreeObject = serviceController.getSelectedTreeObject(serviceController.getViewer());
		ServiceDefinition sd = (ServiceDefinition) serviceController.getSelectedServiceDefinition(serviceController.getViewer());
		
		if ( sdTreeObject == null || sd == null ) {
			return;
		}
				
		TreeParent treeParent = sdTreeObject.getParent();
		
		ServerDefinition serverDefinition = (ServerDefinition) serviceController.getSelectedServerDefinition(treeParent);
				
		String address = serverDefinition.getAddress() + sd.getAddress();
		

		Map<String, String> params = getParams();
		
		String out = serviceController.invokeService(address, params, txtLog);
		txtResult.setText(out);
		
		if ( sd.isTabular() && out.length() > 0 ) {
			fillResultTable(out);
		}
	}
	
	private Map<String, String> getParams() {
		Map<String, String> params = new HashMap<String, String>();
		
		int itemCount = tableViewer.getTable().getItemCount();
		if ( itemCount > 0 ) {
			
			for(int i=0; i < itemCount; i++) {
				EditableTableItem ti = (EditableTableItem) tableViewer.getElementAt(i);
				
				params.put(ti.name, ti.value);
				
			}
		}
		
		return params;
	}
	
	private void saveServiceParams() {
		TreeObject sdTreeObject = serviceController.getSelectedTreeObject(serviceController.getViewer());
		ServiceDefinition sd = (ServiceDefinition) serviceController.getSelectedServiceDefinition(serviceController.getViewer());
		
		if ( sdTreeObject == null || sd == null ) {
			return;
		}
				
		TreeParent treeParent = sdTreeObject.getParent();
		
		ServerDefinition serverDefinition = (ServerDefinition) serviceController.getSelectedServerDefinition(treeParent);
		
		serviceController.saveServiceParams(serverDefinition, sd, getParams());
	}
	
}
