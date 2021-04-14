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
public class SearchPassengerDTO {

    private String telCode;
    private String phoneNumber;
    private String name;
}
