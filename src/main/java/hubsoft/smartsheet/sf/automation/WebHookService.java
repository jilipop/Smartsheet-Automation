package hubsoft.smartsheet.sf.automation;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.models.*;
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
    private final Smartsheet smartsheet;

    @Autowired
    public WebHookService(Constants constants) {
        this.constants = constants;
         smartsheet = new SmartsheetBuilder()
                .setAccessToken(constants.getAccessToken())
                .build();
    }

    public void updateSheets() {
        try {
            Sheet inputSheet = smartsheet.sheetResources().getSheet(constants.getInputSheetId(), null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
            System.out.println(inputSheet.getRows().size() + " Zeilen aus der Datei " + inputSheet.getName() + " geladen.");

            List<Row> newRows = checkForNewRows(inputSheet);
            List<Sheet> templates = Collections.emptyList();

            if (newRows.size() > 0) {
            templates = smartsheet.folderResources()
                    .getFolder(constants.getTemplateFolderId(), null)
                    .getSheets();
            }

            for (Row row: newRows) {
                String jobNumber = getRelevantCell(row, constants.getJobNumberColumnId()).getDisplayValue();
                String projectName = getRelevantCell(row, constants.getProjectNameColumnId()).getDisplayValue();

                if (!StringUtils.hasText(projectName))
                    throw new InvalidParameterException("Breche ab, weil kein Projektname eingegeben wurde.");

                long workspaceId;
                if (getRelevantCell(row, constants.getLabelColumnId()).getValue().equals("Mädchenfilm")) {
                    workspaceId = constants.getMaedchenFilmWorkSpaceId();
                } else if (getRelevantCell(row, constants.getLabelColumnId()).getValue().equals("Eleven")){
                    workspaceId = constants.getElevenWorkSpaceId();
                } else throw new InvalidNameException("Breche ab, denn das Label ist weder \"Mädchenfilm\" noch \"Eleven\".");

                Folder targetFolder = smartsheet.workspaceResources().folderResources()
                        .createFolder(workspaceId, new Folder().setName(jobNumber + "_" + projectName));

                for (Sheet template: templates) {
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
            ReferenceSheet.sheet = inputSheet;

        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            System.out.println("Die Verarbeitung der neuen Projekteinträge ist gescheitert.");
            ex.printStackTrace();
        }
    }

    private List<Row> checkForNewRows(Sheet inputSheet) {
        List<Row> newRows = new ArrayList<>();
        try {
            double previousLatestJobNumber = getHighestJobNumber(ReferenceSheet.sheet);

            for (Row row : inputSheet.getRows()) {
                Cell jobNumberCell = getRelevantCell(row, constants.getJobNumberColumnId());
                if (Objects.nonNull(jobNumberCell) && (double) jobNumberCell.getValue() > previousLatestJobNumber)
                    newRows.add(row);
            }
        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            System.out.println("Die Prüfung auf neue Projekteinträge ist gescheitert.");
            ex.printStackTrace();
        }
        return newRows;
    }

    private double getHighestJobNumber(Sheet sheet){
        double highestValue = 0d;
        for (Row row: sheet.getRows()){
            Cell jobNumberCell = getRelevantCell(row, constants.getJobNumberColumnId());
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
            return hmacHeader.equals(calculateHmac(constants.getSharedSecret(), requestBody));
        }
        catch (GeneralSecurityException ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }

    private String calculateHmac(String sharedSecret, String callbackBody)throws GeneralSecurityException {
        String HMAC_SHA256_ALGORITHM = "HmacSHA256";
        int HMAC_RADIX = 16;

        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init( new SecretKeySpec(sharedSecret.getBytes(), HMAC_SHA256_ALGORITHM));

        byte[]rawHmac = mac.doFinal(callbackBody.getBytes());
        return new BigInteger(1, rawHmac).toString(HMAC_RADIX);
    }
}