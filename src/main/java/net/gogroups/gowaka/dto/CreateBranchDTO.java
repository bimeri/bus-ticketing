package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/13/21 9:33 PM <br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBranchDTO {

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must be less than 50 characters")
    private String name;
    @Size(max = 50, message = "address must be less than 50 characters")
    private String address = "";

}
