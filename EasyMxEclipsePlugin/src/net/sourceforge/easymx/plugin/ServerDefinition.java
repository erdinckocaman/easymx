package net.sourceforge.easymx.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ServerDefinition {

	private String address;
	private String name;
	
	private List<ServiceDefinition> serviceDefinitionList;
	
	public ServerDefinition() {
		serviceDefinitionList = new ArrayList<ServiceDefinition>();
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addService(ServiceDefinition serviceDefinition) {
		serviceDefinitionList.add(serviceDefinition);
	}
	
	
	public List<ServiceDefinition> getServiceDefinitionList() {
		return serviceDefinitionList;
	}
	

	private boolean hasServiceWithName(String name) {
		for(ServiceDefinition serviceDefinition: serviceDefinitionList) {
			if ( serviceDefinition.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public List<ServiceDefinition> findServicesNotExistingIn(ServerDefinition sd) {
		List<ServiceDefinition> additionalServices = new ArrayList<ServiceDefinition>();
		
		for(ServiceDefinition serviceDefinition: serviceDefinitionList) {
			if ( !sd.hasServiceWithName(serviceDefinition.getName())) {
				additionalServices.add(serviceDefinition);
			}
		}
		
		return additionalServices;
	}

	public void removeServiceDefinition(String name) {
		Iterator<ServiceDefinition> iterator = serviceDefinitionList.iterator();
		while ( iterator.hasNext() ) {
			ServiceDefinition sd = iterator.next();
			
			if ( name == null )  {
				continue;
			}
			
			if ( name.equals(sd.getName()) ) {
				iterator.remove();
			}
		}
	}

}
