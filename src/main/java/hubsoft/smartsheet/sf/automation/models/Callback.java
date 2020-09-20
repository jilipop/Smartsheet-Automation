package hubsoft.smartsheet.sf.automation.models;

import java.util.List;

public class Callback {

    private String nonce;
    private String timestamp;
    private Long webhookId;
    private String scope;
    private Long scopeObjectId;
    private List<Event> events;
    public String newWebhookStatus;

    public Callback() {
    }

    public Callback(Long scopeObjectId, List<Event> events) {
        this.scopeObjectId = scopeObjectId;
        this.events = events;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(Long webhookId) {
        this.webhookId = webhookId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Long getScopeObjectId() {
        return scopeObjectId;
    }

    public void setScopeObjectId(Long scopeObjectId) {
        this.scopeObjectId = scopeObjectId;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public String getNewWebhookStatus() {
        return newWebhookStatus;
    }

    public void setNewWebhookStatus(String newWebhookStatus) {
        this.newWebhookStatus = newWebhookStatus;
    }
}
