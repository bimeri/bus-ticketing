package net.gowaka.gowaka.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/4/20 12:05 AM <br/>
 */
@Controller
@RequestMapping(value = {"/", "/ui/**"}, produces = {MediaType.TEXT_HTML_VALUE})
public class HomeController {

    @GetMapping()
    public String showHomePage() {
        return "forward:/index.html";
    }
}