package app.owlcms.fly;

public class App implements Comparable<App> {
    AppType appType;
    String name;
	public boolean created;

    public App(String s, AppType appType) {
        this.name = s;
        this.appType = appType;
    }

    @Override
    public int compareTo(App o) {
       return this.appType.compareTo(o.appType);
    }

    @Override
    public String toString() {
        return "App [appType=" + appType + ", name=" + name + "]";
    }

}
