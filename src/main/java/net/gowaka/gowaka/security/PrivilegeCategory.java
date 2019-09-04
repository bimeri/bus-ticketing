package net.gowaka.gowaka.security;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PrivilegeCategory{
    private Long id;
    private String name;
}
