package uk.nhs.digital.iucds.middleware.transformer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class XMLTransformerTest {

  public static final String XML_FILE = "src/test/resources/PEMExample.xml";
  
  private XMLTransformer xmlTransformer;
  
  @Before
  public void setup() {
    xmlTransformer = new XMLTransformer();
  }
  
  @Test
  public void transform() throws IOException, TransformerException {
    
    xmlTransformer.transform(Files.readAllBytes(new File(XML_FILE).toPath()));
  }
  public static void main(String[] args) throws TransformerException, IOException{
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    File file = new File("src/test/resources/PEMExample.xml");
    StreamSource sourceXml = new StreamSource(new FileInputStream(file));
    StreamSource xslt = new StreamSource(new File("src/main/resources/cda.xsl"));
    StreamResult resultXml = new StreamResult(outputStream);
    doXsltTransform(sourceXml, xslt, resultXml);
    System.out.println(outputStream.toByteArray());
    FileUtils.writeByteArrayToFile(new File("src/test/resources/report.html"), outputStream.toByteArray());
  }
  public static void doXsltTransform(StreamSource sourceXml, StreamSource xslt,
      StreamResult resultXml) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer(xslt);
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(sourceXml, resultXml);
  }
}