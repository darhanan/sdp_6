package kz.aitu.filesystem.visitor;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;

/**
 * Second dispatch step: one overload per concrete element type.
 *
 * <p>The compiler picks the overload from the static type of {@code this} inside each
 * element's {@code accept} method, so no instanceof / casts are needed anywhere.</p>
 */
public interface Visitor {

    void visit(FileNode file);

    void visit(DirectoryNode directory);
}
