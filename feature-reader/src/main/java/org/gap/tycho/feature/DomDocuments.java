package org.gap.tycho.feature;

import java.nio.file.Path;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

public final class DomDocuments {
	private DomDocuments() {
	}

	public static Document fromFile(Path compositeXml) throws SAXException, DocumentException {
		SAXReader reader = new SAXReader();
		reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		reader.setValidation(false);
		return reader.read(compositeXml.toFile());
	}
}
