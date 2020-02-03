package ru.rt.eip.simpletariffs.validation;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.WebApplicationInitializer;

@EnableScheduling
@SpringBootApplication
public class LcrPrefixesValidationApplication extends SpringBootServletInitializer implements WebApplicationInitializer {

    public static void main(String[] args) {
        configureApplication(new SpringApplicationBuilder()).run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return configureApplication(builder);
    }

    private static SpringApplicationBuilder configureApplication(SpringApplicationBuilder builder) {
        return builder.sources(LcrPrefixesValidationApplication.class)
                      .bannerMode(Banner.Mode.OFF);
    }

}
