package net.gogroups.gowaka.network.api.cbs.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 10:00 PM <br/>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CBSErrorMessage {

    private String code;
    private String message;
    private String endpoint;

}
