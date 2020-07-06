package uk.nhs.digital.iucds.middleware.service;

import java.io.IOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.digital.iucds.middleware.NHS111ReportData;

@Slf4j
@Data
@Component
public class NHS111ReportDataBuilder {

  private static final String H1_STR = "h1";
  private static final String H1_TAG = "<h1>";
  private static final String EMPTY_STR = "";
  private static final String SPACE = " ";
  private static final String DOC_TITLE = "doctitle";
  private static final String HEADER = "header";
  private static final String TITLE_BAR = "titlebar";
  private static final String FOOTER = "footer";
  private static final String TABLE_STR = "table";
  private static final String PATIENT_BANNER = "patientBanner";
  private static final String BORN = "Born";
  private static final String GENDER = "Gender";
  private static final String NHS_NUMBER = "NHS No.";
  private static final String UNVERIFIED_NHS_NUMBER = "Unverified NHS No.";
  private static final String LOCAL_PATIENT_ID = "Local Patient ID";
  private static final String HOME_ADDR = "Home Address";
  private static final String HOME_PHONE = "Home Phone";
  private static final String MOBILE_PHONE = "Mobile Phone";
  private static final String EMERGENCY_PHONE = "Emergency Phone";
  private static final String GP_PRACTICE = "GP Practice";

  NHS111ReportData report;

  public NHS111ReportData buildNhs111Report(Document doc) throws IOException {
    report = new NHS111ReportData();

    setDoctitle(doc);

    buildPatientBanner(doc);

    buildPatientsReportedCondition(doc);

    buildSpecialPatientNotes(doc);

    buildPathwaysDisposition(doc);

    buildConsultationSummary(doc);

    buildPathwaysAssessment(doc);

    buildAdviceGiven(doc);

    buildHeaderTable(doc);

    buildTitlebarTable(doc);

    buildFooter(doc);

    return report;
  }

  private void buildPatientsReportedCondition(Document doc) {
    Element element = doc.select(H1_STR).get(0);

    String htmlString = getHtmlString(element);

    report.setPatientsReportedCondition(htmlString.replaceAll(H1_TAG, EMPTY_STR));
  }

  private void buildSpecialPatientNotes(Document doc) {
    Element element = doc.select(H1_STR).get(1);

    String htmlString = getHtmlString(element);

    report.setSpecialPatientNotes(htmlString.replaceAll(H1_TAG, EMPTY_STR));
  }

  private void buildPathwaysDisposition(Document doc) {
    Element element = doc.select(H1_STR).get(2);

    String htmlString = getHtmlString(element);

    report.setPathwaysDisposition(htmlString.replaceAll(H1_TAG, EMPTY_STR));
  }

  private void buildConsultationSummary(Document doc) {
    Element element = doc.select(H1_STR).get(3);
    StringBuilder sb = new StringBuilder(element.toString());
    sb.append("<span class=\"label\"><ul>");
    Node node = element.nextSibling();
    while (node != null && !node.nodeName().startsWith("h")) {
      if (!node.outerHtml().equals("<br>") && !node.outerHtml().isEmpty()
          && !node.outerHtml().equals(" ")) {
        sb.append("<li>").append(node.outerHtml()).append("</li>");
      }
      node = node.nextSibling();
    }
    sb.append("</ul></span>");
    report.setConsultationSummary(sb.toString().replaceAll(H1_TAG, EMPTY_STR));
  }

  private void buildPathwaysAssessment(Document doc) {
    Element element = doc.select(H1_STR).get(4);
    StringBuilder sb = new StringBuilder(element.toString());
    sb.append("<span class=\"label\"><ul>");
    Node node = element.nextSibling();
    while (node != null && !node.nodeName().startsWith("h")) {
      if (!node.outerHtml().equals("<br>") && !node.outerHtml().isEmpty()) {
        sb.append("<li>").append(node.outerHtml()).append("</li>");
      }
      node = node.nextSibling();
    }
    sb.append("</ul></span>");
    report.setPathwaysAssessment(sb.toString().replaceAll(H1_TAG, EMPTY_STR));
  }

  private void buildAdviceGiven(Document doc) {
    Element element = doc.select(H1_STR).get(5);
    StringBuilder sb = new StringBuilder(element.toString());

    Node node = element.nextSibling();
    sb.append("<span class=\"label\">");
    while (node != null && !node.nodeName().startsWith("d")) {
      sb.append(node.outerHtml());
      node = node.nextSibling();
    }
    sb.append("</span>");
    report.setAdviceGiven(sb.toString().replaceAll(H1_TAG, EMPTY_STR));
  }

