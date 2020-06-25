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
import uk.nhs.digital.iucds.middleware.utility.SsmUtility;
import uk.nhs.digital.iucds.middleware.utility.StagedStopwatch;

@Slf4j
@EnableAsync
@Data
@Component
public class MiddlewareDeleteTask {

  private static final String EMAIL_ITEM_VIEW_PAGE_SIZE = "ems-email-item-view-page-size";
  private static final String EMS_REPORT_FROM = "ems-email-from";
  private static final String EMAIL_USERNAME = "ems-email-username";
  private static final String EMAIL_PASSWORD = "ems-email-password";
  private static final String IUCDS_ENV = "iucds-environment";
  private static String iucdsEnvironment;
  
  @Autowired
  private StagedStopwatch stopwatch;

  @Autowired
  private DeleteUtility deleteUtility;

  private SsmUtility ssmUtility;
  private final DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
  private ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

  @Autowired
  public MiddlewareDeleteTask() throws Exception {
    this.ssmUtility = new SsmUtility();
    iucdsEnvironment = ssmUtility.getIucdsEnvironment(IUCDS_ENV);
    log.info("IUCDS middleware environment : {} ", iucdsEnvironment);
    ExchangeCredentials credentials =
        new WebCredentials(ssmUtility.getParameter(EMAIL_USERNAME), ssmUtility.getParameter(EMAIL_PASSWORD));
    service.setCredentials(credentials);
    service.autodiscoverUrl(ssmUtility.getParameter(EMAIL_USERNAME));
  }

  public MiddlewareDeleteTask(ExchangeService service, SsmUtility ssmUtility) {
    this.service = service;
    this.ssmUtility = ssmUtility;
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
    ItemView view = new ItemView(Integer.parseInt(ssmUtility.getParameter(EMAIL_ITEM_VIEW_PAGE_SIZE)));
    SearchFilterCollection searchFilterCollection =
        new SearchFilter.SearchFilterCollection(LogicalOperator.And);
    searchFilterCollection.add(
        new SearchFilter.IsNotEqualTo(EmailMessageSchema.From, ssmUtility.getParameter(EMS_REPORT_FROM)));
    searchFilterCollection.add(new SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, false));
    return service.findItems(WellKnownFolderName.Inbox, searchFilterCollection, view);
  }
}