package app.owlcms.fly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
		                	<h3>Publish LIVE results to anyone the Internet, for free</h3>
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
						<h3>Run olympic weightlifting competitions in the cloud, for free, without any specialized equipment</h3>
						<ul>
							<li>
								If you have good Internet at your club, you can run competitions without having to install any software.
								The minimum is connect a laptop for the announcer.  Then if you wish you can add as many additional displays as you need. You
								can even referee using phones.
							</li>
							<li>
								Use the <b>Login</b> button to create and manage your applications on the fly.io cloud.
							</li>
						</ul>
					</div>
		            """);

		Html propagandaInfo = new Html("""
		        <div>
		        	<h3>How does this work and how can it be free?</h3>
					<div>
						The owlcms software is free.  See the 
						<a style="text-decoration:underline" target="_blank" href="https://github.com/owlcms/owlcms4/blob/public/LICENSE.txt">LICENSE</a>
					</div>
		        	<div>
		        		Fly.io is a cloud provider that has promotional billing for small users. They
		        		also do not bill if the actual monthly usage is under 5$.
		        		Running owlcms, its database, and the public results server costs less than
		        		the minimum billable amount, so it is free.
		        	</div>
		        	<div>
		        		This cloud application dashboard is a "remote control" application for fly.io. This site runs the fly.io application management commands for you.
		        	</div>
		        	<div>
		        		The commercial relationship is directly between you and fly.io. All we do is
		        		"type the commands" on your behalf. We do not
		        		get a percentage of your non-existent fees, and do not get any other
		        		advantage.
		        	</div>
		        </div>
		        """);

		Html localPropagandaInfo = new Html("""
		        <div>
		        	<h3>Running owlcms at the competition site</h3>
		        	<div>
					    If you are hosting a major competition, we actually recommend that you run it locally
		        		on a laptop and use your own router to isolate you from an Internet provider outage.
						See <a style="text-decoration:underline" target="_blank" href="https://owlcms.github.io/owlcms4/#/InstallationOverview?id=stand-alone-laptop-installation">this link</a> for local installation instructions.
					</div>
					<div>
						Then you can use this site to publish the results. See 
						<a style="text-decoration:underline" target="_blank" href="https://owlcms.github.io/owlcms4/#/PublicResults">this link</a> for instructions.
					</div>
					<div>
		        		Running on-site also allows using devices compliant with IWF TCRR rules that you can either
		        		build
		        		(<a style="text-decoration:underline" target="_blank" 
		        		href="https://github.com/jflamy/owlcms-firmata/blob/main/README.md">see this
		        		page</a>)
		        		or buy from suppliers like <a style="text-decoration:underline" target="_blank" 
		        		href="https://blue-owl.nemikor.com">blue-owl</a>.
		        		
		        	</div>
		        </div>
		        """);

		Div mapContainer = new Div();
		mapContainer.setWidth("975px");
		mapContainer.setHeight("650px");
		mapContainer.getStyle().set("overflow", "hidden");
		IFrame map = new IFrame(
		        "https://www.google.com/maps/d/embed?mid=1cFqfyfoF_RSoM56GewSPDWbuoHihsw4&ehbc=2E312F&z=2");
		map.setWidth("1000px");
		map.setHeight("733px");
		map.getStyle().set("position", "relative");
		map.getStyle().set("top", "-70px");
		map.getStyle().set("left", "-5px");
		mapContainer.add(map);
		Html mapDescription = new Html("""
			<div style="width: 950px">
				Current users of owlcms. Usage data gathering started Jan 01 2023. Usage of versions prior to v45 
				and usage without internet access is not represented.
			</div>
			""");
		mapDescription.getStyle().set("margin-top", "0em");
		Div mapContainerContainer = new Div();
		Html mapTitle = new Html("""
			<div style="width: 950px">
				<h3> Join an International Community</h3>
				<div>
				owlcms is used world-wide to run competitions of all sizes, from club meets to continental 
				championships with multiple platforms. Have a look at the 
				<a style="text-decoration: underline" target="_blank" href="https://owlcms.github.io/owlcms4/#/index">full documentation</a>
				</div>
				<div>Go to to the
				<a style="text-decoration:underline" target="_blank" href="https://groups.google.com/g/owlcms">owlcms users group forum</a> and subscribe
				to get updates, exchange with other users, and ask questions.
				</div>
			</div>
			""");
		mapContainerContainer.add(mapTitle);
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

		Div page = new Div(owlcmsInfo, publicResultsInfo, mapContainerContainer, localPropagandaInfo, propagandaInfo);
		page.setClassName("page");
		owlcmsInfo.setClassName("info");
		publicResultsInfo.setClassName("info");
		propagandaInfo.setClassName("info");
		localPropagandaInfo.setClassName("info");

		view.add(topRow, page);
	}

}
