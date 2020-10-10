package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.SheetResources;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Sheet;
import com.smartsheet.api.models.enums.ObjectExclusion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReferenceSheets implements CommandLineRunner{

    private final Constants constants;
    private final Smartsheet smartsheet;

    private final static Map<Long, Sheet> sheets = new HashMap<>();

    @Autowired
    public ReferenceSheets(Constants constants, Smartsheet smartsheet) {
        this.constants = constants;
        this.smartsheet = smartsheet;
    }

    @Override
    public void run(String... args) throws SmartsheetException {
        for (long sheetId: constants.getInputSheetIds()) {
            Sheet sheet = save(sheetId);
            sheets.put(sheetId, sheet);
        }
    }

    private Sheet save(long inputSheetId) throws SmartsheetException {
            SheetResources sheetResources = smartsheet.sheetResources();
            Sheet sheet = sheetResources.getSheet(inputSheetId,
                    null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS),
                    null, null, null,
                    null, null);
            System.out.println(sheet.getTotalRowCount() + " Zeilen aus der Datei " + sheet.getName() + " geladen.");
            return sheet;
    }

    public static Sheet getSheet(long sheetId) {
        return sheets.get(sheetId);
    }

    public static void setSheet(long sheetId, Sheet sheet) {
        sheets.put(sheetId, sheet);
    }
}