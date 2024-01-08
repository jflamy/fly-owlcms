package app.owlcms.fly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
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

import app.owlcms.fly.flydata.App;
import app.owlcms.fly.flydata.AppType;
import app.owlcms.fly.flydata.EarthLocation;


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
						try {
							this.setToken(string);
						} catch (Exception e) {
							e.printStackTrace();
						}
						logger.info("login token retrieved {}", string);
						logger.warn("cross-check 1: {}", getToken());
					};
					Consumer<String> errorConsumer = (string) -> {
						logger.error("token {}", string);
						tokenStatus = -1;
					};
					String commandString = "fly auth token";
					// last argument is null because we don't want to provide a token
					// since we are fetching one
					runCommand("retrieving token from config ", commandString, outputConsumer, errorConsumer, false, null);

					Files.delete(configFile);
					logger.warn("status {} deleted {}", tokenStatus == 0, configFile.toAbsolutePath().toString());
					logger.warn("cross-check 2: {}", getToken());

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
	
	public synchronized List<EarthLocation> getServerLocations(EarthLocation clientLocation) {
		ProcessBuilder builder = createProcessBuilder(getToken());
		List<EarthLocation> locations = getLocations(builder, execArea, UI.getCurrent());
		if (clientLocation != null) {
			for (EarthLocation l : locations) {
				l.calculateDistance(clientLocation);
			}
		}
		locations.sort(Comparator.comparing(EarthLocation::getDistance));
		logger.warn("servers: {}",locations);
		return locations;
	}

	public String getReason() {
		return reason;
	}

	public String getToken() {
		return Main.getAccessToken(ui.getSession());
	}

	public String getUserName() {
		return (String) VaadinSession.getCurrent().getAttribute("userName");
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setToken(String newToken) {
		Main.setAccessToken(ui.getSession(), newToken);
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
		execArea.clear(ui);
		new Thread(() -> {
			ProcessBuilder builder = createProcessBuilder(getToken());
			builder.environment().put("VERSION", "stable");
			builder.environment().put("REGION", getRegion());
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
		return appNames;
	}
	
	private synchronized List<EarthLocation> getLocations(ProcessBuilder builder, ExecArea execArea, UI ui)
	        throws NoPermissionException {
		List<EarthLocation> locations = new ArrayList<>();
		appNameStatus = 0;
		try {
			String commandString = "flyctl platform regions --json | jq -r '.[] | select(.RequiresPaidPlan == false) | [.Name, .Code, .Latitude, .Longitude] | @tsv'";
			Consumer<String> outputConsumer = (string) -> {
				String[] values = string.split("\t");
				locations.add(new EarthLocation(values[0], values[1], Double.parseDouble(values[2]), Double.parseDouble(values[3])));
			};
			Consumer<String> errorConsumer = (string) -> {
				appNameStatus = -1;
				execArea.appendError(string, ui);
			};
			runCommand("creating app names {}", commandString, outputConsumer, errorConsumer, true, null);
		} catch (Exception e) {
			e.printStackTrace();
			reason = e.getMessage();
			appNameStatus = -2;
		}
		return locations;
	}

	//flyctl platform regions --json | jq -r '.[] | select(.RequiresPaidPlan == false) | [.Name, .Latitude, .Longitude] | @tsv'
	
	private void removeConfig() throws IOException, NoLockException {
		configFile = Path.of(System.getProperty("user.home"), ".fly/config.yml");
		try {
			Files.delete(configFile);
		} catch (IOException e) {
			// ignore.
		}
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
		if (loggingMessage != null && !loggingMessage.isBlank()) {
			logger.warn(loggingMessage, commandString);
		}

		// run the command
		Process process = builder.start();

		// output and errors are buffered to the streams, the gobblers will drain
		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), outputConsumer);
		StreamGobbler streamGobbler2 = new StreamGobbler(process.getErrorStream(), errorConsumer);
		executorService.submit(streamGobbler);
		executorService.submit(streamGobbler2);

		// wait for the command to finish
		process.waitFor();

		// wait for the streams to be drained
		executorService.shutdown();
		executorService.awaitTermination(30, TimeUnit.SECONDS);

		// run the callback
		if (callback != null) {
			callback.run();
		}
	}

	private String getRegion() {
		return region;
	}

	private void setRegion(String region) {
		this.region = region;
	}


}
