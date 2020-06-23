package uk.nhs.digital.iucds.middleware.transformer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.digital.iucds.middleware.NHS111ReportData;
import uk.nhs.digital.iucds.middleware.service.NHS111ReportDataBuilder;

public class PDFTransformerTest {

  public static final String OUTPUT_PATH = "target/test-output/resources/output.pdf";
  public static final String HTML_FILE = "src/test/resources/input.html";

  private PDFTransformer pdfTransformer;
  private HTMLReportTransformer htmlReportTransformer;

  @Before
  public void setup() {
    pdfTransformer = new PDFTransformer();
    htmlReportTransformer = new HTMLReportTransformer();
  }

  @Test
  public void transformEmptyReportPdf() throws IOException {
    NHS111ReportDataBuilder build = new NHS111ReportDataBuilder();
    NHS111ReportData buildNhs111Report = build.buildNhs111Report(Jsoup.parse(getInputHtml()));

    String nhs111ReportString = htmlReportTransformer.transform(buildNhs111Report);
    
    byte[] pdfData = pdfTransformer.transform(nhs111ReportString);

    FileUtils.writeByteArrayToFile(new File(OUTPUT_PATH), pdfData);
  }

  private String getInputHtml() throws IOException {
    File inputFile = new File(HTML_FILE);
    return FileUtils.readFileToString(inputFile, StandardCharsets.UTF_8);
  }
}