package net.gowaka.gowaka.network.api.notification.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author  nouks
 */
@Data
@NoArgsConstructor
public class EmailAddress {
    private String email;
    private String name;

    public EmailAddress(String email, String name) {
        this.email = email;
        this.name = name;
    }

    @Override
    public String toString() {
        return "EmailAddress{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
