package app.owlcms.fly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting message in a
 * notification.
 */
@Route("")
// @PreserveOnRefresh
public class MainView extends VerticalLayout {

	@SuppressWarnings("unused")
	final private Logger logger = LoggerFactory.getLogger(MainView.class);

	// private String oldToken;
	private ExecArea execArea;
	private FlyCtlCommands tokenConsumer;
	private LoginOverlay loginOverlay;
	private MainView view;

	public MainView() {
		execArea = new ExecArea();
		execArea.setVisible(false);
		execArea.setSizeFull();
		tokenConsumer = new FlyCtlCommands(UI.getCurrent(), execArea);
		this.removeAll();
		showLandingPage();

	}

	private void showLandingPage() {
		view = this;
		view.setSizeFull();

		Html publicResultsInfo = new Html(
		        """
		                <div>
		                	<h3>Publish live results to the Internet, for free</h3>
		                	<h5 style="margin-top: 0.2em; margin-bottom: 0.2em">From either on-site or cloud owlcms</h4>
		                	<ul>
		                		<li>
		                			Use this site to create a free cloud server that will make your competition results available LIVE, with no delays.
		                			To anyone in the world with internet access, on a phone, tablet, or laptop.
		                		</li>
		                		<li>
		                			This also works if you run your competitions locally on a Windows, Mac or Linux laptop and have Internet access at your site.
		                			Your results will be sent to the cloud results server.
		                		</li>
		                	</ul>
		                </div>
		                """);

		Html owlcmsInfo = new Html(
		        """
		                <div>
		                	<h3>Run competitions in the cloud, for free</h3>
		                	<ul>
		                		<li>
		                			If you have good Internet at your club, you can run competitions without having to	install anything.
		                			You can run a competition on a free cloud server.
		                		</li>
		                		<li>
		                			Use the <b>Login</b> button to create and manage your applications on the fly.io cloud.
		                		</li>
		                	</ol>
		                </div>
		                """);

		Html propagandaInfo = new Html("""
		        <div>
		        	<h3>How does this work and why is it free?</h3>
		        	<div>
		        		Fly.io is a cloud provider that has promotional billing for small users. They
		        		also do not bill if the actual monthly usage is under 5$.
		        		Running owlcms, its database, and the public results server costs less than
		        		the minimum billable amount, so it is free.
		        	</div>
		        	<div>
		        		This application is a "remote control" application for fly.io. Instead of you
		        		having to understand how to create and manage
		        		an application on fly.io, this site runs the commands for you.
		        	</div>
		        	<div>
		        		The commercial relationship is directly between you and fly.io. All we do is
		        		"type the commands" on your behalf. We do not
		        		get a percentage of your non-existent fees, and do not get any other
		        		advantage.
		        	</div>
		        </div>
		        """);

		Html antiPropagandaInfo = new Html("""
		        <div>
		        	<h3>When to use cloud owlcms? (or not)</h3>
		        	<div>
		        		If you are hosting a major competition, we recommend that you run it locally
		        		on a laptop, and that you use your own Wifi router
		        		instead of relying on the facilities' router. In this way you can run the
		        		competition even if there is an Internet failure.
		        		Running on-site also allows using IWF-Compliant MQTT devices that you can
		        		build
		        		(<a style="text-decoration:underline"
		        		href="https://github.com/jflamy/owlcms-firmata/blob/main/README.md">see this
		        		page</a>)
		        		or buy from suppliers like <a style="text-decoration:underline"
		        		href="https://blue-owl.nemikor.com">blue-owl</a>.
		        		You would, however, still configure a public results site.
		        	</div>
		        </div>
		        """);

		Div mapContainer = new Div();
		mapContainer.setWidth("1030px");
		mapContainer.setHeight("695px");
		mapContainer.getStyle().set("overflow", "hidden");
		IFrame map = new IFrame(
		        "https://www.google.com/maps/d/embed?mid=1cFqfyfoF_RSoM56GewSPDWbuoHihsw4&ehbc=2E312F&z=2");
		map.setWidth("1000px");
		map.setHeight("733px");
		map.getStyle().set("position", "relative");
		map.getStyle().set("top", "-65px");
		map.getStyle().set("left", "-5px");
		mapContainer.add(map);
		Div mapDescription = new Div("Users of the latest owlcms version, since Jan 01 2023");
		mapDescription.getStyle().set("margin-top", "-1em");
		Div mapContainerContainer = new Div();
		mapContainerContainer.add(mapContainer);
		mapContainerContainer.add(mapDescription);
		mapContainerContainer.setClassName("map");

		Button login = new Button("Login", e -> {
			UI ui = UI.getCurrent();
			loginOverlay = new Login(() -> {
				ui.navigate("apps");
				loginOverlay.setOpened(false);
			}, tokenConsumer);
			view.add(loginOverlay);
			loginOverlay.setOpened(true);
		});
		login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		HorizontalLayout topRow = new HorizontalLayout(new H2("owlcms Cloud Application Dashboard "), login);

		Div page = new Div(owlcmsInfo, publicResultsInfo, mapContainerContainer, propagandaInfo, antiPropagandaInfo);
		page.setClassName("page");
		owlcmsInfo.setClassName("info");
		publicResultsInfo.setClassName("info");
		propagandaInfo.setClassName("info");
		antiPropagandaInfo.setClassName("info");

		view.add(topRow, page);
	}

}
