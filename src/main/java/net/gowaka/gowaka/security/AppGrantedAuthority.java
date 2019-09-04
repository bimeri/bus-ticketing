package net.gowaka.gowaka.security;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;

/**
 * User: Edward Tanko <br/>
 * Date: 29/5/19 9:12 PM <br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AppGrantedAuthority implements GrantedAuthority {
    private String authority;

    @Override
    public String getAuthority() {
        return authority;
    }
}
