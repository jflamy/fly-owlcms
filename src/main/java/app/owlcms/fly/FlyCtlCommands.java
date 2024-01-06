package app.owlcms.fly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;

public class FlyCtlCommands {
	Logger logger = LoggerFactory.getLogger(FlyCtlCommands.class);

	// TODO use time zone to infer a region
	private String region = "yyz";

	private String token;

	private ExecArea execArea;

	private Map<AppType, App> appMap;

	public FlyCtlCommands(ExecArea execArea) {
		this.execArea = execArea;
	}

	public String getToken() {
		return token;
	}

	public Map<AppType, App> getApps() throws NoPermissionException {
		logger.warn("getApps");
		ExecutorService executorService = Executors.newCachedThreadPool();
		ProcessBuilder builder = createProcessBuilder(getToken());
		List<String> appNames = getAppNames(executorService, builder, execArea, UI.getCurrent());
		appMap = buildAppMap(executorService, builder, appNames);
		return appMap;
	}

	public void createPublicResults(String appName, App app) {
		ExecutorService executorService = Executors.newCachedThreadPool();
		ProcessBuilder builder = createProcessBuilder(token);

		builder.environment().put("VERSION", "stable");
		builder.environment().put("REGION", region);
		builder.environment().put("FLY_APP", appName);

		try {
			builder.command("sh", "-x", "-c", "./createPublicResults.sh");
			Process process = builder.start();
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
				logger.info("createPublicResults {}", string);
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

	private void doAppCommand(App app, String commandString, String... envPairs) {
		UI ui = UI.getCurrent();
		execArea.setVisible(true);
		new Thread(() -> {
			ExecutorService executorService = Executors.newCachedThreadPool();
			ProcessBuilder builder = createProcessBuilder(token);

			builder.environment().put("VERSION", "stable");
			builder.environment().put("REGION", region);
			builder.environment().put("FLY_APP", app.name);

			if (envPairs.length > 0) {
				for (int i = 0; i < envPairs.length; i = i + 2) {
					builder.environment().put(envPairs[i], envPairs[i + 1]);
					logger.warn("adding {}={}", envPairs[i], envPairs[i + 1]);
				}
			}

			try {
				logger.info("executing FLY_APP={} {}", app.name, commandString);
				execArea.clear(ui);
				builder.command("sh", "-c", commandString);
				Process process = builder.start();

				StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
					logger.info("appCommand {}", string);
					execArea.append(string, ui);
				});
				StreamGobbler streamGobbler2 = new StreamGobbler(process.getErrorStream(), (string) -> {
					logger.error("error {}", string);
					execArea.append(string, ui);
				});
				executorService.submit(streamGobbler);
				executorService.submit(streamGobbler2);
				process.waitFor();
				Thread.sleep(5000);
				ui.access(() -> execArea.setVisible(false));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void setToken(String newToken) {
		this.token = newToken;
	}

	String reason = "";

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		logger.warn("setting status {} {}", status, LoggerUtils.whereFrom());
		this.status = status;
	}

	int status;

	private List<String> getAppNames(ExecutorService executorService, ProcessBuilder builder, ExecArea area, UI ui)
			throws NoPermissionException {
		List<String> appNames = new ArrayList<>();
		setReason("");
		setStatus(0);

		try {
			builder.command("/bin/sh", "-c", "flyctl apps list --json | jq -r '.[].ID'");
			// logger.debug("env {}", builder.environment());
			Process process = builder.start();
			StreamGobbler streamGobbler1 = new StreamGobbler(process.getInputStream(), (string) -> {
				if (!string.contains("builder")) {
					appNames.add(string);
				}
			});
			StreamGobbler streamGobbler2 = new StreamGobbler(process.getErrorStream(), (string) -> {
				setStatus(-1);
				setReason(string);
				logger.error("error* {}", string);
				area.append(string, ui);
				reason = string;
			});
			executorService.submit(streamGobbler1);
			executorService.submit(streamGobbler2);
			process.waitFor();
			logger.warn("status {}", status);
		} catch (Exception e) {
			e.printStackTrace();
			reason = e.getMessage();
			status = -2;
		}
		if (status != 0) {
			throw new NoPermissionException(reason);
		}
		return appNames;
	}

	private Map<AppType, App> buildAppMap(ExecutorService executorService, ProcessBuilder builder,
			List<String> appNames) {
		Map<AppType, App> apps = new HashMap<>();
		for (String s : appNames) {
			try {
				String command = "fly image show --app " + s + " --json | jq -r .[].Repository";
				builder.command("/bin/sh", "-x", "-c", command);
				Process process = builder.start();
				StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
					AppType appType = AppType.byImage(string);
					App app = new App(s, appType);
					app.created = true;
					apps.put(appType, app);
					logger.info("{}", app);
				});
				executorService.submit(streamGobbler);
				process.waitFor();
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

		if (token != null) {
			builder.environment().put("FLY_ACCESS_TOKEN", token);
		}
		if (Files.exists(Path.of("/app/fly/bin/flyctl"))) {
			builder.environment().put("FLYCTL_INSTALL", "/app/fly");
		} else {
			builder.environment().put("FLYCTL_INSTALL", homeDir + "/.fly");
		}
		logger.warn("FLYCTL_INSTALL {}", builder.environment().get("FLYCTL_INSTALL"));
		logger.warn("FLY_ACCESS_TOKEN {}", builder.environment().get("FLY_ACCESS_TOKEN"));

		builder.environment().put("PATH", "."
				+ File.pathSeparator + homeDir + "/.fly/bin"
				+ File.pathSeparator + "/app/fly/bin"
				+ File.pathSeparator + path);
		return builder;
	}

	public void appDeploy(App app) {
		doAppCommand(app, "fly deploy --app " + app.name + " --image " + app.appType.image + ":stable" + " --ha=false");
	}

	public void appRestart(App app) {
		doAppCommand(app, "fly apps restart --skip-health-checks " + app.name);
	}

	public void appDestroy(App app) {
		doAppCommand(app, "fly apps destroy " + app.name + " --yes");
	}

	public void appCreate(App app) {
		doAppCommand(app, app.appType.script);
	}

	public void appSharedSecret(App app) {
		doAppCommand(app, app.appType.script);
	}

	int hostNameStatus;

	public boolean checkHostname(String value) {
		try {
			ProcessBuilder builder = createProcessBuilder(getToken());
			ExecutorService executorService = Executors.newCachedThreadPool();
			builder.command("sh", "-c", "fly apps create --name " + value + " --org personal");
			Process process = builder.start();
			hostNameStatus = 0;
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
				logger.info("appCommand {}", string);
			});
			StreamGobbler streamGobbler2 = new StreamGobbler(process.getErrorStream(), (string) -> {
				logger.error("error {}", string);
				hostNameStatus = -1;
			});
			executorService.submit(streamGobbler);
			executorService.submit(streamGobbler2);
			process.waitFor();
			return hostNameStatus == 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	int tokenStatus = -1;

	private String userName;

	public boolean doLogin(String username, String password) throws NoLockException {
		// if there is a config.yml file present then we can't proceed, must retry.
		int count = 10;
		Path configFile = Path.of(System.getProperty("user.home"), ".fly/config.yml");
		while (Files.exists(configFile) && count++ < 20) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		if (Files.exists(configFile)) {
			logger.error("config.yml not free");
			throw new NoLockException("config.yml not free");
		}

		// once the file is freed we lock against other instances of ourself
		try (RandomAccessFile raf = new RandomAccessFile(
				Path.of(
						System.getProperty("java.io.tmpdir"),
						"fly_owlcms.tmp")
						.toString(),
				"rw")) {

			logger.warn("temp file {} ", raf.toString());

			count = 0;
			while (raf.getChannel().tryLock() == null && count++ < 500) {
				Thread.sleep(10);
			}
			// lock acquired

			try {
				// no tokan
				ProcessBuilder builder = createProcessBuilder(getToken());
				builder.environment().remove("FLY_ACCESS_TOKEN");
				ExecutorService executorService = Executors.newCachedThreadPool();
				String loginString = "fly auth login --email " + username + " --password '" + password + "'";
				logger.warn("login string {}", loginString);
				builder.command("sh", "-c", loginString);
				Process process = builder.start();
				hostNameStatus = 0;
				StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
					logger.info("login {}", string);
				});
				StreamGobbler streamGobbler2 = new StreamGobbler(process.getErrorStream(), (string) -> {
					logger.error("login error {}", string);
				});
				executorService.submit(streamGobbler);
				executorService.submit(streamGobbler2);
				process.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// now get the token
			try {
				// no tokan
				ProcessBuilder builder = createProcessBuilder(getToken());
				ExecutorService executorService = Executors.newCachedThreadPool();
				builder.command("sh", "-c", "fly auth token");
				Process process = builder.start();
				hostNameStatus = 0;
				StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
					logger.info("token fetch {}", string);
					setToken(string);
					tokenStatus = 0;
				});
				StreamGobbler streamGobbler2 = new StreamGobbler(process.getErrorStream(), (string) -> {
					logger.error("token {}", string);
				});
				executorService.submit(streamGobbler);
				executorService.submit(streamGobbler2);
				process.waitFor();

				Files.delete(configFile);
				logger.warn("status {} deleted {}", tokenStatus == 0, configFile.toAbsolutePath().toString());
				this.userName = username;
				return tokenStatus == 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException | InterruptedException e) {
			return false;
		}
		return false;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void doSetSharedKey(String value) {
		for (App app : appMap.values()) {
			if (app.appType != AppType.OWLCMS && app.appType != AppType.PUBLICRESULTS) {
				continue;
			}
			try {
				ProcessBuilder builder = createProcessBuilder(getToken());
				ExecutorService executorService = Executors.newCachedThreadPool();
				String secretCommandString = "fly secrets set OWLCMS_UPDATEKEY='"+ value + "' --app " + app.name;
				builder.command("sh", "-c", secretCommandString);
				logger.warn("setting secret {}", secretCommandString);
				Process process = builder.start();
				hostNameStatus = 0;
				StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (string) -> {
					logger.info("appCommand {}", string);
				});
				StreamGobbler streamGobbler2 = new StreamGobbler(process.getErrorStream(), (string) -> {
					logger.error("error {}", string);
					hostNameStatus = -1;
				});
				executorService.submit(streamGobbler);
				executorService.submit(streamGobbler2);
				process.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
