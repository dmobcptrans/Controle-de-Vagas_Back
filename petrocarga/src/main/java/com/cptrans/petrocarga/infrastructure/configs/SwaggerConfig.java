package com.cptrans.petrocarga.infrastructure.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;


@Configuration
public class SwaggerConfig {
    
	/**
	 * Bean para configuração da API com Swagger.
	 * 
	 * A API tem como título "PetroCarga API" e versão "1.0.0".
	 * A API tem como descricao "API para gerenciamento e reserva de vagas para estacionamento de veiculos em locais de carga e descarga na cidade de Petrópolis".
	 * A API tem como contato o email "email@email.com" e o nome "Serratec Developers".
	 * A API tem como url de contato "https://github.com/RTIC-STEM/2025_2_CPTRANS_Projeto_Nome".
	 * A API tem como licença "Apache License 2.0" com url "https://www.apache.org/licenses/LICENSE-2.0.html".
	 * A API tem como termos de servico "https://swagger.io/terms/".
	 * A API tem como requisito de seguran a autentica por token JWT, com o nome "bearerAuth".
	 */
    @Bean
    public OpenAPI CustomOpenAPI() {
		final String securitySchemeName = "bearerAuth";

		Contact contact = new Contact();
		contact.setEmail("email@email.com");
		contact.setName("Serratec Developers");
		contact.setUrl("https://github.com/RTIC-STEM/2025_2_CPTRANS_Projeto_Nome");

		License license = new License();
		license.setName("Apache License 2.0");
		license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

		Info info = new Info();
		info.setTitle("PetroCarga API");
		info.setVersion("1.0.0");
		info.setDescription("API para gerenciamento e reserva de vagas para estacionamento de veículos em locais de carga e descarga na cidade de Petrópolis.");
		info.setContact(contact);
		info.setLicense(license);
		info.setTermsOfService("https://swagger.io/terms/");
		return new OpenAPI().info(info) .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description("Insira o token JWT no formato: Bearer <token>")));
    }
}
