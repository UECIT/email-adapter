package uk.nhs.digital.iucds.middleware.transformer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.springframework.stereotype.Component;
import com.itextpdf.io.source.ByteArrayOutputStream;
import lombok.Data;

@Data
@Component
public class XMLTransformer {

  private static final String XSL_FILE = "src/main/resources/cda.xsl";

  public byte[] transform(byte[] bs) throws IOException, TransformerException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    StreamSource sourceXml = new StreamSource(new ByteArrayInputStream(bs));
    StreamSource xslt = new StreamSource(new File(XSL_FILE));
    StreamResult resultXml = new StreamResult(outputStream);
    doXsltTransform(sourceXml, xslt, resultXml);

    return outputStream.toByteArray();
  }

  private static void doXsltTransform(StreamSource sourceXml, StreamSource xslt,
      StreamResult resultXml) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer(xslt);
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(sourceXml, resultXml);
  }
}