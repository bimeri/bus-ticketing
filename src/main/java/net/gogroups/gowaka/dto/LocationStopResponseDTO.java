package net.gogroups.gowaka.dto;

/**
 * @author nouks
 *
 * @date 28 Oct 2019
 */
public class LocationStopResponseDTO extends LocationResponseDTO {
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
