package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Nnouka Stephen <br/>
 * Date: 12/21/20 11:00 PM <br/>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GWUserDTO {
    private String email;
    private String fullName;
}
