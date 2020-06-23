package uk.nhs.digital.iucds.middleware.transformer;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import lombok.Data;
import uk.nhs.digital.iucds.middleware.NHS111ReportData;

/**
 * Converts an {@link EncounterReport} into an HTML report document
 */
@Data
@Component
public class HTMLReportTransformer {

  private TemplateEngine templateEngine;

  public String transform(NHS111ReportData buildNhs111Report) {
    templateEngine = new TemplateEngine();
    templateEngine.addTemplateResolver(new ClassLoaderTemplateResolver());
    Context context = new Context();
    context.setVariable("report", buildNhs111Report);
    return templateEngine.process("/templates/NHS111ReportTemplate.html", context);
  }
}