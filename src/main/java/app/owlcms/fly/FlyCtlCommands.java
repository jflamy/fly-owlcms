package app.owlcms.fly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyCtlCommands {
	Logger logger = LoggerFactory.getLogger(FlyCtlCommands.class);

	

	// TODO use time zone to infer a region
	private String region = "yyz";

	public Map<AppType, App> getApps(String token) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		ProcessBuilder builder = createProcessBuilder(token);
		List<String> appNames = getAppNames(executorService, builder);
		Map<AppType, App> apps = buildAppMap(executorService, builder, appNames);
		return apps;

	}

	private List<String> getAppNames(ExecutorService executorService, ProcessBuilder builder) {
		List<String> appNames = new ArrayList<>();
		try {
			builder.command("/bin/sh", "-c", "fly apps list --json | jq -r '.[].ID'");
			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
				if (!string.contains("builder")) {
					appNames.add(string);
				}
			});
			executorService.submit(streamGobbler);
			process.waitFor(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appNames;
	}

	private Map<AppType, App> buildAppMap(ExecutorService executorService, ProcessBuilder builder, List<String> appNames) {
		Map<AppType, App> apps = new HashMap<>();
		for (String s : appNames) {
			try {
				String command = "fly image show --app " + s + " --json | jq -r .[].Repository";
				builder.command("/bin/sh", "-c", command);
				Process process = builder.start();
				StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
					AppType appType = AppType.byImage(string);
					App app = new App(s, appType);
					apps.put(appType, app);
					logger.warn("{}", app);
				});
				executorService.submit(streamGobbler);
				process.waitFor(5, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return apps;
	}

	private ProcessBuilder createProcessBuilder(String token) {
		String homeDir = System.getProperty("user.home");
		String path = System.getenv("PATH");
		ProcessBuilder builder = new ProcessBuilder();
		builder.environment().put("FLYCTL_INSTALL", homeDir + "/.fly");
		builder.environment().put("PATH", homeDir + "/.fly/bin" + ";" + path);
		return builder;
	}

	public void createPublicResults(String token, String appName, App app) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		ProcessBuilder builder = createProcessBuilder(token);

		builder.environment().put("VERSION", "stable");
		builder.environment().put("REGION", region);
		builder.environment().put("FLY_APP", appName);

		try {
			builder.command("sh", "-c", "./createPublicResults.sh");
			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
				logger.info("{}", string);
			});
			executorService.submit(streamGobbler);
			process.waitFor(5, TimeUnit.SECONDS);
			app.name = appName;
			app.created = true;
		} catch (Exception e) {
			app.name = appName;
			app.created = false;
			e.printStackTrace();
		}
	}


	public void destroyApp(String token, App app) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		ProcessBuilder builder = createProcessBuilder(token);

		builder.environment().put("VERSION", "stable");
		builder.environment().put("REGION", region);
		builder.environment().put("FLY_APP", "owlcms-results");

		try {
			builder.command("sh", "-c", "fly destroy --yes "+app.name);
			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
				logger.info("{}", string);
			});
			executorService.submit(streamGobbler);
			process.waitFor(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
