package example.app.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnablePdx;

/**
 * The SpringBootApacheGeodeServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(locators = "localhost[10334]")
@EnablePdx
public class SpringBootApacheGeodeServerApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(SpringBootApacheGeodeServerApplication.class).web(false).build().run(args);
	}

	@Configuration
	@EnableLocator
	@EnableManager(start = true)
	@Profile("locator-manager")
	@SuppressWarnings("unused")
	static class LocatorManagerConfiguration {
	}
}
