package kz.aitu.filesystem;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.model.TreeIntegrityException;
import kz.aitu.filesystem.visitor.TotalSizeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T8: invalid mutations are rejected and leave the tree unchanged; valid nesting still works. */
class TreeIntegrityTest {

    @Test
    @DisplayName("T8: negative sizes are rejected on creation and on resize")
    void negativeSizeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad.bin", -1));

        FileNode file = new FileNode("ok.bin", 10);
        assertThrows(IllegalArgumentException.class, () -> file.resize(-5));
        assertEquals(10, file.size());
    }

    @Test
    @DisplayName("T8: duplicate sibling names are rejected, same name in another folder is fine")
    void duplicateSiblingRejected() {
        DirectoryNode root = new DirectoryNode("root");
        root.addFile("notes.txt", 1);

        assertThrows(TreeIntegrityException.class, () -> root.addFile("notes.txt", 2));
        assertThrows(TreeIntegrityException.class, () -> root.addDirectory("notes.txt"));
        assertEquals(1, root.children().size());

        root.addDirectory("sub").addFile("notes.txt", 3);
        assertEquals(4L, new TotalSizeVisitor().run(root));
    }

    @Test
    @DisplayName("T8: an element cannot have two parents; remove + add moves it")
    void secondParentRejected() {
        DirectoryNode left = new DirectoryNode("left");
        DirectoryNode right = new DirectoryNode("right");
        FileNode shared = left.addFile("shared.dat", 7);

        assertThrows(TreeIntegrityException.class, () -> right.add(shared));
        assertTrue(right.isEmpty());

        left.remove("shared.dat");
        right.add(shared);
        assertSame(right, shared.parent().orElseThrow());
        assertTrue(left.isEmpty());
    }

    @Test
    @DisplayName("T8: cycles (self, child-into-descendant) are rejected")
    void cyclesRejected() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode middle = root.addDirectory("middle");
        DirectoryNode leaf = middle.addDirectory("leaf");

        assertThrows(TreeIntegrityException.class, () -> root.add(root));
        assertThrows(TreeIntegrityException.class, () -> leaf.add(root));

        root.remove("middle");
        assertThrows(TreeIntegrityException.class, () -> leaf.add(middle));
        assertTrue(leaf.isEmpty());
    }

    @Test
    @DisplayName("T8: invalid names are rejected")
    void invalidNamesRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(" "));
        assertThrows(IllegalArgumentException.class, () -> new FileNode("a/b.txt", 1));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(".."));
    }

    @Test
    @DisplayName("T8: deep valid nesting is still supported")
    void deepNestingSupported() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode cursor = root;
        for (int level = 1; level <= 50; level++) {
            cursor = cursor.addDirectory("level" + level);
        }
        cursor.addFile("deep.txt", 42);

        assertEquals(42L, new TotalSizeVisitor().run(root));
    }

    @Test
    @DisplayName("Encapsulation: children() is a read-only snapshot")
    void childrenAreReadOnly() {
        DirectoryNode root = new DirectoryNode("root");
        root.addFile("a.txt", 1);
        List<Element> children = root.children();

        assertThrows(UnsupportedOperationException.class, () -> children.add(new FileNode("b.txt", 1)));
        assertThrows(UnsupportedOperationException.class, children::clear);
        assertEquals(1, root.children().size());
    }
}
