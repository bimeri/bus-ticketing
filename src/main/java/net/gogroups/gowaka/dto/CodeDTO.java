package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class CodeDTO {
    @NotBlank(message = "code is required")
    private String code;
}
