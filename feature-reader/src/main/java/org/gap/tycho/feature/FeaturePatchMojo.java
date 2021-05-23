package org.gap.tycho.feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.xml.sax.SAXException;


public class FeaturePatchMojo {
	private String bundleId;
	private String repo;
	private String patchFile;


	public static void main(String[] args) throws Exception {
		DefaultParser parser = new DefaultParser();
		CommandLine commandLine = parser.parse(buildOptions(), args);
		(new FeaturePatchMojo(commandLine.getOptionValue("repo"), commandLine.getOptionValue("id"), 
				commandLine.getOptionValue("feature-file"))).patchVersion();
	}

	private static Options buildOptions() {
		Options options = new Options();
		options.addRequiredOption("r", "repo", true, "P2 repository url");
		options.addRequiredOption("i", "id", true, "Bundle symbolic name");
		options.addRequiredOption("f", "feature-file", true, "Feature file to patch");
		return options;
	}
	
	
	public FeaturePatchMojo(String repo, String bundleId, String patchFile) {
		this.repo = repo;
		this.bundleId = bundleId;
		this.patchFile = patchFile;
	}
	
	public void patchVersion() throws SAXException, DocumentException, URISyntaxException, IOException {
		try(P2Repository repository = new P2Repository(this.repo)) {
			repository.init();
			
			Document document = DomDocuments.fromFile(Paths.get(patchFile));
			Element node = (Element) document.selectSingleNode("//feature/requires/import");
			node.attribute("version").setText(repository.getVersion(bundleId));
			
			try (FileWriter writer = new FileWriter(new File(patchFile))) {
				document.write(writer);
			}
		}
	}
	
}
