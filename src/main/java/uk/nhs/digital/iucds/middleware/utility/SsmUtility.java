package uk.nhs.digital.iucds.middleware.utility;

import org.springframework.stereotype.Component;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import lombok.Data;

@Data
@Component
public class SsmUtility {

  private static final String IUCDS_ENV = "iucds-environment";
  private static String iucdsEnvironment;
  private static final String IUCDS = "iucds";
  private AWSSimpleSystemsManagement ssm =
      AWSSimpleSystemsManagementClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
  
  private String getIucdsEnvironment(String parameterName) {
    GetParameterRequest request = new GetParameterRequest();
    request.setName(parameterName);
    request.setWithDecryption(true);
    iucdsEnvironment = ssm.getParameter(request).getParameter().getValue();
    return iucdsEnvironment;
  }
  
  public String getParameter(String parameterName) {
    getIucdsEnvironment(IUCDS_ENV);
    GetParameterRequest request = new GetParameterRequest();
    request.setName(IUCDS + "-" + iucdsEnvironment + "-" + parameterName);
    request.setWithDecryption(true);
    return ssm.getParameter(request).getParameter().getValue();
  }
}
