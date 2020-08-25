package hubsoft.smartsheet.sf.automation;

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
public class ReferenceSheet implements CommandLineRunner{

    private final Constants constants;
    private final Smartsheet smartsheet;

    private final static Map<Long, Sheet> sheets = new HashMap<>();

    @Autowired
    public ReferenceSheet(Constants constants) {
        this.constants = constants;
        smartsheet = new SmartsheetBuilder()
                .setAccessToken(constants.getAccessToken())
                .build();
    }

    @Override
    public void run(String... args){
        for (long sheetId: constants.getInputSheetIds()) {
            Sheet sheet = save(sheetId);
            sheets.put(sheetId, sheet);
        }
    }

    private Sheet save(long inputSheetId){
        try {
            Sheet sheet = smartsheet.sheetResources().getSheet(inputSheetId, null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
            System.out.println(sheet.getRows().size() + " Zeilen aus der Datei " + sheet.getName() + " geladen.");
            return sheet;
        } catch (SmartsheetException e) {
            System.out.println(e.getMessage());
            System.out.println("Vorher-Zustand des Eingabe-Sheets konnte nicht gespeichert werden.");
            return null;
        }
    }

    public static Sheet getSheet(long sheetId) {
        return sheets.get(sheetId);
    }

    public static void setSheet(long sheetId, Sheet sheet) {
        sheets.put(sheetId, sheet);
    }
}