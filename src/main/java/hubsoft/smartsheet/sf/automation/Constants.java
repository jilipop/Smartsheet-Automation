package hubsoft.smartsheet.sf.automation;

import hubsoft.smartsheet.sf.automation.enums.Id;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Constants {

    @Value("${constants.access-token}")
    private String accessToken;

    @Value("${constants.shared-secret.2020}")
    private String sharedSecret2020;

    @Value("${constants.shared-secret.2021}")
    private String sharedSecret2021;

    @Value("${constants.shared-secret.2022}")
    private String sharedSecret2022;

    @Value("${constants.shared-secret.2023}")
    private String sharedSecret2023;

    @Value("${constants.shared-secret.2024}")
    private String sharedSecret2024;

    @Value("${constants.shared-secret.2025}")
    private String sharedSecret2025;

    @Value("${constants.ids.input-sheet.2020}")
    private long inputSheetId2020;

    @Value("${constants.ids.input-sheet.2021}")
    private long inputSheetId2021;

    @Value("${constants.ids.input-sheet.2022}")
    private long inputSheetId2022;

    @Value("${constants.ids.input-sheet.2023}")
    private long inputSheetId2023;

    @Value("${constants.ids.input-sheet.2024}")
    private long inputSheetId2024;

    @Value("${constants.ids.input-sheet.2025}")
    private long inputSheetId2025;

    @Value("${constants.ids.template-folder}")
    private long templateFolderId;

    @Value("${constants.ids.timing-workspace-maedchenfilm}")
    private long timingWorkspaceMaedchenFilmId;

    @Value("${constants.ids.timing-workspace-eleven}")
    private long timingWorkspaceElevenId;

    @Value("${constants.ids.shotlist-workspace-maedchenfilm}")
    private long shotlistWorkspaceMaedchenFilmId;

    @Value("${constants.ids.shotlist-workspace-eleven}")
    private long shotlistWorkspaceElevenId;

    @Value("${constants.ids.timing-sheet}")
    private long timingTemplateId;

    @Value("${constants.ids.shotlist-sheet}")
    private long shotlistTemplateId;

    @Value("${constants.ids.maedchenfilm-workspace}")
    private long maedchenFilmWorkSpaceId;

    @Value("${constants.ids.eleven-workspace}")
    private long elevenWorkSpaceId;

    private final EnumMap<Id, Long> ids = new EnumMap<>(Id.class);
    private final Set<Long> inputSheetIds = new HashSet<>();
    private final Map<Long, String> sharedSecrets = new HashMap<>();

    public String getAccessToken() {
        return accessToken;
    }

    public Map<Long, String> getSharedSecrets() {
        if (sharedSecrets.size() == 0){
            sharedSecrets.put(inputSheetId2020, sharedSecret2020);
            sharedSecrets.put(inputSheetId2021, sharedSecret2021);
            sharedSecrets.put(inputSheetId2022, sharedSecret2022);
            sharedSecrets.put(inputSheetId2023, sharedSecret2023);
            sharedSecrets.put(inputSheetId2024, sharedSecret2024);
            sharedSecrets.put(inputSheetId2025, sharedSecret2025);
        }
        return sharedSecrets;
    }

    public Set<Long> getInputSheetIds() {
        if (inputSheetIds.size() == 0) {
            inputSheetIds.add(inputSheetId2020);
            inputSheetIds.add(inputSheetId2021);
            inputSheetIds.add(inputSheetId2022);
            inputSheetIds.add(inputSheetId2023);
            inputSheetIds.add(inputSheetId2024);
            inputSheetIds.add(inputSheetId2025);
        }
        return inputSheetIds;
    }

    public EnumMap<Id, Long> getIds() {
       if (ids.size() == 0) {
           ids.put(Id.TEMPLATE_FOLDER, templateFolderId);
           ids.put(Id.TIMING_WORKSPACE_MF, timingWorkspaceMaedchenFilmId);
           ids.put(Id.TIMING_WORKSPACE_ELEVEN, timingWorkspaceElevenId);
           ids.put(Id.SHOTLIST_WORKSPACE_MF, shotlistWorkspaceMaedchenFilmId);
           ids.put(Id.SHOTLIST_WORKSPACE_ELEVEN, shotlistWorkspaceElevenId);
           ids.put(Id.TIMING_TEMPLATE, timingTemplateId);
           ids.put(Id.SHOTLIST_TEMPLATE, shotlistTemplateId);
           ids.put(Id.MF_WORKSPACE, maedchenFilmWorkSpaceId);
           ids.put(Id.ELEVEN_WORKSPACE, elevenWorkSpaceId);
       }
        return ids;
    }
}
