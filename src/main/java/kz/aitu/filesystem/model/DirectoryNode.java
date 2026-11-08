package kz.aitu.filesystem.model;

import kz.aitu.filesystem.visitor.Visitor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A directory that owns uniquely named child elements.
 *
 * <p>Invariants protected by this class (all checked <em>before</em> anything is changed):</p>
 * <ul>
 *     <li>sibling names are unique;</li>
 *     <li>an element has at most one parent;</li>
 *     <li>a directory can never become its own descendant (no cycles).</li>
 * </ul>
 * Children are kept in a {@link TreeMap}, so iteration order is always by name.
 */
public final class DirectoryNode extends AbstractElement {

    private final Map<String, AbstractElement> childrenByName = new TreeMap<>();

    public DirectoryNode(String name) {
        super(name);
    }

    /** Adds an existing file; returns it to allow fluent tree building. */
    public FileNode add(FileNode file) {
        attach(file);
        return file;
    }

    /** Adds an existing directory (with its subtree); returns it to allow fluent tree building. */
    public DirectoryNode add(DirectoryNode directory) {
        attach(directory);
        return directory;
    }

    /** Convenience factory: creates a file, adds it, and returns it. */
    public FileNode addFile(String fileName, long sizeInBytes) {
        return add(new FileNode(fileName, sizeInBytes));
    }

    /** Convenience factory: creates an empty sub-directory, adds it, and returns it. */
    public DirectoryNode addDirectory(String directoryName) {
        return add(new DirectoryNode(directoryName));
    }

    /** Detaches the named child so it can be re-attached elsewhere. */
    public Optional<Element> remove(String childName) {
        AbstractElement removed = childrenByName.remove(childName);
        if (removed == null) {
            return Optional.empty();
        }
        removed.detach();
        return Optional.of(removed);
    }

    /** Read-only snapshot of the children, ordered by name. */
    public List<Element> children() {
        return List.copyOf(childrenByName.values());
    }

    public Optional<Element> child(String childName) {
        return Optional.ofNullable(childrenByName.get(childName));
    }

    public boolean isEmpty() {
        return childrenByName.isEmpty();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "DirectoryNode[" + name() + ", children=" + childrenByName.size() + "]";
    }

    private void attach(AbstractElement child) {
        Objects.requireNonNull(child, "child");
        rejectCycle(child);
        rejectSecondParent(child);
        rejectDuplicateName(child);
        childrenByName.put(child.name(), child);
        child.attachTo(this);
    }

    private void rejectCycle(AbstractElement child) {
        for (DirectoryNode cursor = this; cursor != null; cursor = cursor.parent().orElse(null)) {
            if (cursor == child) {
                throw new TreeIntegrityException(
                        "Cycle: '" + child.name() + "' cannot be placed inside itself or its descendant '" + name() + "'");
            }
        }
    }

    private static void rejectSecondParent(AbstractElement child) {
        if (child.hasParent()) {
            throw new TreeIntegrityException(
                    "'" + child.name() + "' already belongs to '" + child.parent().orElseThrow().name()
                            + "'; remove it there first");
        }
    }

    private void rejectDuplicateName(AbstractElement child) {
        if (childrenByName.containsKey(child.name())) {
            throw new TreeIntegrityException(
                    "Duplicate name: '" + name() + "' already contains '" + child.name() + "'");
        }
    }
}
