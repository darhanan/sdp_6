package kz.aitu.filesystem.model;

import kz.aitu.filesystem.visitor.Visitor;

import java.util.Optional;

/**
 * A node of the in-memory file system tree.
 *
 * <p>Elements only know how to <em>accept</em> a visitor (first dispatch on the element's
 * runtime type); what happens to them is decided by the visitor (second dispatch on the
 * visitor's runtime type). Elements never contain operation logic such as size summing,
 * searching or statistics.</p>
 */
public interface Element {

    /** Name of this element inside its parent directory (never contains '/'). */
    String name();

    /** The directory that owns this element, or empty when the element is a root. */
    Optional<DirectoryNode> parent();

    /** First dispatch step: calls the {@code visit} overload that matches the concrete type. */
    void accept(Visitor visitor);
}
