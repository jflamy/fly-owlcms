package app.owlcms.fly;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import com.github.mvysny.vaadinboot.VaadinBoot;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;

/**
 * Run {@link #main(String[])} to launch app in Embedded Jetty.
 */
public final class Main {

	public static VaadinBoot vaadinBoot;
	private static Map<VaadinSession, String> accessTokens = new ConcurrentHashMap<>();

	public static void main(@NotNull String[] args) throws Exception {
		vaadinBoot = new VaadinBoot();
		vaadinBoot.setAppName("fly-manager");
		vaadinBoot.run();
	}

	public static String getAccessToken(VaadinSession session) {
		if (session != null) {
			checkSessionStatus(session);
			return accessTokens.get(session);
		} else {
			return null;
		}
	}

	public static String setAccessToken(VaadinSession session, String token) {
		if (session != null) {
			checkSessionStatus(session);
			if (token == null) {
				accessTokens.remove(session);
				return null;
			}
			return accessTokens.put(session, token);
		} else {
			return null;
		}
	}

	private static void checkSessionStatus(VaadinSession session) {
		session.access(() -> {
			try {
				if (session.getState() != VaadinSessionState.OPEN) {
					accessTokens.remove(session);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}