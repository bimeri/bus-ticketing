package net.gowaka.gowaka.dto;

import lombok.Getter;
import lombok.Setter;
import net.gowaka.gowaka.domain.model.SeatStructure;

@Getter
@Setter
public class SeatStructureDTO {
    private Long id;
    private int numberOfSeats;
    private String image;

    public SeatStructureDTO() {
    }

    public SeatStructureDTO(SeatStructure seatStructure, String imageBaseUri) {
        this.id = seatStructure.getId();
        this.numberOfSeats = seatStructure.getNumberOfSeats();
        this.image = imageBaseUri.endsWith("/") ? imageBaseUri + seatStructure.getImage() :
        imageBaseUri + "/" + seatStructure.getImage();
    }

    @Override
    public String toString() {
        return "SeatStructureDTO{" +
                "id=" + id +
                ", numberOfSeats=" + numberOfSeats +
                ", image='" + image + '\'' +
                '}';
    }
}
