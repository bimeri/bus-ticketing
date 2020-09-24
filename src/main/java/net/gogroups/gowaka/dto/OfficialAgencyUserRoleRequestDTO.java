package net.gogroups.gowaka.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 7:31 PM <br/>
 */
@Data
public class OfficialAgencyUserRoleRequestDTO {

    private String userId;
    List<String> roles;

    public OfficialAgencyUserRoleRequestDTO() {
        this.roles = new ArrayList<>();
    }
}
