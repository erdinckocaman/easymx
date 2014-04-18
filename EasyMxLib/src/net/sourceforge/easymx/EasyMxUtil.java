package net.sourceforge.easymx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.DateFormatUtils;


public class EasyMxUtil {

	public static interface PropertyListener {
		
		public Object convertPropertyValue(String property, Object value);
	};
	
	public String generateXml(Collection<?> beanList, 
			PropertyListener propertyListener, 
			String... propertyList ) {
		
		if ( propertyList == null ) {
			return "";
		}
		  
		if ( propertyList.length == 0 ) {
			return "";
		}
		  
		if ( beanList == null ) {
			beanList = Collections.EMPTY_LIST;
		}
				
		List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
		
		for(Object bean: beanList) {
			Map<String,Object> valueMap = new HashMap<String, Object>();
			
			for(String property: propertyList ) {
				Object val = getProperty(bean, property);
				
				if ( propertyListener != null ) {
					val = propertyListener.convertPropertyValue(property, val);
				}
				valueMap.put(property, val);
			  }
			
			rows.add(valueMap);
			
		  }
		
		
		StringBuffer str = new StringBuffer(8096);
		str.append("<result>");

		String line = System.getProperty("line.separator");
		for(Map<String,Object> row:rows) {
			
			str.append(line).append("<row>").append(line);
			
			for(String property: propertyList ) {
				Object propVal = row.get(property);
				
				String sub = "";
				if ( propVal != null ) {
					if ( propVal instanceof Date ) {
						sub = formatDate((Date) propVal);
					}else {
						sub = propVal.toString();
					}
					
				}
				
				sub = StringEscapeUtils.escapeXml(sub);				
				
				str.append("<").append(property).append(" value=\"").append(sub).append("\" />");
			}
			
			str.append(line).append("</row>").append(line);
		}
		
		str.append("</result>");
		return str.toString();

	}

	public String generateXml(List<?> beanList, String... propertyList) {
		return generateXml(beanList, null, propertyList);
	}
	
	
	public Object getProperty(Object bean, String property) {
		try {
			return PropertyUtils.getProperty(bean, property);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	public String formatDate(Date date) {
		return DateFormatUtils.ISO_DATETIME_FORMAT.format(date);
	}

}
