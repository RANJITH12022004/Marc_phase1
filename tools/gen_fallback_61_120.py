#!/usr/bin/env python3
"""Generate Java Entry blocks for MarcLocalResponder ids 61-120."""

def kw_from_title(title):
    stop = {"from", "the", "a", "an", "or", "and", "in", "on", "at", "to", "of", "not", "is", "are", "with", "after", "while", "when", "only", "for", "if", "be", "by", "up", "too", "all", "no", "one", "two"}
    words = []
    for w in title.lower().replace("/", " ").replace("-", " ").split():
        w = "".join(c for c in w if c.isalnum())
        if len(w) >= 3 and w not in stop:
            words.append(w)
    return words[:6] if words else ["help"]

def entry(title, desc, extra_kw=None):
    kws = kw_from_title(title)
    if extra_kw:
        for k in extra_kw:
            if k not in kws:
                kws.append(k)
    kw_str = ", ".join(f'"{k}"' for k in kws[:8])
    t = title.replace("\\", "\\\\").replace('"', '\\"')
    d = desc.replace("\\", "\\\\").replace('"', '\\"')
    return f'            new Entry("{t}",\n                    "{d}",\n                    {kw_str})'

BIKE_61_90 = [
("Bike Not Starting in Gear", "Most bikes have a safety switch on the side stand or clutch. Check if the side stand is fully up. Ensure the bike is in neutral. Clean or adjust the side stand switch. Temporarily bypass only for testing — never ride with it bypassed permanently.", ["gear", "neutral", "stand"]),
("White Smoke from Exhaust", "White smoke usually means coolant or water entering the combustion chamber. Check coolant level and look for oil-water mixing. This can indicate a damaged head gasket or cracked cylinder head. Stop riding and get it checked by a mechanic immediately.", ["white", "smoke", "coolant"]),
("Rear Brake Not Effective", "Check rear brake pedal free play. Adjust the brake rod or cable. Inspect brake shoes or pads for wear. Clean the brake drum or disc. Bleed the brake if it feels spongy.", ["rear", "brake"]),
("Front Brake Lever Too Soft", "Bleed the front brake system to remove air. Check brake fluid level and top up if low. Inspect for leaks in hoses and caliper. Replace brake fluid every 2 years.", ["front", "brake", "soft", "spongy"]),
("Engine Idling Too Low", "Warm up the engine. Locate the idle adjustment screw on the carburetor and turn it slightly clockwise to increase RPM. Clean the pilot jet. Ideal idle speed is usually 1200-1500 RPM.", ["idle", "rpm", "low"]),
("Bike Shuts Off While Riding", "Check for loose wiring, especially around the ignition coil and CDI. Inspect fuel flow. Clean spark plug. This can also happen due to overheating or faulty kill switch.", ["shuts", "off", "riding", "stall"]),
("Steering Feels Heavy", "Check tyre pressure. Lubricate or adjust steering head bearings. Ensure front forks are not bent. Low tyre pressure is the most common cause.", ["steering", "heavy"]),
("Silencer Getting Red Hot", "This indicates a lean fuel mixture. Clean carburetor jets and check air filter. Do not ride if the silencer is glowing red — it can cause fire.", ["silencer", "red", "hot", "lean"]),
("Oil Leak from Engine Cover", "Tighten all cover bolts in a criss-cross pattern. Replace the gasket if damaged. Clean the area and monitor the leak. Low oil can damage the engine.", ["oil", "leak", "cover"]),
("Poor Mileage", "Clean air filter, spark plug, and carburetor. Check tyre pressure. Avoid aggressive riding. Use good quality fuel. Decarbonize the engine if it has high mileage.", ["mileage", "fuel", "consumption"]),
("Self Starter Turns but Engine Not Starting", "Check spark plug for spark. Ensure fuel is reaching the carburetor. Check kill switch position. Inspect timing and ignition system.", ["starter", "turns", "crank"]),
("Chain Rust and Stiff", "Clean the chain with kerosene or chain cleaner. Apply fresh chain lube generously. If the chain is badly rusted, replace both chain and sprockets together.", ["chain", "rust", "stiff"]),
("Horn Sounds Weak", "Clean horn terminals and ground connection. Check voltage at the horn. Replace the horn if it is old or damaged by water.", ["horn", "weak"]),
("Front Tyre Wobbling", "Check if the wheel rim is bent. Tighten wheel spokes evenly. Check wheel bearings for play. Balance the wheel if needed.", ["front", "tyre", "wobble"]),
("Engine Missing on One Cylinder (in twin cylinder bikes)", "Check both spark plugs. Clean or replace the faulty one. Inspect high tension wires. Clean carburetor or injectors on that side.", ["missing", "cylinder", "twin"]),
("Brake Light Not Working", "Check brake light switch at the brake pedal or lever. Replace the bulb. Check wiring and fuse.", ["brake", "light"]),
("Fuel Tank Empty Fast", "Check for leaks in tank, pipes, and carburetor. Riding style also affects mileage. Get the carburetor tuned properly.", ["fuel", "empty", "fast"]),
("Clutch Cable Broken", "Replace the clutch cable immediately. Lubricate the new cable properly and adjust free play. Do not ride without clutch.", ["clutch", "cable", "broken"]),
("Engine Sound Abnormal (Ticking)", "Check engine oil level and condition. Ticking can be due to loose tappet adjustment or low oil pressure. Get it checked soon.", ["ticking", "sound", "noise"]),
("Speedometer Cable Broken", "Replace the speedometer cable. Apply grease before installing. Digital speedometers may need sensor replacement.", ["speedometer", "cable"]),
("Bike Stalls When Brake Applied", "This usually happens due to low idle speed. Increase idle RPM slightly. Clean carburetor and check air-fuel mixture.", ["stall", "brake"]),
("Rear View Mirror Loose", "Tighten the mirror lock nut properly. Use thread locker if available. Replace the mirror if threads are damaged.", ["mirror", "loose"]),
("Petrol Overflow from Carburetor", "Float needle valve is stuck or dirty. Clean the float chamber and needle. Replace needle valve if worn.", ["overflow", "carburetor", "float"]),
("Tyre Heating Up Too Much", "Check air pressure — it may be too low. Avoid overloading the bike. Check wheel alignment.", ["tyre", "heating", "hot"]),
("Neutral Gear Hard to Find", "Adjust clutch cable free play. Check engine oil quality. The gear shifter mechanism may need lubrication or adjustment.", ["neutral", "gear", "hard"]),
("Battery Draining Quickly", "Check for parasitic drain (something left on). Test alternator charging. Clean terminals. Replace battery if it is more than 2-3 years old.", ["battery", "drain", "draining"]),
("Exhaust Leak", "Tighten exhaust manifold nuts. Replace exhaust gasket if damaged. Leaking exhaust can cause noise and power loss.", ["exhaust", "leak"]),
("Handlebar Vibration at High Speed", "Check wheel balancing and alignment. Tighten steering bearings. Check tyre condition and pressure.", ["handlebar", "vibration", "speed"]),
("Kick Starter Slipping", "Check kick starter ratchet mechanism. Clean and grease it. Replace worn ratchet if needed.", ["kick", "slipping"]),
("Engine Hesitation During Acceleration", "Clean main jet and needle jet in carburetor. Check spark plug condition. Ensure good fuel flow and clean air filter.", ["hesitation", "acceleration"]),
]

