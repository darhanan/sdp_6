package kz.aitu.filesystem.visitor;

/**
 * Where the current node sits relative to the root the traversal started from.
 *
 * @param relativePath names from the root's child down to the node, joined with '/';
 *                     the root itself has path "" (or its own name if the root is a file)
 * @param depth        number of edges from the root; the root has depth 0
 */
public record VisitContext(String relativePath, int depth) {
}
