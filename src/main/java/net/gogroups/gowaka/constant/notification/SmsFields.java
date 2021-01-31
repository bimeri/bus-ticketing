package net.gogroups.gowaka.constant.notification;

public enum SmsFields {
    SMS_LABEL("GoWaka");

    private String message;
    SmsFields(String message) {this.message = message;}

    public String getMessage() {
        return message;
    }
}
