package kz.aitu.filesystem.model;

/**
 * Thrown when a structural change would break a tree invariant
 * (duplicate sibling name, second parent, or a cycle).
 * The tree is always left unchanged when this exception is thrown.
 */
public class TreeIntegrityException extends RuntimeException {

    public TreeIntegrityException(String message) {
        super(message);
    }
}
