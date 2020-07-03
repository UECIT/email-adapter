package uk.nhs.digital.iucds.middleware.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.ItemAttachment;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import microsoft.exchange.webservices.data.search.filter.SearchFilter.SearchFilterCollection;
import uk.nhs.digital.iucds.middleware.NHS111ReportData;
import uk.nhs.digital.iucds.middleware.transformer.HTMLReportTransformer;
import uk.nhs.digital.iucds.middleware.transformer.PDFTransformer;
import uk.nhs.digital.iucds.middleware.utility.SsmUtility;
import uk.nhs.digital.iucds.middleware.utility.StagedStopwatch;

@Slf4j
@Data
@Component
public class EmailService {

  private static final String EMS_REPORT_SUBJECT = "ems-email-subject";
  private static final String EMS_REPORT_BODY = "ems-email-body";
  private static final String EMS_REPORT_RECIPIENT = "ems-email-recipients";
  private static final String EMAIL_ITEM_VIEW_PAGE_SIZE = "ems-email-item-view-page-size";
  private static final String EMS_REPORT_FROM = "ems-email-from";
  
  @Autowired
  private NHS111ReportDataBuilder reportBuilder;
  
  @Autowired
  private HTMLReportTransformer htmlReportTransformer;
  
  @Autowired
  private PDFTransformer pdfTransformer;
  
  private StagedStopwatch stopwatch;
  private NHS111ReportData nhs111Report;
  private byte[] convertedPdf;
  
  public Attachment getAttachmentFromEmailMessage(EmailMessage emailMessage, StagedStopwatch stopwatch) throws Exception {
    this.stopwatch = stopwatch;
    emailMessage
        .load(new PropertySet(BasePropertySet.FirstClassProperties, ItemSchema.MimeContent));
    log.info("attachment count: {} ", emailMessage.getAttachments().getCount());
    Attachment attachment = null;
    int attachementSize = emailMessage.getAttachments().getItems().size();
    if (attachementSize > 0) {
      if (attachementSize == 1) {
        attachment = emailMessage.getAttachments().getItems().get(0);
        stopwatch.finishStage("Getting html attchement from email");
        return attachment;
      } else {
        log.info("multiple attachemnt email: {} ", attachementSize);
        emailMessage.move(WellKnownFolderName.JunkEmail);
      }
    }
    return attachment;
  }

  public FileAttachment getFileAttachment(Attachment attachment) throws Exception {
    if (attachment instanceof FileAttachment) {
      FileAttachment fileAttachment = (FileAttachment) attachment;
      fileAttachment.load();
      return fileAttachment;
    } else if (attachment instanceof ItemAttachment) {
      ItemAttachment itemAttachment = (ItemAttachment) attachment;

      String name = itemAttachment.getName();
      log.info("ItemAttachment name : {} ", name);
      Item attachedItem = itemAttachment.getItem();
      log.info("ItemAttachment attachedItem : {} ", attachedItem);
    }
    return null;
  }
  
  public NHS111ReportData buildNhs111Report(Document doc) throws Exception {
    NHS111ReportData buildNhs111Report = reportBuilder.buildNhs111Report(doc);
    stopwatch.finishStage("NHS 111 Report transformation");
    this.nhs111Report = buildNhs111Report;
    return buildNhs111Report;
  }
  
  public byte[] convertNhs111ReportToPdf() throws Exception {
    String nhs111ReportString = htmlReportTransformer.transform(nhs111Report);
    byte[] transformedPdf = pdfTransformer.transform(Jsoup.parse(nhs111ReportString).html());
    stopwatch.finishStage("pdf transformation");
    this.convertedPdf = transformedPdf;
    return transformedPdf;
  }
  
  public void createEmailMeassageAndSend(ExchangeService service, SsmUtility ssmUtility, String fileName) throws Exception {
    // Create an email message and set properties on the message.
    EmailMessage message = new EmailMessage(service);
    message.setSubject(ssmUtility.getParameter(EMS_REPORT_SUBJECT));
    message.setBody(new MessageBody(ssmUtility.getParameter(EMS_REPORT_BODY)));
    String recipientsString = ssmUtility.getParameter(EMS_REPORT_RECIPIENT);
    String[] recipients = recipientsString.split(",");
    for (String recipient : recipients) {
      message.getToRecipients().add(recipient); 
    }
    FileAttachment addFileAttachment =
        message.getAttachments().addFileAttachment(fileName, convertedPdf);
    addFileAttachment.setContentType("application/pdf");
    stopwatch.finishStage("Added pdf attchement to mail");
    message.send();
    stopwatch.finishStage("Sent an email with pdf attachement");
  }

  public FindItemsResults<Item> fetchEmailsFromInbox(ExchangeService service, SsmUtility ssmUtility) throws Exception {
    ItemView view = new ItemView(Integer.parseInt(ssmUtility.getParameter(EMAIL_ITEM_VIEW_PAGE_SIZE)));
    SearchFilterCollection searchFilterCollection =
        new SearchFilter.SearchFilterCollection(LogicalOperator.And);
    searchFilterCollection.add(
        new SearchFilter.IsEqualTo(EmailMessageSchema.From, ssmUtility.getParameter(EMS_REPORT_FROM)));
    searchFilterCollection.add(new SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, false));
    view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Descending); 
    return service.findItems(WellKnownFolderName.Inbox, searchFilterCollection, view);
  }
}