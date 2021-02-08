package com.github.pulsebeat02.minecraftmedialibrary.logger;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    public static volatile BufferedWriter WRITER;
    public static boolean VERBOSE;

    static {
        try {
            final File f = new File("mml.log");
            System.out.println(f.getAbsolutePath());
            if (f.createNewFile()) {
                System.out.println("File Created (" + f.getName() + ")");
            } else {
                System.out.println("Log File Exists Already");
            }
            WRITER = new BufferedWriter(new FileWriter(f, false));
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void info(@NotNull final String info) {
        directPrint(System.currentTimeMillis() + ": [INFO] " + info + "\n");
    }

    public static void warn(@NotNull final String warning) {
        directPrint(System.currentTimeMillis() + ": [WARN] " + warning + "\n");
    }

    public static void error(@NotNull final String error) {
        directPrint(System.currentTimeMillis() + ": [ERROR] " + error + "\n");
    }

    private static void directPrint(@NotNull final String line) {
        if (VERBOSE) {
            try {
                WRITER.write(line);
                WRITER.flush();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void setVerbose(final boolean verbose) {
        VERBOSE = verbose;
    }

}
