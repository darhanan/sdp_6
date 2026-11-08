package kz.aitu.filesystem.visitor;

import java.util.Objects;
import java.util.regex.Pattern;

/** A configurable, case-sensitive rule applied to a file name (not to its path). */
@FunctionalInterface
public interface NameCriterion {

    boolean matches(String fileName);

    static NameCriterion any() {
        return fileName -> true;
    }

    static NameCriterion exactly(String expectedName) {
        Objects.requireNonNull(expectedName, "expectedName");
        return expectedName::equals;
    }

    static NameCriterion containing(String fragment) {
        Objects.requireNonNull(fragment, "fragment");
        return fileName -> fileName.contains(fragment);
    }

    /** {@code withExtension("log")} matches "error.log" but not "log.txt". */
    static NameCriterion withExtension(String extension) {
        Objects.requireNonNull(extension, "extension");
        String suffix = extension.startsWith(".") ? extension : "." + extension;
        return fileName -> fileName.endsWith(suffix);
    }

    /** Shell-style pattern: {@code *} = any run of characters, {@code ?} = one character. */
    static NameCriterion glob(String globPattern) {
        Objects.requireNonNull(globPattern, "globPattern");
        Pattern regex = Pattern.compile(globToRegex(globPattern));
        return fileName -> regex.matcher(fileName).matches();
    }

    private static String globToRegex(String globPattern) {
        StringBuilder regex = new StringBuilder();
        StringBuilder literal = new StringBuilder();
        for (char symbol : globPattern.toCharArray()) {
            if (symbol == '*' || symbol == '?') {
                regex.append(Pattern.quote(literal.toString()));
                literal.setLength(0);
                regex.append(symbol == '*' ? ".*" : ".");
            } else {
                literal.append(symbol);
            }
        }
        return regex.append(Pattern.quote(literal.toString())).toString();
    }
}
