import com.sun.javafx.PlatformUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class Launcher {
    public static void checkOut(String getAddress, String dirName) throws GitAPIException {
        Git.cloneRepository().setURI(getAddress).setDirectory(new File(dirName)).call();
    }
    public static void compile(String from, String to, String mainClass) throws IOException, InterruptedException {
        File file = new File(from + "/src/" + new Collection<>(mainClass.split("\\.")).join("/") + ".java");
        replaceAllInFile(
                file,
                "getClass\\(\\)\\.getResource\\(\"/([^\n\"]*)\"\\)",
                "Thread.currentThread().getContextClassLoader().getResource(\"$1\")"
        );
        Collection<String> result = compile(new File("dir/src/"));
        new File("dirOut").mkdirs();
        String command = System.getProperty("java.home") + "/../bin/javac -Xlint:none -encoding UTF-8 -d dirOut -cp json-simple-1.1.jar" +
                (PlatformUtil.isWindows() ? ";" : ":") + ". " + result.join(" ");
        System.out.println(command);
        Process process = Runtime.getRuntime().exec(command);
        captureOutput(process);
        process.waitFor();
    }

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
    private static void replaceAllInFile(File file, String regexp, String replacement) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String result = "";
        String buffer;
        while ((buffer = bufferedReader.readLine()) != null) {
            result += buffer + "\n";
        }
        result = result.replaceAll(regexp, replacement);
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            outputStreamWriter.write(result);
        }
    }
    private static Collection<String> compile(File directory) throws IOException {
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
    }
    public static void launch(String dir, String dirOut, String mainClass, String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                URLClassLoader urlClassLoader = new URLClassLoader(
                        new URL[]{
                                new File(dirOut).toURI().toURL(),
                                new File(dir).toURI().toURL(),
                                new File(dir + "/src").toURI().toURL(),
                                new File("json-simple-1.1.jar").toURI().toURL()
                        }
                );
                Thread.currentThread().setContextClassLoader(urlClassLoader);
                //System.out.println(new File("dirOut/").getAbsolutePath());
                Class clazz = urlClassLoader.loadClass(mainClass);
                Method main = clazz.getDeclaredMethod("main", String[].class);
                assert Modifier.isStatic(main.getModifiers());
                String path = System.getProperty("user.dir");
                System.setProperty("user.dir", new File(dir).getAbsolutePath());
                main.invoke(null, (Object) args);
                System.setProperty("user.dir", new File(path).getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }).start();
        countDownLatch.await();
    }
}
