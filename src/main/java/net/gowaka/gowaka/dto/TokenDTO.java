package net.gowaka.gowaka.dto;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/15/19 12:27 AM <br/>
 */
@Data
public class TokenDTO {

    private String header;
    private String issuer;
    private String accessToken;
    private String refreshToken;
    private Long expiredIn;
    private String type;
    private UserDTO userDetails;
}
