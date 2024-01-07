package app.owlcms.fly;

import org.jetbrains.annotations.NotNull;

import com.github.mvysny.vaadinboot.VaadinBoot;

/**
 * Run {@link #main(String[])} to launch app in Embedded Jetty.
 */
public final class Main {

	public static VaadinBoot vaadinBoot;

	public static void main(@NotNull String[] args) throws Exception {
        vaadinBoot = new VaadinBoot();
        vaadinBoot.setAppName("fly-manager");
		vaadinBoot.run();
    }
}