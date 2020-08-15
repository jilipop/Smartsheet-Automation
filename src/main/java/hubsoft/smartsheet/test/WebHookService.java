package hubsoft.smartsheet.test;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.ObjectExclusion;
import com.smartsheet.api.models.enums.SheetTemplateInclusion;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.InvalidNameException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class WebHookService {

    private static final String accessToken = "myl8v8t7nn72rurogq9teo7jne";
    private static final long inputSheetId = 8036104550016900L;

    private final long templateFolderId = 4042879761966980L;
    private final long jobNumberColumnId = 6784391581067140L;
    private final long labelColumnId = 3406691860539268L;
    private final long projectNameColumnId = 591942093432708L;

    private final long maedchenFilmWorkSpaceId = 6383315788818308L;
    private final long elevenWorkSpaceId = 6224986114418564L;

    private static final String HMAC_SHA256_ALGORITHM="HmacSHA256";
    private static final int HMAC_RADIX=16;
    private static final String sharedSecret="55o5ouq4hpqwvf4j5upny871w4";

    private static Sheet savedSheet;
    private static List<Row> newRows = new ArrayList<>();

    private static Smartsheet smartsheet = new SmartsheetBuilder()
            .setAccessToken(accessToken)
            .build();

    public static void saveSheet(){
        try {
            savedSheet = smartsheet.sheetResources().getSheet(inputSheetId, null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
        } catch (SmartsheetException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println(savedSheet.getRows().size() + " Zeilen aus der Datei " + savedSheet.getName() + " geladen.");
    }

    public void updateSheets() {
        try {
            Sheet inputSheet = smartsheet.sheetResources().getSheet(inputSheetId, null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
            System.out.println(inputSheet.getRows().size() + " Zeilen aus der Datei " + inputSheet.getName() + " geladen.");

            List<Sheet> templates = smartsheet.folderResources()
                    .getFolder(templateFolderId, null)
                    .getSheets();

            double previousLatestJobNumber = getHighestJobNumber(savedSheet);

            for (Row row: inputSheet.getRows()){
                Cell jobNumberCell = getRelevantCell(row, jobNumberColumnId);
                if (Objects.nonNull(jobNumberCell) && (double) jobNumberCell.getValue() > previousLatestJobNumber)
                    newRows.add(row);
            }

            for (Row row: newRows) {
                String jobNumber = getRelevantCell(row, jobNumberColumnId).getDisplayValue();
                String projectName = getRelevantCell(row, projectNameColumnId).getDisplayValue();
                long workspaceId;

                if (getRelevantCell(row, labelColumnId).getValue().equals("Mädchenfilm")) {
                    workspaceId = maedchenFilmWorkSpaceId;
                } else if (getRelevantCell(row, labelColumnId).getValue().equals("Eleven")){
                    workspaceId = elevenWorkSpaceId;
                } else throw new InvalidNameException("Das Label ist weder \"Mädchenfilm\" noch \"Eleven\"");

                Folder targetFolder = smartsheet.workspaceResources().folderResources()
                        .createFolder(workspaceId, new Folder().setName(jobNumber + "_" + projectName));

                for (Sheet template : templates) {
                    Sheet sheetParameters = new Sheet();
                    sheetParameters.setFromId(template.getId());
                    sheetParameters.setName(template.getName()
                            .replace("00000", jobNumber)
                            .replace("Projekte", projectName));

                    smartsheet.sheetResources().createSheetInFolderFromTemplate(
                            targetFolder.getId(),
                            sheetParameters,
                            EnumSet.of(SheetTemplateInclusion.ATTACHMENTS,
                                    SheetTemplateInclusion.CELLLINKS,
                                    SheetTemplateInclusion.DATA,
                                    SheetTemplateInclusion.DISCUSSIONS,
                                    SheetTemplateInclusion.FORMS)
                    );
                }
            }
            savedSheet = inputSheet;

        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private double getHighestJobNumber(Sheet sheet){
        double highestValue = 0d;
        for (Row row: sheet.getRows()){
            Cell jobNumberCell = getRelevantCell(row, jobNumberColumnId);
            if (jobNumberCell != null) {
                double jobNumber = (double) jobNumberCell.getValue();
                if (jobNumber > highestValue)
                    highestValue = jobNumber;
            }
        }
        return highestValue;
    }

    private Cell getRelevantCell(Row row, long id){
        return row.getCells().stream()
                .filter(cell -> id == cell.getColumnId())
                .findFirst()
                .orElse(null);
    }

    public boolean authenticateCallBack(String hmacHeader, String requestBody) {
        try{
            return hmacHeader.equals(calculateHmac(sharedSecret, requestBody));
        }
        catch (GeneralSecurityException ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }

    private String calculateHmac(String sharedSecret, String callbackBody)throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init( new SecretKeySpec(sharedSecret.getBytes(), HMAC_SHA256_ALGORITHM));

        byte[]rawHmac = mac.doFinal(callbackBody.getBytes());
        return new BigInteger(1, rawHmac).toString(HMAC_RADIX);
    }
}
