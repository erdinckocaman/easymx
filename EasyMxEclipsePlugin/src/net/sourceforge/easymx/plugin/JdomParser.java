package net.sourceforge.easymx.plugin;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class JdomParser {
	
	public Document readFrom(Reader reader) {
		SAXBuilder builder = new SAXBuilder();

        Document doc;

        try {
			doc = builder.build(reader);
			
			return doc;

		} catch (JDOMException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);

		}
	}
	
	public Document readFrom(String str) {
		StringReader reader  = new StringReader(str);
		
		try {
			return readFrom(reader);
		}finally {
			reader.close();
		}
		
	}
	
	
	public String writeElement(Element rootElement) {
		Document doc = new Document();

		doc.setRootElement(rootElement);
    	XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
    	StringWriter strWriter = new StringWriter(2048);
    	
    	try {
			outp.output(doc, strWriter);
		} catch (IOException e) {
			throw new RuntimeException(e);

		}finally {
			try {
				strWriter.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		String str = strWriter.toString();
		
		return str;

		
	}
	
	public void writeDocument(Document doc, Writer writer) {
    	XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
    	
    	
    	try {
			outp.output(doc, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	

}
