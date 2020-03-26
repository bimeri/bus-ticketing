package net.gowaka.gowaka.constant.notification;

public enum  EmailFields {
    WELCOME_SUBJECT("Welcome to GoWaka"),
    TICKET_SUBJECT("GoWaka eTicket");

    private String message;
    EmailFields(String message) {this.message = message;}

    public String getMessage() {
        return message;
    }
}
