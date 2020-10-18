package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.DestinationType;
import com.smartsheet.api.models.enums.FolderCopyInclusion;
import com.smartsheet.api.models.enums.ObjectExclusion;
import com.smartsheet.api.models.enums.SheetTemplateInclusion;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class SmartsheetRepository {

    private final Smartsheet smartsheet;

    public SmartsheetRepository(Smartsheet smartsheet) {
        this.smartsheet = smartsheet;
    }

    public Sheet getSheet(long sheetId) throws SmartsheetException {
        Sheet sheet = smartsheet.sheetResources().getSheet(sheetId,
                null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS),
                null, null, null,
                null, null);
        System.out.println(sheet.getTotalRowCount() + " Zeilen aus der Datei " + sheet.getName() + " geladen.");
        return sheet;
    }

    public Folder copyFolderToWorkspace(long folderId, long workspaceId, String name) throws SmartsheetException {
        ContainerDestination destination = new ContainerDestination()
                .setDestinationType(DestinationType.WORKSPACE)
                .setDestinationId(workspaceId)
                .setNewName(name);

        return smartsheet.folderResources().copyFolder(
                folderId,
                destination,
                EnumSet.of(FolderCopyInclusion.ATTACHMENTS,
                        FolderCopyInclusion.CELLLINKS,
                        FolderCopyInclusion.DATA,
                        FolderCopyInclusion.DISCUSSIONS,
                        FolderCopyInclusion.FILTERS,
                        FolderCopyInclusion.FORMS,
                        FolderCopyInclusion.RULERECIPIENTS,
                        FolderCopyInclusion.RULES,
                        FolderCopyInclusion.SHARES),
                null
        );
    }

    public List<Sheet> renameSheets(long targetFolderId, String combinedName) throws SmartsheetException {
        List<Sheet> templateSheets = smartsheet.folderResources()
                .getFolder(targetFolderId, null)
                .getSheets();

        for (Sheet template: templateSheets) {
            Sheet sheetParameters = new Sheet();
            sheetParameters
                    .setName(template.getName()
                            .replace("00000_Projekte", combinedName)
                            .replace("00000_Projekt", combinedName)
                            .replace("XXXXX_Projekte", combinedName)
                            .replace("XXXXX_Projekt", combinedName)
                    )
                    .setId(template.getId());

            smartsheet.sheetResources().updateSheet(sheetParameters);
        }
        return templateSheets;
    }

    public Sheet copySheetToWorkspace(long sheetId, long targetWorkspaceId, String combinedName, String nameAppendix) throws SmartsheetException {
        Sheet sheet = new Sheet();
        sheet.setFromId(sheetId);
        sheet.setName(combinedName + "_" + nameAppendix);

        return smartsheet.sheetResources().createSheetInWorkspaceFromTemplate(
                targetWorkspaceId,
                sheet,
                EnumSet.of(
                        SheetTemplateInclusion.ATTACHMENTS,
                        SheetTemplateInclusion.CELLLINKS,
                        SheetTemplateInclusion.DATA,
                        SheetTemplateInclusion.DISCUSSIONS,
                        SheetTemplateInclusion.FORMS
                )
        );
    }
    public Sheet loadSheetWithRelevantRows(Sheet sheetToGet, Set<Integer> rowsToRead) throws SmartsheetException {
        return smartsheet.sheetResources().getSheet(
                sheetToGet.getId(),
                null,
                null,
                null,
                rowsToRead,
                null,
                null,
                null
        );
    }

    public void insertRowsIntoSheet(Sheet sheetToUpdate, List<Row> rows){
        try {
            smartsheet.sheetResources().rowResources().updateRows(
                    sheetToUpdate.getId(),
                    rows
            );
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("Die Daten konnten nicht in die Tabelle eingetragen werden.");
        }
    }
}
