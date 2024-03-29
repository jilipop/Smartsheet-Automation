package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Sheet;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Profile("!test")
public class ReferenceSheets implements CommandLineRunner{

    private final Constants constants;
    private final SmartsheetRepository repository;

    private final static Map<Long, Sheet> sheets = new HashMap<>();

    public ReferenceSheets(Constants constants, SmartsheetRepository repository) {
        this.constants = constants;
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws SmartsheetException {
        for (long sheetId: constants.getInputSheetIds()) {
            Sheet sheet = repository.getSheet(sheetId);
            sheets.put(sheetId, sheet);
        }
    }

    public static Sheet getSheet(long sheetId) {
        return sheets.get(sheetId);
    }

    public static void setSheet(long sheetId, Sheet sheet) {
        sheets.put(sheetId, sheet);
    }
}