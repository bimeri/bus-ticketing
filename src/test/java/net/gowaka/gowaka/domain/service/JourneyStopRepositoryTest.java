package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.JourneyRepository;
import net.gowaka.gowaka.domain.repository.JourneyStopRepository;
import net.gowaka.gowaka.domain.repository.TransitAndStopRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class JourneyStopRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JourneyStopRepository stopRepository;
    @Autowired
    private JourneyRepository journeyRepository;
    @Autowired
    private TransitAndStopRepository transitAndStopRepository;
    @Test
    public void whenDeleteJourneyStop_thenDelete() {
        Location location = new Location();
        location.setAddress("Mile 17 Motto Park");
        location.setCity("Buea");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        location.setCity("Kumba");
        location.setAddress("Buea Road Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location);
        transitAndStopRepository.save(transitAndStop1);
        location.setCity("Muyuka");
        location.setAddress("Muyuka Main Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location);
        transitAndStopRepository.save(transitAndStop2);
        location.setCity("Ekona");
        location.setAddress("Ekona Main Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location);
        transitAndStopRepository.save(transitAndStop3);
        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop);
        journey.setDestination(transitAndStop1);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);

        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);

        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey = journeyRepository.save(journey);

        // when
        List<JourneyStop> journeyStopSet = journey.getJourneyStops();
        assertEquals(2, journeyStopSet.size());
        journeyStopSet.forEach(
                journeyStop2 -> stopRepository.delete(journeyStop2)
        );
        JourneyStop journeyStop3 = new JourneyStop();
        journeyStop3.setTransitAndStop(transitAndStop2);
        journeyStop3.setAmount(1500);
        journeyStop3.setJourney(journey);
        JourneyStop journeyStop4 = new JourneyStop();
        journeyStop4.setTransitAndStop(transitAndStop3);
        journeyStop4.setJourney(journey);
        journeyStop4.setAmount(500);
        List<JourneyStop> journeyStopSet1 = new ArrayList<>();
        journeyStopSet1.add(journeyStop3);
        journeyStopSet1.add(journeyStop4);
        journey.setJourneyStops(journeyStopSet1);
        journey = journeyRepository.save(journey);
        List<JourneyStop> journeyStopSet2 = journey.getJourneyStops();
        assertEquals(2, journeyStopSet2.size());

        List<JourneyStop> journeyStopList = stopRepository.findAll();
        assertEquals(2, journeyStopList.size());
    }
}
