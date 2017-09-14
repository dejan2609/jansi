/*
 * Copyright (C) 2009-2017 the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.jansi;

import static org.fusesource.jansi.Ansi.ansi;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.fusesource.hawtjni.runtime.Library;
import org.fusesource.jansi.internal.CLibrary;
import static org.fusesource.jansi.internal.CLibrary.isatty;

/**
 * Main class for the library, providing executable jar to diagnose Jansi setup. 
 */
public class AnsiMain {
    public static void main(String... args) throws IOException {
        System.out.println("Jansi " + getJansiVersion()
            + " (Jansi native " + getPomPropertiesVersion("org.fusesource.jansi/jansi-native")
            + ", HawtJNI runtime " + getPomPropertiesVersion("org.fusesource.hawtjni/hawtjni-runtime") + ")");

        System.out.println();

        // info on native library
        System.out.println("library.jansi.path= " + System.getProperty("library.jansi.path", ""));
        System.out.println("library.jansi.version= " + System.getProperty("library.jansi.version", ""));
        Library lib = new Library("jansi", CLibrary.class);
        lib.load();
        /* TODO enable when upgrading hawtjni-runtime to 1.16 with https://github.com/fusesource/hawtjni/pull/36
        System.out.println("path: " + lib.getNativeLibraryPath());
        if (lib.getNativeLibrarySourceUrl() != null) {
            System.out.println("source: " + lib.getNativeLibrarySourceUrl());
        }*/

        System.out.println();

        System.out.println("os.name= " + System.getProperty("os.name") + ", "
                        + "os.version= " + System.getProperty("os.version") + ", "
                        + "os.arch= " + System.getProperty("os.arch"));
        System.out.println("file.encoding= " + System.getProperty("file.encoding"));
        System.out.println("java.version= " + System.getProperty("java.version") + ", "
                        + "java.vendor= " + System.getProperty("java.vendor") + ","
                        + " java.home= " + System.getProperty("java.home"));

        System.out.println();

        System.out.println("jansi.passthrough= " + Boolean.getBoolean("jansi.passthrough"));
        System.out.println("jansi.strip= " + Boolean.getBoolean("jansi.strip"));
        System.out.println("jansi.force= " + Boolean.getBoolean("jansi.force"));
        System.out.println(Ansi.DISABLE + "= " + Boolean.getBoolean(Ansi.DISABLE));

        System.out.println();

        System.out.println("IS_WINDOWS= " + AnsiConsole.IS_WINDOWS);
        if (AnsiConsole.IS_WINDOWS) {
            System.out.println("IS_CYGWIN= " + AnsiConsole.IS_CYGWIN);
            System.out.println("IS_MINGW= " + AnsiConsole.IS_MINGW);
        }

        System.out.println();

        if( isatty(CLibrary.STDOUT_FILENO) == 0 ) {
            System.out.println("stdout *IS* a TTY");
        } else {
            System.out.println("stdout is *NOT* a TTY");
        }

        AnsiConsole.systemInstall();
        try {
            if (args.length == 0) {
                printJansiLogoDemo();
                return;
            }

            System.out.println();

            if (args.length == 1) {
                File f = new File(args[0]);
                if (f.exists())
                {
                    // write file content
                    System.out.println(ansi().bold().a("\"" + args[0] + "\" content:").reset());
                    writeFileContent(f);
                    return;
                }
            }

            // write args without Jansi then with Jansi AnsiConsole
            System.out.println(ansi().bold().a("original args:").reset());
            int i = 1;
            for (String arg: args) {
                AnsiConsole.system_out.print(i++ + ": ");
                AnsiConsole.system_out.println(arg);
            }

            System.out.println(ansi().bold().a("Jansi filtered args:").reset());
            i = 1;
            for (String arg: args) {
                System.out.print(i++ + ": ");
                System.out.println(arg);
            }
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    private static String getJansiVersion() {
        Package p = AnsiMain.class.getPackage();
        return ( p == null ) ? null : p.getImplementationVersion();
    }

    private static String getPomPropertiesVersion(String path) throws IOException {
        InputStream in = AnsiMain.class.getResourceAsStream("/META-INF/maven/" + path + "/pom.properties");
        if (in == null) {
            return null;
        }
        try {
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("version");
        } finally {
            closeQuietly(in);
        }
    }

    private static void printJansiLogoDemo() throws IOException {
        Reader in = new InputStreamReader(AnsiMain.class.getResourceAsStream("jansi.txt"), "UTF-8");
        try {
            char[] buf = new char[1024];
            while (in.read(buf) >= 0) {
                System.out.print(buf);
            }
        } finally {
            closeQuietly(in);
        }
    }

    private static void writeFileContent(File f) throws IOException {
        InputStream in = new FileInputStream(f);
        try {
            byte[] buf = new byte[1024];
            int l = 0;
            while ((l = in.read(buf)) >= 0) {
                System.out.write(buf, 0, l);
            }
        } finally {
            closeQuietly(in);
        }
    }

    private static void closeQuietly(Closeable c) {
        try {
            c.close();
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
}
