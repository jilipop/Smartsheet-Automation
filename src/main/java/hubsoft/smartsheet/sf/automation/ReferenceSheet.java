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

@Component
public class ReferenceSheet implements CommandLineRunner{

    private final Constants constants;
    private final Smartsheet smartsheet;

    public static Sheet sheet;

    @Autowired
    public ReferenceSheet(Constants constants) {
        this.constants = constants;
        smartsheet = new SmartsheetBuilder()
                .setAccessToken(constants.getAccessToken())
                .build();
    }

    @Override
    public void run(String... args){
        save();
    }

    private void save(){
        try {
            sheet = smartsheet.sheetResources().getSheet(constants.getInputSheetId(), null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
            System.out.println(sheet.getRows().size() + " Zeilen aus der Datei " + sheet.getName() + " geladen.");
        } catch (SmartsheetException e) {
            System.out.println(e.getMessage());
            System.out.println("Vorher-Zustand des Eingabe-Sheets konnte nicht gespeichert werden.");
        }
    }
}