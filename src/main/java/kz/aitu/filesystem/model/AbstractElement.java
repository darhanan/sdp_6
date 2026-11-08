package kz.aitu.filesystem.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Shared name validation and parent bookkeeping for both element types.
 *
 * <p>The class is package-private and sealed: only {@link FileNode} and {@link DirectoryNode}
 * extend it, and only {@link DirectoryNode} may change the parent link. This keeps the
 * "one parent per element" invariant inside the model package.</p>
 */
abstract sealed class AbstractElement implements Element permits FileNode, DirectoryNode {

    private final String name;
    private DirectoryNode parent;

    AbstractElement(String name) {
        this.name = requireValidName(name);
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final Optional<DirectoryNode> parent() {
        return Optional.ofNullable(parent);
    }

    final boolean hasParent() {
        return parent != null;
    }

    final void attachTo(DirectoryNode newParent) {
        this.parent = Objects.requireNonNull(newParent, "newParent");
    }

    final void detach() {
        this.parent = null;
    }

    private static String requireValidName(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            throw new IllegalArgumentException("Element name must not be blank");
        }
        if (candidate.contains("/")) {
            throw new IllegalArgumentException("Element name must not contain '/': " + candidate);
        }
        if (candidate.equals(".") || candidate.equals("..")) {
            throw new IllegalArgumentException("Element name must not be '.' or '..'");
        }
        return candidate;
    }
}
