package hubsoft.smartsheet.sf.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	static {
		System.setProperty("Smartsheet.trace.parts", "RequestBodySummary,ResponseBodySummary");
		System.setProperty("Smartsheet.trace.pretty", "true");
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
