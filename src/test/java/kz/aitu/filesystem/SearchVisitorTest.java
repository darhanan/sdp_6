package kz.aitu.filesystem;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.NameCriterion;
import kz.aitu.filesystem.visitor.SearchVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchVisitorTest {

    @Test
    @DisplayName("T3: name criterion AND inclusive minimum size are both required")
    void combinesBothConditions() {
        DirectoryNode root = SampleTrees.campus().root();

        // name matches, size 120 >= 100 -> in; name matches, size 80 < 100 -> out
        assertEquals(List.of("lectures/week01/notes.txt"),
                new SearchVisitor(NameCriterion.glob("*.txt"), 100).run(root));
        // size big enough but name does not match -> out
        assertEquals(List.of(),
                new SearchVisitor(NameCriterion.withExtension("txt"), 900_000).run(root));
    }

    @Test
    @DisplayName("T3: threshold is inclusive (size == threshold matches)")
    void thresholdIsInclusive() {
        DirectoryNode root = SampleTrees.campus().root();

        assertEquals(List.of("media/thumbs/a.png"), new SearchVisitor(NameCriterion.withExtension("png"), 2_048).run(root));
        assertEquals(List.of(), new SearchVisitor(NameCriterion.withExtension("png"), 2_049).run(root));
    }

    @Test
    @DisplayName("T4: paths are root-relative, '/'-separated and sorted")
    void pathsAreRootRelativeAndSorted() {
        SampleTrees.Campus campus = SampleTrees.campus();
        SearchVisitor pdfs = new SearchVisitor(NameCriterion.withExtension("pdf"), 0);

        assertEquals(List.of("lectures/week01/intro.pdf", "lectures/week02/visitor.pdf"), pdfs.run(campus.root()));
        assertEquals(List.of("week01/intro.pdf", "week02/visitor.pdf"), pdfs.run(campus.lectures()));
        assertEquals(List.of("intro.pdf"), pdfs.run(campus.week01()));
    }

    @Test
    @DisplayName("T4: order is lexicographic (String.compareTo) regardless of insertion order")
    void orderIndependentOfInsertion() {
        DirectoryNode forward = new DirectoryNode("forward");
        DirectoryNode backward = new DirectoryNode("backward");
        List<String> names = List.of("alpha.txt", "Zeta.txt", "beta.txt", "Beta.txt");
        names.forEach(name -> forward.addFile(name, 1));
        for (int index = names.size() - 1; index >= 0; index--) {
            backward.addFile(names.get(index), 1);
        }
        forward.addDirectory("a").addFile("x.txt", 1);
        backward.addDirectory("a").addFile("x.txt", 1);

        SearchVisitor search = new SearchVisitor(NameCriterion.any(), 0);
        List<String> expected = List.of("Beta.txt", "Zeta.txt", "a/x.txt", "alpha.txt", "beta.txt");

        assertEquals(expected, search.run(forward));
        assertEquals(expected, search.run(backward));
    }

    @Test
    @DisplayName("T6: empty tree and no-match configurations return an empty list")
    void emptyAndNoMatch() {
        assertTrue(new SearchVisitor(NameCriterion.any(), 0).run(new DirectoryNode("void")).isEmpty());
        assertTrue(new SearchVisitor(NameCriterion.exactly("missing.doc"), 0).run(SampleTrees.campus().root()).isEmpty());
    }

    @Test
    @DisplayName("T6: a file root is reported by its own name when it matches")
    void fileRoot() {
        assertEquals(List.of("solo.log"), new SearchVisitor(NameCriterion.glob("solo.???"), 10).run(new FileNode("solo.log", 10)));
    }

    @Test
    @DisplayName("T7: a reused search visitor returns the same list, not a growing one")
    void reuseDoesNotDuplicateMatches() {
        DirectoryNode root = SampleTrees.campus().root();
        SearchVisitor search = new SearchVisitor(NameCriterion.containing("notes"), 0);

        List<String> first = search.run(root);
        List<String> second = search.run(root);

        assertEquals(List.of("lectures/week01/notes.txt", "lectures/week02/notes.txt"), first);
        assertEquals(first, second);
    }

    @Test
    @DisplayName("Negative threshold is rejected at construction")
    void negativeThresholdRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(NameCriterion.any(), -1));
    }
}
