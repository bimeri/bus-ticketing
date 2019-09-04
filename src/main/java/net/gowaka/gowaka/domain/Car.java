package net.gowaka.gowaka.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:58 AM <br/>
 */
@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Car {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String licensePlateNumber;
    private Boolean isAgencyIndicator;
    @ElementCollection
    private List<String> images = new ArrayList<>();
    private Boolean isCarApproved;
    private LocalDateTime timestamp;


}
