package net.gogroups.gowaka.domain.service.utilities;

import net.gogroups.gowaka.domain.model.Bus;
import net.gogroups.gowaka.domain.model.Car;
import net.gogroups.gowaka.domain.model.Journey;

public class CheckInCodeGenerator {
    public static String generateCode(String userId, Journey journey, Integer seat) {

        String code = "SR";
        Car car = journey.getCar();
        if (car.getIsOfficialAgencyIndicator()) {
            code = ((Bus) car).getOfficialAgency().getCode();
        }
//        VT234-234LT-9-161
        return code + journey.getId().toString() + "-" + car.getLicensePlateNumber() + "-" + seat + "-" + userId;
    }

    public static String generateCode(Journey journey, Integer seat, String tag) {
        return journey == null || journey.getId() == null ? tag + "-" + seat : tag + "-" + journey.getId().toString() + "-" + seat;
    }
}
