package uk.nhs.digital.iucds.middleware.transformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.xml.transform.TransformerException;
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
}
