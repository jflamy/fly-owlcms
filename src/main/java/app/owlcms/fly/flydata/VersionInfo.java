package app.owlcms.fly.flydata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vdurmont.semver4j.Semver;

import ch.qos.logback.classic.Logger;

public class VersionInfo {
	private String referenceVersionString;
	private String currentVersionString;
	Logger logger = (Logger) LoggerFactory.getLogger(VersionInfo.class);
	private Integer comparison;

	public VersionInfo(String currentVersionString) {
		this.currentVersionString = currentVersionString;
		this.updateReferenceVersionString();
	}
	
	public void updateReferenceVersionString(boolean preRelease) {
		String apiUrl = "https://api.github.com/repos/owlcms/owlcms4/releases";
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			Scanner scanner = new Scanner(url.openStream());
			String inline = "";
			while (scanner.hasNext()) {
				inline += scanner.nextLine();
			}
			scanner.close();

			JsonParser parser = new JsonParser();
			List<JsonObject> releases = new ArrayList<>();
			parser.parse(inline).getAsJsonArray().forEach(jsonElement -> releases.add(jsonElement.getAsJsonObject()));

			List<Semver> versions = new ArrayList<>();
			for (JsonObject release : releases) {
				String tagName = release.get("tag_name").getAsString();
				versions.add(new Semver(tagName));
			}

			Collections.sort(versions, Comparator.reverseOrder());
			this.referenceVersionString = versions.get(0).getValue();

			if (!"latest".equals(currentVersionString)) {
				ComparableVersion currentVersion = new ComparableVersion(this.currentVersionString);
				ComparableVersion referenceVersion = new ComparableVersion(this.referenceVersionString);
				this.comparison = currentVersion.compareTo(referenceVersion);
			} else {
				this.comparison = -1;
			}

		} catch (IOException e) {
			logger.error("version fetch failed", e);
			this.comparison = 0;
			this.referenceVersionString = "unknown";
		}
	}

	public void updateReferenceVersionString() {
		updateReferenceVersionString(this.currentVersionString.contains("-")||this.currentVersionString.contentEquals("prerelease"));
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
