package kz.aitu.filesystem;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.model.TreeIntegrityException;
import kz.aitu.filesystem.visitor.NameCriterion;
import kz.aitu.filesystem.visitor.SearchVisitor;
import kz.aitu.filesystem.visitor.StatisticsVisitor;
import kz.aitu.filesystem.visitor.TotalSizeVisitor;

import java.util.List;

/**
 * Deterministic demonstration on an own "city-hall" archive.
 *
 * <p>Optional arguments change the search configuration:
 * {@code <glob pattern> <min size in bytes>}, e.g. {@code "*.pdf" 2000000}.</p>
 */
public final class App {

    private static final String DEFAULT_PATTERN = "*.log";
    private static final long DEFAULT_MIN_SIZE = 1_048_576L;

    private App() {
    }

    public static void main(String[] args) {
        String pattern = args.length > 0 ? args[0] : DEFAULT_PATTERN;
        long minSize = args.length > 1 ? Long.parseLong(args[1]) : DEFAULT_MIN_SIZE;

        Archive archive = buildCityHallArchive();
        DirectoryNode cityHall = archive.root();
        DirectoryNode budget = archive.budget();

        printSection("1. TotalSizeVisitor");
        TotalSizeVisitor totalSize = new TotalSizeVisitor();
        System.out.printf("city-hall total : %,d bytes%n", totalSize.run(cityHall));
        System.out.printf("budget subtree  : %,d bytes%n", totalSize.run(budget));

        printSection("2. SearchVisitor  (name glob = \"" + pattern + "\", size >= " + minSize + " B)");
        printPaths(new SearchVisitor(NameCriterion.glob(pattern), minSize).run(cityHall));
        System.out.println("-- same search started at budget/ (paths are relative to the chosen root):");
        printPaths(new SearchVisitor(NameCriterion.glob(pattern), minSize).run(budget));

        printSection("3. StatisticsVisitor");
        StatisticsVisitor statistics = new StatisticsVisitor();
        System.out.println("city-hall : " + statistics.run(cityHall));
        System.out.println("budget    : " + statistics.run(budget));
        System.out.println("city-hall (same instance, run again) : " + statistics.run(cityHall));

        printSection("4. Rejected mutations (tree stays unchanged)");
        demonstrateRejections(cityHall, budget);
        System.out.printf("total after rejected changes : %,d bytes%n", totalSize.run(cityHall));
    }

    /** The demo tree plus a handle to one subtree used as an alternative root. */
    record Archive(DirectoryNode root, DirectoryNode budget) {
    }

    static Archive buildCityHallArchive() {
        DirectoryNode cityHall = new DirectoryNode("city-hall");
        cityHall.addFile("readme.txt", 1_024);

        DirectoryNode budget = cityHall.addDirectory("budget");
        budget.addFile("plan-2026.docx", 120_000);
        DirectoryNode year2025 = budget.addDirectory("2025");
        year2025.addFile("q1-report.pdf", 2_400_000);
        year2025.addFile("q2-report.pdf", 2_150_000);
        year2025.addFile("summary.xlsx", 380_000);
        year2025.addFile("audit.log", 1_048_576);
        budget.addDirectory("2026");

        DirectoryNode photos = cityHall.addDirectory("photos");
        photos.addFile("logo.png", 45_000);
        DirectoryNode nauryz = photos.addDirectory("events").addDirectory("nauryz-2026");
        nauryz.addFile("img_001.jpg", 4_200_000);
        nauryz.addFile("img_002.jpg", 3_900_000);

        DirectoryNode logs = cityHall.addDirectory("logs");
        logs.addFile("access.log", 15_728_640);
        logs.addFile("error.log", 524_288);
        logs.addDirectory("archive").addFile("access-2025.log", 52_428_800);
        return new Archive(cityHall, budget);
    }

    private static void demonstrateRejections(DirectoryNode cityHall, DirectoryNode budget) {
        attempt("negative size", () -> new FileNode("broken.bin", -10));
        attempt("duplicate sibling 'readme.txt'", () -> cityHall.addFile("readme.txt", 5));
        attempt("second parent for 'budget'", () -> new DirectoryNode("backup").add(budget));
        attempt("cycle: city-hall inside budget", () -> budget.add(cityHall));
    }

    private static void attempt(String description, Runnable change) {
        try {
            change.run();
            System.out.println("ACCEPTED (unexpected): " + description);
        } catch (TreeIntegrityException | IllegalArgumentException rejected) {
            System.out.printf("REJECTED %-32s -> %s%n", description, rejected.getMessage());
        }
    }

    private static void printPaths(List<String> paths) {
        if (paths.isEmpty()) {
            System.out.println("(no matches)");
        }
        paths.forEach(path -> System.out.println("  " + path));
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("=== " + title);
    }
}
