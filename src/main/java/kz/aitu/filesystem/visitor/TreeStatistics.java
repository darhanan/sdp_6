package kz.aitu.filesystem.visitor;

/**
 * Immutable result of {@link StatisticsVisitor}.
 *
 * @param fileCount      number of visited files
 * @param directoryCount number of visited directories, including the root when it is a directory
 * @param maxDepth       largest depth of any visited node; the root has depth 0
 */
public record TreeStatistics(int fileCount, int directoryCount, int maxDepth) {

    public static final TreeStatistics EMPTY = new TreeStatistics(0, 0, 0);
}
