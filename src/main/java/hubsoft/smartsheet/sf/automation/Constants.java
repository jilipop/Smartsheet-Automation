package hubsoft.smartsheet.sf.automation;

import hubsoft.smartsheet.sf.automation.enums.Id;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Constants {

    @Value("${constants.access-token}")
    private String accessToken;

    @Value("${constants.shared-secret-2020}")
    private String sharedSecret2020;

    @Value("${constants.ids.input-sheet-2020}")
    private long inputSheet2020Id;

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

    private final Map<Id, Long> ids = new HashMap<>();

    public String getAccessToken() {
        return accessToken;
    }

    public String getSharedSecret() {
        return sharedSecret2020;
    }

    public long getInputSheetId() {

        return inputSheet2020Id;
    }

    public Map<Id, Long> getIds() {
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
