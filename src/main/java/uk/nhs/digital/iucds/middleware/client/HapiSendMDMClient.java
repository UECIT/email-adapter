package uk.nhs.digital.iucds.middleware.client;

import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
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
  private static HapiContext context = new DefaultHapiContext();
  private final AWSSimpleSystemsManagement ssm =
      AWSSimpleSystemsManagementClientBuilder.defaultClient();
  private static String TCP_HOST;
  private static int PORT_NUMBER;

  public HapiSendMDMClient() {
    TCP_HOST = getParameter("TCP_HOST");
    PORT_NUMBER = Integer.parseInt(getParameter("PORT_NUMBER"));
  }

  public static String sendMDM(byte[] transform) {
    try {
      MDM_T02 build = new MDMT02MessageBuilder().Build(transform);
      Connection connection = context.newClient(TCP_HOST, PORT_NUMBER, false);

      Initiator initiator = connection.getInitiator();
      Parser parser = context.getPipeParser();
      log.info("Sending message: {} ", parser.encode(build));

      Message response = initiator.sendAndReceive(build);
      String responseString = parser.encode(response);
      log.info("Received response: {}", responseString);
      return responseString;

    } catch (Exception e) {
      log.error("Exception", e);
      return "ERROR";
    }
  }

  private String getParameter(String parameterName) {
    GetParameterRequest request = new GetParameterRequest();
    request.setName(parameterName);
    request.setWithDecryption(true);
    return ssm.getParameter(request).getParameter().getValue();
  }
}
