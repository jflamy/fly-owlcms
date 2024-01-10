package app.owlcms.fly;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;

import app.owlcms.fly.flydata.App;
import app.owlcms.fly.flydata.AppType;
import app.owlcms.fly.flydata.EarthLocation;
import app.owlcms.fly.flydata.GeoLocator;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting message in a
 * notification.
 */
@Route("apps")
public class AppsView extends VerticalLayout {

	private static final String LEFT_LABEL_WIDTH = "10em";
	private long lastClick = 0;

	@SuppressWarnings("unused")
	final private Logger logger = LoggerFactory.getLogger(AppsView.class);

	private ExecArea execArea;
	private FlyCtlCommands flyCommands;
	private VerticalLayout intro;
	private VerticalLayout apps;
	private String regionCode;
	private String clientIpString;

	public AppsView() {
		clientIpString = getClientIp();
		execArea = new ExecArea();
		execArea.setVisible(false);
		execArea.setSizeFull();
		flyCommands = new FlyCtlCommands(UI.getCurrent(), execArea);
		if (flyCommands.getToken() == null) {
			Login loginOverlay = new Login(flyCommands);
			loginOverlay.setCallback(() -> {
				getServerLocations();
				loginOverlay.setOpened(false);
				this.removeAll();
				showApplicationView();
			});
			loginOverlay.setOpened(true);
			add(loginOverlay);
		} else {
			getServerLocations();
			showApplicationView();
		}
	}



	private void getServerLocations() {
		EarthLocation clientIpLocation;
		clientIpLocation = GeoLocator.locate(clientIpString);
		// list is sorted with closest region at the top
		serverLocations = flyCommands.getServerLocations(clientIpLocation);
	}



	private void showApplicationView() {
		this.setSpacing(false);
		this.setHeightFull();
		H2 title = new H2("owlcms Cloud Applications - " + flyCommands.getUserName());
		Button logoutButton = new Button("Logout", (e) -> {
			flyCommands.setToken(null);
			Notification.show("Logged out", 1000, Position.TOP_END);
			UI.getCurrent().navigate("");
		});

		intro = buildIntro();
		apps = buildAppsPlaceholder();
		add(new HorizontalLayout(title, logoutButton), intro, apps, execArea);
		UI ui = UI.getCurrent();
		doListApplications(apps, ui);
	}

	private VerticalLayout buildAppsPlaceholder() {
		VerticalLayout apps = new VerticalLayout();
		apps.setMargin(false);
		apps.setPadding(false);
		return apps;
	}

	private ConfirmDialog buildDeletionDialog(App app, Runnable callback) {
		ConfirmDialog deletionDialog = new ConfirmDialog();
		deletionDialog.setHeader("Deletion Confirmation Required");
		deletionDialog.setText(new Html("""
				<div>
				This will remove the application and make the name available again.
				</div>
				"""));

		deletionDialog.setConfirmText("Delete");
		deletionDialog.setConfirmButtonTheme("error primary");
		deletionDialog.setCancelButtonTheme("primary");
		deletionDialog.setCancelable(true);
		deletionDialog.setCancelText("Cancel");
		deletionDialog.addConfirmListener(e -> {

			if (app.appType == AppType.OWLCMS) {
				Map<AppType, App> apps2 = flyCommands.getApps();
				App dbApp = apps2.get(AppType.DB);
				// App dbApp = apps2.values().stream()
				// 		.peek(l -> logger.warn("l.name {} app.name {}",l.name,app.name))
				// 		.filter(l -> l.name.contentEquals(app.name + "-db"))
				// 		.findAny()
				// 		.orElse(null);
				if (dbApp != null) {
					execArea.append("Deleting OWLCMS " + app.name, UI.getCurrent());
					flyCommands.appDestroy(app, null);
					execArea.append("Deleting OWLCMS database " + dbApp.name, UI.getCurrent());
					flyCommands.appDestroy(dbApp, callback);
				} else {
					execArea.append("Deleting OWLCMS - no database " + app.name, UI.getCurrent());
					flyCommands.appDestroy(app, callback);
				}
			} else {
				execArea.append("Deleting PUBLICRESULTS " + app.name, UI.getCurrent());
				flyCommands.appDestroy(app, callback);
			}
		});
		deletionDialog.addCancelListener(e -> {
			deletionDialog.close();
		});
		return deletionDialog;
	}

	private VerticalLayout buildIntro() {
		Html p1 = new Html("""
		        <div style="width: 60em">
		        This page creates and manages your owlcms applications on the fly.io cloud.
				<ul style="margin-top: 0">
					<li>Scenario 1: <b>owlcms in the cloud</b>: create only owlcms, no need to create a publicresults</li>
					<li>Scenario 2: <b>owlcms in the cloud, publicresults in the cloud</b>. Create both applications and then set the shared key using this page.</li>
					<li>Scenario 3: <b>owlcms at the competition site and publicresults in the cloud</b>:
					 Create only publicresults and then set the shared key.  You need to copy the shared key to the laptop</li>
				</ul>
		        </div>
		        """);
		VerticalLayout intro = new VerticalLayout(p1);
		intro.setSpacing(false);
		intro.setPadding(false);
		intro.setMargin(false);
		intro.getStyle().set("margin-top", "1em");
		return intro;
	}

