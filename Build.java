// Cross-platform build script. Needs only a JDK (17+ recommended).
//   Build:          java Build.java
//   Build and run:  java Build.java run
// Output: dist/pokewilds.jar (plus settings/mods folders copied from game-files/)

import javax.tools.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

public class Build {
    static final Path ROOT = Paths.get(System.getProperty("user.dir"));
    static final Path SRC = ROOT.resolve("src");
    static final Path ASSETS = ROOT.resolve("assets");
    static final Path LIBS = ROOT.resolve("libs/runtime-libs.jar");
    static final Path CLASSES = ROOT.resolve("build/classes");
    static final Path DIST = ROOT.resolve("dist");
    static final Path JAR = DIST.resolve("pokewilds.jar");

    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        deleteTree(ROOT.resolve("build"));
        Files.createDirectories(CLASSES);
        Files.createDirectories(DIST);

        System.out.println("Compiling...");
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if (javac == null) fail("No Java compiler found. Install a JDK (not just a JRE).");
        List<String> sources;
        try (Stream<Path> s = Files.walk(SRC)) {
            sources = s.filter(p -> p.toString().endsWith(".java")).map(Path::toString).collect(Collectors.toList());
        }
        List<String> opts = new ArrayList<>(Arrays.asList(
            "--release", "8", "-nowarn", "-encoding", "UTF-8", "-Xlint:none",
            "-cp", LIBS.toString(), "-d", CLASSES.toString()));
        opts.addAll(sources);
        int rc = javac.run(null, null, null, opts.toArray(new String[0]));
        if (rc != 0) fail("Compilation failed.");

        System.out.println("Packaging...");
        Files.deleteIfExists(JAR);
        Set<String> seen = new HashSet<>();
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(JAR)))) {
            // Compiled game classes and assets first, so they override anything in the libs jar.
            addTree(out, CLASSES, seen);
            addTree(out, ASSETS, seen);
            try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(Files.newInputStream(LIBS)))) {
                ZipEntry e;
                while ((e = in.getNextEntry()) != null) {
                    if (e.isDirectory() || !seen.add(e.getName())) continue;
                    out.putNextEntry(new ZipEntry(e.getName()));
                    in.transferTo(out);
                    out.closeEntry();
                }
            }
        }

        Path gameFiles = ROOT.resolve("game-files");
        if (Files.isDirectory(gameFiles)) copyTreeIfMissing(gameFiles, DIST);

        System.out.printf("Done in %.1fs: %s%n", (System.currentTimeMillis() - t0) / 1000.0, ROOT.relativize(JAR));

        if (args.length > 0 && args[0].equals("run")) {
            String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
            List<String> cmd = new ArrayList<>(Arrays.asList(java, "-jar", "pokewilds.jar"));
            cmd.addAll(Arrays.asList(args).subList(1, args.length));
            Process p = new ProcessBuilder(cmd).directory(DIST.toFile()).inheritIO().start();
            System.exit(p.waitFor());
        }
    }

    static void addTree(ZipOutputStream out, Path base, Set<String> seen) throws IOException {
        List<Path> files;
        try (Stream<Path> s = Files.walk(base)) { files = s.filter(Files::isRegularFile).sorted().collect(Collectors.toList()); }
        for (Path f : files) {
            String name = base.relativize(f).toString().replace('\\', '/');
            if (!seen.add(name)) continue;
            out.putNextEntry(new ZipEntry(name));
            Files.copy(f, out);
            out.closeEntry();
        }
    }

    // Copies settings/mods into dist/ without overwriting the player's own edits there.
    static void copyTreeIfMissing(Path from, Path to) throws IOException {
        try (Stream<Path> s = Files.walk(from)) {
            for (Path f : (Iterable<Path>) s::iterator) {
                Path dst = to.resolve(from.relativize(f).toString());
                if (Files.isDirectory(f)) Files.createDirectories(dst);
                else if (!Files.exists(dst)) Files.copy(f, dst);
            }
        }
    }

    static void deleteTree(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (Stream<Path> s = Files.walk(p)) {
            for (Path f : s.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(f);
        }
    }

    static void fail(String msg) { System.err.println("BUILD FAILED: " + msg); System.exit(1); }
}
