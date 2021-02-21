package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Author: Edward Tanko <br/>
 * Date: 4/29/20 4:33 PM <br/>
 */
@Data
public class RefreshTokenDTO {

    @NotBlank(message = "refresh token is required.")
    private String refreshToken;
}
