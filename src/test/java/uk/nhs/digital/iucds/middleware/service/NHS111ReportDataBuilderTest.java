package uk.nhs.digital.iucds.middleware.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class NHS111ReportDataBuilderTest {

  private static final String HTML_FILE = "src/test/resources/input.html";
  
  private NHS111ReportDataBuilder reportBuilder = new NHS111ReportDataBuilder();
  
  @Test
  public void testBuildNhs111Report() throws IOException {
    File inputFile = new File(HTML_FILE);
    String readFileToString = FileUtils.readFileToString(inputFile, StandardCharsets.UTF_8);
    Document doc = Jsoup.parse(readFileToString);
    
    reportBuilder.buildNhs111Report(doc);
  }
}