package app.owlcms.fly.flydata;

public class App implements Comparable<App> {
    public AppType appType;
    public String name;
    public boolean created;
    public String regionCode;
	private VersionInfo versionInfo;
	public boolean stopped;
	public String machine;

    public App(String s, AppType appType, String region, String version, String machine, String status) {
        this.name = s;
        this.appType = appType;
        this.regionCode = region;
        this.versionInfo = new VersionInfo(version);
        this.machine = machine;
        this.stopped = status == null ? true : status.equalsIgnoreCase("stopped");
    }

    @Override
    public int compareTo(App o) {
       return this.appType.compareTo(o.appType);
    }

    @Override
    public String toString() {
        return "App [appType=" + appType + ", name=" + name + ", regionCode=" + regionCode + ", versionInfo="
                + versionInfo + ", stopped=" + stopped + ", machine=" + machine + "]";
    }

	public String getCurrentVersion() {
		return versionInfo.getCurrentVersionString();
	}

    public String getReferenceVersion() {
		return versionInfo.getReferenceVersionString();
	}

    public boolean isUpdateRequired() {
        return versionInfo == null || versionInfo.getComparison() < 0;
    }

	public VersionInfo getVersionInfo() {
		return versionInfo;
	}

	public void setVersionInfo(VersionInfo versionInfo) {
		this.versionInfo = versionInfo;
	}

}
