package net.gowaka.gowaka.network.api.cbs.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 5:09 PM <br/>
 */
@Data
public class CBSBenefitDTO {

    private Long id;
    private Double coveragePercentage;
    private String description;
}
