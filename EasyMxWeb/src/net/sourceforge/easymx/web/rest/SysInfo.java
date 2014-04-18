package net.sourceforge.easymx.web.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import net.sourceforge.easymx.EasyMxUtil;

import org.apache.commons.io.IOUtils;



@Path("/sysInfo")
public class SysInfo {
	
	@Context
	ServletContext servletContext;
	
	@GET
	@Path("getTime")
	public String getTime() {
		return new Date().toString();
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("getRuntimeInfo")
	public String getRuntimeInfo() {
		Runtime runtime = Runtime.getRuntime();
		
		String path = servletContext.getRealPath("WEB-INF/build-config.properties");
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		
		Properties props = new Properties();
		try {
			props.load(fis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		IOUtils.closeQuietly(fis);
		
		String buildNo = props.getProperty("build.no");
		String buildDate = props.getProperty("build.date");
		
		int div = 1024*1024;
		
		Map<String, Object> infoMap = new HashMap<String, Object>();
		
		infoMap.put("processors", runtime.availableProcessors());
		infoMap.put("freeMemory", runtime.freeMemory() / div);
		infoMap.put("maxMemory", runtime.maxMemory() / div );
		infoMap.put("totalMemory", runtime.totalMemory() /div);
		infoMap.put("buildNo", buildNo);
		infoMap.put("buildDate", buildDate);
		
		
		return new EasyMxUtil().generateXml(Collections.singletonList(infoMap), "buildNo",
				"buildDate", "processors", "freeMemory", "totalMemory", "maxMemory");
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("getSysProps")
	public String getSysProps() {
		Properties props = System.getProperties();
		
		List<Map<String,String>> propsNormalized = new ArrayList<Map<String,String>>();
		
		
		for(Map.Entry<Object, Object> entry:props.entrySet()) {
			Map<String, String> val  = new HashMap<String, String>();
			
			String name = "";
			if( entry.getKey() != null ) {
				name = entry.getKey().toString();
			}
			
			String value = "";
			
			if ( entry.getValue() != null) {
				value = entry.getValue().toString();
			}
			
			val.put("name", name);
			val.put("value", value);
			propsNormalized.add(val);
			
		}
		
		Collections.sort(propsNormalized, new Comparator<Map<String, String>>() {
			
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				return o1.get("name").compareTo(o2.get("name"));
			}
		});
		
		return new EasyMxUtil().generateXml(propsNormalized, "name", "value");
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("getSysPropByName")
	public String getSysPropByName(@QueryParam("name") String name) {
		if ( name == null ) {
			return "";
		}
		
		Properties props = System.getProperties();
		
		return props.getProperty(name);
	}
	
	
}
