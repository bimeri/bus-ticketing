package net.gowaka.gowaka.domain.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author nouks
 *
 * @date 28 Oct 2019
 */
@Entity
@Table(name = "journey_stop")
@Getter
@Setter
public class JourneyStop implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journey_id")
    private Journey journey;

    @ManyToOne
    @JoinColumn(name = "transit_stop_id")
    private TransitAndStop transitAndStop;

    private double amount;

    public JourneyStop() {
    }

    public JourneyStop(Journey journey, TransitAndStop transitAndStop, double amount) {
        this.journey = journey;
        this.transitAndStop = transitAndStop;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JourneyStop)) return false;
        JourneyStop that = (JourneyStop) o;
        return Double.compare(that.amount, amount) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(journey.getId(), that.journey.getId()) &&
                Objects.equals(transitAndStop.getId(), that.transitAndStop.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, journey.getId(), transitAndStop.getId(), amount);
    }

    @Override
    public String toString() {
        return "JourneyStop{" +
                "id=" + id +
                ", transitAndStop=" + transitAndStop +
                ", amount=" + amount +
                '}';
    }
}
