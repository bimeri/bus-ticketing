package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.model.Bus;
import net.gogroups.gowaka.domain.model.Journey;
import net.gogroups.gowaka.domain.model.OfficialAgency;
import net.gogroups.gowaka.domain.model.SharedRide;
import net.gogroups.gowaka.domain.service.utilities.CheckInCodeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Java6Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CheckInCodeGeneratorTest {
    @Test
    public void generateCodeWithTag_whenJourneyNull() {
        assertThat(CheckInCodeGenerator.generateCode(null, 1, "GG"))
                .isEqualTo("GG-1");
    }
    @Test
    public void generateCodeWithTag_whenJourneyIdNull(){
        Journey journey = new Journey();
        assertThat(CheckInCodeGenerator.generateCode(journey, 1, "GO"))
                .isEqualTo("GO-1");
    }
    @Test
    public void generateCode_whenCarIsNotOfficialAgency() {
        SharedRide sharedRide = new SharedRide();
        sharedRide.setLicensePlateNumber("14klZ");
        sharedRide.setIsOfficialAgencyIndicator(false);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setCar(sharedRide);
        assertThat(CheckInCodeGenerator.generateCode("13", journey, 40))
                .isEqualTo("SR1-" + sharedRide.getLicensePlateNumber() + "-40-13");
    }
    @Test
    public void generateCode_whenCarIsOfficialAgency() {
        Bus bus = new Bus();
        bus.setLicensePlateNumber("14klZ45");
        bus.setIsOfficialAgencyIndicator(true);
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setCode("GKZ");
        bus.setOfficialAgency(officialAgency);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setCar(bus);
        assertThat(CheckInCodeGenerator.generateCode("13", journey, 40))
                .isEqualTo(officialAgency.getCode()+"1-" + bus.getLicensePlateNumber() + "-40-13");
    }
}
