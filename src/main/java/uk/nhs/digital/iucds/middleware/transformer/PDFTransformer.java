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

/**
 * Converts HTML into a PDF
 */
@Data
@Component
public class PDFTransformer {

  private static final String FONTS = "./src/main/resources/fonts/calibri/";
  
  public byte[] transform(String html) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    ConverterProperties properties = new ConverterProperties();
    FontProvider fontProvider = new DefaultFontProvider(false, false, false);
    fontProvider.addFont("./src/main/resources/fonts/calibri/CALIBRI.ttf");
    fontProvider.addFont("./src/main/resources/fonts/calibri/CALIBRIB.ttf");
    fontProvider.addFont("./src/main/resources/fonts/calibri/CALIBRII.ttf");
    fontProvider.addFont("./src/main/resources/fonts/calibri/CALIBRIL.ttf");
    fontProvider.addFont("./src/main/resources/fonts/calibri/CALIBRILI.ttf");
    fontProvider.addFont("./src/main/resources/fonts/calibri/CALIBRIZ.ttf");
    //fontProvider.addDirectory(FONTS);
    properties.setFontProvider(fontProvider);
    HtmlConverter.convertToPdf(new StringInputStream(html), outputStream, properties);

    return outputStream.toByteArray();
  }
}
