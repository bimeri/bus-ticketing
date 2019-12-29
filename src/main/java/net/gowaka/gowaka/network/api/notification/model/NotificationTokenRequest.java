package net.gowaka.gowaka.network.api.notification.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author nouks
 *
 */
@Data
@NoArgsConstructor
public class NotificationTokenRequest {
    private String email;
    private String password;

    public NotificationTokenRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
