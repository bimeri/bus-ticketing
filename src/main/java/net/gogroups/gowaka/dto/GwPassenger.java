package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/7/21 2:19 PM <br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GwPassenger {

    private String name;
    private String idNumber;
    private String phoneNumber;
    private String email;

    private String directedToAccount;
}
