package net.gowaka.gowaka.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.gowaka.gowaka.domain.model.SeatStructure;

@Data
@NoArgsConstructor
public class SeatStructureDTO {
    private Long id;
    private int numberOfSeats;
    private String image;

    public SeatStructureDTO(SeatStructure seatStructure) {
        this.id = seatStructure.getId();
        this.numberOfSeats = seatStructure.getNumberOfSeats();
        this.image = "seatstructures/" + seatStructure.getImage();
    }

}
