import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class _Main {
    private static final String GIT_ADDRESS = "https://github.com/lerivi/CheckCheckerDesktop";
    private static final String MAIN_CLASS = "app.UI_Main";
































    public static void main(String[] args) throws GitAPIException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        /*Git.cloneRepository().setURI(GIT_ADDRESS).setCredentialsProvider(
                new UsernamePasswordCredentialsProvider("nikita202", "Nerfo202")
        ).setDirectory(Paths.get("dir/").toFile()).call();*/
        Collection<String> result = compile(new File("dir/src/"));
        new File("dirOut").mkdirs();
        String command = System.getProperty("java.home") + "/../bin/javac -d dirOut -cp json-simple-1.1.jar:. " + result.join(" ");
        System.out.println(command);
        Process process = Runtime.getRuntime().exec(command);
        captureOutput(process);
        process.waitFor();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                URLClassLoader urlClassLoader = new URLClassLoader(
                        new URL[]{
                                new File("dirOut/").toURI().toURL(),
                                new File("dir/").toURI().toURL(),
                                new File("json-simple-1.1.jar").toURI().toURL()
                        }
                );
                Thread.currentThread().setContextClassLoader(urlClassLoader);
                //System.out.println(new File("dirOut/").getAbsolutePath());
                Class clazz = urlClassLoader.loadClass(MAIN_CLASS);
                Method main = clazz.getDeclaredMethod("main", String[].class);
                assert Modifier.isStatic(main.getModifiers());
                System.setProperty("user.dir", new File("dir/").getAbsolutePath());
                main.invoke(null, (Object) args);
                System.setProperty("user.dir", new File("").getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }).start();
        countDownLatch.await();
    }
    private static Collection<String> compile(File directory) {
        assert directory.isDirectory();
        Collection<String> result = new Collection<>();
        for (File file: directory.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(compile(file));
            } else {
                String path = file.getAbsolutePath();
                if (path.substring(path.lastIndexOf('.') + 1).equals("java")) {
                    result.add(path);
                }
            }
        }
        return result;
    };

    private static class InputStreamConsumer extends Thread {

        private InputStream is;
        private PrintStream out;

        public InputStreamConsumer(InputStream is, PrintStream out) {
            this.is = is;
            this.out = out;
        }

        @Override
        public void run() {

            try {
                int value = -1;
                while ((value = is.read()) != -1) {
                    out.print((char)value);
                }
            } catch (IOException exp) {
                exp.printStackTrace();
            }
        }
    }

    private static void captureOutput(Process p) {

        InputStreamConsumer stdout;
        InputStreamConsumer errout;

        errout = new InputStreamConsumer(p.getErrorStream(), System.err);
        stdout = new InputStreamConsumer(p.getInputStream(), System.out);
        errout.start();
        stdout.start();
    }
}
