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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import microsoft.exchange.webservices.data.search.filter.SearchFilter.SearchFilterCollection;
import uk.nhs.digital.iucds.middleware.utility.DeleteUtility;
import uk.nhs.digital.iucds.middleware.utility.StagedStopwatch;

@Slf4j
@EnableAsync
@Data
@Component
public class MiddlewareDeleteTask {

  @Autowired
  private StagedStopwatch stopwatch;

  @Autowired
  private DeleteUtility deleteUtility;

  private final DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
  private ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
  private AWSSimpleSystemsManagement ssm =
      AWSSimpleSystemsManagementClientBuilder.standard().withRegion(Regions.US_WEST_2).build();

  public MiddlewareDeleteTask() throws Exception {
    ExchangeCredentials credentials =
        new WebCredentials(getParameter("username"), getParameter("password"));
    service.setCredentials(credentials);
    service.autodiscoverUrl(getParameter("username"));
  }

  public MiddlewareDeleteTask(ExchangeService service, AWSSimpleSystemsManagement ssm) {
    this.service = service;
    this.ssm = ssm;
  }

  @Async
  @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}",
      initialDelayString = "${initialDelay.in.milliseconds}")
  public void deleteMails() throws InterruptedException {
    String timeStamp = LocalDateTime.now().format(FOMATTER);
    log.info("Invocation started: {}", timeStamp);

    try {
      FindItemsResults<Item> findResults = getFindItemsResults();

      while (findResults.getTotalCount() > 0) {
        for (Object item : findResults.getItems()) {
          try {
            EmailMessage emailMessage = (EmailMessage) item;

            deleteUtility.setMailsIsReadAndDelete(emailMessage);

          } catch (Exception e) {
            log.error("Exception", e);
          }
        }
        findResults = getFindItemsResults();
      }
    } catch (Exception e) {
      log.error("Exception", e);
    }
    timeStamp = LocalDateTime.now().format(FOMATTER);
    log.info("Invocation completed: {}", timeStamp);
  }

  private FindItemsResults<Item> getFindItemsResults() throws Exception {
    ItemView view = new ItemView(Integer.parseInt(getParameter("EMAIL_ITEM_VIEW")));
    SearchFilterCollection searchFilterCollection =
        new SearchFilter.SearchFilterCollection(LogicalOperator.And);
    searchFilterCollection.add(
        new SearchFilter.IsNotEqualTo(EmailMessageSchema.From, getParameter("EMS_REPORT_SENDER")));
    searchFilterCollection.add(new SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, false));
    return service.findItems(WellKnownFolderName.Inbox, searchFilterCollection, view);
  }

  public String getParameter(String parameterName) {
    GetParameterRequest request = new GetParameterRequest();
    request.setName(parameterName);
    request.setWithDecryption(true);
    return ssm.getParameter(request).getParameter().getValue();
  }
}