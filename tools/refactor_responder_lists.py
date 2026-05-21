#!/usr/bin/env python3
"""Split giant Arrays.asList into base + extra arrays to fix javac limits/issues."""
from pathlib import Path

path = Path(r"c:\Users\ranjith\Downloads\Marc\app\src\main\java\com\marc\helmet\utils\MarcLocalResponder.java")
text = path.read_text(encoding="utf-8")

def extract_block(name, next_marker):
    start = text.index(f"private static final List<Entry> {name} = Arrays.asList(")
    start_body = start + len(f"private static final List<Entry> {name} = Arrays.asList(")
    end = text.index(next_marker, start_body)
    body = text[start_body:end].strip()
    return body

fa_body = extract_block("FIRST_AID", "    private static final List<Entry> BIKE_REPAIR")
bike_body = extract_block("BIKE_REPAIR", "    public static String respond")

def split_at_comment(body, comment_substr):
    idx = body.index(comment_substr)
    base = body[:idx].rstrip()
    if base.endswith(","):
        base = base[:-1].rstrip()
    extra = body[idx:].strip()
    return base, extra

fa_base, fa_extra = split_at_comment(fa_body, "// --- first aid ids 61-90 ---")
bike_base, bike_extra = split_at_comment(bike_body, "// --- bike repair ids 61-90 ---")

def to_array(name, entries_src):
    # entries_src is sequence of "new Entry..." lines
    lines = [ln for ln in entries_src.splitlines() if ln.strip()]
    out = [f"    private static final Entry[] {name} = {{"]
    for ln in lines:
        s = ln.strip()
        if s.startswith("//"):
            out.append("            " + s)
            continue
        if s.endswith("),"):
            s = s[:-2] + "},"
        elif s.endswith(")"):
            s = s[:-1] + "},"
        out.append("            " + s)
    out.append("    };")
    return "\n".join(out)

fa_base_arr = to_array("FIRST_AID_BASE", fa_base)
fa_extra_arr = to_array("FIRST_AID_EXTRA_61_120", fa_extra)
bike_base_arr = to_array("BIKE_REPAIR_BASE", bike_base)
bike_extra_arr = to_array("BIKE_REPAIR_EXTRA_61_120", bike_extra)

helper = """
    @SafeVarargs
    private static List<Entry> mergeEntries(Entry[]... parts) {
        List<Entry> out = new ArrayList<>();
        for (Entry[] part : parts) {
            out.addAll(Arrays.asList(part));
        }
        return out;
    }

    private static String formatReply(Entry e) {
        return e.title + "\\n\\n" + e.description;
    }

"""

new_lists = f"""
    {fa_base_arr}

    {fa_extra_arr}

    private static final List<Entry> FIRST_AID =
            mergeEntries(FIRST_AID_BASE, FIRST_AID_EXTRA_61_120);

    {bike_base_arr}

    {bike_extra_arr}

    private static final List<Entry> BIKE_REPAIR =
            mergeEntries(BIKE_REPAIR_BASE, BIKE_REPAIR_EXTRA_61_120);

"""

# Replace from FIRST_AID declaration through end of BIKE_REPAIR declaration
start = text.index("    private static final List<Entry> FIRST_AID = Arrays.asList(")
end = text.index("    public static String respond(String userText) {")

prefix = text[:start]
suffix = text[end:]

# Remove duplicate formatReply if present in suffix
if "formatReply" in suffix and "private static String formatReply" not in prefix:
    pass

if "mergeEntries" not in prefix:
    # insert helper before FIRST_AID_BASE
    insert_at = prefix.rfind("    // --- Data")
    if insert_at == -1:
        insert_at = prefix.rfind("    private static final class Entry {")
        insert_at = prefix.index("    }", insert_at) + len("    }") + 1
    prefix = prefix[:insert_at] + helper + prefix[insert_at:]

new_text = prefix + new_lists + suffix

# Ensure respond uses formatReply
new_text = new_text.replace(
    'return bestFirstAid.entry.title + ": " + bestFirstAid.entry.description;',
    "return formatReply(bestFirstAid.entry);",
)
new_text = new_text.replace(
    'return bestBike.entry.title + ": " + bestBike.entry.description;',
    "return formatReply(bestBike.entry);",
)

path.write_text(new_text, encoding="utf-8")
print("Refactored", path, "lines", new_text.count("\n") + 1)
