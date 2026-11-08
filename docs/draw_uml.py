#!/usr/bin/env python3
"""Draws docs/uml-class-diagram.png (UML class diagram of the Visitor file system)."""
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
from matplotlib.patches import FancyBboxPatch  # noqa: E402

OUT = Path(__file__).resolve().parent / "uml-class-diagram.png"
LINE_H = 2.05
INK, MUTED, BORDER = "#0b0b0b", "#52514e", "#3b3b3b"
FILL = {"interface": "#e8f0fb", "abstract": "#fdf0e8", "class": "#ffffff", "record": "#eaf7f1"}

fig, ax = plt.subplots(figsize=(15, 10))
ax.set_xlim(0, 150)
ax.set_ylim(0, 100)
ax.axis("off")
boxes = {}


def box(key, x, y_top, w, name, kind="class", stereotype=None, fields=(), methods=()):
    header = ([f"«{stereotype}»"] if stereotype else []) + [name]
    h = (len(header) + len(fields) + len(methods)) * LINE_H + 2.2 + (0.8 if fields else 0)
    y = y_top - h
    ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle="square,pad=0", fc=FILL[kind], ec=BORDER, lw=1.3))
    cy = y_top - 1.4
    for k, line in enumerate(header):
        italic = kind in ("interface", "abstract") and k == len(header) - 1
        ax.text(x + w / 2, cy, line, ha="center", va="center", fontsize=9.5 if k == len(header) - 1 else 8,
                fontweight="bold" if k == len(header) - 1 else "normal",
                style="italic" if italic else "normal", color=INK if k == len(header) - 1 else MUTED)
        cy -= LINE_H
    cy += LINE_H * 0.35
    ax.plot([x, x + w], [cy, cy], color=BORDER, lw=1)
    cy -= LINE_H * 0.65
    for line in fields:
        ax.text(x + 0.8, cy, line, ha="left", va="center", fontsize=7.8, family="monospace", color=INK)
        cy -= LINE_H
    if fields:
        cy += LINE_H * 0.35
        ax.plot([x, x + w], [cy, cy], color=BORDER, lw=1)
        cy -= LINE_H * 0.65
    for line in methods:
        ax.text(x + 0.8, cy, line, ha="left", va="center", fontsize=7.8, family="monospace", color=INK)
        cy -= LINE_H
    boxes[key] = (x, y, w, h)


def anchor(key, side, frac=0.5):
    x, y, w, h = boxes[key]
    return {"top": (x + w * frac, y + h), "bottom": (x + w * frac, y),
            "left": (x, y + h * frac), "right": (x + w, y + h * frac)}[side]


def arrow(start, end, kind, label=None, label_pos=0.5, label_offset=(0, 0), connection="arc3"):
    styles = {
        "extends": dict(arrowstyle="-|>,head_length=1.2,head_width=0.6", ls="-", fc="white"),
        "implements": dict(arrowstyle="-|>,head_length=1.2,head_width=0.6", ls="--", fc="white"),
        "uses": dict(arrowstyle="->,head_length=0.8,head_width=0.4", ls="--", fc=BORDER),
        "composition": dict(arrowstyle="-", ls="-", fc=BORDER),
    }
    st = styles[kind]
    ax.annotate("", xy=end, xytext=start,
                arrowprops=dict(arrowstyle=st["arrowstyle"], linestyle=st["ls"], color=BORDER, lw=1.2,
                                fc=st["fc"], ec=BORDER, shrinkA=0, shrinkB=0, connectionstyle=connection))
    if label:
        lx = start[0] + (end[0] - start[0]) * label_pos + label_offset[0]
        ly = start[1] + (end[1] - start[1]) * label_pos + label_offset[1]
        ax.text(lx, ly, label, fontsize=7.8, color=MUTED, ha="center", va="center",
                bbox=dict(fc="white", ec="none", pad=0.6))


# ---------------------------------------------------------------- model (left)
box("Element", 14, 97, 38, "Element", "interface", "interface",
    methods=("+ name(): String", "+ parent(): Optional<DirectoryNode>", "+ accept(v: Visitor): void"))
box("Abstract", 14, 79, 38, "AbstractElement", "abstract", "abstract, sealed",
    fields=("- name: String", "- parent: DirectoryNode"),
    methods=("~ attachTo(p: DirectoryNode)", "~ detach()", "- requireValidName(n)"))
box("File", 1, 53, 33, "FileNode", "class",
    fields=("- sizeInBytes: long  {>= 0}",),
    methods=("+ size(): long", "+ resize(bytes: long)", "+ accept(v) { v.visit(this) }"))
box("Dir", 37, 53, 40, "DirectoryNode", "class",
    fields=("- childrenByName: TreeMap<String,", "      AbstractElement>"),
    methods=("+ add(f: FileNode): FileNode", "+ add(d: DirectoryNode): DirectoryNode",
             "+ addFile(name, size) / addDirectory(name)", "+ remove(name): Optional<Element>",
             "+ children(): List<Element>  {read-only}", "+ accept(v) { v.visit(this) }",
             "- rejectCycle / rejectSecondParent /", "  rejectDuplicateName"))
box("Exc", 1, 24, 33, "TreeIntegrityException", "class", fields=(), methods=("extends RuntimeException",))

# ---------------------------------------------------------------- visitors (right)
box("Visitor", 92, 97, 34, "Visitor", "interface", "interface",
    methods=("+ visit(f: FileNode): void", "+ visit(d: DirectoryNode): void"))
