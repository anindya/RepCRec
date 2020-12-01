package nyu.adb.Sites;

import lombok.Getter;

import java.util.Arrays;

/**
 * Site status supported by the system
 * Sites only know if they are up or in recovery.
 * Sites will not know if they are down, siteManger will only hold the DOWN status.
 */
public enum SiteStatus {
    UP("up"),
    DOWN("down"),
    IN_RECOVERY("in_recovery");

    @Getter
    private final String value;

    SiteStatus(String value) {
        this.value = value;
    }

    SiteStatus getSiteStatus(String value) {
        return Arrays.stream(SiteStatus.class.getEnumConstants())
                .filter(e -> e.getValue().equals(value)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