  private String getHtmlString(Element element) {
    StringBuilder sb = new StringBuilder(element.toString());

    Node node = element.nextSibling();
    sb.append("<span class=\"label\">");
    while (node != null && !node.nodeName().startsWith("h")) {
      sb.append(node.outerHtml());
      node = node.nextSibling();
    }
    sb.append("</span>");
    return sb.toString();
  }

  private void buildHeaderTable(Document doc) {
    String header = doc.getElementsByClass(HEADER).select(TABLE_STR).get(1).html();
    report.setHeader(header);
  }

  private void buildTitlebarTable(Document doc) {
    String titlebar = doc.getElementsByClass(TITLE_BAR).select(TABLE_STR).html();
    report.setTitlebar(titlebar);
  }

  private void buildFooter(Document doc) {
    String footer = doc.getElementsByClass(FOOTER).html();
    report.setFooter(footer);
  }

  private void setDoctitle(Document doc) {
    log.info(doc.getElementsByClass(DOC_TITLE).text());
    report.setTitle(doc.getElementsByClass(DOC_TITLE).text());
  }

  private void buildPatientBanner(Document doc) {
    String patientBanner = doc.getElementById(PATIENT_BANNER).text();
    String patientName = patientBanner.substring(0, patientBanner.indexOf(BORN));
    report.setTitle(
        patientName.replaceAll(", ", SPACE) + " - " + doc.getElementsByClass(DOC_TITLE).text());
    log.info(patientBanner);
    log.info(patientName);
    report.setPatientName(patientName);
    String dob =
        patientBanner.substring(patientBanner.lastIndexOf(BORN), patientBanner.indexOf(GENDER));
    log.info(dob);
    String gender;
    String nhsNo;
    if (patientBanner.contains(UNVERIFIED_NHS_NUMBER)) {
      gender = patientBanner.substring(patientBanner.lastIndexOf(GENDER),
          patientBanner.indexOf(UNVERIFIED_NHS_NUMBER));
      log.info(gender);
      nhsNo = patientBanner.substring(patientBanner.lastIndexOf(UNVERIFIED_NHS_NUMBER),
          patientBanner.indexOf(LOCAL_PATIENT_ID));
      log.info(nhsNo);
    } else {
      gender = patientBanner.substring(patientBanner.lastIndexOf(GENDER),
          patientBanner.indexOf(NHS_NUMBER));
      log.info(gender);
      nhsNo = patientBanner.substring(patientBanner.lastIndexOf(NHS_NUMBER),
          patientBanner.indexOf(LOCAL_PATIENT_ID));
      log.info(nhsNo);
    }
    String localPatientId = patientBanner.substring(patientBanner.lastIndexOf(LOCAL_PATIENT_ID),
        patientBanner.indexOf(HOME_ADDR));
    log.info(localPatientId);
    log.info(patientBanner.substring(patientBanner.lastIndexOf(HOME_ADDR),
        patientBanner.indexOf(HOME_PHONE)));
    String homePhone = patientBanner.substring(patientBanner.lastIndexOf(HOME_PHONE),
        patientBanner.indexOf(MOBILE_PHONE));
    log.info(homePhone.split(SPACE)[2]);
    String mobilePhone = patientBanner.substring(patientBanner.lastIndexOf(MOBILE_PHONE),
        patientBanner.indexOf(EMERGENCY_PHONE));
    log.info(mobilePhone.split(SPACE)[2]);
    String emergencyPhone = patientBanner.substring(patientBanner.lastIndexOf(EMERGENCY_PHONE),
        patientBanner.indexOf(GP_PRACTICE));
    log.info(emergencyPhone.split(SPACE)[2]);
    log.info(
        patientBanner.substring(patientBanner.lastIndexOf(GP_PRACTICE), patientBanner.length()));
    Element table = doc.getElementById(PATIENT_BANNER).select(TABLE_STR).get(1);
    Elements rows = table.select("tr");
    Element row = rows.get(0);
    Elements cols = row.select("td");
    String homeAddress = cols.get(0).select("p").html();
    log.info(homeAddress);
    report.setHomeAddress(homeAddress.replaceAll(HOME_ADDR + "\n", EMPTY_STR));
    String gpPractice = cols.get(2).select("p").html();
    log.info(gpPractice);
    report.setGpAddress(gpPractice.replaceAll(GP_PRACTICE + "\n", EMPTY_STR));

    report.setDob(dob.split(SPACE)[1]);
    report.setGender(gender.split(SPACE)[1]);
    report.setNhsNo(nhsNo.split(SPACE)[3] + nhsNo.split(SPACE)[4] + nhsNo.split(SPACE)[5]);
    report.setLocalPatientId(localPatientId.split(SPACE)[3]);
    report.setHomePhone(homePhone.split(SPACE)[2]);
    report.setMobilePhone(mobilePhone.split(SPACE)[2]);
    report.setEmergencyPhone(emergencyPhone.split(SPACE)[2]);
  }
}