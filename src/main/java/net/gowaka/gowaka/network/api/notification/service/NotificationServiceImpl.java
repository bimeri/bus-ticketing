package net.gowaka.gowaka.network.api.notification.service;

import net.gowaka.gowaka.network.api.notification.model.SendEmailDTO;
import net.gowaka.gowaka.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author nouks
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    private NotificationRestClient restClient;

    @Autowired
    public NotificationServiceImpl(NotificationRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void sendEmail(SendEmailDTO email) {
        restClient.sendEmail(email);
    }
}
