package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 10:53 AM <br/>
 */
@Data
public class CreatePersonalAgencyDTO {
    @NotBlank(message = "name is required.")
    @Size(max = 50, message = "name can not be more than 50 characters")
    private String name;
}
