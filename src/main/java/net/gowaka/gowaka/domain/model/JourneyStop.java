package net.gowaka.gowaka.domain.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author nouks
 *
 * @date 28 Oct 2019
 */
@Entity
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

    public Journey getJourney() {
        return journey;
    }

    public void setJourney(Journey journey) {
        this.journey = journey;
    }

    public TransitAndStop getTransitAndStop() {
        return transitAndStop;
    }

    public void setTransitAndStop(TransitAndStop transitAndStop) {
        this.transitAndStop = transitAndStop;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JourneyStop)) return false;
        JourneyStop that = (JourneyStop) o;
        return journey != null && transitAndStop != null
                ? journey.getId().equals(that.getJourney().getId()) &&
                transitAndStop.getId().equals(that.getTransitAndStop().getId())
                : that.getJourney() == null || that.getTransitAndStop() == null;
    }

    @Override
    public int hashCode() {
        return journey != null && transitAndStop != null
                ? Objects.hash(journey.getId(), transitAndStop.getId()) : 0 ;
    }

    @Override
    public String toString() {
        return "JourneyStop{" +
                "transitAndStop=" + transitAndStop +
                ", amount=" + amount +
                '}';
    }
}
