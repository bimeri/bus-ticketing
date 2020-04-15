package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.dto.BookedJourneyStatusDTO;
import net.gowaka.gowaka.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailContentBuilder {

    private TemplateEngine templateEngine;
    private FileStorageService fileStorageService;

    @Autowired
    public EmailContentBuilder(TemplateEngine templateEngine, FileStorageService fileStorageService) {
        this.templateEngine = templateEngine;
        this.fileStorageService = fileStorageService;
    }

    public String buildWelcomeEmail(String name, String username, String loginUrl, int year) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("username", username);
        context.setVariable("login_url", loginUrl);
        context.setVariable("year", year);
        context.setVariable("logo", fileStorageService.getLogo());
        return templateEngine.process("welcome-email", context);
    }

    public String buildTicketEmail(BookedJourneyStatusDTO bookedJourneyStatusDTO) {
        Context context = new Context();
        context.setVariable("journeyInfo", bookedJourneyStatusDTO);
        context.setVariable("logo", fileStorageService.getLogo());
        return templateEngine.process("ticket-email", context);
    }

    public String buildTicketPdfHtml(BookedJourneyStatusDTO bookedJourneyStatusDTO) {
        Context context = new Context();
        context.setVariable("journeyInfo", bookedJourneyStatusDTO);
        context.setVariable("logo", fileStorageService.getLogo());

        return templateEngine.process("ticket-pdf", context);
    }

}
