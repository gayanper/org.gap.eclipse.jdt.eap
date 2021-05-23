package org.gap.tycho.feature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.SAXException;

public class P2Repository implements AutoCloseable {
	private String url;
	
	private File workDir;
	
	private Map<String, String> versions = new HashMap<>();
	
	public P2Repository(String url) {
		this.url = url;
		this.workDir = new File(System.getProperty("java.io.tmpdir"), String.valueOf(System.currentTimeMillis()));
		this.workDir.mkdirs();
	}

	public void init() throws URISyntaxException, IOException {
		File artifactsFile = new File(workDir, "compositeContent.jar");
		Request.Get(compositContentURI()).execute().saveContent(artifactsFile);

		try (ZipFile zipFile = new ZipFile(artifactsFile)) {
			ZipEntry zipEntry = zipFile.stream().filter(e -> !e.isDirectory()).findFirst()
					.orElseThrow(() -> new FileNotFoundException("compositeContent.xml"));

			Path compositeXml = Paths.get(workDir.getAbsolutePath(), "compositeContent.xml");
			try (InputStream zipStream = zipFile.getInputStream(zipEntry)) {
				Files.copy(zipStream, compositeXml);
			}
			
			String ibuildVersion = findIBuildVersion(compositeXml);
			File contentXmlFile = new File(workDir, "content.xml");
			Request.Get(contentURI(ibuildVersion)).execute().saveContent(contentXmlFile);
			Document document = DomDocuments.fromFile(Paths.get(contentXmlFile.getAbsolutePath()));
			List<Node> units = document.selectNodes("//repository/units/unit");
			units.stream().map(Element.class::cast).forEach(u -> {
				versions.put(u.attributeValue("id"), u.attributeValue("version"));
			});
		} catch (SAXException | DocumentException e) {
			throw new IOException(e);
		}
	}

	public String getVersion(String symbolicName) {
		return versions.get(symbolicName);
	}
	
	private String findIBuildVersion(Path compositeXml) throws SAXException, DocumentException {
		Document compositeArtDoc = DomDocuments.fromFile(compositeXml);
		List<Node> children = compositeArtDoc.selectNodes("//repository/children/child");
		return ((Element)children.get(children.size() - 1)).attributeValue("location");
	}


	private URI compositContentURI() throws URISyntaxException {
		URIBuilder builder = new URIBuilder(this.url);
		List<String> paths = new ArrayList<>(builder.getPathSegments());
		paths.add("compositeContent.jar");
		return builder.setPathSegments(paths).build();
	}

	private URI contentURI(String ibuildVersion) throws URISyntaxException {
		URIBuilder builder = new URIBuilder(this.url);
		List<String> paths = new ArrayList<>(builder.getPathSegments());
		paths.add(ibuildVersion);
		paths.add("content.xml");
		return new URIBuilder(this.url).setPathSegments(paths).build();
	}

	@Override
	public void close() throws IOException {
		Files.walkFileTree(Paths.get(this.workDir.getAbsolutePath()), new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
