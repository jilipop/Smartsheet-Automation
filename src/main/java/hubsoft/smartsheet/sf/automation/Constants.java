package hubsoft.smartsheet.sf.automation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Constants {

    @Value("${constants.access-token}")
    private String accessToken;

    @Value("${constants.shared-secret}")
    private String sharedSecret;

    @Value("${constants.ids.input-sheet}")
    private long inputSheetId;

    @Value("${constants.ids.template-folder}")
    private long templateFolderId;

    @Value("${constants.ids.timing-sheet}")
    private long timingTemplateId;

    @Value("${constants.ids.shotlist-sheet}")
    private long shotlistTemplateId;

    @Value("${constants.ids.job-number-column}")
    private long jobNumberColumnId;

    @Value("${constants.ids.label-column}")
    private long labelColumnId;

    @Value("${constants.ids.client-name-column}")
    private long clientNameColumnId;

    @Value("${constants.ids.project-name-column}")
    private long projectNameColumnId;

    @Value("${constants.ids.asp-column}")
    private long aspColumnId;

    @Value("${constants.ids.kv-column}")
    private long kvColumnId;

    @Value("${constants.ids.t-column}")
    private long tColumnId;

    @Value("${constants.ids.sl-column}")
    private long slColumnId;

    @Value("${constants.ids.maedchenfilm-workspace}")
    private long maedchenFilmWorkSpaceId;

    @Value("${constants.ids.eleven-workspace}")
    private long elevenWorkSpaceId;

    private final Map<String, Long> ids = new HashMap<>();

    public String getAccessToken() {
        return accessToken;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public long getInputSheetId() {

        return inputSheetId;
    }

    public Map<String, Long> getIds() {
        ids.put("inputSheet", inputSheetId);
        ids.put("templateFolder", templateFolderId);
        ids.put("timingTemplate", timingTemplateId);
        ids.put("shotlistTemplate", shotlistTemplateId);
        ids.put("jobNumberColumn", jobNumberColumnId);
        ids.put("labelColumn", labelColumnId);
        ids.put("clientNameColumn", clientNameColumnId);
        ids.put("projectNameColumn", projectNameColumnId);
        ids.put("aspColumn", aspColumnId);
        ids.put("kvColumn", kvColumnId);
        ids.put("tColumn", tColumnId);
        ids.put("slColumn", slColumnId);
        ids.put("maedchenFilmWorkSpace", maedchenFilmWorkSpaceId);
        ids.put("elevenWorkSpace", elevenWorkSpaceId);
        return ids;
    }
}
