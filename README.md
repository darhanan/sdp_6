# Assignment 06 — Visitor Pattern: Advanced File System

**Student:** Darkhan Tynyyshtyk · **Group:** SE-2523 · **Course:** ShP-2216 Software Design Patterns

An in-memory hierarchical file system (`FileNode`, `DirectoryNode`) with three independent
operations implemented as visitors: `TotalSizeVisitor`, `SearchVisitor`, `StatisticsVisitor`.

## Requirements

- JDK 17 or newer (`java -version`)
- Maven 3.9+ **or** the bundled Maven wrapper (`./mvnw`, downloads Maven on first use)

## Run the tests

```bash
mvn test
# or, without a local Maven installation:
./mvnw test          # Windows: mvnw.cmd test
```

Expected: `Tests run: 28, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`.

## Run the demonstration

```bash
mvn -q compile exec:java
```

Change the search configuration (glob pattern and inclusive minimum size in bytes):

```bash
mvn -q compile exec:java -Dexec.args='"*.pdf" 2200000'
```

Alternative without the exec plugin:

```bash
mvn -q package -DskipTests && java -jar target/visitor-file-system-1.0.0.jar "*.jpg" 4000000
```

## Project layout

```
src/main/java/kz/aitu/filesystem/
├── App.java                    deterministic demo on the own "city-hall" archive
├── model/
│   ├── Element.java            common interface: name(), parent(), accept(Visitor)
│   ├── AbstractElement.java    sealed, package-private: name validation + parent link
│   ├── FileNode.java           leaf, size >= 0
│   ├── DirectoryNode.java      children in a TreeMap, integrity checks
│   └── TreeIntegrityException.java
└── visitor/
    ├── Visitor.java            visit(FileNode), visit(DirectoryNode)
    ├── TreeVisitor.java        owns traversal, path and depth; resets state per run
    ├── VisitContext.java       (relativePath, depth)
    ├── TotalSizeVisitor.java
    ├── SearchVisitor.java + NameCriterion.java
    └── StatisticsVisitor.java + TreeStatistics.java
src/test/java/kz/aitu/filesystem/
├── DoubleDispatchTest.java     T1
├── TotalSizeVisitorTest.java   T2, T6, T7
├── SearchVisitorTest.java      T3, T4, T6, T7
├── StatisticsVisitorTest.java  T5, T6, T7
├── TreeIntegrityTest.java      T8 + encapsulation
└── SampleTrees.java            own "campus" fixture
```

## Design decisions (short)

- **Double dispatch:** `element.accept(visitor)` → `visitor.visit(this)`. The first dispatch picks the
  element's `accept`; inside it the static type of `this` selects the `visit` overload.
  No `instanceof`, `getClass()`, reflection or casts are used to choose behaviour.
- **Traversal owner:** the visitor, centralised in `TreeVisitor.visit(DirectoryNode)` (pre-order DFS,
  children in name order). Concrete visitors only override `onFile` / `onDirectory`.
- **Paths & depth:** root depth = 0, each level adds 1. A path is the names from the root's child down to
  the file joined with `/` (the root name is not included). A file used as the root is reported by its name.
  Search results are sorted with `String.compareTo`.
- **State:** visitor instances are reusable (not thread-safe). State is reset automatically at the start of
  every top-level traversal, so results never depend on previous runs.
- **Integrity:** negative sizes, invalid names, duplicate sibling names, a second parent and cycles are
  rejected *before* anything changes. `children()` returns an unmodifiable snapshot.
- **Extension boundary:** a new *operation* = a new `TreeVisitor` subclass; `model/` stays untouched
  (this is how `StatisticsVisitor` was added — see git history). A new *element type* would require a new
  `visit` overload in `Visitor` and therefore changes in every visitor.
