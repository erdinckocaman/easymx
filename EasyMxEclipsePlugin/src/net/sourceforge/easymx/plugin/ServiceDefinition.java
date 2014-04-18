package net.sourceforge.easymx.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ServiceDefinition {
	
	public static class ServiceParam {
		private String name;
		private String value;
		

		public ServiceParam(String name, String value) {
			this.name = name;
			this.value = value;
			 
		}
		
		public String getName() {
			return name;
		}
		
		public String getValue() {
			return value;
		}
		

		
	}

	private String address;
	private String name;
	private String resultType;
	
	private List<ServiceParam> serviceParams;
	
	public ServiceDefinition() {
		serviceParams = new ArrayList<ServiceDefinition.ServiceParam>();
		resultType = "text";

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
	
	public boolean isTabular() {
		return "tabular".equals(resultType);
	}
	
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	
	public String getResultType() {
		return resultType;
	}
	
	public void addParam(String name, String value) {
		serviceParams.add(new ServiceParam(name, value));
	}
	
	public List<ServiceParam> getServiceParams() {
		return serviceParams;
	}

	public void reloadParams(Map<String, String> params) {
		if ( params == null ) {
			params = new HashMap<String, String>();
		}
		
		serviceParams.clear();
		
		for(Map.Entry<String, String> entry : params.entrySet()) {
			addParam(entry.getKey(), entry.getValue());
		}
		
	}
}
