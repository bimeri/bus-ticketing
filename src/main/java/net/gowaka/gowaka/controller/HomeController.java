package net.gowaka.gowaka.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/"}, produces = MediaType.TEXT_HTML_VALUE)
public class HomeController {
       @GetMapping
       public String home(Model model) {
              return "forward:/index.html";
       }
}