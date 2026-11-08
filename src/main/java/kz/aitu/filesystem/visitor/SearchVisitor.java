package kz.aitu.filesystem.visitor;

import kz.aitu.filesystem.model.FileNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Finds files whose name matches a {@link NameCriterion} AND whose size is
 * {@code >= minSizeInclusive}. Returns root-relative '/'-separated paths sorted
 * lexicographically ({@link String#compareTo}), so the output never depends on insertion order.
 */
public final class SearchVisitor extends TreeVisitor<List<String>> {

    private final NameCriterion nameCriterion;
    private final long minSizeInclusive;
    private final List<String> matchingPaths = new ArrayList<>();

    public SearchVisitor(NameCriterion nameCriterion, long minSizeInclusive) {
        this.nameCriterion = Objects.requireNonNull(nameCriterion, "nameCriterion");
        if (minSizeInclusive < 0) {
            throw new IllegalArgumentException("Minimum size must be >= 0, got " + minSizeInclusive);
        }
        this.minSizeInclusive = minSizeInclusive;
    }

    @Override
    protected void resetState() {
        matchingPaths.clear();
    }

    @Override
    protected void onFile(FileNode file, VisitContext context) {
        if (isMatch(file)) {
            matchingPaths.add(context.relativePath());
        }
    }

    @Override
    public List<String> result() {
        return matchingPaths.stream().sorted().toList();
    }

    private boolean isMatch(FileNode file) {
        return nameCriterion.matches(file.name()) && file.size() >= minSizeInclusive;
    }
}
