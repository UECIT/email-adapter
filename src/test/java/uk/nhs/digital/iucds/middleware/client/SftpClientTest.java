package uk.nhs.digital.iucds.middleware.client;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class SftpClientTest {

  private static final String HTML_FILE = "src/test/resources/input.html";
  private static final String PRIVATE_KEY_PATH = "target/test/resources/id_rsa";
  
  private SftpClient getSut() {
    return new SftpClient("localhost", "2222", "test", PRIVATE_KEY_PATH, "output");
  }
  
  @Test
  public void testSendFileToServer() throws IOException, Exception {
    
    getSut().SendFileToServer(FileUtils.readFileToByteArray(new File(HTML_FILE)), "report");
  }
}
