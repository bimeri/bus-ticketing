package net.gowaka.gowaka.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */
@Data
public class ResponseCarDTO {
    private Long id;
    private String name;
    private String licensePlateNumber;
    private Boolean isOfficialAgencyIndicator;
    private Boolean isCarApproved;
    private LocalDateTime timestamp;
}
