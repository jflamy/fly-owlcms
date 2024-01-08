package app.owlcms.fly.flydata;

import java.util.HashMap;
import java.util.Map;

public enum AppType {
    OWLCMS("owlcms/owlcms","createOwlcms.sh"),
    PUBLICRESULTS("owlcms/publicresults","createPublicResults.sh"),
    DB("flyio/postgres-flex","");

    public final String image;
    public final String script;

    private static final Map<String, AppType> BY_IMAGE = new HashMap<>();
    static {
        for (AppType e : values()) {
            BY_IMAGE.put(e.image, e);
        }
    }

    private AppType(String image, String script) {
        this.image = image;
        this.script = script;
    }

    public static AppType byImage(String image) {
        return BY_IMAGE.get(image);
    }
}
