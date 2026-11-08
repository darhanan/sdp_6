package kz.aitu.filesystem;

import kz.aitu.filesystem.model.DirectoryNode;

/**
 * Own test fixture (sizes in bytes):
 * <pre>
 * campus/                          depth 0
 * ├── lectures/                    depth 1
 * │   ├── empty-week/              depth 2 (empty)
 * │   ├── week01/                  depth 2
 * │   │   ├── intro.pdf      5000  depth 3
 * │   │   └── notes.txt       120  depth 3
 * │   └── week02/                  depth 2
 * │       ├── notes.txt        80  depth 3
 * │       └── visitor.pdf    7000  depth 3
 * ├── media/                       depth 1
 * │   ├── lecture-01.mp4   900000  depth 2
 * │   └── thumbs/                  depth 2
 * │       ├── a.png          2048  depth 3
 * │       └── b.png           512  depth 3
 * └── readme.md               300  depth 1
 * </pre>
 * Totals: 8 files, 7 directories, max depth 3, 915 060 bytes.
 */
final class SampleTrees {

    static final long CAMPUS_TOTAL_BYTES = 915_060L;
    static final long LECTURES_TOTAL_BYTES = 12_200L;

    /** Handles to the interesting directories of one freshly built campus tree. */
    record Campus(DirectoryNode root, DirectoryNode lectures, DirectoryNode week01, DirectoryNode media) {
    }

    private SampleTrees() {
    }

    static Campus campus() {
        DirectoryNode root = new DirectoryNode("campus");
        root.addFile("readme.md", 300);

        DirectoryNode lectures = root.addDirectory("lectures");
        DirectoryNode week01 = lectures.addDirectory("week01");
        week01.addFile("intro.pdf", 5_000);
        week01.addFile("notes.txt", 120);
        DirectoryNode week02 = lectures.addDirectory("week02");
        week02.addFile("visitor.pdf", 7_000);
        week02.addFile("notes.txt", 80);
        lectures.addDirectory("empty-week");

        DirectoryNode media = root.addDirectory("media");
        media.addFile("lecture-01.mp4", 900_000);
        DirectoryNode thumbs = media.addDirectory("thumbs");
        thumbs.addFile("a.png", 2_048);
        thumbs.addFile("b.png", 512);
        return new Campus(root, lectures, week01, media);
    }
}
