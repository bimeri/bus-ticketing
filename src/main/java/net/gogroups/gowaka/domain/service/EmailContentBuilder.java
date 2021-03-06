package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.constant.RefundStatus;
import net.gogroups.gowaka.domain.model.RefundPaymentTransaction;
import net.gogroups.gowaka.domain.model.User;
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
        context.setVariable("notes", "Your online ticket purchase does not cover extra luggage charges");
        context.setVariable("logo", getLogo());
        return templateEngine.process("ticket-email", context);
    }

    public String buildTicketPdfHtml(BookedJourneyStatusDTO bookedJourneyStatusDTO) {
        Context context = new Context();
        context.setVariable("journeyInfo", bookedJourneyStatusDTO);
        context.setVariable("notes", "Your online ticket purchase does not cover extra luggage charges");
        context.setVariable("logo", getLogo());

        return templateEngine.process("ticket-pdf", context);
    }

    public String buildRefundStatusEmail(RefundPaymentTransaction paymentTransaction, User user) {

        boolean approved = RefundStatus.valueOf(paymentTransaction.getRefundStatus()) == RefundStatus.APPROVED;
        boolean refunded = RefundStatus.valueOf(paymentTransaction.getRefundStatus()) == RefundStatus.REFUNDED;

        Context context = new Context();
        context.setVariable("approvalName", paymentTransaction.getApprovalName());
        context.setVariable("cause", paymentTransaction.getRefundResponseMessage());
        context.setVariable("approved", approved);
        context.setVariable("refunded", refunded);
        context.setVariable("refunderName", paymentTransaction.getRefunderName());
        context.setVariable("refundedDate", paymentTransaction.getRefundedDate());
        context.setVariable("message", refunded ?
                "Your refund request has been refunded." : approved ?
                "Your refund request has been approved." : "Your refund request has been declined");
        context.setVariable("amount", paymentTransaction.getAmount());
        context.setVariable("currency", "XAF");
        context.setVariable("fullName", user.getFullName());
        context.setVariable("logo", getLogo());
        return templateEngine.process("refund-email", context);
    }


    public String buildJourneyStatusEmail(String message) {

        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("logo", getLogo());
        return templateEngine.process("journey-update-email", context);
    }


    private String getLogo() {
        return fileStorageService.getFilePath("logo.png", "/", FileAccessType.PUBLIC);
    }
}
