package nyu.adb.Sites;

public enum SiteStatus {
    UP("up"),
    DOWN("down"),
    IN_RECOVERY("in_recovery");

    private final String value;

    SiteStatus(String value) {
        this.value = value;
    }

    SiteStatus getSiteStatus(String value) {
        return SiteStatus.valueOf(value);
    }
}
