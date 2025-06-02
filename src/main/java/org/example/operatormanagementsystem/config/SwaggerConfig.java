package org.example.operatormanagementsystem.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration// config du an
public class SwaggerConfig {
    @Bean
    public OpenAPI custonSwagger() {
        return new OpenAPI()
                .info(new Info().description("Du AN TEST ABC").contact(new Contact().email("tranduytrung251105@gmail.com")))
                .addServersItem(new Server().url("http://localhost:8083/").description("Server local host"));
    }
}