BIKE_91_120 = [
("Bike Starts Only in Neutral", "This is due to the neutral safety switch or side stand switch. Clean both switches thoroughly. Adjust or replace the faulty switch. You can temporarily bypass for testing but get it fixed properly for safety.", ["neutral", "safety", "switch"]),
("Blue Smoke from Exhaust", "Blue smoke indicates engine oil is burning. Check engine oil level. Worn piston rings, valve stem seals, or cylinder wear are common causes. Get a compression test done at a mechanic.", ["blue", "smoke", "oil"]),
("Front Suspension Hard", "Check front fork oil level and quality. If oil is less or dirty, change the fork oil. Replace fork seals if leaking. Adjust preload if your bike has adjustable suspension.", ["suspension", "fork", "hard"]),
("Rear Tyre Burst While Riding", "Stay calm, hold handlebar firmly and slow down gradually without sudden braking. Stop at a safe place. Replace or repair the tyre immediately. Always carry a puncture kit and air pump.", ["burst", "tyre", "blowout"]),
("Carburetor Overflow", "Float needle is stuck or dirty. Remove the float chamber, clean the needle valve and seat. Replace the needle if it has grooves or is worn out.", ["carburetor", "overflow", "float"]),
("No Electric Power at All", "Check main fuse near the battery. Clean battery terminals. Measure battery voltage. Check for loose or broken main wiring harness.", ["electric", "power", "fuse", "dead"]),
("Engine Backfiring", "Usually caused by lean mixture or exhaust leak. Check air filter, carburetor settings, and exhaust gasket. Decarbonize if needed.", ["backfire", "backfiring"]),
("Wheel Spokes Loose", "Tighten spokes evenly in a criss-cross pattern using a spoke wrench. Do not over-tighten. Get the wheel trued at a cycle/mechanic shop.", ["spokes", "loose", "wheel"]),
("Throttle Stuck Open", "Immediately switch off the engine using kill switch. Stop the bike safely. Lubricate or replace the throttle cable. This is very dangerous — fix immediately.", ["throttle", "stuck", "open"]),
("Low Pickup in New Bike", "Most new bikes need running-in. Do not ride above 60-70% of max RPM for first 500-1000 km. Service at 500 km as per manufacturer schedule.", ["new", "pickup", "running"]),
("Sprocket Teeth Worn", "Inspect chain and both front & rear sprockets. If teeth are hooked or sharp, replace chain and sprocket kit together.", ["sprocket", "worn", "teeth"]),
("Coolant Leak (Liquid Cooled)", "Check radiator, hoses, and water pump for leaks. Top up coolant only with recommended mixture. Tighten hose clamps. Get it repaired before riding.", ["coolant", "leak", "radiator"]),
("Bike Jerks in Gear", "Check chain tension and lubrication. Adjust clutch cable. Clean spark plug. This can also happen due to worn clutch plates.", ["jerks", "gear"]),
("Regulator Rectifier Faulty", "Battery overcharges or drains quickly. The regulator rectifier is usually the culprit. Replace it with a good quality one.", ["regulator", "rectifier"]),
("Side Stand Bent", "Straighten the side stand if possible. Weld reinforcement if needed. Check side stand switch functioning after repair.", ["side", "stand", "bent"]),
("Engine Oil Consumption High", "Check for leaks. Worn piston rings or valve guides are common reasons. Monitor oil level daily and get engine checked.", ["oil", "consumption", "burning"]),
("LED Headlight Very Dim", "Check voltage supply. Clean connectors. Many cheap LED headlights fail quickly — replace with good branded ones.", ["led", "headlight", "dim"]),
("Gear Slipping", "Check clutch adjustment. Worn clutch plates or damaged gear dogs are common causes. Needs transmission repair.", ["gear", "slipping"]),
("Rust on Disc Brake", "Clean the disc with brake cleaner. Light surface rust is normal. If deep grooves are present, replace the disc.", ["disc", "rust", "brake"]),
("Bike Smells of Burning Rubber", "Check if chain is too tight, tyre rubbing anywhere, or clutch slipping. Stop immediately and inspect.", ["burning", "rubber", "smell"]),
("Neutral Indicator Not Working", "Clean neutral switch on the gearbox. Check wiring and bulb. Common problem on older bikes.", ["neutral", "indicator"]),
("Fork Oil Seal Replacement", "Oil leakage from fork requires seal replacement. Drain old oil, replace seals and dust seals, refill with correct quantity and grade of fork oil.", ["fork", "seal", "oil"]),
("Fuel Pump Noise (FI Bikes)", "Continuous whining noise from fuel pump is normal when ignition is on. If very loud or absent, check fuel filter and pump condition.", ["fuel", "pump", "noise", "fi"]),
("Handlebar Heating Up", "Check if exhaust is touching cables or fairing. Ensure proper routing of wires. Apply heat resistant tape if needed.", ["handlebar", "heating"]),
("Loss of Top Speed", "Clean air filter, spark plug, carburetor, exhaust. Check tyre pressure and chain lubrication. Restricted exhaust is a common cause.", ["top", "speed", "loss"]),
("Battery Acid Leak", "Clean the area immediately with baking soda solution. Replace the battery. Acid leak can damage wiring and frame.", ["battery", "acid", "leak"]),
("Piston Seizure", "Engine locks suddenly. Usually due to low oil, overheating or lean mixture. This needs major engine repair or replacement.", ["piston", "seizure", "seized"]),
("All Lights Dim While Running", "Indicates weak charging system. Check magneto, regulator, and battery. Clean all earth connections.", ["lights", "dim", "charging"]),
("Chain Jumping Off Sprocket", "Chain is badly worn or sprocket teeth are damaged. Replace chain and sprocket set together. Adjust tension correctly.", ["chain", "jumping", "sprocket"]),
("General Bike Not Starting Checklist", "1. Fuel in tank 2. Spark plug & spark 3. Air filter clean 4. Battery charged 5. Kill switch ON 6. Side stand up 7. In Neutral or clutch pressed. Follow this order before calling mechanic.", ["not", "starting", "checklist"]),
]

