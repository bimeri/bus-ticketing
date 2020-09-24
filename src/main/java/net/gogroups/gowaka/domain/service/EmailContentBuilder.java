package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.dto.BookedJourneyStatusDTO;
import net.gogroups.storage.constants.FileAccessType;
import net.gogroups.storage.service.FileStorageService;
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
        context.setVariable("logo", getLogo());
        return templateEngine.process("welcome-email", context);
    }

    public String buildTicketEmail(BookedJourneyStatusDTO bookedJourneyStatusDTO) {
        Context context = new Context();
        context.setVariable("journeyInfo", bookedJourneyStatusDTO);
        context.setVariable("logo", getLogo());
        return templateEngine.process("ticket-email", context);
    }

    public String buildTicketPdfHtml(BookedJourneyStatusDTO bookedJourneyStatusDTO) {
        Context context = new Context();
        context.setVariable("journeyInfo", bookedJourneyStatusDTO);
        context.setVariable("logo", getLogo());

        return templateEngine.process("ticket-pdf", context);
    }


    private String getLogo() {
        return fileStorageService.getFilePath("logo.png", "/", FileAccessType.PUBLIC);
    }
}
