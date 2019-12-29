package net.gowaka.gowaka.network.api.notification.model;

import lombok.Data;

/**
 * @author nouks
 *
 */
@Data
public class NotificationTokenResponse {
    private String header;
    private String issuer;
    private String accessToken;
    private String type;

}
