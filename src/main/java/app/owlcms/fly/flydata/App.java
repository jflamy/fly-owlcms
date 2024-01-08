package app.owlcms.fly.flydata;

public class App implements Comparable<App> {
    public AppType appType;
    public String name;
    public boolean created;
    public String regionCode;
    public String version;

    public App(String s, AppType appType, String region, String version) {
        this.name = s;
        this.appType = appType;
        this.regionCode = region;
        this.version = version;
    }

    @Override
    public int compareTo(App o) {
       return this.appType.compareTo(o.appType);
    }

    @Override
    public String toString() {
        return "App [appType=" + appType + ", name=" + name + ", regionCode=" + regionCode + "]";
    }

}
