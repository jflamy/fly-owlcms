package app.owlcms.fly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

public class FlyCtlCommands {
	int appNameStatus;
	int hostNameStatus;
	Logger logger = LoggerFactory.getLogger(FlyCtlCommands.class);
	String reason = "";
	int tokenStatus = -1;
	private Map<AppType, App> appMap;
	private Path configFile;
	private ExecArea execArea;

	// FIXME need a way to set or infer region
	private String region = "yyz";
	private UI ui;

	public FlyCtlCommands(UI ui, ExecArea execArea) {
		this.ui = ui;
		this.execArea = execArea;
	}

	public void appCreate(App app, Runnable callback) {
		doAppCommand(app, app.appType.script, callback);
	}

	public void appDeploy(App app) {
		doAppCommand(app, "fly deploy --app " + app.name + " --image " + app.appType.image + ":stable" + " --ha=false",
		        null);
	}

	public void appDestroy(App app, Runnable callback) {
		doAppCommand(app, "fly apps destroy " + app.name + " --yes", callback);
	}

	public void appRestart(App app) {
		doAppCommand(app, "fly apps restart --skip-health-checks " + app.name, null);
	}

	public void appSharedSecret(App app) {
		doAppCommand(app, app.appType.script, null);
	}

	public boolean createApp(String value) {
		try {
			hostNameStatus = 0;
			String commandString = "fly apps create --name " + value + " --org personal";
			Consumer<String> outputConsumer = (string) -> {
				logger.info("create output {}", string);
			};
			Consumer<String> errorConsumer = (string) -> {
				logger.error("create error {}", string);
				hostNameStatus = -1;
			};
			runCommand("create App {}", commandString, outputConsumer, errorConsumer, true, null);
			return hostNameStatus == 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	String loginToken = "";
	private String tokenFetch;

	public boolean doLogin(String username, String password) throws NoLockException {
		synchronized (Main.vaadinBoot) {
			// we lock against other HTTP threads in our own JVM - we are alone messing with fly in our container
			try {
				removeConfig();
				try {
					String loginString = "fly auth login --email " + username + " --password '" + password + "'";
					Consumer<String> outputConsumer = (string) -> {
						logger.info("login {}", string);
					};
					Consumer<String> errorConsumer = (string) -> {
						logger.error("login error {}", string);
					};
					// don't use existing token if present!
					runCommand("login", loginString, outputConsumer, errorConsumer, false, null);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// now get the token
				try {
					tokenStatus = 0;
					Consumer<String> outputConsumer = (string) -> {
						logger.info("token fetch {}", string);
						loginToken = string;
					};
					Consumer<String> errorConsumer = (string) -> {
						logger.error("token {}", string);
						tokenStatus = -1;
					};
					String commandString = "fly auth token";
					// last argument is null because we don't want to provide a token
					// since we are fetching one
					runCommand("getting token", commandString, outputConsumer, errorConsumer, true, null);

					Files.delete(configFile);
					logger.warn("status {} deleted {}", tokenStatus == 0, configFile.toAbsolutePath().toString());
					this.setToken(loginToken);
					this.setUserName(username);
					return tokenStatus == 0;
				} catch (IOException | InterruptedException e) {
					return false;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					Files.deleteIfExists(configFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}
	}

	public void doSetSharedKey(String value) {
		UI ui = UI.getCurrent();
		execArea.setVisible(true);
		new Thread(() -> {
			execArea.clear(ui);
			for (App app : appMap.values()) {
				if (app.appType != AppType.OWLCMS && app.appType != AppType.PUBLICRESULTS) {
					continue;
				}
				try {
					hostNameStatus = 0;
					String commandString = "fly secrets set OWLCMS_UPDATEKEY='" + value + "' --app " + app.name;
					Consumer<String> outputConsumer = (string) -> {
						execArea.append(string, ui);
					};
					Consumer<String> errorConsumer = (string) -> {
						hostNameStatus = -1;
						execArea.appendError(string, ui);
					};
					runCommand("setting secret {}", commandString, outputConsumer, errorConsumer, true, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			ui.access(() -> execArea.setVisible(false));
		}).start();
	}

	public synchronized Map<AppType, App> getApps() throws NoPermissionException {
		logger.warn("getApps");
		ProcessBuilder builder = createProcessBuilder(getToken());
		List<String> appNames = getAppNames(builder, execArea, UI.getCurrent());
		appMap = buildAppMap(builder, appNames);
		return appMap;
	}

	public String getReason() {
		return reason;
	}

	public String getToken() {
		ui.access(() -> {
			tokenFetch = (String) ui.getSession().getAttribute("accessToken");
			logger.warn("GETTING TOKEN {} {} {}", ui.getSession(), tokenFetch, LoggerUtils.whereFrom());
		});
		return tokenFetch;
	}

	public String getUserName() {
		return (String) VaadinSession.getCurrent().getAttribute("userName");
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setToken(String newToken) {
		ui.access(() -> {
			logger.warn("SETTING TOKEN {} {} {}", ui.getSession(), newToken, LoggerUtils.whereFrom());
			ui.getSession().setAttribute("accessToken", newToken);
		});
	}

	public void setUserName(String userName) {
		VaadinSession.getCurrent().setAttribute("userName", userName);
	}

	private synchronized Map<AppType, App> buildAppMap(ProcessBuilder builder,
	        List<String> appNames) {
		logger.warn("buildAppMap start");
		Map<AppType, App> apps = new HashMap<>();

		for (String s : appNames) {
			try {
				String commandString = "fly image show --app " + s + " --json | jq -r .[].Repository";
				Consumer<String> outputConsumer = (string) -> {
					AppType appType = AppType.byImage(string);
					App app = new App(s, appType);
					app.created = true;
					apps.put(appType, app);
					logger.info("adding to map {}", app);
				};
				Consumer<String> errorConsumer = (string) -> {
					logger.error("appMap error {}", string);
				};
				runCommand("building app map {}", commandString, outputConsumer, errorConsumer, true, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.warn("buildAppMap stop");
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
		logger.debug("FLYCTL_INSTALL {}", builder.environment().get("FLYCTL_INSTALL"));
		logger.debug("FLY_ACCESS_TOKEN {}", builder.environment().get("FLY_ACCESS_TOKEN"));

		builder.environment().put("PATH", "."
		        + File.pathSeparator + homeDir + "/.fly/bin"
		        + File.pathSeparator + "/app/fly/bin"
		        + File.pathSeparator + path);
		return builder;
	}

	private void doAppCommand(App app, String commandString, Runnable callback, String... envPairs) {
		UI ui = UI.getCurrent();
		execArea.setVisible(true);
		new Thread(() -> {
			ProcessBuilder builder = createProcessBuilder(getToken());
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
				Consumer<String> outputConsumer = (string) -> {
					execArea.append(string, ui);
				};
				Consumer<String> errorConsumer = (string) -> {
					execArea.appendError(string, ui);
				};
				runCommand("=========== running command {}", commandString, outputConsumer, errorConsumer, builder,
				        callback);
				Thread.sleep(5000);
				ui.access(() -> execArea.setVisible(false));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	private synchronized List<String> getAppNames(ProcessBuilder builder, ExecArea execArea, UI ui)
	        throws NoPermissionException {
		logger.warn("getAppNames start");
		List<String> appNames = new ArrayList<>();
		setReason("");
		appNameStatus = 0;
		try {
			String commandString = "flyctl apps list --json | jq -r '.[].ID'";
			Consumer<String> outputConsumer = (string) -> {
				if (!string.contains("builder")) {
					appNames.add(string);
				}
			};
			Consumer<String> errorConsumer = (string) -> {
				appNameStatus = -1;
				setReason(string);
				execArea.appendError(string, ui);
				reason = string;
			};
			runCommand("creating app names {}", commandString, outputConsumer, errorConsumer, true, null);
		} catch (Exception e) {
			e.printStackTrace();
			reason = e.getMessage();
			appNameStatus = -2;
		}
		if (appNameStatus != 0) {
			throw new NoPermissionException(reason);
		}
		logger.warn("getAppNames stop");
		return appNames;
	}

	private void removeConfig() throws IOException, NoLockException {
		configFile = Path.of(System.getProperty("user.home"), ".fly/config.yml");
		Files.delete(configFile);
		if (Files.exists(configFile)) {
			logger.error("could not delete file");
			throw new NoLockException("config.yml not free");
		}
	}

	private void runCommand(String loggingMessage, String commandString, Consumer<String> outputConsumer,
	        Consumer<String> errorConsumer, boolean useToken, Runnable callback)
	        throws IOException, InterruptedException {
		ProcessBuilder builder = createProcessBuilder(useToken ? getToken() : null);
		runCommand(loggingMessage, commandString, outputConsumer, errorConsumer, builder, callback);
	}

	private void runCommand(String loggingMessage, String commandString, Consumer<String> outputConsumer,
	        Consumer<String> errorConsumer, ProcessBuilder builder, Runnable callback)
	        throws IOException, InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		builder.command("sh", "-c", commandString);
		logger.warn(loggingMessage, commandString);
		Process process = builder.start();
		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), outputConsumer);
		StreamGobbler streamGobbler2 = new StreamGobbler(process.getErrorStream(), errorConsumer);
		executorService.submit(streamGobbler);
		executorService.submit(streamGobbler2);
		process.waitFor();
		executorService.shutdown();
		executorService.awaitTermination(30, TimeUnit.SECONDS);
		if (callback != null) {
			callback.run();
		}
	}
}
