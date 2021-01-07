package net.gogroups.gowaka.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChangeSeatResponse {
    private List<ChangeSeatDTO> success;
    private List<ChangeSeatDTO> failure;

    public ChangeSeatResponse() {
        this.success = new ArrayList<>();
        this.failure = new ArrayList<>();
    }

    public ChangeSeatResponse(List<ChangeSeatDTO> success) {
        if (success == null) {
            this.success = new ArrayList<>();
        } else {
            this.success = success;
        }
        this.failure = new ArrayList<>();
    }

    public ChangeSeatResponse(List<ChangeSeatDTO> success, List<ChangeSeatDTO> failure) {
        if (success == null) {
            this.success = new ArrayList<>();
        } else {
            this.success = success;
        }
        if (failure == null) {
            this.failure = new ArrayList<>();
        } else {
            this.failure = failure;
        }
    }
}
