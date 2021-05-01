package net.gogroups.gowaka.dto;

import lombok.Data;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 9:07 PM <br/>
 */
@Data
public class UserDTO {

    private String id;
    private String fullName;
    private String email;
    private List<String> roles;

    //These fields are not present in the token or ApiSecurity
    // there are additional fields on Gowka Database
    private String phoneNumber;
    private String idCardNumber;
    private String qrCodeImage;
    private TokenDTO token;
    private Agency agency;

    @Data
    public static class Agency {

        private Long branchId;
        private String branchName;
        private String branchAddress;

        private Long agencyId;
        private String agencyName;

    }
}
