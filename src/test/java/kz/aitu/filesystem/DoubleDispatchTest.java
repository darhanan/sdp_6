package kz.aitu.filesystem;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.Visitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** T1: each element type reaches its own overloaded visit method. */
class DoubleDispatchTest {

    /** Records which overload was called; deliberately does NOT traverse children. */
    private static final class RecordingVisitor implements Visitor {
        private final List<String> calls = new ArrayList<>();

        @Override
        public void visit(FileNode file) {
            calls.add("file:" + file.name());
        }

        @Override
        public void visit(DirectoryNode directory) {
            calls.add("directory:" + directory.name());
        }
    }

    @Test
    @DisplayName("T1: FileNode.accept -> visit(FileNode), DirectoryNode.accept -> visit(DirectoryNode)")
    void eachElementTypeReachesItsOwnOverload() {
        RecordingVisitor visitor = new RecordingVisitor();

        new FileNode("bus-schedule.csv", 64).accept(visitor);
        new DirectoryNode("transport").accept(visitor);

        assertEquals(List.of("file:bus-schedule.csv", "directory:transport"), visitor.calls);
    }

    @Test
    @DisplayName("T1: dispatch works through the Element interface type (no instanceof needed)")
    void dispatchUsesRuntimeTypeBehindInterface() {
        RecordingVisitor visitor = new RecordingVisitor();
        List<Element> mixed = List.of(new DirectoryNode("parks"), new FileNode("map.svg", 10), new DirectoryNode("roads"));

        mixed.forEach(element -> element.accept(visitor));

        assertEquals(List.of("directory:parks", "file:map.svg", "directory:roads"), visitor.calls);
    }
}
