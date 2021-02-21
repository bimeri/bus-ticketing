package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/19/20 2:10 AM <br/>
 */
@Entity
@Data
public class GgCfsSurveyTemplateJson {

    @Id
    private String id;
    @Lob
    private String surveyTemplateJson;
}
