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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.transform.TransformerException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import uk.nhs.digital.iucds.middleware.client.HapiSendMDMClient;
import uk.nhs.digital.iucds.middleware.service.EmailService;
import uk.nhs.digital.iucds.middleware.transformer.XMLTransformer;
import uk.nhs.digital.iucds.middleware.utility.DeleteUtility;
import uk.nhs.digital.iucds.middleware.utility.FileUtility;
import uk.nhs.digital.iucds.middleware.utility.SsmUtility;
import uk.nhs.digital.iucds.middleware.utility.StagedStopwatch;

@Slf4j
@EnableAsync
@Data
@Component
public class MiddlewareSchedulerTask {

  private static final String EMAIL_USERNAME = "ems-email-username";
  private static final String EMAIL_PASSWORD = "ems-email-password";
  private static final String IUCDS_ENV = "iucds-environment";
  private static final String MIRTH_HOST = "mirth-connect-tcp-host";
  private static final String MIRTH_PORT = "mirth-connect-port-number";
  private static String iucdsEnvironment;
  
  private static final DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
  private ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
  private HapiSendMDMClient client;
  private StagedStopwatch stopwatch; 
  
  @Autowired
  private DeleteUtility deleteUtility;
  
  private SsmUtility ssmUtility;
  
  @Autowired
  private EmailService emailService;
  
  @Autowired
  private XMLTransformer xmlTransformer;
  
  @Autowired
  private FileUtility fileUtility;
  
  @Autowired
  public MiddlewareSchedulerTask() throws Exception {
    this.ssmUtility = new SsmUtility();
    iucdsEnvironment = ssmUtility.getIucdsEnvironment(IUCDS_ENV);
    log.info("IUCDS middleware environment : {} ", iucdsEnvironment);
    ExchangeCredentials credentials =
        new WebCredentials(ssmUtility.getParameter(EMAIL_USERNAME), ssmUtility.getParameter(EMAIL_PASSWORD));
    service.setCredentials(credentials);
    service.autodiscoverUrl(ssmUtility.getParameter(EMAIL_USERNAME));
    client = new HapiSendMDMClient(ssmUtility.getParameter(MIRTH_HOST), ssmUtility.getParameter(MIRTH_PORT));
  }

  public MiddlewareSchedulerTask(ExchangeService service, HapiSendMDMClient client, SsmUtility ssmUtility, EmailService emailService, DeleteUtility deleteUtility) {
    this.service = service;
    this.client = client;
    this.ssmUtility = ssmUtility;
    this.emailService = emailService;
    this.deleteUtility = deleteUtility;
  }

  @Async
  @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}",
      initialDelayString = "${initialDelay.in.milliseconds}")
  public void startEmailProcessing() throws InterruptedException {
    String timeStamp = LocalDateTime.now().format(FOMATTER);
    log.info("Invocation started: {}", timeStamp);

    try {
      FindItemsResults<Item> findResults = emailService.fetchEmailsFromInbox(service, ssmUtility);

      while (findResults.getTotalCount() > 0) {
        for (Object item : findResults.getItems()) {
          try {
            stopwatch = StagedStopwatch.start();
            EmailMessage emailMessage = (EmailMessage) item;
            Attachment attachmentFromEmailMessage = emailService.getAttachmentFromEmailMessage(emailMessage, stopwatch);
            
            if (attachmentFromEmailMessage != null) {
              FileAttachment fileAttachment = emailService.getFileAttachment(attachmentFromEmailMessage);
              
              if (fileAttachment.getContentType().equalsIgnoreCase(MimeTypeUtils.TEXT_HTML_VALUE)) {
                String htmlString = new String(fileAttachment.getContent(), StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(htmlString);
            
                emailService.buildNhs111Report(doc);
                
                emailService.convertNhs111ReportToPdf();
                
                emailService.createEmailMeassageAndSend(service, ssmUtility, fileUtility.createFileName(doc));
              
              } else if (fileAttachment.getContentType().equalsIgnoreCase(MimeTypeUtils.TEXT_XML_VALUE)) {
                sendMDMMessage(client, fileAttachment.getContent(), stopwatch);
              }
            }
            deleteUtility.setMailsIsReadAndDelete(emailMessage, stopwatch);
            
          } catch (Exception e) {
            log.error("Exception", e);
          }
        }
        findResults = emailService.fetchEmailsFromInbox(service, ssmUtility);
      }
    } catch (Exception e) {
      log.error("Exception", e);
    }
    timeStamp = LocalDateTime.now().format(FOMATTER);
    log.info("Invocation completed: {}", timeStamp);
  }
  
  private void sendMDMMessage(HapiSendMDMClient client, byte[] pemXml, StagedStopwatch stopwatch) throws IOException, TransformerException {
    byte[] xml2html = xmlTransformer.transform(pemXml);
    client.sendMDM(xml2html);
    stopwatch.finishStage("Sending MDM message to HIE API");
  }
}