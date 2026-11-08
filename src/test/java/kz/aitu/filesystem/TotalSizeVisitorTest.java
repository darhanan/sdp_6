package kz.aitu.filesystem;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.TotalSizeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TotalSizeVisitorTest {

    @Test
    @DisplayName("T2: sizes are summed across three nesting levels, each file once")
    void sumsAcrossNestingLevels() {
        SampleTrees.Campus campus = SampleTrees.campus();

        assertEquals(SampleTrees.CAMPUS_TOTAL_BYTES, new TotalSizeVisitor().run(campus.root()));
    }

    @Test
    @DisplayName("T2: a subtree only counts its own files")
    void subtreeCountsOnlyItsFiles() {
        SampleTrees.Campus campus = SampleTrees.campus();

        assertEquals(SampleTrees.LECTURES_TOTAL_BYTES, new TotalSizeVisitor().run(campus.lectures()));
        assertEquals(5_120L, new TotalSizeVisitor().run(campus.week01()));
    }

    @Test
    @DisplayName("T2: a single file as root returns its own size")
    void singleFileRoot() {
        assertEquals(4_096L, new TotalSizeVisitor().run(new FileNode("solo.bin", 4_096)));
    }

    @Test
    @DisplayName("T6: empty directories (also nested) have total size 0")
    void emptyStructures() {
        DirectoryNode root = new DirectoryNode("empty");
        root.addDirectory("a").addDirectory("b").addDirectory("c");

        assertEquals(0L, new TotalSizeVisitor().run(new DirectoryNode("nothing")));
        assertEquals(0L, new TotalSizeVisitor().run(root));
    }

    @Test
    @DisplayName("T7: running the same visitor twice does not double the total")
    void reuseDoesNotAccumulate() {
        SampleTrees.Campus campus = SampleTrees.campus();
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        long first = visitor.run(campus.root());
        long second = visitor.run(campus.root());
        long viaAcceptOnly = runWithAcceptOnly(visitor, campus.media());

        assertEquals(SampleTrees.CAMPUS_TOTAL_BYTES, first);
        assertEquals(first, second);
        assertEquals(902_560L, viaAcceptOnly);
    }

    @Test
    @DisplayName("T7: a resized file is reflected in the next run (no stale result)")
    void resultReflectsLatestTree() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode report = root.addFile("report.pdf", 100);
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        assertEquals(100L, visitor.run(root));
        report.resize(250);
        assertEquals(250L, visitor.run(root));
    }

    private static long runWithAcceptOnly(TotalSizeVisitor visitor, DirectoryNode root) {
        root.accept(visitor);
        return visitor.result();
    }
}
