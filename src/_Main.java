import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import sun.misc.Regexp;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class _Main {
    private static final String GIT_ADDRESS = "https://github.com/lerivi/CheckCheckerDesktop";
    private static final String MAIN_CLASS = "app.UI_Main";

    public static void main(String[] args) throws GitAPIException, IOException, InterruptedException {
        System.out.println();
        System.out.println("------------Cloning--------------");
        System.out.println();
        Launcher.checkOut(GIT_ADDRESS, "dir");
        System.out.println();
        System.out.println("------------Compiling--------------");
        System.out.println();
        Launcher.compile("dir", "dirOut", MAIN_CLASS);
        System.out.println();
        System.out.println("------------Launching--------------");
        System.out.println();
        Launcher.launch("dir", "dirOut", MAIN_CLASS, args);
        System.out.println();
        System.out.println("------------Finished!--------------");
        System.out.println();
    }
}