FA_61_90 = [
("Burns from Exhaust Pipe", "Cool the burnt area immediately under running cool water for at least 10-15 minutes. Do not apply ice directly on the burn. Gently remove any clothing if not stuck to the skin. Cover the burn with a clean, non-stick sterile dressing or cloth. Do not burst any blisters. Give pain relief if needed and seek medical help for deep or large burns.", ["burn", "exhaust"]),
("Chest Pain after Impact", "Make the person sit in a comfortable semi-upright position. Loosen tight clothing. Monitor breathing and pulse. If breathing is difficult, chest pain is severe, or the person feels dizzy, call ambulance immediately as it may indicate rib fracture or internal injury.", ["chest", "impact"]),
("Calf Muscle Tear or Cramp", "Rest the leg completely. Gently stretch the calf muscle once the acute pain reduces. Apply ice wrapped in cloth for 15-20 minutes. Use compression bandage. Elevate the leg. Avoid putting full weight for a few days.", ["calf", "cramp", "tear"]),
("Jaw or Facial Fracture", "Support the jaw gently with your hand or a bandage wrapped around the head. Apply ice to reduce swelling. Do not allow the person to eat or drink. Take them to hospital immediately for X-ray.", ["jaw", "facial", "fracture"]),
("Multiple Abrasions on Legs", "Clean all wounds thoroughly with clean water or saline. Remove visible dirt carefully. Apply antiseptic cream and cover with sterile gauze. Change dressing daily. Watch for signs of infection such as redness, swelling, pus or fever.", ["abrasions", "legs"]),
("Suspected Spine Injury", "Do not move the person at all. Support the head and neck in the position found using rolled towels or hands. Keep them still. Call ambulance immediately and monitor breathing until professional help arrives.", ["spine", "spinal"]),
("Hand Fracture", "Immobilize the hand using a padded splint or rolled newspaper. Elevate the hand above heart level. Apply ice to reduce swelling. Give pain relief and take to hospital for X-ray and proper treatment.", ["hand", "fracture"]),
("Dehydration and Heat Stroke", "Move the rider to a shaded or cool area. Loosen tight clothing. Give oral rehydration solution or water with salt and sugar slowly. Wet the body with cool water and fan them. If the person is confused or unconscious, seek emergency medical help.", ["dehydration", "heat", "stroke"]),
("Thumb Sprain or Fracture", "Immobilize the thumb by taping it to the palm or using a splint. Apply ice and elevate the hand. Avoid using the thumb. Seek medical attention if swelling is severe or movement is very painful.", ["thumb", "sprain"]),
("Eye Injury from Dust or Foreign Object", "Do not rub the eye. Rinse gently with clean water. If the object is embedded, cover both eyes with a clean cloth and seek medical help immediately. Do not try to remove embedded objects yourself.", ["eye", "foreign", "dust"]),
("Deep Cut on Palm", "Apply firm pressure with a clean cloth to stop bleeding. Elevate the hand above heart level. Once bleeding stops, clean gently and cover with sterile dressing. Deep cuts may need stitches — go to hospital.", ["palm", "cut", "bleeding"]),
("Hip or Pelvic Bruise", "Make the person lie down in a comfortable position. Apply ice to the area. Avoid walking or putting weight. Pelvic injuries can be serious — seek medical evaluation.", ["hip", "pelvic", "bruise"]),
("Finger Cut with Heavy Bleeding", "Apply direct pressure on the wound. Elevate the hand. Wrap a clean cloth firmly. If bleeding does not stop after 10 minutes, seek immediate medical help.", ["finger", "bleeding"]),
("Lower Leg Fracture", "Do not move the leg. Immobilize using a splint or by tying both legs together gently. Apply ice. Treat for shock and rush to hospital immediately.", ["lower", "leg", "fracture"]),
("Neck Strain or Whiplash", "Support the neck in a comfortable position using a soft collar made from cloth. Apply ice. Avoid sudden neck movements. Consult a doctor for proper assessment.", ["whiplash", "neck", "strain"]),
("Road Rash on Torso", "Clean the area gently. Apply antiseptic cream. Cover with large sterile dressing. Change dressing regularly and watch for infection.", ["torso", "rash"]),
("Broken Ribs", "Encourage gentle breathing. Apply ice to the painful area. Give pain relief. Do not wrap the chest tightly. Seek medical help if breathing becomes difficult.", ["ribs", "broken"]),
("Ankle Fracture", "Do not put weight on the ankle. Immobilize with a splint or bandage. Elevate the foot. Apply ice and take to hospital for X-ray.", ["ankle", "fracture"]),
("Concussion Symptoms", "If the person has headache, dizziness, vomiting or confusion after head injury, treat as concussion. Keep them awake and monitor closely. Seek immediate medical attention.", ["concussion", "headache", "vomiting"]),
("Shoulder Blade Fracture", "Support the arm with a sling. Apply ice. Avoid shoulder movement. Take the person to hospital for proper diagnosis.", ["shoulder", "blade", "scapula"]),
("Crushed Fingers", "Elevate the hand. Apply ice. Cover with clean dressing. Seek urgent medical care as crushed injuries need specialist treatment.", ["crushed", "fingers"]),
("Heat Exhaustion", "Move to cool place. Rest. Give fluids slowly. Cool the body with wet cloth. If symptoms worsen (vomiting, confusion), treat as heat stroke and call ambulance.", ["heat", "exhaustion"]),
("Knee Cap Dislocation", "Do not try to push the kneecap back. Immobilize the leg. Apply ice. Rush to hospital immediately.", ["kneecap", "patella", "dislocation"]),
("Forehead Cut", "Apply pressure to stop bleeding. Clean the wound. Apply sterile dressing. Head wounds bleed heavily — seek medical help if deep.", ["forehead", "cut", "head"]),
("Back Muscle Strain", "Rest in comfortable position. Apply ice for first 48 hours. Avoid bending or lifting. Give pain relief. Seek help if pain radiates to legs.", ["back", "strain", "muscle"]),
("Insect Bite Allergic Reaction", "Remove the sting if present. Apply ice. Give anti-allergic tablet if available. Watch for swelling of face/lips or breathing difficulty — rush to hospital if it occurs.", ["insect", "bite", "allergy"]),
("Shin Bone Bruise", "Rest the leg. Apply ice frequently. Elevate. Use padding for protection. Pain can be severe even if no fracture.", ["shin", "bruise"]),
("Dislocated Toe", "Do not force the toe back. Tape it to the adjacent toe. Apply ice. Seek medical help for proper alignment.", ["toe", "dislocated"]),
("Internal Bleeding Suspicion", "Keep the person lying down with legs elevated. Do not give food or water. Monitor pulse and breathing. Rush to hospital immediately.", ["internal", "bleeding"]),
("Wrist Dislocation", "Support the wrist in a splint without forcing it. Apply ice. Elevate the hand. Take to hospital for proper reduction.", ["wrist", "dislocation"]),
]

