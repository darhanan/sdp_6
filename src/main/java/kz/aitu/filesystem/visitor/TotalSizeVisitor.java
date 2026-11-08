package kz.aitu.filesystem.visitor;

import kz.aitu.filesystem.model.FileNode;

/**
 * Sums the sizes of all files in a subtree.
 * Directories contribute nothing themselves; each file is added exactly once.
 */
public final class TotalSizeVisitor extends TreeVisitor<Long> {

    private long totalBytes;

    @Override
    protected void resetState() {
        totalBytes = 0;
    }

    @Override
    protected void onFile(FileNode file, VisitContext context) {
        totalBytes += file.size();
    }

    @Override
    public Long result() {
        return totalBytes;
    }
}