box("TreeVisitor", 87, 81, 44, "TreeVisitor<R>", "abstract", "abstract  (traversal owner)",
    fields=("- pathFromRoot: Deque<String>", "- currentDepth: int  {-1 = idle}"),
    methods=("+ run(root: Element): R", "+ visit(FileNode)  {final}",
             "+ visit(DirectoryNode)  {final, visits children}", "+ result(): R  {abstract}",
             "# resetState()  {abstract, called per run}", "# onFile(f, ctx: VisitContext)",
             "# onDirectory(d, ctx: VisitContext)"))
box("Size", 84, 44, 19.5, "TotalSizeVisitor", "class", fields=("- totalBytes: long",),
    methods=("TreeVisitor<Long>",))
box("Search", 105.5, 44, 24, "SearchVisitor", "class",
    fields=("- nameCriterion", "- minSizeInclusive", "- matchingPaths"),
    methods=("TreeVisitor<List<String>>",))
box("Stats", 131, 44, 18.5, "StatisticsVisitor", "class",
    fields=("- fileCount", "- directoryCount", "- maxDepth"),
    methods=("TreeVisitor<", "  TreeStatistics>"))
box("Ctx", 133, 97, 16.5, "VisitContext", "record", "record",
    fields=("relativePath", "depth: int"))
box("Name", 99, 20, 30, "NameCriterion", "interface", "interface",
    methods=("+ matches(name): boolean", "any() exactly() containing()", "withExtension() glob()"))
box("TStats", 131, 20, 18.5, "TreeStatistics", "record", "record",
    fields=("fileCount", "directoryCount", "maxDepth"))

# ---------------------------------------------------------------- relations
arrow(anchor("Abstract", "top"), anchor("Element", "bottom"), "implements")
arrow(anchor("File", "top"), anchor("Abstract", "bottom", 0.3), "extends")
arrow(anchor("Dir", "top"), anchor("Abstract", "bottom", 0.7), "extends")
arrow(anchor("TreeVisitor", "top", 0.5), anchor("Visitor", "bottom", 0.6), "implements")
for key, frac in (("Size", 0.25), ("Search", 0.55), ("Stats", 0.85)):
    arrow(anchor(key, "top"), anchor("TreeVisitor", "bottom", frac), "extends")
# composition: DirectoryNode owns 0..* Element
dx, dy, dw, dh = boxes["Dir"]
ex, ey, ew, eh = boxes["Element"]
route_x = dx + dw + 2.5
ax.plot([dx + dw, route_x, route_x, ex + ew],
        [dy + dh - 3, dy + dh - 3, ey + 1.2, ey + 1.2], color=BORDER, lw=1.2)
ax.plot([dx + dw], [dy + dh - 3], marker="D", markersize=8, color=BORDER)
ax.text(route_x - 0.6, (dy + dh + ey) / 2, "children\n0..*", fontsize=7.8, color=MUTED, va="center", ha="right")
ax.text(dx + dw + 1.2, dy + dh - 1.6, "1", fontsize=7.8, color=MUTED)
# dependencies (double dispatch)
vx = boxes["Visitor"][0]
arrow((ex + ew, 92.5), (vx, 92.5), "uses", label="dispatch 1: element.accept(visitor) runs the accept of the runtime element type",
      label_offset=(0, 1.5))
arrow((vx, 88.5), (ex + ew, 88.5), "uses", label="dispatch 2: accept calls visitor.visit(this) -> matching overload",
      label_offset=(0, -1.5))
arrow(anchor("TreeVisitor", "right", 0.85), anchor("Ctx", "bottom"), "uses", label="creates", label_pos=0.45,
      label_offset=(2.5, 0))
arrow(anchor("Search", "bottom", 0.5), anchor("Name", "top", 0.4), "uses", label="uses", label_pos=0.5,
      label_offset=(2.2, 0))
arrow(anchor("Stats", "bottom", 0.5), anchor("TStats", "top", 0.5), "uses", label="returns", label_pos=0.5,
      label_offset=(3.2, 0))
arrow(anchor("Dir", "bottom", 0.2), anchor("Exc", "right", 0.5), "uses", label="throws", label_pos=0.5,
      label_offset=(1, 1.2), connection="arc3,rad=-0.2")

# extension boundary
ax.plot([82, 82], [2, 82], color="#eb6834", lw=1.4, ls=(0, (6, 4)))
ax.text(81, 3.5, "model package: closed for new operations", ha="right", fontsize=8.5, color="#b8481d")
ax.text(83, 3.5, "visitor package: new operation = new TreeVisitor subclass", ha="left", fontsize=8.5,
        color="#b8481d")

# legend
legend_y = 9
ax.text(36, legend_y + 4.5, "Notation", fontsize=8.5, fontweight="bold", color=INK)
for k, (kind, text) in enumerate((("extends", "extends"), ("implements", "implements"), ("uses", "uses / dispatch"))):
    yy = legend_y + 2 - k * 2.6
    arrow((36, yy), (45, yy), kind)
    ax.text(46.5, yy, text, fontsize=8, va="center", color=MUTED)
ax.plot([60, 66], [legend_y + 2, legend_y + 2], color=BORDER, lw=1.2)
ax.plot([60], [legend_y + 2], marker="D", markersize=7, color=BORDER)
ax.text(67, legend_y + 2, "composition", fontsize=8, va="center", color=MUTED)

fig.suptitle("Assignment 06 — UML class diagram: Visitor pattern over an in-memory file system",
             x=0.02, ha="left", fontsize=13, fontweight="bold", color=INK)
fig.tight_layout(rect=(0, 0, 1, 0.97))
fig.savefig(OUT, dpi=170, facecolor="white")
print("wrote", OUT)
