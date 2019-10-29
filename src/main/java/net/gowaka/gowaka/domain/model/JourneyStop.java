package net.gowaka.gowaka.domain.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author nouks
 *
 * @date 28 Oct 2019
 */
@Entity
public class JourneyStop implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "journey_id")
    private Journey journey;
    @Id
    @ManyToOne
    @JoinColumn(name = "transit_stop_id")
    private TransitAndStop transitAndStop;

    private double amount;

    public JourneyStop() {
        this.journey = new Journey();
        this.transitAndStop = new TransitAndStop();
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
}
