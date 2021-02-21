package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 5:24 AM <br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppNoticeDTO {

    private String title;
    private String message;
    private String language;
}
