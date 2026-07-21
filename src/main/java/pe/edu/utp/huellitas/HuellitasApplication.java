package pe.edu.utp.huellitas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "offsetDateTimeProvider")
public class HuellitasApplication {

	public static void main(String[] args) {
		SpringApplication.run(HuellitasApplication.class, args);
	}

	@Bean(name = "offsetDateTimeProvider")
	public DateTimeProvider offsetDateTimeProvider() {
		return () -> Optional.of(OffsetDateTime.now());
	}
}
