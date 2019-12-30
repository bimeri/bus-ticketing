package net.gowaka.gowaka.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailContentBuilder {

    private TemplateEngine templateEngine;

    @Autowired
    public EmailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String buildWelcomeEmail(String name, String username, String loginUrl, int year) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("username", username);
        context.setVariable("login_url", loginUrl);
        context.setVariable("year", year);
        return templateEngine.process("welcome-email", context);
    }

}