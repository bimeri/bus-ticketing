package net.gowaka.gowaka.constant.notification;

public enum  EmailFields {
    WELCOME_SUBJECT("Welcome to GoWaka"),
    WELCOME_MESSAGE("Go! GoWaka is an online booking service used by African travel agencies" +
            " in the bus and train sector and event organizers for managing their operational activities with ease," +
            " GoWaka is based in Cameroon. You are welcome on Board!");
    private String message;
    EmailFields(String message) {this.message = message;}

    public String getMessage() {
        return message;
    }
}
