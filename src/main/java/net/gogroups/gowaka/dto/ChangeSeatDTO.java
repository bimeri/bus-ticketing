package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChangeSeatDTO {
    @NotNull(message = "current seat number is required")
    private Integer currentSeatNumber;
    @NotNull(message = "new seat number is required")
    private Integer newSeatNumber;

    @Override
    public String toString() {
        return "{" +
                "currentSeatNumber=" + currentSeatNumber +
                ", newSeatNumber=" + newSeatNumber +
                '}';
    }
}