FA_91_120 = [
("Severe Road Rash on Thighs", "Gently clean the affected area with clean water or saline solution. Remove any dirt or gravel carefully. Apply antiseptic cream and cover with a non-stick sterile dressing. Change the dressing daily. Watch for signs of infection and seek medical help if the rash is very deep or covers a large area.", ["road", "rash", "thighs"]),
("Broken Nose", "Apply ice wrapped in cloth to reduce swelling. Pinch the nose gently if bleeding. Do not try to straighten the nose yourself. Take the person to hospital for proper evaluation and treatment.", ["nose", "broken"]),
("Elbow Dislocation", "Do not attempt to push the elbow back into place. Support the arm in a sling in the position found. Apply ice to control swelling. Rush to the nearest hospital for professional reduction.", ["elbow", "dislocation"]),
("Abdominal Injury", "Keep the person lying down with knees slightly bent. Do not give any food or water. Apply ice if there is swelling. Watch for signs of internal bleeding and seek emergency medical help immediately.", ["abdominal", "stomach"]),
("Hamstring Tear", "Rest the leg completely. Apply ice for 15-20 minutes every 2-3 hours. Use compression bandage and elevate the leg. Avoid stretching the muscle. Consult a doctor for assessment.", ["hamstring", "tear"]),
("Skull Fracture Suspicion", "Do not apply pressure on the head. Cover any open wound with clean cloth. Keep the person still and call ambulance immediately. Monitor consciousness level.", ["skull", "fracture"]),
("Knuckle Injury", "Immobilize the hand with a padded splint. Apply ice and elevate. Avoid using the hand. Seek medical help for X-ray.", ["knuckle", "injury"]),
("Sunburn on Exposed Skin", "Move to shade. Cool the skin with wet cloth. Apply aloe vera or moisturizer. Give pain relief if needed. Drink plenty of water.", ["sunburn", "skin"]),
("Pelvic Fracture", "Do not move the person. Keep them lying flat on their back. Immobilize both legs by tying them together. Call ambulance immediately as pelvic fractures can cause heavy internal bleeding.", ["pelvic", "fracture"]),
("Lip or Tongue Bite", "Apply ice to reduce swelling. Clean gently with water. If bleeding is heavy, apply pressure with clean gauze. Seek medical help if the cut is deep.", ["lip", "tongue", "bite"]),
("Foot Crush Injury", "Elevate the foot. Apply ice. Cover any open wounds. Do not put weight on the foot. Seek urgent medical care.", ["foot", "crush"]),
("Allergic Reaction to Insect Bite", "Remove the sting if present. Apply ice. Give anti-histamine if available. Watch for difficulty in breathing or swelling of face — rush to hospital immediately if it occurs.", ["allergic", "insect"]),
("Upper Back Injury", "Make the person lie down comfortably. Apply ice. Avoid any bending or twisting. Give pain relief and seek medical evaluation.", ["upper", "back"]),
("Dislocated Jaw", "Support the jaw gently. Apply ice. Do not attempt to fix it yourself. Take the person to hospital for reduction.", ["jaw", "dislocated"]),
("Severe Bruising", "Apply ice for 15-20 minutes every 2 hours. Elevate the area. Give pain relief. Watch for increasing swelling or numbness.", ["bruising", "bruise"]),
("Eye Swelling", "Apply cold compress gently. Do not rub the eye. Seek medical help if vision is affected or pain is severe.", ["eye", "swelling"]),
("Heel Fracture", "Do not put weight on the heel. Immobilize the foot. Apply ice and elevate. Take to hospital for X-ray.", ["heel", "fracture"]),
("Electric Shock from Bike", "Switch off the ignition immediately. Do not touch the person if they are still in contact with current. Check breathing and pulse. Start CPR if needed and call ambulance.", ["electric", "shock"]),
("Multiple Rib Fractures", "Support the chest gently. Apply ice. Encourage slow shallow breathing. Seek immediate hospital care if breathing is difficult.", ["ribs", "multiple"]),
("Finger Amputation Risk", "Control bleeding with firm pressure. Wrap the severed part in clean cloth and place it in a plastic bag over ice. Rush to hospital immediately with the part.", ["amputation", "finger"]),
("Groin Injury", "Rest in comfortable position. Apply ice. Avoid walking. Seek medical help as groin injuries can be serious.", ["groin", "injury"]),
("Scalp Wound", "Apply firm pressure to stop bleeding. Clean gently. Cover with sterile dressing. Scalp wounds bleed heavily — seek medical attention.", ["scalp", "wound"]),
("Tailbone Fracture", "Lie on side or use a cushion with a hole. Apply ice. Avoid sitting for long. Take pain relief and consult a doctor.", ["tailbone", "fracture"]),
("Smoke Inhalation", "Move the person to fresh air. Loosen clothing. Monitor breathing. Seek medical help if coughing, shortness of breath or dizziness persists.", ["smoke", "inhalation"]),
("Deep Thigh Cut", "Apply direct pressure to control bleeding. Elevate the leg. Do not remove the cloth if soaked. Rush to hospital as major arteries may be involved.", ["thigh", "cut", "bleeding"]),
("Nerve Injury in Arm", "Support the arm in a sling. Apply ice. Watch for numbness, tingling or weakness. Seek medical evaluation.", ["nerve", "arm", "numbness"]),
("General Shock", "Keep the person lying down with legs elevated. Keep them warm with a blanket. Do not give anything to eat or drink. Call ambulance.", ["shock", "general"]),
("Broken Collarbone", "Support the arm with a sling. Apply ice. Avoid shoulder movement. Take to hospital for X-ray and treatment.", ["collarbone", "broken"]),
("Severe Headache after Fall", "Rest in a dark quiet place. Apply cold compress. Monitor for vomiting or confusion. Seek immediate medical help if symptoms worsen.", ["headache", "fall"]),
("General Bike Accident First Aid", "1. Ensure scene safety. 2. Check ABC (Airway, Breathing, Circulation). 3. Stop heavy bleeding. 4. Immobilize suspected fractures. 5. Treat for shock. 6. Call ambulance if injury is serious. Stay calm and reassure the injured person.", ["accident", "first", "aid", "checklist"]),
]

def gen_block(entries, comment):
    lines = [f"\n            // --- {comment} ---"]
    for title, desc, extra in entries:
        lines.append(entry(title, desc, extra) + ",")
    return "\n".join(lines)

out = gen_block(BIKE_61_90, "bike repair ids 61-90") + gen_block(BIKE_91_120, "bike repair ids 91-120")
out_fa = gen_block(FA_61_90, "first aid ids 61-90") + gen_block(FA_91_120, "first aid ids 91-120")

path = r"c:\Users\ranjith\Downloads\Marc\tools\fallback_61_120_snippet.txt"
with open(path, "w", encoding="utf-8") as f:
    f.write("===FIRST_AID===\n")
    f.write(out_fa)
    f.write("\n===BIKE===\n")
    f.write(out)
print("Wrote", path, "FA chars", len(out_fa), "Bike chars", len(out))
