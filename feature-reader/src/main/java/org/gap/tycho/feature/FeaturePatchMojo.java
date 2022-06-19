package org.gap.tycho.feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
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
	private String pomFile;
	private String proxyFeaturePath;


	public static void main(String[] args) throws Exception {
		DefaultParser parser = new DefaultParser();
		CommandLine commandLine = parser.parse(buildOptions(), args);
		(new FeaturePatchMojo(commandLine.getOptionValue("repo"), commandLine.getOptionValue("id"), 
				commandLine.getOptionValue("feature-file"), commandLine.getOptionValue("pom-file"),
				commandLine.getOptionValue("proxy-feature"))).patchVersion();
	}

	private static Options buildOptions() {
		Options options = new Options();
		options.addRequiredOption("r", "repo", true, "P2 repository url");
		options.addRequiredOption("i", "id", true, "Bundle symbolic name");
		options.addRequiredOption("f", "feature-file", true, "Feature file to patch");
		options.addRequiredOption("p", "pom-file", true, "POM file to patch version property");
		options.addRequiredOption("x", "proxy-feature", true, "Proxy Feature path");
		return options;
	}
	
	
	public FeaturePatchMojo(String repo, String bundleId, String patchFile, String pomFile, String proxyFeaturePath) {
		this.repo = repo;
		this.bundleId = bundleId;
		this.patchFile = patchFile;
		this.pomFile = pomFile;
		this.proxyFeaturePath = proxyFeaturePath;
	}
	
	public void patchVersion() throws SAXException, DocumentException, URISyntaxException, IOException {
		try(P2Repository repository = new P2Repository(this.repo)) {
			repository.init();
			String version = repository.getVersion(bundleId);
			
			// patch the feature.xml
			Document featureDocument = DomDocuments.fromFile(Paths.get(patchFile).normalize().toAbsolutePath());
			Element node = (Element) featureDocument.selectSingleNode("//feature/requires/import");
			node.attribute("version").setText(version);
			
			try (FileWriter writer = new FileWriter(new File(patchFile))) {
				featureDocument.write(writer);
			}
			
			// patch the pom.xml
			Document pomDocument = DomDocuments.fromFile(Paths.get(pomFile).normalize().toAbsolutePath());
			node = (Element) pomDocument.selectSingleNode("/*[local-name()=\"project\"]/*[local-name()=\"properties\"]/*[local-name()=\"jdt.patch.version\"]");
			node.setText(version);
			
			try (FileWriter writer = new FileWriter(new File(pomFile))) {
				pomDocument.write(writer);
			}

			// patch the proxy feature and pom.xml
			Path proxyPOMFile = Paths.get(proxyFeaturePath, "pom.xml");
			Document proxyPomDocument = DomDocuments.fromFile(proxyPOMFile.normalize().toAbsolutePath());
			node = (Element) proxyPomDocument.selectSingleNode("/*[local-name()=\"project\"]/*[local-name()=\"version\"]");
			node.setText(version);
			
			try (FileWriter writer = new FileWriter(proxyPOMFile.toFile())) {
				proxyPomDocument.write(writer);
			}

			Path proxyFeatureFile = Paths.get(proxyFeaturePath, "feature.xml");
			Document proxyFeatureDocument = DomDocuments.fromFile(proxyFeatureFile.normalize().toAbsolutePath());
			node = (Element) proxyFeatureDocument.selectSingleNode("//feature");
			node.attribute("version").setText(version);
			
			try (FileWriter writer = new FileWriter(proxyFeatureFile.toFile())) {
				proxyFeatureDocument.write(writer);
			}
		}
	}
	
}
