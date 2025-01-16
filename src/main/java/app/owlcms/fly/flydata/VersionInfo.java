package app.owlcms.fly.flydata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vdurmont.semver4j.Semver;

import ch.qos.logback.classic.Logger;

public class VersionInfo {
	private String referenceVersionString;
	private String currentVersionString;
	final private static Logger logger = (Logger) LoggerFactory.getLogger(VersionInfo.class);
	private Integer comparison;

	public VersionInfo(String currentVersionString) {
		this.currentVersionString = currentVersionString;
		this.updateReferenceVersionString();
	}

	public void updateReferenceVersionString(boolean preRelease) {
		String apiUrl = "https://api.github.com/repos/owlcms/owlcms4/releases";
		this.referenceVersionString = fetchLatestReleaseVersion(apiUrl);

		if (!"latest".equals(currentVersionString)) {
			ComparableVersion currentVersion = new ComparableVersion(this.currentVersionString);
			ComparableVersion referenceVersion = new ComparableVersion(this.referenceVersionString);
			this.comparison = currentVersion.compareTo(referenceVersion);
		} else {
			this.comparison = -1;
		}
	}

	public void updateReferenceVersionString() {
		updateReferenceVersionString(
				this.currentVersionString.contains("-") || this.currentVersionString.contentEquals("prerelease"));
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

	public static String fetchLatestReleaseVersion(String apiUrl) {
		long now = System.currentTimeMillis();
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
			logger.info("fetchLatestReleaseVersion took {} ms", System.currentTimeMillis() - now);
			return versions.get(0).getValue();

		} catch (IOException e) {
			e.printStackTrace();
			return "unknown";
		}
	}
}
