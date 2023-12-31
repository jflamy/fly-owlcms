package app.owlcms.fly;

import java.util.HashMap;
import java.util.Map;

public enum AppType {
    OWLCMS("owlcms/owlcms"),
    PUBLICRESULTS("owlcms/publicresults"),
    DB("flyio/postgres-flex");

    public final String image;
    private static final Map<String, AppType> BY_IMAGE = new HashMap<>();
    static {
        for (AppType e : values()) {
            BY_IMAGE.put(e.image, e);
        }
    }

    private AppType(String image) {
        this.image = image;
    }

    public static AppType byImage(String image) {
        return BY_IMAGE.get(image);
    }
}
