package net.gowaka.gowaka.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 7:34 PM <br/>
 */
@Data
public class OfficialAgencyUserDTO {

    private String id;
    private String fullName;
    private List<String> roles;

    public OfficialAgencyUserDTO() {
        this.roles = new ArrayList<>();
    }
}
