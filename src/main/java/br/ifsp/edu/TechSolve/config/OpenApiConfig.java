package br.ifsp.edu.TechSolve.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("TechSolve - API de Chamados Técnicos")
                .version("1.0.0")
                .description("Documentação automática gerada com SpringDoc OpenAPI 3"))
            .addSecurityItem(new SecurityRequirement().addList(schemeName))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(schemeName,
                    new SecurityScheme()
                        .name(schemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            );
    }
}
