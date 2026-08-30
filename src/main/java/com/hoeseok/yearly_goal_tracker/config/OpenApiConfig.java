package com.hoeseok.yearly_goal_tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Yearly Goal Tracker API")
                        .description("연간 목표 트래커 백엔드 REST API 문서")
                        .version("v1.0.0"));
    }
}
