from pathlib import Path
text = Path(r"c:\Users\ranjith\Downloads\Marc\app\src\main\java\com\marc\helmet\utils\MarcLocalResponder.java").read_text(encoding="utf-8")
fa = text.split("private static final List<Entry> BIKE_REPAIR")[0].split("FIRST_AID = Arrays.asList(")[1]
bike = text.split("private static final List<Entry> BIKE_REPAIR = Arrays.asList(")[1].split("public static String respond")[0]
print("FIRST_AID entries:", fa.count("new Entry("))
print("BIKE entries:", bike.count("new Entry("))
