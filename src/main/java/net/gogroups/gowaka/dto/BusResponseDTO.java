package net.gogroups.gowaka.dto;

import lombok.Data;

/**
 *@author Nnouka Stephen
 *@date: 26 Sep 2019
 *
 */
@Data
public class BusResponseDTO {
    private Long id;
    private Boolean isCarApproved;
    private String name;
    private String licensePlateNumber;
    private Integer numberOfSeats;
    private SeatStructureDTO seatStructure;
}
