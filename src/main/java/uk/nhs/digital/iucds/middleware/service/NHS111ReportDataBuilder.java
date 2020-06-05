package uk.nhs.digital.iucds.middleware.service;

import java.io.IOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.digital.iucds.middleware.NHS111ReportData;

@Slf4j
public class NHS111ReportDataBuilder {

  public NHS111ReportData buildNhs111Report(Document doc) throws IOException {
    NHS111ReportData report = new NHS111ReportData();
    log.info(doc.getElementsByClass("doctitle").text());
    report.setTitle(doc.getElementsByClass("doctitle").text());
    String patientBanner = doc.getElementById("patientBanner").text();
    String patientName = patientBanner.substring(0, patientBanner.indexOf("Born"));
    report.setTitle(
        patientName.replaceAll(", ", " ") + " - " + doc.getElementsByClass("doctitle").text());
    log.info(patientBanner);
    log.info(patientName);
    report.setPatientName(patientName);
    String dob =
        patientBanner.substring(patientBanner.lastIndexOf("Born"), patientBanner.indexOf("Gender"));
    log.info(dob);
    String gender = patientBanner.substring(patientBanner.lastIndexOf("Gender"),
        patientBanner.indexOf("Unverified NHS No."));
    log.info(gender);
    String nhsNo = patientBanner.substring(patientBanner.lastIndexOf("Unverified NHS No."),
        patientBanner.indexOf("Local Patient ID"));
    log.info(nhsNo);
    String localPatientId = patientBanner.substring(patientBanner.lastIndexOf("Local Patient ID"),
        patientBanner.indexOf("Home Address"));
    log.info(localPatientId);
    log.info(patientBanner.substring(patientBanner.lastIndexOf("Home Address"),
        patientBanner.indexOf("Home Phone")));
    String homePhone = patientBanner.substring(patientBanner.lastIndexOf("Home Phone"),
        patientBanner.indexOf("Mobile Phone"));
    log.info(homePhone.split(" ")[2]);
    String mobilePhone = patientBanner.substring(patientBanner.lastIndexOf("Mobile Phone"),
        patientBanner.indexOf("Emergency Phone"));
    log.info(mobilePhone.split(" ")[2]);
    String emergencyPhone = patientBanner.substring(patientBanner.lastIndexOf("Emergency Phone"),
        patientBanner.indexOf("GP Practice"));
    log.info(emergencyPhone.split(" ")[2]);
    log.info(
        patientBanner.substring(patientBanner.lastIndexOf("GP Practice"), patientBanner.length()));
    Element table = doc.getElementById("patientBanner").select("table").get(1);
    Elements rows = table.select("tr");
    Element row = rows.get(0);
    Elements cols = row.select("td");
    String html2 = cols.get(0).select("p").html();
    log.info(html2);
    report.setHomeAddress(html2.replaceAll("Home Address\n", ""));
    String html3 = cols.get(2).select("p").html();
    log.info(html3);
    report.setGpAddress(html3.replaceAll("GP Practice\n", ""));

    Element ele0 = doc.select("h1").get(0);
    StringBuilder sb0 = new StringBuilder(ele0.toString());

    Node next0 = ele0.nextSibling();
    sb0.append("<span class=\"label\">");
    while (next0 != null && !next0.nodeName().startsWith("h")) {
      sb0.append(next0.outerHtml());
      next0 = next0.nextSibling();
    }
    sb0.append("</span>");
    report.setPatientsReportedCondition(sb0.toString().replaceAll("<h1>", ""));

    Element ele4 = doc.select("h1").get(1);
    StringBuilder sb4 = new StringBuilder(ele4.toString());

    Node next4 = ele4.nextSibling();
    sb4.append("<span class=\"label\">");
    while (next4 != null && !next4.nodeName().startsWith("h")) {
      sb4.append(next4.outerHtml());
      next4 = next4.nextSibling();
    }
    sb4.append("</span>");
    report.setSpecialPatientNotes(sb4.toString().replaceAll("<h1>", ""));

    Element ele2 = doc.select("h1").get(2);
    StringBuilder sb3 = new StringBuilder(ele2.toString());

    Node next3 = ele2.nextSibling();
    sb3.append("<span class=\"label\">");
    while (next3 != null && !next3.nodeName().startsWith("h")) {
      sb3.append(next3.outerHtml());
      next3 = next3.nextSibling();
    }
    sb3.append("</span>");
    report.setPathwaysDisposition(sb3.toString().replaceAll("<h1>", ""));

    Element element = doc.select("h1").get(3);
    StringBuilder sb = new StringBuilder(element.toString());
    sb.append("<span class=\"label\"><ul>");
    Node next = element.nextSibling();
    while (next != null && !next.nodeName().startsWith("h")) {
      if (!next.outerHtml().equals("<br>") && !next.outerHtml().isEmpty()
          && !next.outerHtml().equals(" ")) {
        sb.append("<li>").append(next.outerHtml()).append("</li>");
      }
      next = next.nextSibling();
    }
    sb.append("</ul></span>");
    report.setConsultationSummary(sb.toString().replaceAll("<h1>", ""));

    Element element1 = doc.select("h1").get(4);
    StringBuilder sb1 = new StringBuilder(element1.toString());
    sb1.append("<span class=\"label\"><ul>");
    Node next1 = element1.nextSibling();
    while (next1 != null && !next1.nodeName().startsWith("h")) {
      if (!next1.outerHtml().equals("<br>") && !next1.outerHtml().isEmpty()) {
        sb1.append("<li>").append(next1.outerHtml()).append("</li>");
      }
      next1 = next1.nextSibling();
    }
    sb1.append("</ul></span>");
    report.setPathwaysAssessment(sb1.toString().replaceAll("<h1>", ""));

    Element ele = doc.select("h1").get(5);
    StringBuilder sb2 = new StringBuilder(ele.toString());

    Node next2 = ele.nextSibling();
    sb2.append("<span class=\"label\">");
    while (next2 != null && !next2.nodeName().startsWith("d")) {
      sb2.append(next2.outerHtml());
      next2 = next2.nextSibling();
    }
    sb2.append("</span>");
    report.setAdviceGiven(sb2.toString().replaceAll("<h1>", ""));

    report.setDob(dob.split(" ")[1]);
    report.setGender(gender.split(" ")[1]);
    report.setNhsNo(nhsNo.split(" ")[3] + nhsNo.split(" ")[4] + nhsNo.split(" ")[5]);
    report.setLocalPatientId(localPatientId.split(" ")[3]);
    report.setHomePhone(homePhone.split(" ")[2]);
    report.setMobilePhone(mobilePhone.split(" ")[2]);
    report.setEmergencyPhone(emergencyPhone.split(" ")[2]);

    String header = doc.getElementsByClass("header").select("table").get(1).html();
    report.setHeader(header);

    String titlebar = doc.getElementsByClass("titlebar").select("table").html();
    report.setTitlebar(titlebar);

    String footer = doc.getElementsByClass("footer").html();
    report.setFooter(footer);

    return report;
  }
}