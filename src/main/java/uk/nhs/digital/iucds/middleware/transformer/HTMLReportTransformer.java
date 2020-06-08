package uk.nhs.digital.iucds.middleware.transformer;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import uk.nhs.digital.iucds.middleware.NHS111ReportData;

/**
 * Converts an {@link EncounterReport} into an HTML report document
 */
public class HTMLReportTransformer {

  private final TemplateEngine templateEngine;

  public HTMLReportTransformer() {
    templateEngine = new TemplateEngine();
    templateEngine.addTemplateResolver(new ClassLoaderTemplateResolver());
  }

  public String transform(NHS111ReportData buildNhs111Report) {
    Context context = new Context();
    NHS111ReportData NHS111Report = buildNhs111Report;
    context.setVariable("report", NHS111Report);
    return templateEngine.process("/templates/NHS111ReportTemplate.html", context);
  }
}
