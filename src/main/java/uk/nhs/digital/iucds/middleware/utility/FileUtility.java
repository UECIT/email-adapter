package uk.nhs.digital.iucds.middleware.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
public class FileUtility {

  public String createFileName(Document doc) throws ParseException {
    Element patientBanner = doc.getElementById("patientBanner");
    Elements table = patientBanner.select("table").first().select("td");
    String name = table.get(0).text().split(", ")[0];
    String dobString = table.get(1).text().split(" ")[1];
    SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
    Date date = format.parse(dobString);
    String dob = DateFormatUtils.format(date, "YYYYMMdd");
    String[] dobArr = table.get(3).text().split(" ");
    String nhsNumber = dobArr[3] + dobArr[4] + dobArr[5];
    return nhsNumber + "_" + name + "_" + dob;
  }
}
