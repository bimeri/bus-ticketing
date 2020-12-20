package net.gogroups.gowaka.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Author: Edward Tanko <br/>
 * Date: 4/13/20 9:24 AM <br/>
 */
public class HtmlToPdfGenaratorTest {

    private HtmlToPdfGenarator htmlToPdfGenarator;

    @BeforeEach
    void setUp() throws Exception {
        htmlToPdfGenarator = new HtmlToPdfGenarator();
    }

    @Test
    void createPdf() throws Exception {
        String html = "<!DOCTYPE HTML>\n" +
                "<html>\n" +
                "<head>\n" +
                "</head>\n" +
                "<body>\n" +
                "   <p>Hello me!</p >\n" +
                "</body>\n" +
                "</html>";

        File myfile = htmlToPdfGenarator.createPdf(html, "mypdffile");
        assertThat(myfile.exists()).isTrue();
        assertThat(myfile.getAbsolutePath()).contains(".pdf");
        assertThat(myfile.getAbsolutePath()).contains("mypdffile");
    }
}
