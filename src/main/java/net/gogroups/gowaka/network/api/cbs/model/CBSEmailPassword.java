package net.gogroups.gowaka.network.api.cbs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 4:55 PM <br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBSEmailPassword {

    private String email;
    private String password;

}
