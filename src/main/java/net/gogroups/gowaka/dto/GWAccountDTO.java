package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/13/21 7:02 AM <br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GWAccountDTO {

    private String name;
    private String idNumber;
    private String phoneNumber;
    private String email;

}
