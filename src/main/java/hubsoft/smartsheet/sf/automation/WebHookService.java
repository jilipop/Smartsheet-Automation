package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.DestinationType;
import com.smartsheet.api.models.enums.FolderCopyInclusion;
import com.smartsheet.api.models.enums.ObjectExclusion;
import com.smartsheet.api.models.enums.SheetTemplateInclusion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.InvalidNameException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.util.*;

@Service
public class WebHookService {

    private final Constants constants;
    private final Map<String, Long> ids;
    private final Smartsheet smartsheet;

    @Autowired
    public WebHookService(Constants constants) {
        this.constants = constants;
        ids = constants.getIds();
        smartsheet = new SmartsheetBuilder()
                .setAccessToken(constants.getAccessToken())
                .build();
    }

    public void processTemplates() {
        try {
            Sheet inputSheet = smartsheet.sheetResources().getSheet(ids.get("inputSheet"), null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
            System.out.println(inputSheet.getRows().size() + " Zeilen aus der Datei " + inputSheet.getName() + " geladen.");

            Set<Row> rowsToProcess = checkForRowsToProcess(inputSheet);

            for (Row row: rowsToProcess) {
                Cell jobNumberCell = getCellByColumnId(row, ids.get("jobNumberColumn"));
                String jobNumber = checkAndGetCellContent(jobNumberCell, "Job-Nr.");

                Cell clientNameCell = getCellByColumnId(row, ids.get("clientNameColumn"));
                String clientName = checkAndGetCellContent(clientNameCell, "Kundenname");

                Cell projectNameCell = getCellByColumnId(row, ids.get("projectNameColumn"));
                String projectName = checkAndGetCellContent(projectNameCell, "Projektname");

                Cell aspCell = getCellByColumnId(row, ids.get("aspColumn"));
                String asp = "";
                if (aspCell != null && StringUtils.hasText(aspCell.getDisplayValue()))
                    asp = aspCell.getDisplayValue();

                String combinedName = combineName(jobNumber, clientName, projectName);

                long targetFolderId;
                if (newCheckmark(row, "kvColumn")) {
                    targetFolderId = copyFolder(row, combinedName).getId();
                } else {
                    Long id = getFolderIdIfExists(getTargetWorkSpaceId(row), jobNumber);
                    if (id == null) {
                        Folder folderParameters = new Folder();
                        folderParameters.setName(combinedName);

                        targetFolderId = smartsheet.workspaceResources().folderResources()
                                .createFolder(getTargetWorkSpaceId(row), folderParameters)
                                .getId();
                    } else {
                        targetFolderId = id;
                    }
                }

                if (newCheckmark(row, "tColumn"))
                    copySheetToFolder(ids.get("timingTemplate"), targetFolderId, combinedName, "Timing");
                if (newCheckmark(row, "slColumn"))
                    copySheetToFolder(ids.get("shotlistTemplate"), targetFolderId, combinedName, "Shotlist");

                List<Sheet> targetSheets = renameSheets(targetFolderId, combinedName);

                Sheet sheetToUpdate = targetSheets.stream()
                        .filter(sheet -> sheet.getName().contains("Finanzen"))
                        .findFirst()
                        .orElse(null);

                if (sheetToUpdate != null)
                    insertDataIntoFirstRow(sheetToUpdate, Map.of("Position", projectName, "Empfänger", asp));
            }

            ReferenceSheet.setSheet(inputSheet);
            System.out.println("Aktualisierung abgeschlossen.");

        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            ex.printStackTrace();
            System.out.println("Die Verarbeitung der neuen Projekteinträge ist gescheitert.");
        }
    }

    private Set<Row> checkForRowsToProcess(Sheet inputSheet) {
        Set<Row> rowsToProcess = new HashSet<>();
        try {
            for (Row row : inputSheet.getRows()) {
                if (newCheckmark(row, "kvColumn") || newCheckmark(row, "tColumn") || newCheckmark(row, "slColumn")){
                    rowsToProcess.add(row);
                }
            }
        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            ex.printStackTrace();
            System.out.println("Die Prüfung auf neue Projekteinträge ist gescheitert.");
        }
        return rowsToProcess;
    }

    private boolean newCheckmark(Row row, String columnKey) {
        Cell cell = getCellByColumnId(row, ids.get(columnKey));
        if (Objects.nonNull(cell) && cell.getValue().equals(true)) {
            cell = getCellByColumnId(sameRowInReferenceSheet(row), ids.get(columnKey));
            return !(Objects.nonNull(cell) && cell.getValue().equals(true));
        }
        return false;
    }

    private Cell getCellByColumnId(Row row, long id){
        if (row != null)
            return row.getCells().stream()
                .filter(cell -> id == cell.getColumnId())
                .findFirst()
                .orElse(null);
        else return null;
    }

    private Row sameRowInReferenceSheet(Row row){
        return ReferenceSheet.getSheet().getRows().stream()
                .filter(oldRow -> oldRow.getId().equals(row.getId()))
                .findFirst()
                .orElse(null);
    }

    private Cell getCellByColumnTitle(Sheet sheet, Row row, String name){
        Column column = sheet.getColumns().stream()
                .filter(col -> name.equals(col.getTitle()))
                .findFirst()
                .orElse(null);

        if (column == null)
            return null;
        else
            return getCellByColumnId(row, column.getId());
    }

    private String checkAndGetCellContent(Cell cell, String description){
        if (cell == null || !StringUtils.hasText(cell.getDisplayValue()))
            throw new InvalidParameterException("Breche ab, weil folgender Eintrag in der Tabelle fehlt: " + description);
        else
            return cell.getDisplayValue();
    }

    private String combineName(String jobNumber, String clientName, String projectName){
        final int maxFileNameLengthImposedBySmartsheet = 50;
        final int longestTemplateNameFixedChars = 23; //inklusive "_" zwischen Kundenname und Projektname
        final int remainingLength = maxFileNameLengthImposedBySmartsheet - longestTemplateNameFixedChars;
        final int minimumProjectNameLength = 4;

        if (clientName.length() > remainingLength - minimumProjectNameLength)
            clientName = clientName.substring(0, remainingLength - minimumProjectNameLength -1);

        if (clientName.length() + projectName.length() > remainingLength)
            projectName = projectName.substring(0, remainingLength - clientName.length() -1);

        return jobNumber + "_" + clientName + "_" + projectName;
    }

    private Long getFolderIdIfExists(long workSpaceId, String jobNumber) throws SmartsheetException {
        List<Folder> folders = smartsheet.workspaceResources().getWorkspace(
                workSpaceId,
                null,
                null)
                .getFolders();

        Folder targetFolder = folders.stream()
                .filter(folder -> folder.getName().contains(jobNumber))
                .findFirst()
                .orElse(null);

        if (targetFolder == null)
            return null;
        else
            return targetFolder.getId();
    }

    private Folder copyFolder(Row row, String combinedName) throws InvalidNameException, SmartsheetException {
        ContainerDestination destination = new ContainerDestination()
                .setDestinationType(DestinationType.WORKSPACE)
                .setDestinationId(getTargetWorkSpaceId(row))
                .setNewName(combinedName);

        return smartsheet.folderResources().copyFolder(
                ids.get("templateFolder"),
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

    private long getTargetWorkSpaceId(Row row) throws InvalidNameException {
        Cell labelCell = getCellByColumnId(row, ids.get("labelColumn"));

        if (labelCell != null && labelCell.getValue().equals("Mädchenfilm")) {
            return ids.get("maedchenFilmWorkSpace");
        } else if (labelCell != null && labelCell.getValue().equals("Eleven")){
            return ids.get("elevenWorkSpace");
        } else throw new InvalidNameException("Breche ab, denn das Label ist weder \"Mädchenfilm\" noch \"Eleven\".");
    }

    private List<Sheet> renameSheets(long targetFolderId, String combinedName) throws SmartsheetException {
        List<Sheet> templateSheets = smartsheet.folderResources()
                .getFolder(targetFolderId, null)
                .getSheets();

        for (Sheet template: templateSheets) {
            Sheet sheetParameters = new Sheet();
            sheetParameters
                    .setName(template.getName()
                            .replace("00000_Projekte", combinedName)
                    )
                    .setId(template.getId());

            smartsheet.sheetResources().updateSheet(sheetParameters);
        }
        return templateSheets;
    }

    private void insertDataIntoFirstRow(Sheet sheetToUpdate, Map<String, String> cellData) {
        try {
            sheetToUpdate = smartsheet.sheetResources().getSheet(
                    sheetToUpdate.getId(),
                    null,
                    null,
                    null,
                    Set.of(1),
                    null,
                    null,
                    null
            );
            Row rowToUpdate = sheetToUpdate.getRows().get(0);
            List<Cell> cellsToUpdate = new ArrayList<>();

            Sheet finalSheetToUpdate = sheetToUpdate;
            cellData.forEach((columnTitle, value) -> {
                Cell targetCell = getCellByColumnTitle(finalSheetToUpdate, rowToUpdate, columnTitle);
                if (targetCell != null) {
                    targetCell.setValue(value);
                    cellsToUpdate.add(targetCell);
                }
            });
            Row newRow = new Row();
            newRow.setId(rowToUpdate.getId());
            newRow.setCells(cellsToUpdate);

            smartsheet.sheetResources().rowResources().updateRows(
                    finalSheetToUpdate.getId(),
                    List.of(newRow)
            );
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("Projektname und Empfänger konnten nicht eingetragen werden.");
        }
    }

    private void copySheetToFolder(long sheetId, long targetFolderId, String combinedName, String nameAppendix) throws SmartsheetException {
        Sheet sheet = new Sheet();
        sheet.setFromId(sheetId);
        sheet.setName(combinedName + "_" + nameAppendix);

        smartsheet.sheetResources().createSheetInFolderFromTemplate(
                targetFolderId,
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

    public boolean authenticateCallBack(String hmacHeader, String requestBody) {
        try {
            return hmacHeader.equals(calculateHmac(constants.getSharedSecret(), requestBody));
        } catch (GeneralSecurityException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    private String calculateHmac(String sharedSecret, String callbackBody) throws GeneralSecurityException {
        String HMAC_SHA256_ALGORITHM = "HmacSHA256";
        int HMAC_RADIX = 16;

        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init( new SecretKeySpec(sharedSecret.getBytes(), HMAC_SHA256_ALGORITHM));

        byte[]rawHmac = mac.doFinal(callbackBody.getBytes());
        return new BigInteger(1, rawHmac).toString(HMAC_RADIX);
    }
}