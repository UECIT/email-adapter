package uk.nhs.digital.iucds.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter.SearchFilterCollection;

@SpringBootTest
public class MiddlewareDeleteTaskTest {

  @MockBean
  private MiddlewareSchedulerTask schedulerTask;
  
  @MockBean
  private MiddlewareDeleteTask deleteTask;
  
  @Mock
  private ExchangeService service;

  @Mock
  private AWSSimpleSystemsManagement ssm;

  @Mock
  private EmailMessage message;

  @Mock
  private FindItemsResults<Item> items = new FindItemsResults<Item>();

  @Spy
  private GetParameterResult result;

  @Spy
  private Parameter param;

  @Spy
  private Parameter param1;

  private MiddlewareDeleteTask getSut() throws Exception {
    return new MiddlewareDeleteTask(service, ssm);
  }

  @Test
  public void contextLoads() throws Exception {
    Mockito.when(ssm.getParameter(Mockito.any(GetParameterRequest.class))).thenReturn(result);
    Mockito.when(result.getParameter()).thenReturn(param);
    Mockito.when(param.getValue()).thenReturn("test");
    Mockito.when(ssm.getParameter(new GetParameterRequest().withName("EMAIL_ITEM_VIEW")))
        .thenReturn(result);
    Mockito.when(result.getParameter()).thenReturn(param1);
    Mockito.when(param1.getValue()).thenReturn("100");

    Mockito
        .when(service.findItems(Mockito.any(WellKnownFolderName.class),
            Mockito.any(SearchFilterCollection.class), Mockito.any(ItemView.class)))
        .thenReturn(items);
    Mockito.when(items.getTotalCount()).thenReturn(2, 0);
    Mockito.when(service.getRequestedServerVersion()).thenReturn(ExchangeVersion.Exchange2010_SP2);

    getSut().deleteMails();

    // Basic integration test that shows the context starts up properly
    assertThat(schedulerTask).isNotNull();
    Mockito.verify(service, Mockito.times(2)).findItems(Mockito.any(WellKnownFolderName.class),
        Mockito.any(SearchFilterCollection.class), Mockito.any(ItemView.class));
  }
}
