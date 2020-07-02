package uk.nhs.digital.iucds.middleware.utility;

import org.springframework.stereotype.Component;
import lombok.Data;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceResponseException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

@Data
@Component
public class DeleteUtility {

  public void setMailsIsReadAndDelete(EmailMessage emailMessage, StagedStopwatch stopwatch)
      throws ServiceResponseException, Exception {
    emailMessage.setIsRead(true);
    emailMessage.update(ConflictResolutionMode.AlwaysOverwrite);
    emailMessage.delete(DeleteMode.HardDelete);
    stopwatch.finishStage("Making email unread after reading email");
  }
}
