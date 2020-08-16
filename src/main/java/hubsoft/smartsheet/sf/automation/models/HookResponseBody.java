package hubsoft.smartsheet.sf.automation.models;

public class HookResponseBody {
    private final String smartsheetHookResponse;

    public HookResponseBody(String smartsheetHookResponse) {
        this.smartsheetHookResponse = smartsheetHookResponse;
    }

    public String getSmartsheetHookResponse() {
        return smartsheetHookResponse;
    }
}
