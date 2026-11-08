package kz.aitu.filesystem;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.StatisticsVisitor;
import kz.aitu.filesystem.visitor.TreeStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsVisitorTest {

    @Test
    @DisplayName("T5: counts files, directories (root included) and max depth of the whole tree")
    void statisticsOfWholeTree() {
        TreeStatistics stats = new StatisticsVisitor().run(SampleTrees.campus().root());

        assertEquals(new TreeStatistics(8, 7, 3), stats);
    }

    @Test
    @DisplayName("T5: depth is measured from the chosen root, not from the absolute top")
    void depthIsRelativeToChosenRoot() {
        SampleTrees.Campus campus = SampleTrees.campus();

        assertEquals(new TreeStatistics(4, 4, 2), new StatisticsVisitor().run(campus.lectures()));
        assertEquals(new TreeStatistics(3, 2, 2), new StatisticsVisitor().run(campus.media()));
        assertEquals(new TreeStatistics(2, 1, 1), new StatisticsVisitor().run(campus.week01()));
    }

    @Test
    @DisplayName("T5: an empty directory at the deepest level still raises max depth")
    void emptyDirectoryCountsForDepth() {
        DirectoryNode root = new DirectoryNode("root");
        root.addFile("top.txt", 1);
        root.addDirectory("a").addDirectory("b").addDirectory("c").addDirectory("d");

        assertEquals(new TreeStatistics(1, 5, 4), new StatisticsVisitor().run(root));
    }

    @Test
    @DisplayName("T6: empty directory root and single file root")
    void emptyAndSingleNodeStructures() {
        assertEquals(new TreeStatistics(0, 1, 0), new StatisticsVisitor().run(new DirectoryNode("empty")));
        assertEquals(new TreeStatistics(1, 0, 0), new StatisticsVisitor().run(new FileNode("one.txt", 0)));
    }

    @Test
    @DisplayName("T7: results do not depend on run order and do not accumulate")
    void runOrderDoesNotMatter() {
        SampleTrees.Campus campus = SampleTrees.campus();
        StatisticsVisitor reused = new StatisticsVisitor();

        TreeStatistics bigFirst = reused.run(campus.root());
        TreeStatistics smallSecond = reused.run(campus.week01());
        TreeStatistics bigAgain = reused.run(campus.root());

        assertEquals(new TreeStatistics(8, 7, 3), bigFirst);
        assertEquals(new TreeStatistics(2, 1, 1), smallSecond);
        assertEquals(bigFirst, bigAgain);
    }
}
