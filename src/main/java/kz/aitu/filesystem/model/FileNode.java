package kz.aitu.filesystem.model;

import kz.aitu.filesystem.visitor.Visitor;

/** A leaf of the tree: a named file with a non-negative size in bytes. */
public final class FileNode extends AbstractElement {

    private long sizeInBytes;

    public FileNode(String name, long sizeInBytes) {
        super(name);
        this.sizeInBytes = requireNonNegative(sizeInBytes);
    }

    public long size() {
        return sizeInBytes;
    }

    /** Changes the size; a negative value is rejected and the old size is kept. */
    public void resize(long newSizeInBytes) {
        this.sizeInBytes = requireNonNegative(newSizeInBytes);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FileNode[" + name() + ", " + sizeInBytes + " B]";
    }

    private static long requireNonNegative(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("File size must be >= 0, got " + bytes);
        }
        return bytes;
    }
}
