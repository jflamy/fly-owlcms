package app.owlcms.fly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import com.github.mvysny.vaadinboot.VaadinBoot;

/**
 * Run {@link #main(String[])} to launch your app in Embedded Jetty.
 * @author mavi
 */
public final class Main {
    public static void main(@NotNull String[] args) throws Exception {
        cleanSetup();

        VaadinBoot vaadinBoot = new VaadinBoot();
        vaadinBoot.setAppName("fly-manager");
		vaadinBoot.run();
    }

	private static void cleanSetup() {
		try {
            // we don't want config.yml from the installation (or from aborted
            // previous runs)
            Path configFile = Path.of(System.getProperty("user.home"), ".fly/config.yml");
			Files.delete(configFile);
		} catch (IOException e) {
		}
	}
}