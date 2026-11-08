package kz.aitu.filesystem.visitor;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Base class that owns the traversal for every concrete visitor (Template Method).
 *
 * <p><b>Traversal owner:</b> the visitor. {@link #visit(DirectoryNode)} reports the directory and
 * then calls {@code accept(this)} on every child, in name order (pre-order depth-first search).
 * Because the model forbids cycles and shared children, every reachable node is visited exactly
 * once. Subclasses never iterate children themselves; they only react in {@link #onFile} and
 * {@link #onDirectory}.</p>
 *
 * <p><b>State and reuse:</b> instances are reusable but not thread-safe. Whenever a new top-level
 * traversal starts (the visitor is idle and receives its first {@code visit} call),
 * {@link #resetState()} is called, so results never leak from a previous run and do not depend on
 * which run came first. {@link #run(Element)} is the recommended entry point.</p>
 *
 * @param <R> type of the result produced by one traversal
 */
public abstract class TreeVisitor<R> implements Visitor {

    private static final int IDLE = -1;

    private final Deque<String> pathFromRoot = new ArrayDeque<>();
    private int currentDepth = IDLE;

    /** Visits the whole subtree of {@code root} from a clean state and returns the result. */
    public final R run(Element root) {
        root.accept(this);
        return result();
    }

    @Override
    public final void visit(FileNode file) {
        enter(file);
        try {
            onFile(file, currentContext(file));
        } finally {
            exit();
        }
    }

    @Override
    public final void visit(DirectoryNode directory) {
        enter(directory);
        try {
            onDirectory(directory, currentContext(directory));
            for (Element child : directory.children()) {
                child.accept(this);
            }
        } finally {
            exit();
        }
    }

    /** Result of the most recent traversal. */
    public abstract R result();

    /** Clears accumulated state; called automatically at the start of each traversal. */
    protected abstract void resetState();

    protected void onFile(FileNode file, VisitContext context) {
        // default: ignore files
    }

    protected void onDirectory(DirectoryNode directory, VisitContext context) {
        // default: ignore directories
    }

    private void enter(Element element) {
        if (currentDepth == IDLE) {
            pathFromRoot.clear();
            resetState();
        }
        currentDepth++;
        if (currentDepth > 0) {
            pathFromRoot.addLast(element.name());
        }
    }

    private void exit() {
        if (currentDepth > 0) {
            pathFromRoot.removeLast();
        }
        currentDepth--;
    }

    /** A root directory has path ""; nodes below it get "child/grandchild/...". */
    private VisitContext currentContext(DirectoryNode directory) {
        return new VisitContext(String.join("/", pathFromRoot), currentDepth);
    }

    /** A file that is itself the traversal root is reported by its own name. */
    private VisitContext currentContext(FileNode file) {
        String path = pathFromRoot.isEmpty() ? file.name() : String.join("/", pathFromRoot);
        return new VisitContext(path, currentDepth);
    }
}
