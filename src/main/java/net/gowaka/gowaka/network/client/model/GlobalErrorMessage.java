package net.gowaka.gowaka.network.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 10:00 PM <br/>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalErrorMessage {

    private String code;
    private String message;
}
