package app.owlcms.fly;

import java.util.HashMap;
import java.util.Map;

public enum AppType {
    OWLCMS("owlcms/owlcms"),
    PUBLICRESULTS("owlcms/publicresults"),
    DB("flyio/postgres-flex")
    ;

    public final String image;

    private AppType(String image) {
        this.image = image;
    }

        // ... enum values

    private static final Map<String, AppType> BY_IMAGE = new HashMap<>();
    
    static {
        for (AppType e: values()) {
            System.err.println("map "+e.image+" "+e.name());
            BY_IMAGE.put(e.image, e);
        }
        System.err.println("check "+BY_IMAGE.get(OWLCMS.image));
    }

   // ... fields, constructor, methods

    public static AppType byImage(String image) {
        System.err.println("getting "+image+" "+BY_IMAGE.get(image));
        return BY_IMAGE.get(image);
    }
}
