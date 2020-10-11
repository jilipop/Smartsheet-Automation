package hubsoft.smartsheet.sf.automation.configuration;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import hubsoft.smartsheet.sf.automation.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmartsheetRepositoryConfig {

    private final Constants constants;

    @Autowired
    public SmartsheetRepositoryConfig(Constants constants) {
        this.constants = constants;
    }

    @Bean
    public Smartsheet getSmartsheet() {
        return new SmartsheetBuilder()
                .setAccessToken(constants.getAccessToken())
                .build();
    }
}
