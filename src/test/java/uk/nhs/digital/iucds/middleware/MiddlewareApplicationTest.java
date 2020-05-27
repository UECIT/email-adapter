/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package uk.nhs.digital.iucds.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.MimeTypeUtils;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter.SearchFilterCollection;
import uk.nhs.digital.iucds.middleware.client.HapiSendMDMClient;

@SpringBootTest
public class MiddlewareApplicationTest {

  private static final String HTML_FILE = "src/test/resources/output.html";
  
  @MockBean
  private MiddlewareSchedulerTask tasks;

  @Mock
  private ExchangeService service;

  @Mock
  private AWSSimpleSystemsManagement ssm;

  @Mock
  private EmailMessage message;

  @Mock
  private FindItemsResults<Item> items = new FindItemsResults<Item>();

  @Spy
  private ArrayList<Item> item = new ArrayList<Item>();

  @Spy
  private AttachmentCollection attachments;

  @Spy
  private List<Attachment> attachmectItems;

  @Mock
  private FileAttachment attachment;

  @Spy
  private GetParameterResult result;

  @Spy
  private Parameter param;
  
  @Spy
  private HapiSendMDMClient client;
  
  @Spy
  private Parameter param1;
  
  @Spy
  private GetParameterResult result1;
  
  @Spy
  private Parameter param2;
  
  private MiddlewareSchedulerTask getSut() throws Exception {
    return new MiddlewareSchedulerTask(service, ssm, client);
  }

  @Test
  public void contextLoads() throws Exception {
    items.getItems().add(message);
    item.add(message);
    Mockito.when(ssm.getParameter(Mockito.any(GetParameterRequest.class))).thenReturn(result);
    Mockito.when(result.getParameter()).thenReturn(param);
    Mockito.when(param.getValue()).thenReturn("test");
    Mockito.when(ssm.getParameter(new GetParameterRequest().withName("EMAIL_ITEM_VIEW"))).thenReturn(result);
    Mockito.when(result.getParameter()).thenReturn(param1);
    Mockito.when(param1.getValue()).thenReturn("100");
    Mockito.when(ssm.getParameter(new GetParameterRequest().withName("PORT_NUMBER"))).thenReturn(result);
    Mockito.when(result.getParameter()).thenReturn(param2);
    Mockito.when(param2.getValue()).thenReturn("4646");
    
    Mockito
        .when(service.findItems(Mockito.any(WellKnownFolderName.class),
            Mockito.any(SearchFilterCollection.class), Mockito.any(ItemView.class)))
        .thenReturn(items);
    Mockito.when(items.getTotalCount()).thenReturn(2, 0);
    Mockito.when(items.getItems()).thenReturn(item);
    Mockito.when(item.get(0)).thenReturn(message);
    Mockito.when(message.getAttachments()).thenReturn(attachments);
    Mockito.when(attachments.getCount()).thenReturn(1);
    Mockito.when(attachments.getItems()).thenReturn(attachmectItems);
    Mockito.when(attachmectItems.get(0)).thenReturn(attachment);
    Mockito.when(attachment.getContentType()).thenReturn(MimeTypeUtils.TEXT_HTML_VALUE);
    Mockito.when(attachment.getContent()).thenReturn(FileUtils.readFileToByteArray(new File(HTML_FILE)));
    Mockito.when(service.getRequestedServerVersion()).thenReturn(ExchangeVersion.Exchange2010_SP2);
    Mockito.doNothing().when(client).sendMDM(Mockito.any());

    getSut().sendMails();

    // Basic integration test that shows the context starts up properly
    assertThat(tasks).isNotNull();
    Mockito.verify(service, Mockito.times(2)).findItems(Mockito.any(WellKnownFolderName.class),
        Mockito.any(SearchFilterCollection.class), Mockito.any(ItemView.class));
  }
}
