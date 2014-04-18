package net.sourceforge.easymx.plugin;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Parent;
import org.osgi.framework.BundleContext;


import com.sun.jersey.api.client.Client;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "EasyMxEclipsePlugin"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	
	private final static String FILE_NAME = ".easymx-servers.xml";
	private List<ServerDefinition> serverDefinitions;
	private JdomParser jdomParser = new JdomParser();
	
	Client restfulClient;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		createFileIfNotExists();
		readServerDefinitions();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		
		if ( restfulClient != null ) {
			restfulClient.destroy();
		}
		super.stop(context);
		
		super.stop(context);
	}
	
	public Client getRestfulClient() {
		if ( restfulClient == null ) {
			restfulClient = Client.create();
			restfulClient.setConnectTimeout(60 * 1000);
			restfulClient.setReadTimeout(60 * 1000 * 3);
			
		}
		return restfulClient;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	private void readServerDefinitions(){
		jdomParser = new JdomParser();
		
		System.out.println("ServerDefinitions read from=" + getDefinitionFile());
		
		Document doc;
		try {
			doc = jdomParser.readFrom(FileUtils.readFileToString(getDefinitionFile(),
					"UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		serverDefinitions = new ArrayList<ServerDefinition>();
		
		Element rootElement = doc.getRootElement();
		@SuppressWarnings("unchecked")
		List<Element> serverElementList = rootElement.getChildren("server");
		Iterator<Element> iterator = serverElementList.iterator();
		
		while ( iterator.hasNext() ) {
			Element serverElement = (Element) iterator.next();
			
			ServerDefinition serverDefinition = new ServerDefinition();
			serverDefinition.setName(serverElement.getAttributeValue("name"));
			serverDefinition.setAddress(serverElement.getAttributeValue("address"));
			
			serverDefinitions.add(serverDefinition);
			
			@SuppressWarnings("unchecked")
			List<Element> serviceElementList = serverElement.getChildren("service");
			if ( serviceElementList == null ) {
				serviceElementList = new ArrayList<Element>();
			}
			
			for(Element element: serviceElementList) {
				ServiceDefinition serviceDefinition = new ServiceDefinition();
				serviceDefinition.setName(element.getAttributeValue("name"));
				serviceDefinition.setAddress(element.getAttributeValue("address"));
				Attribute attrib = element.getAttribute("resultType");
				if ( attrib != null ) {
					serviceDefinition.setResultType(attrib.getValue());
				}
				
				serverDefinition.addService(serviceDefinition);
				
				@SuppressWarnings("unchecked")
				List<Element> paramElements =  element.getChildren("param");
				if ( paramElements == null ) {
					paramElements = new ArrayList<Element>();
				}
				
				for(Element paramElement: paramElements) {
					serviceDefinition.addParam(paramElement.getAttributeValue("name"),
							paramElement.getAttributeValue("value"));
				}
			}
		}
		
	}
	
	private void createFileIfNotExists() throws Exception{
		File file = getDefinitionFile();
		
		if ( !file.exists() ) {
			FileUtils.writeStringToFile(file, "<easymx></easymx>");
		}
	}
	
	private File getDefinitionFile() {
		String homeDir = System.getProperty("user.home");
		
		String fileSeparator = System.getProperty("file.separator");
		
		String path = homeDir + fileSeparator + FILE_NAME;
		
		return new File(path);
	}
	
	public List<ServerDefinition> getServerDefinitions() {
		return serverDefinitions;
	}
	
	/**
	 * 
	 * @param serverDefinition
	 * XML parsing and file store
	 */
	public void addServerDefinition(ServerDefinition serverDefinition) {
		Document doc = readServersFile();
		
		Element serverElement = new Element("server");
		serverElement.setAttribute("name", serverDefinition.getName());
		serverElement.setAttribute("address", serverDefinition.getAddress());
		
		doc.getRootElement().addContent(serverElement);
		
		writerServersFile(doc);
		
		readServerDefinitions();
	}
	
	public void addServiceDefinition(ServerDefinition serverDefinition, ServiceDefinition serviceDefinition) {
		Document doc = readServersFile();
		
		Element elementFound = findServerElement(doc, serverDefinition.getName());
		
		if ( elementFound != null ) {
			Element sdElement = new Element("service");
			sdElement.setAttribute("name", serviceDefinition.getName());
			sdElement.setAttribute("address", serviceDefinition.getAddress());
			sdElement.setAttribute("resultType", serviceDefinition.getResultType());
			elementFound.addContent(sdElement);
			
			writerServersFile(doc);
			
			serverDefinition.addService(serviceDefinition);
			
		}
		
	}
	
	private Element findServerElement(Document doc, String name) {
		Element elementFound = null;
		
		@SuppressWarnings("unchecked")
		List<Element> serverElementList = doc.getRootElement().getChildren("server");
		if ( serverElementList == null ) {
			serverElementList = new ArrayList<Element>();
		}
		
		for(Element element: serverElementList) {
			if ( element.getAttributeValue("name").equals(name) ) {
				elementFound = element;
				break;
			}
		}
		
		return elementFound;
	}

	public void removeServer(ServerDefinition sd) {
		Document doc = readServersFile();
		Element serverElement = findServerElement(doc, sd.getName());
		
		if ( serverElement == null ) {
			return;
		}
		
		Parent parent = serverElement.getParent();
		parent.removeContent(serverElement);
		
		writerServersFile(doc);
		
		readServerDefinitions();
		
	}
	
	public void editServerDefinition(String oldServerName,
			ServerDefinition serverDefinition) {
		
		Document doc = readServersFile();
		
		Element serverElement = findServerElement(doc, oldServerName);
		if ( serverElement == null ) {
			return;
		}
		
		serverElement.setAttribute("name", serverDefinition.getName());
		serverElement.setAttribute("address", serverDefinition.getAddress());
		
		writerServersFile(doc);
	}
	
	private Document readServersFile() {
		Document doc;
		try {
			doc = jdomParser.readFrom(FileUtils.readFileToString(getDefinitionFile(),
					"UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return doc;
	}
	
	private void writerServersFile(Document doc) {
		StringWriter writer = new StringWriter();
		jdomParser.writeDocument(doc,writer);
		
		try {
			FileUtils.writeStringToFile(getDefinitionFile(), writer.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeService(ServerDefinition serverDefinition, ServiceDefinition sd) {
		Document doc = readServersFile();
		
		Element serverElement = findServerElement(doc, serverDefinition.getName());
		if ( serverElement == null ) {
			return;
		}
		
		
		Element sdElementFound = findServiceElement(doc, serverDefinition, sd.getName());
		
		if ( sdElementFound != null ) {
			sdElementFound.getParent().removeContent(sdElementFound);
			
			serverDefinition.removeServiceDefinition(sd.getName());
		}
		
		writerServersFile(doc);
	}
	
	private Element findServiceElement(Document doc, ServerDefinition serverDefinition, String name) {
		Element serverElement = findServerElement(doc, serverDefinition.getName());
		if ( serverElement == null ) {
			return null;
		}
		
		
		Element sdElementFound = null;
		
		@SuppressWarnings("unchecked")
		List<Element> serviceElementList = serverElement.getChildren("service");
		if ( serviceElementList == null ) {
			serviceElementList = new ArrayList<Element>();
		}
		
		for(Element serviceElement: serviceElementList) {
			if ( serviceElement.getAttributeValue("name").equals(name)) {
				sdElementFound = serviceElement;
				break;
			}
		}

		return sdElementFound;
	}

	public void saveServiceParams(ServerDefinition serverDefinition,
			ServiceDefinition sd, Map<String, String> params) {
		
		Document doc = readServersFile();
		Element serviceElement = findServiceElement(doc, serverDefinition, sd.getName());
		
		if ( serviceElement == null ) {
			return;
		}
		
		serviceElement.removeChildren("param");
		for(Map.Entry<String, String> entry: params.entrySet()) {
			Element sub = new Element("param");
			
			sub.setAttribute("name", entry.getKey());
			sub.setAttribute("value", entry.getValue());
			
			serviceElement.addContent(sub);
		}
		
		writerServersFile(doc);
		
	}

	public void editServiceDefinition(ServerDefinition serverDefinition,
			String oldServiceName, ServiceDefinition serviceDefinition) {
		
		Document doc = readServersFile();
		
		Element serviceElement = findServiceElement(doc, serverDefinition, oldServiceName);
		
		if ( serviceElement == null ) {
			return;
		}
		
		serviceElement.setAttribute("name", serviceDefinition.getName());
		serviceElement.setAttribute("address", serviceDefinition.getAddress());
		serviceElement.setAttribute("resultType", serviceDefinition.getResultType());
		
		writerServersFile(doc);
		
	}
	
}