	public String getClientIp() {
		HttpServletRequest request;
		VaadinServletRequest current = VaadinServletRequest.getCurrent();
		request = current.getHttpServletRequest();
		String remoteAddr = "";
		if (request != null) {
			remoteAddr = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddr == null || "".equals(remoteAddr)) {
				remoteAddr = request.getRemoteAddr();
			} else if (remoteAddr.contains(", ")) {
				String[] remoteAddresses = remoteAddr.split(", ");
				remoteAddr = remoteAddresses[0];
			}
		}
		return remoteAddr;
	}

	
	EarthLocation serverLoc = null;
	private List<EarthLocation> serverLocations;
	private void doListApplications(VerticalLayout appsArea, UI ui) {
		long timeMillis = System.currentTimeMillis();
		if (timeMillis - lastClick < 100) {
			lastClick = timeMillis;
			return;
		}
		lastClick = timeMillis;

			appsArea.removeAll();
			execArea.clear(ui);
			execArea.setVisible(true);
			execArea.append("Retrieving your application configurations. Please wait.", ui);
			ui.push();
			
		new Thread(() -> {
			// ui.access(() -> {
			// this also retrieves the region for the applications if available
			Map<AppType, App> appsList = flyCommands.getApps();
			regionCode = null;
			for (App app : appsList.values()) {
				if (app.regionCode != null && !app.regionCode.isBlank()) {
					regionCode = app.regionCode;
				}
				break;
			}
			
			ui.access(() -> {
				showApps(appsList, appsArea);
				execArea.clear(ui);
				execArea.setVisible(false);
			});
		}).start();
	}

	private Div showApplication(App app) {
		Div publicResultsSection = new Div();
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setPadding(false);
		publicResultsSection.add(hl);

		hl.setAlignItems(Alignment.CENTER);
		NativeLabel label = new NativeLabel(app.appType.toString());
		label.setWidth(LEFT_LABEL_WIDTH);
		hl.add(label);
		UI ui = UI.getCurrent();

		if (app.created) {
			showExistingApplication(app, publicResultsSection, hl, ui);
		} else {
			showNewApplication(app, publicResultsSection, hl, ui);
		}
		return publicResultsSection;
	}

	private void showNewApplication(App app, Div publicResultsSection, HorizontalLayout hl, UI ui) {
		TextField nameField = new TextField("");
		nameField.setAllowedCharPattern("[A-Za-z0-9-]");
		nameField.setValue(app.name);
		nameField.setPlaceholder("Enter application name");
		nameField.setWidth("15em");
		hl.add(nameField);
		nameField.setRequired(true);
		nameField.setRequiredIndicatorVisible(true);

		ComboBox<EarthLocation> serverCombo = new ComboBox<>();

		serverCombo.setRenderer(new TextRenderer<>(EarthLocation::getFullName));
		serverCombo.setItemLabelGenerator(EarthLocation::getFullName);
		serverCombo.setLabel("Select a server location");
		serverCombo.setWidth("20em");
		serverCombo.setItems(serverLocations);

		if (regionCode != null) {
			serverLoc = serverLocations.stream().filter(l -> regionCode.contentEquals(l.getCode())).findAny()
					.orElse(null);
		} else {
			serverLoc = serverLocations.get(0);
		}
		serverCombo.setValue(serverLoc);

		Button creationButton = new Button("Create",
				e -> {
					if (nameField.getValue() == null || nameField.getValue().isBlank()) {
						nameField.setErrorMessage("You must provide a value");
						nameField.setInvalid(true);
					} else {
						String siteName = nameField.getValue() + ".fly.net";
						boolean ok = flyCommands.createApp(nameField.getValue());
						if (!ok) {
							nameField.setErrorMessage(siteName + " is already taken.");
							nameField.setInvalid(true);
						} else {
							// this is what we want
							nameField.setInvalid(false);
							app.name = nameField.getValue();
							app.regionCode = serverCombo.getValue().getCode();
							flyCommands.appCreate(app, () -> doListApplications(apps, ui));
						}
					}
				});
		hl.add(serverCombo, creationButton);
		publicResultsSection.add(hl);
		hl.setAlignItems(Alignment.BASELINE);
		publicResultsSection.getStyle().set("margin-top", "-1em");
	}

	private void showExistingApplication(App app, Div publicResultsSection, HorizontalLayout hl, UI ui) {
		Anchor a = new Anchor("https://" + app.name + ".fly.dev", app.name, AnchorTarget.BLANK);
		a.getStyle().set("text-decoration", "underline");
		HorizontalLayout hlA = new HorizontalLayout(a, new Span("("+ app.regionCode +")"));
		hlA.setMargin(false);
		hlA.setPadding(false);
		hlA.setWidth("15em");
		hl.add(hlA);

		Button updateButton = new Button("Update",
		        e -> flyCommands.appDeploy(app));
		hl.add(updateButton);

		Button restartButton = new Button("Restart",
		        e -> flyCommands.appRestart(app));
		hl.add(restartButton);
		ConfirmDialog deletionDialog = buildDeletionDialog(app,
		        () -> doListApplications(apps, ui));
		Button deleteButton = new Button("Delete");
		deleteButton.addClickListener(event -> {
			deletionDialog.open();
		});
		hl.add(deleteButton);

		if (app.appType == AppType.PUBLICRESULTS) {
			HorizontalLayout hl1 = new HorizontalLayout();
			hl1.setMargin(false);
			NativeLabel label1 = new NativeLabel("Shared Key");
			label1.setWidth(LEFT_LABEL_WIDTH);
			Html sharedKeyExplanation1 = new Html("""
			        <div style="margin-bottom:0; width: 45em">
			        	<div>
			        		<em>YOU NEED TO SET THE SHARED KEY ONCE, when both owlcms and publicresults are present.</em>
			        	</div>
			        </div>
			        """);
			hl1.add(label1, sharedKeyExplanation1);

			HorizontalLayout hl2 = new HorizontalLayout();
			NativeLabel label2 = new NativeLabel("Shared Key");
			label2.setWidth(LEFT_LABEL_WIDTH);
			TextField sharedKeyField = new TextField();
			sharedKeyField.setTitle("Shared string between owlcms and public results");
			sharedKeyField.setPlaceholder("enter a shared string");
			sharedKeyField.setWidth("15em");
			sharedKeyField.setValue(generateRandomString(20));
			Button sharedKeyButton = new Button("Set Shared Key and restart apps",
			        e -> {
				        if (sharedKeyField.getValue() == null || sharedKeyField.getValue().isBlank()) {
					        sharedKeyField.setErrorMessage("The shared key cannot be empty");
					        sharedKeyField.setInvalid(true);
				        } else {
					        flyCommands.doSetSharedKey(sharedKeyField.getValue());
				        }
			        });
			hl2.add(label2, sharedKeyField, sharedKeyButton);

			HorizontalLayout hl3 = new HorizontalLayout();
			hl3.setMargin(false);
			NativeLabel label3 = new NativeLabel("");
			label3.setWidth(LEFT_LABEL_WIDTH);
			Html sharedKeyExplanation3 = new Html("""
					<div>
						The shared key is a value that is exchanged between owlcms and publicresults so they can trust one another. Setting the shared key is only needed once.
						<ul style="margin:0; width: 45em">
							<li>
								<em>IIf your owlcms is running locally at the competition site</em>, you will need to set this
								value as the shared key on the laptop using the owlcms user interface, in the Preparation - Settings - Connexions section.
							</li>
							<li>
								<em>If you are running both owlcms and publicresults in the cloud</em>, then this button will set the shared key for both.
								Wait until you have created both owlcms and publicresults.
							</li>
						</ul>
					</div>
					""");
			hl3.add(label3,sharedKeyExplanation3);

			hl2.getStyle().set("margin-top", "1em");
			publicResultsSection.add(hl2,hl3);
		}
	}

	private void showApps(Map<AppType, App> appMap, VerticalLayout apps) {
		App owlcmsApp = appMap.get(AppType.OWLCMS);
		App publicApp = appMap.get(AppType.PUBLICRESULTS);

		// apps.getStyle().set("margin-top", "1em");
		apps.add(new Hr());
		Div showOwlcmsApp;
		if (owlcmsApp != null) {
			showOwlcmsApp = showApplication(owlcmsApp);
			apps.add(showOwlcmsApp);
		} else {
			owlcmsApp = new App("", AppType.OWLCMS, getCurrentRegion(), "stable");
			showOwlcmsApp = showApplication(owlcmsApp);
			apps.add(showOwlcmsApp);
		}
		Div showPublicApp;
		apps.add(new Hr());
		if (publicApp != null) {
			showPublicApp = showApplication(publicApp);
			apps.add(showPublicApp);
		} else {
			publicApp = new App("", AppType.PUBLICRESULTS, getCurrentRegion(), "stable");
			showPublicApp = showApplication(publicApp);
			apps.add(showPublicApp);
		}
		//showPublicApp.getStyle().set("margin-top", "0.5em");
	}

	private String getCurrentRegion() {
		return null;
	}
	
	// Define printable characters
	private static final String PRINTABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";

	public String generateRandomString(int length) {
		SecureRandom random = new SecureRandom();
		StringBuilder stringBuilder = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(PRINTABLE_CHARACTERS.length());
			char randomChar = PRINTABLE_CHARACTERS.charAt(randomIndex);
			stringBuilder.append(randomChar);
		}

		return stringBuilder.toString();
	}
}
