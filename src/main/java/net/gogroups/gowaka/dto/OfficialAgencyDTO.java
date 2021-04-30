package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:31 PM <br/>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfficialAgencyDTO {

    private Long id;
    private String agencyName;
    private String logo;
    private String policy;
    private String code;
    private String agencyRegistrationNumber;
    private List<Bus> buses = new ArrayList<>();
    private OfficialAgencyAdminUserDTO agencyAdmin;
    private long numberOfCompletedTrips;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String address;
    private String phoneNumber;
    private List<Branch> branches = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Bus {
        private Long id;
        private String name;
        private String licensePlateNumber;
        private Integer numberOfSeats;
        private LocalDateTime updatedAt;
        private String updatedBy;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Branch {
        private Long id;
        private String name;
        private String address;
        private LocalDateTime updatedAt;
        private String updatedBy;
    }

}
