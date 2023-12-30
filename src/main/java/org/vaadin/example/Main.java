package org.vaadin.example;

import org.jetbrains.annotations.NotNull;

import com.github.mvysny.vaadinboot.VaadinBoot;

/**
 * Run {@link #main(String[])} to launch your app in Embedded Jetty.
 * @author mavi
 */
public final class Main {
    public static void main(@NotNull String[] args) throws Exception {
        new VaadinBoot().run();
    }
}