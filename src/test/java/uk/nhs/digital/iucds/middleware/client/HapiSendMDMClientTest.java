package uk.nhs.digital.iucds.middleware.client;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class HapiSendMDMClientTest {

  private static final String HTML_FILE = "src/test/resources/output.html";
  
  private HapiSendMDMClient getSut() throws Exception {
    return new HapiSendMDMClient("localhost", "5656");
  }

  @Test
  public void testSendMDM() throws IOException, Exception {
    
    getSut().sendMDM(FileUtils.readFileToByteArray(new File(HTML_FILE)));
  }
}
