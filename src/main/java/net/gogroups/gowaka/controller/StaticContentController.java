package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.constant.StaticContent;
import net.gogroups.gowaka.dto.StaticContentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/4/20 12:05 AM <br/>
 */
@RestController
@RequestMapping("/api/public")
public class StaticContentController {

    @GetMapping("/about")
    public ResponseEntity<StaticContentDTO> getAboutUs() {
        return ResponseEntity.ok(new StaticContentDTO(StaticContent.ABOUT_US));
    }
    @GetMapping("/terms_and_conditions")
    public ResponseEntity<StaticContentDTO> getTermsAndCondition() {
        return ResponseEntity.ok(new StaticContentDTO(StaticContent.TERMS_AND_CONDITION));
    }
}
