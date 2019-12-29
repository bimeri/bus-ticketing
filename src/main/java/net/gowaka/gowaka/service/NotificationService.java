package net.gowaka.gowaka.service;

import net.gowaka.gowaka.network.api.notification.model.SendEmailDTO;

public interface NotificationService {
    void sendEmail(SendEmailDTO email);
}
