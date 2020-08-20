package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.DestinationType;
import com.smartsheet.api.models.enums.FolderCopyInclusion;
import com.smartsheet.api.models.enums.ObjectExclusion;
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

    private final Map<String, Long> ids;
    private final Smartsheet smartsheet;

    @Autowired
    public WebHookService(Constants constants) {
        ids = constants.getIds();
        smartsheet = new SmartsheetBuilder()
                .setAccessToken(constants.getAccessToken())
                .build();
    }

    public void processTemplates() {
        try {
            Sheet inputSheet = smartsheet.sheetResources().getSheet(ids.get("inputSheet"), null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
            System.out.println(inputSheet.getRows().size() + " Zeilen aus der Datei " + inputSheet.getName() + " geladen.");

            List<Row> newRows = checkForNewRows(inputSheet);

            for (Row row: newRows) {
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

                String combinedName = combineName(clientName, projectName);

                Folder targetFolder = copyFolder(row, jobNumber, combinedName);
                List<Sheet> targetSheets = renameSheets(targetFolder, jobNumber, combinedName);

                Sheet sheetToUpdate = targetSheets.stream()
                        .filter(sheet -> sheet.getName().contains("Finanzen"))
                        .findFirst()
                        .orElse(null);

                insertDataIntoFirstRow(sheetToUpdate, Map.of("Position", projectName, "Empfänger", asp));
            }

            ReferenceSheet.setSheet(inputSheet);

        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            ex.printStackTrace();
            System.out.println("Die Verarbeitung der neuen Projekteinträge ist gescheitert.");
        }
    }

    private List<Row> checkForNewRows(Sheet inputSheet) {
        List<Row> newRows = new ArrayList<>();
        try {
            double previousLatestJobNumber = getHighestJobNumber(ReferenceSheet.getSheet());

            for (Row row : inputSheet.getRows()) {
                Cell jobNumberCell = getCellByColumnId(row, ids.get("jobNumberColumn"));
                if (Objects.nonNull(jobNumberCell)
                        && (double) jobNumberCell.getValue() > previousLatestJobNumber)
                    newRows.add(row);
            }
        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            System.out.println("Die Prüfung auf neue Projekteinträge ist gescheitert.");
        }
        return newRows;
    }

    private double getHighestJobNumber(Sheet sheet){
        double highestValue = 0d;
        for (Row row: sheet.getRows()){
            Cell jobNumberCell = getCellByColumnId(row, ids.get("jobNumberColumn"));
            if (jobNumberCell != null) {
                double jobNumber = (double) jobNumberCell.getValue();
                if (jobNumber > highestValue)
                    highestValue = jobNumber;
            }
        }
        return highestValue;
    }

    private Cell getCellByColumnId(Row row, long id){
        return row.getCells().stream()
                .filter(cell -> id == cell.getColumnId())
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

    String combineName(String clientName, String projectName){
        final int maxFileNameLengthImposedBySmartsheet = 50;
        final int longestTemplateNameFixedChars = 23; //inklusive "_" zwischen Kundenname und Projektname
        final int remainingLength = maxFileNameLengthImposedBySmartsheet - longestTemplateNameFixedChars;
        final int minimumProjectNameLength = 4;

        if (clientName.length() > remainingLength - minimumProjectNameLength)
            clientName = clientName.substring(0, remainingLength - minimumProjectNameLength -1);

        if (clientName.length() + projectName.length() > remainingLength)
            projectName = projectName.substring(0, remainingLength - clientName.length() -1);

        return clientName + "_" + projectName;
    }

    private Folder copyFolder(Row row, String jobNumber, String combinedName) throws InvalidNameException, SmartsheetException {
        ContainerDestination destination = new ContainerDestination()
                .setDestinationType(DestinationType.WORKSPACE)
                .setDestinationId(getTargetWorkSpaceId(row))
                .setNewName(jobNumber + "_" + combinedName);

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

    private Long getTargetWorkSpaceId(Row row) throws InvalidNameException {
        Cell labelCell = getCellByColumnId(row, ids.get("labelColumnId"));

        if (labelCell != null && labelCell.getValue().equals("Mädchenfilm")) {
            return ids.get("maedchenFilmWorkSpace");
        } else if (labelCell != null && labelCell.getValue().equals("Eleven")){
            return ids.get("elevenWorkSpace");
        } else throw new InvalidNameException("Breche ab, denn das Label ist weder \"Mädchenfilm\" noch \"Eleven\".");
    }

    private List<Sheet> renameSheets(Folder targetFolder, String jobNumber, String combinedName) throws SmartsheetException {
        List<Sheet> templateSheets = smartsheet.folderResources()
                .getFolder(targetFolder.getId(), null)
                .getSheets();

        for (Sheet template: templateSheets) {
            Sheet sheetParameters = new Sheet();
            sheetParameters
                    .setName(template.getName()
                            .replace("00000", jobNumber)
                            .replace("Projekte", combinedName)
                    )
                    .setId(template.getId());

            smartsheet.sheetResources().updateSheet(sheetParameters);
        }
        return templateSheets;
    }

    public void insertDataIntoFirstRow(Sheet sheetToUpdate, Map<String, String> cellData) {
        try {
            if (sheetToUpdate != null) {
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
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("Projektname und Empfänger konnten nicht eingetragen werden.");
        }
    }

    public boolean authenticateCallBack(String hmacHeader, String requestBody) {
   /*     try{
            return hmacHeader.equals(calculateHmac(constants.getSharedSecret(), requestBody));
        }
        catch (GeneralSecurityException ex){
            System.out.println(ex.getMessage());
            return false;
        }*/
        return true;
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