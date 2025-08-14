package orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info()
                .title("Orders & Clients API")
                .version("1.0.0")
                .description("REST API для керування клієнтами та замовленнями (Spring Boot + JPA/Hibernate)")
                .contact(new Contact().name("API Team")));
    }

}
