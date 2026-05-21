#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
snippet = (root / "tools" / "fallback_61_120_snippet.txt").read_text(encoding="utf-8")
fa, bike = snippet.split("===BIKE===")
fa = fa.replace("===FIRST_AID===", "").strip()
bike = bike.strip()

java_path = root / "app" / "src" / "main" / "java" / "com" / "marc" / "helmet" / "utils" / "MarcLocalResponder.java"
java = java_path.read_text(encoding="utf-8")

marker_fa = (
    '            new Entry("Spine Injury",\n'
    '                    "Do not move the person at all. Support head and neck in position found. Call ambulance immediately.",\n'
    '                    "spine", "spinal", "paralysis", "neck")\n'
    "    );\n\n"
    "    private static final List<Entry> BIKE_REPAIR"
)
insert_fa = (
    '            new Entry("Spine Injury",\n'
    '                    "Do not move the person at all. Support head and neck in position found. Call ambulance immediately.",\n'
    '                    "spine", "spinal", "paralysis", "neck"),\n'
    + fa
    + "\n    );\n\n"
    "    private static final List<Entry> BIKE_REPAIR"
)
if marker_fa not in java:
    raise SystemExit("FA marker not found")
java = java.replace(marker_fa, insert_fa)

marker_bike = (
    '            new Entry("Engine Oil Pressure Warning Light",\n'
    '                    "Stop the engine immediately. Check oil level. If low, top up. If light stays on even after topping up, do not ride — take the bike to a mechanic as it can cause major engine damage.",\n'
    '                    "oil", "pressure", "warning", "light")\n'
    "    );\n\n"
    "    public static String respond"
)
insert_bike = (
    '            new Entry("Engine Oil Pressure Warning Light",\n'
    '                    "Stop the engine immediately. Check oil level. If low, top up. If light stays on even after topping up, do not ride — take the bike to a mechanic as it can cause major engine damage.",\n'
    '                    "oil", "pressure", "warning", "light"),\n'
    + bike
    + "\n    );\n\n"
    "    public static String respond"
)
if marker_bike not in java:
    raise SystemExit("Bike marker not found")
java = java.replace(marker_bike, insert_bike)

java = java.replace(
    'return bestFirstAid.entry.title + ": " + bestFirstAid.entry.description;',
    "return formatReply(bestFirstAid.entry);",
)
java = java.replace(
    'return bestBike.entry.title + ": " + bestBike.entry.description;',
    "return formatReply(bestBike.entry);",
)

if "formatReply" not in java:
    fmt = (
        "\n    private static String formatReply(Entry e) {\n"
        '        return e.title + "\\n\\n" + e.description;\n'
        "    }\n"
    )
    java = java.replace(
        "    public static String respond(String userText) {", fmt + "    public static String respond(String userText) {"
    )

java_path.write_text(java, encoding="utf-8")
print("Patched", java_path, "lines", java.count("\n") + 1)
