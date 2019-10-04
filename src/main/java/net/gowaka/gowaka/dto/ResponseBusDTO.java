package net.gowaka.gowaka.dto;

import lombok.Data;

/**
 *@author Nnouka Stephen
 *@date: 26 Sep 2019
 *
 */
@Data
public class ResponseBusDTO extends BusDTO {
    private Long id;
    private Boolean isCarApproved;
}
