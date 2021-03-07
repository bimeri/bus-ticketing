package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/7/21 2:15 PM <br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneNumberDTO {

    @NotBlank(message = "telCode is required")
    @Size(max = 3, message = "telCode can not be more that 3 characters")
    private String telCode;
    @NotBlank(message = "phoneNumber is required")
    @Size(max = 10, message = "phoneNumber must be less than 50 characters")
    private String phoneNumber;
}
