package net.gogroups.gowaka.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.gogroups.gowaka.domain.model.SeatStructure;

@Data
@NoArgsConstructor
public class SeatStructureDTO {
    private Long id;
    private int numberOfSeats;
    private String image;

    public SeatStructureDTO(SeatStructure seatStructure) {
        if (seatStructure != null) {
            this.id = seatStructure.getId();
            this.numberOfSeats = seatStructure.getNumberOfSeats();
            this.image = "seatstructures/" + seatStructure.getImage();
        }
    }

}
