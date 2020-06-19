package uk.nhs.digital.iucds.middleware.transformer;

import java.io.IOException;
import org.springframework.stereotype.Component;
import com.amazonaws.util.StringInputStream;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.layout.font.FontProvider;
import lombok.Data;

/**
 * Converts HTML into a PDF
 */
@Data
@Component
public class PDFTransformer {

  public static final String[] FONTS = {
      "src/main/resources/fonts/calibri/Calibri-Regular.ttf",
      "src/main/resources/fonts/calibri/Calibri-Light-Italic.ttf",
      "src/main/resources/fonts/calibri/Calibri-Light.ttf",
      "src/main/resources/fonts/calibri/Calibri-Italic.ttf",
      "src/main/resources/fonts/calibri/Calibri-Bold-Italic.ttf",
      "src/main/resources/fonts/calibri/Calibri-Bold.TTF"
  };
  
  public byte[] transform(String html) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    ConverterProperties properties = new ConverterProperties();
    FontProvider fontProvider = new DefaultFontProvider(false, false, false);
    for (String font : FONTS) {
        String fontName = PDFTransformer.class.getResource(font).toString();
        FontProgram fontProgram = FontProgramFactory.createFont(fontName);
        fontProvider.addFont(fontProgram);
    }
    properties.setFontProvider(fontProvider);
    HtmlConverter.convertToPdf(new StringInputStream(html), outputStream, properties);

    return outputStream.toByteArray();
  }
}
