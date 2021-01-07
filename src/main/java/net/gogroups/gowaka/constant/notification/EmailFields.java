package net.gogroups.gowaka.constant.notification;

public enum  EmailFields {
    WELCOME_SUBJECT("Welcome to GoWaka"),
    UPDATED_TICKET_SUBJECT("GoWaka eTicket: [UPDATED]"),
    TICKET_SUBJECT("GoWaka eTicket");

    private String message;
    EmailFields(String message) {this.message = message;}

    public String getMessage() {
        return message;
    }
}
