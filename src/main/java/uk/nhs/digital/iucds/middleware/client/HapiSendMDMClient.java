package uk.nhs.digital.iucds.middleware.client;

import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.MDM_T02;
import ca.uhn.hl7v2.parser.Parser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.digital.iucds.middleware.service.MDMT02MessageBuilder;

@Slf4j
@Data
@Component
public class HapiSendMDMClient {

  private final DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
  private HapiContext context = new DefaultHapiContext();
  private String TCP_HOST;
  private String PORT_NUMBER;

  public HapiSendMDMClient() {
  }
  
  public HapiSendMDMClient(String host, String port) {
    this.TCP_HOST = host;
    this.PORT_NUMBER = port;
  }
  
  public void sendMDM(byte[] transform) {
    
    try {
      MDM_T02 build = new MDMT02MessageBuilder().build(transform);
      Connection connection = context.newClient(TCP_HOST, Integer.parseInt(PORT_NUMBER), false);

      Initiator initiator = connection.getInitiator();
      Parser parser = context.getPipeParser();
      log.info("Sending message: {} ", parser.encode(build));

      Message response = initiator.sendAndReceive(build);
      String responseString = parser.encode(response);
      log.info("Received response: {}", responseString);

    } catch (Exception e) {
      log.error("Exception", e);
    }
  }
}