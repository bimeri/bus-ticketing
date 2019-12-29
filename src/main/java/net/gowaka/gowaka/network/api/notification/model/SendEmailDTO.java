package net.gowaka.gowaka.network.api.notification.model;

import lombok.Data;

import java.util.List;

/**
 * @author nouks
 */
@Data
public class SendEmailDTO {
    private String fromAddress;
    private String message;
    private String subject;
    private List<EmailAddress> bccAddresses;
    private List<EmailAddress> ccAddresses;
    private List<EmailAddress> toAddresses;
}
