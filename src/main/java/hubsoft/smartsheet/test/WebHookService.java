package hubsoft.smartsheet.test;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.ObjectExclusion;
import com.smartsheet.api.models.enums.SheetTemplateInclusion;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class WebHookService {
    private static final String accessToken = "e48p52d7sh91fg6os0t8b8lxf2";
    private static final long inputSheetId = 1062229786290052L;
    private static final long fileNameColumnId = 7925470170769284L;
    private final long templateFolderId = 3998895001888644L;

    private static final String HMAC_SHA256_ALGORITHM="HmacSHA256";
    private static final int HMAC_RADIX=16;
    private static final String sharedSecret="55o5ouq4hpqwvf4j5upny871w4";

    private static Sheet savedSheet;
    private static Set<String> fileNames = new HashSet<>();

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
        System.out.println("Loaded " + savedSheet.getRows().size() + " rows from sheet: " + savedSheet.getName());
    }

    public void updateSheets() {
        try {
            Sheet inputSheet = smartsheet.sheetResources().getSheet(inputSheetId, null, EnumSet.of(ObjectExclusion.NONEXISTENT_CELLS), null, null, null, null, null);
            System.out.println("Loaded " + inputSheet.getRows().size() + " rows from sheet: " + inputSheet.getName());

            List<Sheet> templates = smartsheet.folderResources()
                    .getFolder(templateFolderId, null)
                    .getSheets();

            for (Row row: inputSheet.getRows()){
                Cell fileNameCell = getFileNameCell(row);
                if (Objects.nonNull(fileNameCell))
                    fileNames.add(fileNameCell.getDisplayValue());
            }

            for (Row row: savedSheet.getRows()){
                Cell fileNameCell = getFileNameCell(row);
                if (Objects.nonNull(fileNameCell) && fileNames.contains(fileNameCell.getDisplayValue())) {
                    fileNames.remove(fileNameCell.getDisplayValue());
                }
            }

            int projectCounter = 1;
            for (String project: fileNames) {
                Folder targetFolder = smartsheet.homeResources().folderResources()
                        .createFolder(new Folder().setName(project));

                for (Sheet template : templates) {
                    Sheet sheetParameters = new Sheet();
                    sheetParameters.setFromId(template.getId());
                    sheetParameters.setName(project + projectCounter);
                    projectCounter += 1;

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
                projectCounter = 1;
            }
            savedSheet = inputSheet;

        } catch (Exception ex) {
            System.out.println("Fehler : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Cell getFileNameCell (Row row){
        return row.getCells().stream()
                .filter(cell -> fileNameColumnId == cell.getColumnId())
                .filter(cell -> StringUtils.hasText(cell.getDisplayValue()))
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
