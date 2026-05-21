from pathlib import Path
lines = Path(r"c:\Users\ranjith\Downloads\Marc\app\src\main\java\com\marc\helmet\utils\MarcLocalResponder.java").read_text(encoding="utf-8").splitlines()
for i, line in enumerate(lines, 1):
    if 235 <= i <= 795:
        n = line.count('"') - line.count('\\"')
        if n % 2 == 1:
            print("odd", i, line[:100])
