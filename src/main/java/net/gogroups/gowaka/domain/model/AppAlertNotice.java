package net.gogroups.gowaka.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 5:38 AM <br/>
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AppAlertNotice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "language")
    private String language;

    @Column(name = "status")
    private Boolean status = Boolean.TRUE;

}
