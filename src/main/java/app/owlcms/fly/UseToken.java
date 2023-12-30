package org.vaadin.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UseToken {
	Logger logger = LoggerFactory.getLogger(UseToken.class);

	List<String> list = new ArrayList<>();

	public List<String> getApps(String token) {
		logger.warn("{}", "getApps");
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		ProcessBuilder builder = createProcessBuilder(token);
		try {
			builder.command("/bin/sh", "-c", "fly apps list --json | jq -r '.[].ID'");
			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
				logger.warn("{}", string);
				if (!string.contains("builder")) {
					list.add(string);
				}
			});
			executorService.submit(streamGobbler);
			process.waitFor(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String s : list) {
			try {
				List<String> repos = new ArrayList<>();
				String command = "fly image show --app " + s + " --json | jq .[].Repository";
				logger.warn("command {}", command);
				builder.command("/bin/sh", "-c", command);
				Process process = builder.start();
				StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
					logger.warn("repo  {} {}", s, string);
					repos.add(string);
				});
				executorService.submit(streamGobbler);
				process.waitFor(5, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// to get owlcms or publicresults
			// fly image show --app APP --json | jq .[].Repository
		}
		return list;

	}

	private ProcessBuilder createProcessBuilder(String token) {
		String homeDir = System.getProperty("user.home");
		String path = System.getenv("PATH");
		ProcessBuilder builder = new ProcessBuilder();
		builder.environment().put("FLYCTL_INSTALL", homeDir + "/.fly");
		builder.environment().put("PATH", homeDir + "/.fly/bin" + ";" + path);
		if (token == null || token.isBlank()) {
			builder.environment().put("FLY_ACCESS_TOKEN", "fo1_cTlqK6Rp20TZa3kH-WqGr7XTkyLJdeAi-ZAl96lec8g");
		}
		return builder;
	}

	public List<String> createPublicResults(String token) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		ProcessBuilder builder = createProcessBuilder(token);

		builder.environment().put("VERSION", "stable");
		builder.environment().put("REGION", "yyz");
		builder.environment().put("FLY_APP", "owlcms-results");

		try {
			builder.command("sh", "-c", "./createPublicResults.sh");
			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
				logger.info("{}", string);
			});
			executorService.submit(streamGobbler);
			process.waitFor(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
}
