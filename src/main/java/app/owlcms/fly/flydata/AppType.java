package app.owlcms.fly.flydata;

import java.util.HashMap;
import java.util.Map;

public enum AppType {
    OWLCMS("owlcms/owlcms","scripts/createOwlcms.sh"),
    PUBLICRESULTS("owlcms/publicresults","scripts/createPublicResults.sh"),
    TRACKER("owlcms/tracker","scripts/createTracker.sh"),
    DB("flyio/postgres-flex",null);

    public final String image;
    public final String create;
    
    private static final Map<String, AppType> BY_IMAGE = new HashMap<>();
    static {
        for (AppType e : values()) {
            BY_IMAGE.put(e.image, e);
        }
    }

    private AppType(String image, String create) {
        this.image = image;
        this.create = create;
    }

    public static AppType byImage(String image) {
        return BY_IMAGE.get(image);
    }
}
