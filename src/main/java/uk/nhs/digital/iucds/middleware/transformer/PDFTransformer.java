package uk.nhs.digital.iucds.middleware.transformer;

import java.io.IOException;
import org.springframework.stereotype.Component;
import com.amazonaws.util.StringInputStream;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.layout.font.FontProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts HTML into a PDF
 */
@Slf4j
@Data
@Component
public class PDFTransformer {

  private static final String FONTS = "src/main/resources/fonts/calibri/";
  
  public byte[] transform(String html) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    ConverterProperties properties = new ConverterProperties();
    FontProvider fontProvider = new DefaultFontProvider(false, false, true);
    fontProvider.addDirectory(FONTS);
    properties.setFontProvider(fontProvider);
    log.info("fontProvider {} ", fontProvider.getFontSet().getFonts());
    HtmlConverter.convertToPdf(new StringInputStream(html), outputStream);

    return outputStream.toByteArray();
  }
}