package app.owlcms.fly;

import java.security.SecureRandom;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting message in a
 * notification.
 */
@Route("apps")
// @PreserveOnRefresh
public class AppsView extends VerticalLayout {

	private static final String LEFT_LABEL_WIDTH = "10em";
	private long lastClick = 0;
	final private Logger logger = LoggerFactory.getLogger(AppsView.class);
	private ExecArea execArea;
	private FlyCtlCommands tokenConsumer;
	private VerticalLayout intro;
	private VerticalLayout apps;

	public AppsView() {
		execArea = new ExecArea();
		execArea.setVisible(false);
		execArea.setSizeFull();
		tokenConsumer = new FlyCtlCommands(UI.getCurrent(), execArea);
		if (tokenConsumer.getToken() == null) {
			Login loginOverlay = new Login(tokenConsumer);
			loginOverlay.setCallback(() -> {
				loginOverlay.setOpened(false);
				this.removeAll();
				showApplicationView();
			});
			loginOverlay.setOpened(true);
			add(loginOverlay);
		} else {
			showApplicationView();
		}
	}

	private void showApplicationView() {
		this.setSpacing(false);
		this.setHeightFull();
		H2 title = new H2("owlcms Cloud Applications - " + tokenConsumer.getUserName());
		Button logoutButton = new Button("Logout", (e) -> {
			tokenConsumer.setToken(null);
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
			execArea.append("Deleting " + app.name, UI.getCurrent());
			tokenConsumer.appDestroy(app, callback);
			if (app.appType == AppType.OWLCMS) {
				App dbApp = tokenConsumer.getApps().get(AppType.DB);
				execArea.append("Deleting associated database " + dbApp.name, UI.getCurrent());
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
				<ul>
					<li>Scenario 1: <b>cloud owlcms only</b>: create only owlcms, don't create a publicresults</li>
					<li>Scenario 2: <b>both owlcms and publicresults in the cloud</b>. Create both using this page, then set the shared key.</li>
					<li>Scenario 3: <b>owlcms at the competition site and cloud publicresults</b>:
					 create only publicresults, and set the shared key.  You need to copy the shared key to the laptop</li>
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

	private void doListApplications(VerticalLayout apps, UI ui) {
		long timeMillis = System.currentTimeMillis();
		if (timeMillis - lastClick < 100) {
			lastClick = timeMillis;
			return;
		}
		lastClick = timeMillis;

		new Thread(() -> {
			ui.access(() -> {
				apps.removeAll();
				execArea.clear(ui);
				execArea.setVisible(true);
				execArea.append("Retrieving your application configurations. Please wait.", ui);
				ui.push();
				showApps(tokenConsumer.getApps(), apps);
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
			Anchor a = new Anchor("https://" + app.name + ".fly.dev", app.name, AnchorTarget.BLANK);
			a.getStyle().set("text-decoration", "underline");
			a.setWidth("15em");
			hl.add(a);
			Button updateButton = new Button("Update",
			        e -> tokenConsumer.appDeploy(app));
			hl.add(updateButton);

			Button restartButton = new Button("Restart",
			        e -> tokenConsumer.appRestart(app));
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
						        tokenConsumer.doSetSharedKey(sharedKeyField.getValue());
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
		} else {
			TextField nameField = new TextField("");
			nameField.setValue(app.name);
			nameField.setPlaceholder("enter application name");
			nameField.setWidth("15em");
			hl.add(nameField);
			nameField.setRequired(true);
			nameField.setRequiredIndicatorVisible(true);

			Button creationButton = new Button("Create",
			        e -> {
				        if (nameField.getValue() == null || nameField.getValue().isBlank()) {
					        nameField.setErrorMessage("You must provide a value");
					        nameField.setInvalid(true);
				        } else {
					        String siteName = nameField.getValue() + ".fly.net";
					        boolean ok = tokenConsumer.createApp(nameField.getValue());
					        if (!ok) {
						        nameField.setErrorMessage(siteName + " is already taken.");
						        nameField.setInvalid(true);
					        } else {
						        // this is what we want
						        nameField.setInvalid(false);
						        app.name = nameField.getValue();
						        tokenConsumer.appCreate(app, () -> doListApplications(apps, ui));
					        }
				        }
			        });
			hl.add(creationButton);
			publicResultsSection.add(hl);
		}
		return publicResultsSection;
	}

	private void showApps(Map<AppType, App> appMap, VerticalLayout apps) {
		App owlcmsApp = appMap.get(AppType.OWLCMS);
		App publicApp = appMap.get(AppType.PUBLICRESULTS);

		apps.getStyle().set("margin-top", "1em");
		if (owlcmsApp != null) {
			Div showOwlcmsApp = showApplication(owlcmsApp);
			apps.add(showOwlcmsApp);
		} else {
			owlcmsApp = new App("", AppType.OWLCMS);
			Div showOwlcmsApp = showApplication(owlcmsApp);
			apps.add(showOwlcmsApp);
		}
		if (publicApp != null) {
			Div showPublicApp = showApplication(publicApp);
			apps.add(showPublicApp);
		} else {
			publicApp = new App("", AppType.PUBLICRESULTS);
			Div showPublicApp = showApplication(publicApp);
			apps.add(showPublicApp);
		}
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