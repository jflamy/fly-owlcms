package app.owlcms.fly.flydata;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class VersionInfo {
	private String referenceVersionString;
	private String currentVersionString;
	Logger logger = (Logger) LoggerFactory.getLogger(VersionInfo.class);
	private Integer comparison;

	VersionInfo(String currentVersionString) {
		this.currentVersionString = currentVersionString;
		this.updateReferenceVersionString();
	}
	
	public void updateReferenceVersionString(boolean preRelease) {
		String suffix = preRelease ? "-prerelease" : "";
		String str = "https://raw.githubusercontent.com/owlcms/owlcms4" + suffix + "/master/version.txt";
		HttpRequest request1 = HttpRequest.newBuilder(URI.create(str)).build();
		HttpClient client1 = HttpClient.newHttpClient();
		CompletableFuture<HttpResponse<String>> future = client1.sendAsync(request1, BodyHandlers.ofString());
		try {
			future
			        .orTimeout(5, TimeUnit.SECONDS)
			        .whenComplete((response, exception) -> {
				        if (exception != null) {
					        return;
				        }
				        this.referenceVersionString = response.body();
				        if (!"latest".equals("currentVersionString")) {
							// the build process for the container sets a maven-style version as the tag
							// we want to pull the explicit version
					        ComparableVersion currentVersion = new ComparableVersion(this.currentVersionString);
					        ComparableVersion referenceVersion = new ComparableVersion(this.referenceVersionString);
					        this.comparison = currentVersion.compareTo(referenceVersion);
				        } else {
					        // the update will replace "latest" with the current reference version tag.
					        this.comparison = -1;
				        }

			        })
			        .join();
		} catch (Throwable e) {
			logger.error("version fetch timed out");
			// do an update with "latest" as a defensive measure
			this.comparison = 0;
			this.referenceVersionString = "unknown";
			return;
		}
	}

	public void updateReferenceVersionString() {
		updateReferenceVersionString(this.currentVersionString.contains("-"));
	}

	public String getReferenceVersionString() {
		if (referenceVersionString == null) {
			updateReferenceVersionString();
		}
		return referenceVersionString;
	}

	public Integer getComparison() {
		return comparison;
	}

	public String getCurrentVersionString() {

		return currentVersionString;
	}

}
