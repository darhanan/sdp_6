package kz.aitu.filesystem.visitor;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;

/**
 * Counts files and directories and records the maximum depth of the visited subtree.
 *
 * <p>Added after the first two visitors without touching {@code FileNode},
 * {@code DirectoryNode} or their {@code accept} methods: a new operation is just a new
 * {@link TreeVisitor} subclass.</p>
 */
public final class StatisticsVisitor extends TreeVisitor<TreeStatistics> {

    private int fileCount;
    private int directoryCount;
    private int maxDepth;

    @Override
    protected void resetState() {
        fileCount = 0;
        directoryCount = 0;
        maxDepth = 0;
    }

    @Override
    protected void onFile(FileNode file, VisitContext context) {
        fileCount++;
        recordDepth(context);
    }

    @Override
    protected void onDirectory(DirectoryNode directory, VisitContext context) {
        directoryCount++;
        recordDepth(context);
    }

    @Override
    public TreeStatistics result() {
        return new TreeStatistics(fileCount, directoryCount, maxDepth);
    }

    private void recordDepth(VisitContext context) {
        maxDepth = Math.max(maxDepth, context.depth());
    }
}
