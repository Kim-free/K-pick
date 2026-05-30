package com.example.kpick.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI kpickOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("K-PICK API")
                        .description("K-PICK 서비스 API 명세서")
                        .version("v1"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local server")
                ));
    }
}
