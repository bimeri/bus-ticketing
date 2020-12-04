package net.gogroups.gowaka.domain.model;

import lombok.Data;
import net.gogroups.security.accessconfig.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/17/20 8:17 AM <br/>
 */
@Data
@MappedSuperclass
public class BaseEntity {

    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected String updatedBy = "";

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        addUpdatedByUser();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        addUpdatedByUser();
    }

    void addUpdatedByUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserDetailsImpl user = (UserDetailsImpl) principal;
            this.updatedBy = user.getFullName() + "<" + user.getUsername() + ">";
        } catch (Exception ex) {
            this.updatedBy = "Default";
        }
    }
}
