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

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.ItemAttachment;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import microsoft.exchange.webservices.data.search.filter.SearchFilter.SearchFilterCollection;
import uk.nhs.digital.iucds.middleware.client.HapiSendMDMClient;
import uk.nhs.digital.iucds.middleware.service.NHS111ReportDataBuilder;
import uk.nhs.digital.iucds.middleware.transformer.HTMLReportTransformer;
import uk.nhs.digital.iucds.middleware.transformer.PDFTransformer;
import uk.nhs.digital.iucds.middleware.utility.DeleteUtility;
import uk.nhs.digital.iucds.middleware.utility.StagedStopwatch;

@Slf4j
@EnableAsync
@Data
@Component
public class MiddlewareSchedulerTask {

  @Autowired
  private StagedStopwatch stopwatch; 
  
  @Autowired
  private DeleteUtility deleteUtility;
  
  @Autowired
  private NHS111ReportDataBuilder reportBuilder;
  
  @Autowired
  private HTMLReportTransformer htmlReportTransformer;
  
  @Autowired
  private PDFTransformer pdfTransformer;
  
  private final DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
  private ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
  private AWSSimpleSystemsManagement ssm =
      AWSSimpleSystemsManagementClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
  private HapiSendMDMClient client;
  
  public MiddlewareSchedulerTask() throws Exception {
    ExchangeCredentials credentials =
        new WebCredentials(getParameter("username"), getParameter("password"));
    service.setCredentials(credentials);
    service.autodiscoverUrl(getParameter("username"));
    client = new HapiSendMDMClient(getParameter("TCP_HOST"), getParameter("PORT_NUMBER"));
  }

  public MiddlewareSchedulerTask(ExchangeService service, AWSSimpleSystemsManagement ssm,
      HapiSendMDMClient client, NHS111ReportDataBuilder reportBuilder,
      HTMLReportTransformer htmlReportTransformer, PDFTransformer pdfTransformer) {
    this.service = service;
    this.ssm = ssm;
    this.client = client;
    this.reportBuilder = reportBuilder;
    this.htmlReportTransformer = htmlReportTransformer;
    this.pdfTransformer = pdfTransformer;
  }

  @Async
  @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}",
      initialDelayString = "${initialDelay.in.milliseconds}")
  public void sendMails() throws InterruptedException {
    String timeStamp = LocalDateTime.now().format(FOMATTER);
    log.info("Invocation started: {}", timeStamp);

    try {
      FindItemsResults<Item> findResults = getFindItemsResults();

      while (findResults.getTotalCount() > 0) {
        for (Object item : findResults.getItems()) {
          try {
            EmailMessage emailMessage = (EmailMessage) item;

            Attachment attachmentFromEmailMessage = getAttachmentFromEmailMessage(emailMessage);
            
            if (attachmentFromEmailMessage != null) {
              getFileContentFromAttachment(attachmentFromEmailMessage);
            }
            
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

  private Attachment getAttachmentFromEmailMessage(EmailMessage emailMessage) throws Exception {
    emailMessage
        .load(new PropertySet(BasePropertySet.FirstClassProperties, ItemSchema.MimeContent));
    log.info("attachment count: {} ", emailMessage.getAttachments().getCount());
    Attachment attachment = null;
    if (emailMessage.getAttachments().getItems().size() != 0) {
      attachment = emailMessage.getAttachments().getItems().get(0);
      stopwatch.finishStage("Getting html attchement from email");
      return attachment;
    }
    return attachment;
  }

  private void getFileContentFromAttachment(Attachment attachment) throws Exception {
    if (attachment instanceof FileAttachment) {
      FileAttachment fileAttachment = (FileAttachment) attachment;
      fileAttachment.load();

      if (fileAttachment.getContentType().equalsIgnoreCase(MimeTypeUtils.TEXT_HTML_VALUE)) {
        // convert bytes[] to string
        String htmlString = new String(fileAttachment.getContent(), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(htmlString);

        NHS111ReportData buildNhs111Report = reportBuilder.buildNhs111Report(doc);
        stopwatch.finishStage("NHS 111 Report transformation");

        String nhs111ReportString = htmlReportTransformer.transform(buildNhs111Report);
        byte[] transform = pdfTransformer.transform(Jsoup.parse(nhs111ReportString).html());
        stopwatch.finishStage("pdf transformation");

        sendMDMMessage(transform);

        createEmailMeassageAndSend(doc, transform);

      }

    } else if (attachment instanceof ItemAttachment) {
      ItemAttachment itemAttachment = (ItemAttachment) attachment;

      String name = itemAttachment.getName();
      log.info("ItemAttachment name : {} ", name);
      Item attachedItem = itemAttachment.getItem();
      log.info("ItemAttachment attachedItem : {} ", attachedItem);
    }
  }

  private void createEmailMeassageAndSend(Document doc, byte[] transform) throws Exception {
    // Create an email message and set properties on the message.
    EmailMessage message = new EmailMessage(service);
    message.setSubject(getParameter("EMS_REPORT_SUBJECT"));
    message.setBody(new MessageBody(getParameter("EMS_REPORT_BODY")));
    message.getToRecipients().add(getParameter("EMS_REPORT_RECIPIENT"));
    FileAttachment addFileAttachment =
        message.getAttachments().addFileAttachment(createFileName(doc), transform);
    addFileAttachment.setContentType("application/pdf");
    stopwatch.finishStage("Added pdf attchement to mail");
    message.send();
    stopwatch.finishStage("Sent an email with pdf attachement");
  }

  private void sendMDMMessage(byte[] transform) {
    client.sendMDM(transform);
    stopwatch.finishStage("Sending MDM message to HIE API");
  }

  private FindItemsResults<Item> getFindItemsResults() throws Exception {
    ItemView view = new ItemView(Integer.parseInt(getParameter("EMAIL_ITEM_VIEW")));
    SearchFilterCollection searchFilterCollection =
        new SearchFilter.SearchFilterCollection(LogicalOperator.And);
    searchFilterCollection.add(
        new SearchFilter.IsEqualTo(EmailMessageSchema.From, getParameter("EMS_REPORT_SENDER")));
    searchFilterCollection.add(new SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, false));
    return service.findItems(WellKnownFolderName.Inbox, searchFilterCollection, view);
  }

  public String getParameter(String parameterName) {
    GetParameterRequest request = new GetParameterRequest();
    request.setName(parameterName);
    request.setWithDecryption(true);
    return ssm.getParameter(request).getParameter().getValue();
  }

  private String createFileName(Document doc) throws ParseException {
    Element patientBanner = doc.getElementById("patientBanner");
    Elements table = patientBanner.select("table").first().select("td");
    String name = table.get(0).text().split(" ")[1];
    String dobString = table.get(1).text().split(" ")[1];
    SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
    Date date = format.parse(dobString);
    String dob = DateFormatUtils.format(date, "YYYYMMdd");
    String[] dobArr = table.get(3).text().split(" ");
    String nhsNumber = dobArr[3] + dobArr[4] + dobArr[5];
    return nhsNumber + "_" + name + "_" + dob;
  }
}
