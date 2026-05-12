package com.marc.helmet.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fully offline responder for MARC using curated first-aid + bike-fix descriptions.
 * No network / Gemini dependency.
 */
public final class MarcLocalResponder {

    private MarcLocalResponder() {}

    private static final class Entry {
        final String title;
        final String description;
        final Set<String> keywords;

        Entry(String title, String description, String... keywords) {
            this.title = title;
            this.description = description;
            this.keywords = new HashSet<>();
            if (keywords != null) {
                for (String k : keywords) {
                    if (k != null && !k.trim().isEmpty()) {
                        this.keywords.add(k.trim().toLowerCase(Locale.US));
                    }
                }
            }
            // also add tokens from title
            for (String t : tokenize(title)) {
                if (t.length() >= 3) {
                    this.keywords.add(t);
                }
            }
        }
    }

    // --- Data (from user-provided list) ---
    // Keep matching keyword sets short and high-signal.
    private static final List<Entry> FIRST_AID = Arrays.asList(
            new Entry("Elbow Fracture",
                    "Suspected elbow fracture is common in bike falls. Do not try to straighten or move the elbow at all. Keep the arm in the position you found it. Make a simple sling using a cloth, scarf or triangular bandage to support the forearm and tie it gently around the neck. Apply ice wrapped in a clean cloth for 15-20 minutes every 2-3 hours to reduce swelling. Give paracetamol for pain if the person has no allergies. Completely immobilize the arm and rush to the nearest hospital immediately for X-ray and proper casting. Do not let the rider move the arm or ride again.",
                    "elbow", "fracture", "arm", "sling"),
            new Entry("Knee Dislocation",
                    "Knee dislocation is a serious medical emergency. Do not attempt to push the knee back into place yourself as it can damage nerves and blood vessels. Keep the person lying flat and support the leg with pillows or rolled towels on both sides to prevent any movement. Apply ice pack wrapped in cloth for 15 minutes. Check the foot for pulse, colour and sensation. If the foot is cold or blue, go to hospital immediately. Call an ambulance and do not allow any weight on the injured leg.",
                    "knee", "dislocation", "leg"),
            new Entry("Collarbone Fracture",
                    "Collarbone fractures occur frequently during bike accidents. Keep the arm of the injured side close to the body. Support it with a broad arm sling. Apply ice wrapped in cloth to reduce pain and swelling. Avoid any shoulder or arm movement. Mild pain relief medicines like paracetamol or ibuprofen can be given. Take the person to hospital quickly for X-ray and further treatment.",
                    "collarbone", "clavicle", "fracture", "shoulder", "sling"),
            new Entry("Wrist Fracture",
                    "For suspected wrist fracture, immobilize the wrist and hand using a splint such as a ruler, cardboard or folded magazine. Keep the hand elevated above heart level to reduce swelling. Apply cold compress for 15-20 minutes. Do not move the wrist. Give pain relief medication and transport the rider to hospital immediately for proper diagnosis and casting.",
                    "wrist", "fracture", "splint", "hand"),
            new Entry("Shoulder Dislocation",
                    "Shoulder dislocation causes severe pain and visible deformity. Do not try to force the shoulder back into place. Support the arm gently against the chest with a sling. Apply ice to control swelling. Keep the person calm and take them to the nearest emergency room as soon as possible for professional reduction.",
                    "shoulder", "dislocation", "sling"),
            new Entry("Road Rash on Arms and Elbows",
                    "Clean the road rash gently with clean drinking water or saline. Carefully remove any visible dirt or gravel. Apply antiseptic cream such as Betadine and cover with sterile gauze or clean cloth. Change the dressing daily. Watch for signs of infection like increasing redness, swelling, pus or fever. Seek medical help if the rash is deep or large.",
                    "road", "rash", "abrasion", "scrape", "gravel"),
            new Entry("Road Rash",
                    "Clean the road rash gently with clean drinking water or saline. Carefully remove any visible dirt or gravel. Apply antiseptic cream such as Betadine and cover with sterile gauze or clean cloth. Change the dressing daily. Watch for signs of infection like increasing redness, swelling, pus or fever. Seek medical help if the rash is deep or large.",
                    "road", "rash", "abrasion", "scrape", "gravel"),
            new Entry("Ankle Sprain",
                    "Follow the RICE protocol strictly: Rest the ankle completely, Ice for 15-20 minutes every 2 hours, Compression with a crepe bandage (not too tight), Elevation above heart level. Avoid putting any weight on the injured ankle. Use a stick or crutch if available. Consult a doctor if swelling and pain do not reduce within 48 hours.",
                    "ankle", "sprain", "rice", "bandage"),
            new Entry("Head Injury",
                    "Even with a helmet, head injury can be dangerous. Do not move the person if neck injury is suspected. Check consciousness level, bleeding from ears or nose, vomiting or confusion. If the person is unconscious, confused or vomiting, call ambulance immediately. Keep them still, monitor breathing and wait for medical help.",
                    "head", "injury", "concussion", "vomit", "unconscious", "neck"),
            new Entry("Finger Dislocation or Fracture",
                    "Do not force the finger back into place. Splint the injured finger to the adjacent finger using tape. Apply ice and elevate the hand. Give pain relief if needed and take the person to hospital for proper X-ray and treatment.",
                    "finger", "dislocation", "fracture", "tape", "buddy"),
            new Entry("Lower Back Pain after Fall",
                    "Make the person lie on a firm surface in a comfortable position. Apply ice for the first 48 hours. Avoid bending, twisting or lifting. Give mild pain relief. If there is numbness in legs, tingling or loss of bladder/bowel control, seek emergency medical care immediately.",
                    "lower", "back", "spine", "numbness", "tingling"),
            new Entry("Thigh Bone (Femur) Fracture",
                    "Suspected femur fracture is very serious and painful. Do not move the leg. Immobilize by tying both legs together or using a long splint. Keep the person lying down, cover with blanket to treat shock and rush to hospital immediately.",
                    "femur", "thigh", "fracture", "leg"),
            new Entry("Severe Bleeding",
                    "Apply firm direct pressure on the wound with a clean cloth for at least 10-15 minutes without peeking. Elevate the limb if possible. Do not remove the first cloth if soaked — add more layers. If bleeding continues, seek immediate medical help.",
                    "bleeding", "blood", "cut", "wound"),
            new Entry("Severe Bleeding from Cut",
                    "Apply firm direct pressure on the wound with a clean cloth for at least 10-15 minutes without peeking. Elevate the limb if possible. Do not remove the first cloth if soaked — add more layers. If bleeding continues, seek immediate medical help.",
                    "bleeding", "blood", "cut", "wound"),
            new Entry("Shin Bone Fracture",
                    "Suspected tibia or fibula fracture requires complete rest. Immobilize the leg with a splint. Apply ice and elevate. Do not allow weight bearing. Take the person to hospital for X-ray and casting.",
                    "shin", "tibia", "fibula", "fracture", "splint"),
            new Entry("Hip Fracture or Pelvic Injury",
                    "Do not move the person. Keep them lying flat on their back. Immobilize both legs by tying them together. Treat for shock and call ambulance immediately. Pelvic injuries can cause internal bleeding.",
                    "hip", "pelvic", "pelvis", "fracture"),
            new Entry("Neck Pain / Whiplash",
                    "Suspected neck injury is dangerous. Do not move the head or neck. Support the head in the position found using rolled towels. Call ambulance and wait for professional help. Keep the person still.",
                    "neck", "whiplash", "spine"),
            new Entry("Palm Abrasion",
                    "Clean the palm gently with water. Remove dirt carefully. Apply antiseptic and cover with gauze. Keep the hand elevated. Change dressing daily and watch for infection.",
                    "palm", "abrasion", "hand", "gauze"),
            new Entry("Forearm Fracture",
                    "Immobilize the forearm with a splint. Support with a sling. Apply ice. Give pain relief and take to hospital for X-ray.",
                    "forearm", "fracture", "splint", "sling"),
            new Entry("Burns",
                    "Cool the burn under running water for 10-15 minutes. Do not apply ice directly. Cover with clean cloth. Seek medical help for deep burns.",
                    "burn", "exhaust", "engine", "hot"),
            new Entry("Rib Fracture",
                    "Encourage gentle breathing. Apply ice to the area. Give pain relief. Advise against heavy breathing or coughing forcefully. Seek medical attention.",
                    "rib", "fracture", "chest", "breathing"),
            new Entry("Knee Sprain or Ligament Tear",
                    "Rest the knee completely. Apply ice, compression and elevation (RICE). Avoid weight bearing. Use a knee cap or bandage for support and consult an orthopedic doctor.",
                    "knee", "sprain", "ligament", "tear", "rice"),
            new Entry("Eye Injury from Dust or Insect",
                    "Do not rub the eye. Rinse gently with clean water. If something is embedded, cover both eyes and seek medical help immediately.",
                    "eye", "dust", "insect"),
            new Entry("Dehydration during Long Ride",
                    "Move the person to shade. Give oral rehydration solution or water with salt and sugar. Rest in a cool place. If dizziness or confusion persists, seek medical help.",
                    "dehydration", "dizzy", "ors", "water"),
            new Entry("Heel Pain after Hard Landing",
                    "Rest the foot. Apply ice. Elevate. Avoid walking barefoot. Use soft footwear and consult a doctor if pain continues.",
                    "heel", "pain", "foot"),
            new Entry("Burns from Exhaust Pipe",
                    "Cool the burn under running water for 10-15 minutes. Do not apply ice directly. Cover with clean cloth. Seek medical help for deep burns.",
                    "burn", "exhaust", "pipe"),
            new Entry("Thumb Injury",
                    "Immobilize the thumb. Apply ice. Elevate the hand. Take to hospital if swelling is severe or movement is not possible.",
                    "thumb", "hand"),
            new Entry("Chest Pain after Impact",
                    "Make the person sit in a comfortable position. Loosen tight clothing. Monitor breathing. If pain is severe or breathing is difficult, call ambulance immediately.",
                    "chest", "pain", "breathing"),
            new Entry("Calf Muscle Tear",
                    "Rest the leg. Apply ice. Use compression bandage. Elevate. Avoid stretching the muscle. Consult a doctor.",
                    "calf", "muscle", "tear"),
            new Entry("Jaw Injury",
                    "Support the jaw gently. Apply ice. Do not eat hard food. Take to hospital for evaluation.",
                    "jaw", "face"),
            new Entry("Multiple Abrasions on Legs",
                    "Clean all wounds with water. Apply antiseptic cream. Cover with gauze. Watch for infection and change dressing daily.",
                    "abrasions", "legs", "wounds"),
            new Entry("Spine Injury Suspicion",
                    "Do not move the person at all. Support head and neck in position found. Call ambulance immediately.",
                    "spine", "spinal", "paralysis", "neck"),
            new Entry("Hand Fracture",
                    "Immobilize the hand with a splint. Elevate above heart level. Apply ice and seek medical help for X-ray.",
                    "hand", "fracture", "splint"),

            // --- ids 31-60 (second block) ---
            new Entry("Upper Arm Fracture",
                    "For suspected upper arm (humerus) fracture, support the arm gently against the body using a sling. Place a padded splint along the arm if possible. Apply ice wrapped in cloth to reduce swelling. Do not move the arm. Give pain relief medication and transport the person to hospital immediately for X-ray and treatment.",
                    "upper", "arm", "humerus", "fracture", "sling"),
            new Entry("Foot Fracture",
                    "Do not allow weight on the injured foot. Immobilize the foot with a splint or by wrapping with a bandage. Elevate the foot above heart level. Apply ice for 15-20 minutes. Use crutches if available and take the rider to hospital for proper diagnosis and casting.",
                    "foot", "fracture", "splint", "bandage"),
            new Entry("Nose Bleeding after Fall",
                    "Make the person sit upright and lean slightly forward. Pinch the soft part of the nose for 10-15 minutes continuously. Apply ice on the bridge of the nose. Do not let them blow the nose. If bleeding continues after 20 minutes, seek medical help.",
                    "nose", "bleeding", "blood"),
            new Entry("Tailbone Injury",
                    "Make the person lie on their side in a comfortable position. Apply ice to the area for 15-20 minutes. Avoid sitting for long periods. Give pain relief and advise them to see a doctor if pain persists or there is difficulty in passing motion.",
                    "tailbone", "coccyx"),
            new Entry("Deep Cut on Leg",
                    "Apply firm pressure with a clean cloth to stop bleeding. Elevate the leg above heart level. Do not remove the cloth if it gets soaked — add more layers. Clean the wound gently once bleeding stops and cover with sterile dressing. Seek medical attention promptly.",
                    "deep", "cut", "leg", "bleeding"),
            new Entry("Elbow Dislocation",
                    "Do not try to force the elbow back into position. Support the arm in a sling without straightening it. Apply ice to reduce swelling. Keep the person calm and rush to the nearest hospital for professional treatment.",
                    "elbow", "dislocation", "sling"),
            new Entry("Groin Strain",
                    "Rest completely and avoid any leg movement that causes pain. Apply ice for 15-20 minutes. Use compression shorts if available. Elevate the area when lying down. Consult a doctor for proper assessment.",
                    "groin", "strain"),
            new Entry("Tooth Injury",
                    "If a tooth is knocked out, rinse it gently with water and try to place it back in the socket if possible. If not, keep it in milk or saline. Take the person to a dentist or hospital immediately.",
                    "tooth", "dentist", "knocked"),
            new Entry("Hip Dislocation",
                    "Do not move the leg. Keep the person lying flat on their back. Immobilize both legs together. Treat for shock and call ambulance immediately as hip dislocations are serious.",
                    "hip", "dislocation"),
            new Entry("Severe Road Rash on Back",
                    "Gently clean the area with saline or clean water. Remove loose dirt. Apply antiseptic cream and cover with non-stick sterile dressing. Change dressing daily and watch for infection. Seek medical help for large or deep abrasions.",
                    "road", "rash", "back", "abrasion"),
            new Entry("Quadriceps Muscle Tear",
                    "Rest the leg completely. Apply ice for 15-20 minutes every 2-3 hours. Use compression bandage. Elevate the leg. Avoid stretching or weight bearing until seen by a doctor.",
                    "quadriceps", "muscle", "tear", "thigh"),
            new Entry("Ear Injury with Bleeding",
                    "Do not plug the ear. Cover the ear loosely with a clean cloth. Keep the person lying on the injured side if bleeding. Seek immediate medical attention as it may indicate serious head injury.",
                    "ear", "bleeding"),
            new Entry("Clavicle Fracture",
                    "Support the arm with a sling. Apply ice. Avoid shoulder movement. Give pain relief and take to hospital for X-ray and figure-of-eight bandage if needed.",
                    "clavicle", "collarbone", "fracture"),
            new Entry("Heel Cord (Achilles) Injury",
                    "Do not stretch the foot. Rest completely. Apply ice. Use a heel raise or soft padding. Seek medical evaluation as Achilles injuries need proper care.",
                    "achilles", "heel", "cord"),
            new Entry("Facial Cuts and Bruises",
                    "Clean the cuts gently. Apply ice to reduce swelling. Use sterile dressing. If the cut is deep or gaping, it may need stitches — go to hospital.",
                    "facial", "cuts", "bruises", "stitches"),
            new Entry("Spinal Cord Injury Suspicion",
                    "Do not move the person at all. Support the head and neck in the position found. Call ambulance immediately and monitor breathing until help arrives.",
                    "spinal", "cord", "spine"),
            new Entry("Knuckle Fracture",
                    "Immobilize the hand with a padded splint. Elevate above heart level. Apply ice and take to hospital for X-ray.",
                    "knuckle", "fracture", "hand"),
            new Entry("Abdominal Injury",
                    "Keep the person lying down with knees bent. Do not give food or water. Apply ice if there is swelling. Watch for signs of internal bleeding and seek emergency help immediately.",
                    "abdominal", "stomach", "internal", "bleeding"),
            new Entry("Hamstring Pull",
                    "Rest the leg. Apply ice. Use gentle compression. Elevate when possible. Avoid sudden movements and consult a physiotherapist or doctor.",
                    "hamstring", "pull"),
            new Entry("Sunstroke / Heat Exhaustion",
                    "Move the person to shade. Loosen clothing. Give cool water or oral rehydration solution. Apply wet cloth on forehead and neck. If confusion or high temperature persists, seek medical help.",
                    "sunstroke", "heat", "exhaustion"),
            new Entry("Wrist Sprain",
                    "Follow RICE: Rest, Ice, Compression, Elevation. Immobilize with a wrist band or splint. Avoid using the hand and consult a doctor if pain continues.",
                    "wrist", "sprain", "rice"),
            new Entry("Lip Cut",
                    "Clean gently with water. Apply ice to reduce swelling. If bleeding does not stop or cut is deep, medical stitches may be required.",
                    "lip", "cut"),
            new Entry("Shin Splints",
                    "Rest the legs. Apply ice. Use compression. Stretch gently after 48 hours. Wear proper footwear and avoid hard surfaces.",
                    "shin", "splints"),
            new Entry("Chest Wall Bruise",
                    "Apply ice. Give pain relief. Encourage slow breathing. If breathing becomes difficult or pain is severe, seek emergency care.",
                    "chest", "bruise"),
            new Entry("Big Toe Fracture",
                    "Tape the big toe to the second toe. Apply ice. Elevate the foot. Wear loose footwear and see a doctor.",
                    "toe", "fracture"),
            new Entry("Burn from Hot Engine",
                    "Cool the burnt area under running water for 10-15 minutes. Do not burst blisters. Cover with clean cloth and seek medical help.",
                    "burn", "engine", "blister"),
            new Entry("Neck Strain",
                    "Keep neck supported. Apply ice for first 48 hours. Give mild pain relief. Avoid sudden neck movements and consult a doctor.",
                    "neck", "strain"),
            new Entry("Multiple Finger Injuries",
                    "Buddy tape injured fingers. Apply ice. Elevate the hand. Seek medical attention for proper splinting.",
                    "multiple", "finger", "injuries"),
            new Entry("Pelvic Bruise",
                    "Rest in comfortable position. Apply ice. Avoid walking. Seek hospital evaluation as pelvic injuries can be serious.",
                    "pelvic", "bruise"),
            new Entry("Ankle Dislocation",
                    "Do not try to realign the ankle. Immobilize with pillows or splint. Apply ice. Elevate and rush to hospital immediately.",
                    "ankle", "dislocation", "splint"),
            new Entry("Spine Injury",
                    "Do not move the person at all. Support head and neck in position found. Call ambulance immediately.",
                    "spine", "spinal", "paralysis", "neck")
    );

    private static final List<Entry> BIKE_REPAIR = Arrays.asList(
            new Entry("Not starting / Spark Plug",
                    "If your bike is not starting or misfiring, check the spark plug first. Switch off the ignition. Remove the spark plug cap and use a spark plug spanner to unscrew it. Inspect the electrode — if it is black, oily or has carbon deposits, clean it with a wire brush or replace it with a new one of the same specification. Check the gap using a feeler gauge (usually 0.6-0.8mm). Reinstall properly and try starting. If the problem continues, the plug may need replacement.",
                    "not", "starting", "start", "spark", "plug", "misfire"),
            new Entry("Low Tyre Pressure",
                    "Low tyre pressure causes poor handling and high fuel consumption. Park the bike on the center stand. Check both front and rear tyre pressure using a reliable gauge. Recommended pressure is usually 25-32 PSI front and 32-38 PSI rear (check your bike manual). Inflate using a foot pump or air compressor at a petrol station. Never ride with low pressure as it can damage the tyre and tube.",
                    "tyre", "tire", "pressure", "psi", "air"),
            new Entry("Air Filter Dirty or Clogged",
                    "A dirty air filter causes hard starting, loss of power and high fuel consumption. Locate the air filter box (usually under the seat or side cover). Open it carefully. Remove the filter. If it is foam type, wash with petrol or soap water, dry completely and apply few drops of engine oil. If it is paper type and very dirty, replace it with a new one. Reinstall and start the bike.",
                    "air", "filter", "clogged", "power"),
            new Entry("Fuel Not Reaching Carburetor",
                    "If the engine is not getting fuel, first check if there is fuel in the tank. Turn the fuel tap to reserve if needed. Check the fuel line for cracks or blockages. Disconnect the fuel line from the carburetor and see if fuel flows out. If not, clean or replace the fuel filter and blow air through the fuel pipe to clear any blockage.",
                    "fuel", "carburetor", "carb", "tap", "reserve", "filter"),
            new Entry("Carburetor Not Working / Flooding",
                    "If the bike is flooding (too much fuel smell) or not starting, turn off the fuel tap. Remove the carburetor drain screw at the bottom and drain excess fuel. Clean the float chamber. Check the needle valve for dirt. Reassemble and ensure the choke is used properly when cold starting. If problem persists, the carburetor needs thorough cleaning or servicing.",
                    "carburetor", "flooding", "fuel", "smell", "choke"),
            new Entry("Fuel Pump Not Working (Fuel Injected)",
                    "On fuel-injected bikes, if the fuel pump is not priming, check the fuse and relay. Listen for a buzzing sound from the fuel pump when you turn the ignition on. Check battery voltage. Ensure the fuel filter is not clogged. If the pump is silent, it may need replacement. Do not attempt to open the pump yourself.",
                    "fuel", "pump", "priming", "buzzing", "fi"),
            new Entry("Starting but Stopping Immediately",
                    "This is usually due to idle or air-fuel mixture issue. Check if the choke is fully off after warming up. Clean the spark plug. Ensure the air filter is clean. Adjust the idle screw on the carburetor slightly clockwise to increase idle RPM. If the problem continues, the pilot jet in the carburetor may be clogged.",
                    "starting", "stopping", "idle", "rpm", "pilot"),
            new Entry("Puncture",
                    "If you hear hissing sound or tyre goes flat, do not ride further. Remove the tube or use tubeless puncture kit. Clean the hole, apply rubber solution and insert the puncture strip. Inflate and check for leaks. For major punctures, replace the tube.",
                    "puncture", "flat", "tube", "tubeless", "hissing"),
            new Entry("Chain loose",
                    "A loose chain causes jerking and can damage sprockets. Place the bike on center stand. Check chain slack — it should be around 2-3 cm up and down movement. Loosen the rear axle nut, adjust the chain adjusters equally on both sides, then tighten the axle nut. Apply chain lube after adjustment.",
                    "chain", "loose", "slack", "sprocket"),
            new Entry("Brake not working",
                    "Check brake fluid level in the reservoir. If low, top up with recommended DOT 3 or 4 fluid. Check brake pads for wear — replace if thickness is less than 2mm. Bleed the brake system if there is spongy feeling. Always test brakes at low speed after any repair.",
                    "brake", "pads", "fluid", "spongy", "dot"),
            new Entry("Engine overheating",
                    "Stop the bike immediately. Check engine oil level. Clean the radiator or fins if air-cooled. Ensure the coolant is at proper level (in liquid-cooled bikes). Check if the fan is working. Avoid riding in high traffic with low speed for long. Let the engine cool down before checking.",
                    "overheat", "overheating", "coolant", "fan", "radiator", "hot"),
            new Entry("Battery not charging",
                    "Check battery terminals for corrosion and clean them. Measure battery voltage — it should be above 12.6V when engine is off. Start the bike and check voltage — it should go above 13.5V. If not, the charging system (magneto or regulator) may have issues.",
                    "battery", "charging", "voltage", "12.6", "13.5"),
            new Entry("Fuel not reaching carburetor",
                    "If the engine is not getting fuel, first check if there is fuel in the tank. Turn the fuel tap to reserve if needed. Check the fuel line for cracks or blockages. Disconnect the fuel line from the carburetor and see if fuel flows out. If not, clean or replace the fuel filter and blow air through the fuel pipe to clear any blockage.",
                    "fuel", "carb", "carburetor", "reserve", "tap", "filter"),
            new Entry("Bike Jerks While Accelerating",
                    "Clean spark plug and air filter. Check fuel quality. Inspect spark plug cap for cracks. Clean carburetor jets. Check chain tension. This symptom is commonly caused by dirty carburetor or weak spark.",
                    "jerks", "accelerating", "spark", "jets", "chain"),
            new Entry("Headlight Not Working",
                    "Check headlight fuse. Inspect bulb — replace if fused. Check wiring connections for looseness. Test the headlight switch. On LED lights, the entire unit may need replacement.",
                    "headlight", "fuse", "bulb"),
            new Entry("Engine Oil Low or Black",
                    "Check engine oil level using the dipstick. If low, top up with recommended grade oil. If oil is very black and thick, change it immediately along with oil filter. Dirty oil causes engine damage.",
                    "oil", "dipstick", "black", "thick"),
            new Entry("Kick Starter Not Returning",
                    "Apply grease on the kick shaft. Check the return spring. If broken, the spring needs replacement. Do not force the kick lever.",
                    "kick", "starter", "spring"),
            new Entry("Headlight not working",
                    "Check headlight fuse. Inspect bulb — replace if fused. Check wiring connections for looseness. Test the headlight switch. On LED lights, the entire unit may need replacement.",
                    "headlight", "light", "bulb", "fuse"),
            new Entry("Horn not working",
                    "Check horn fuse. Test the horn button. Clean the horn terminals. Replace the horn if it is damaged. Horns usually fail due to water entry or loose wiring.",
                    "horn", "beep", "fuse"),
            new Entry("Fuel Tank Cap Vent Blocked",
                    "A blocked vent in the fuel cap causes vacuum and stops fuel flow. Open the cap and clean the small vent hole with a pin. Replace the cap if vent is damaged.",
                    "fuel", "cap", "vent", "vacuum"),
            new Entry("Indicator Lights Not Blinking",
                    "Check indicator fuse. Replace faulty bulb. Check flasher relay (usually near battery). Clean connections. On modern bikes, the entire relay unit may need replacement.",
                    "indicator", "blinking", "relay", "flasher"),
            new Entry("Engine Smoke on Startup",
                    "Blue smoke means engine oil is burning — check piston rings or valve seals. White smoke means coolant is leaking into combustion chamber. Black smoke means rich fuel mixture — clean air filter and carburetor.",
                    "smoke", "blue", "white", "black"),
            new Entry("Loose Wheel Nut",
                    "Park on center stand. Tighten all wheel nuts using proper spanner in star pattern. Check torque as per manual (usually 50-80 Nm). Loose nuts can cause serious accidents.",
                    "wheel", "nut", "torque"),
            new Entry("Throttle Not Responding Smoothly",
                    "Lubricate the throttle cable. Check for cable free play. Clean the throttle housing. If electronic throttle, check for error codes.",
                    "throttle", "cable"),
            new Entry("Self Starter Not Working",
                    "Check battery charge. Verify starter relay click sound. Inspect starter motor for dirt or brush wear. Check all wiring connections.",
                    "self", "starter", "relay", "click"),
            new Entry("Gear Shifting Hard",
                    "Check engine oil level and quality. Adjust clutch cable free play. If problem remains, the gear shifter mechanism or clutch plates may need servicing.",
                    "gear", "shifting", "hard", "clutch"),
            new Entry("Rust in Fuel Tank",
                    "Drain all fuel. Remove the tank if possible. Clean inside with diesel or use fuel tank cleaner. Install a new inline fuel filter. Avoid leaving bike with low fuel for long periods.",
                    "rust", "fuel", "tank", "filter"),
            new Entry("Brake Squeaking",
                    "Clean brake pads and disc with brake cleaner. Check pad wear. Apply anti-squeal paste on the back of pads if available. Replace pads if worn out.",
                    "brake", "squeak", "disc"),
            new Entry("Petrol Smell While Riding",
                    "Check fuel tank, fuel lines, and carburetor for leaks. Tighten all clamps. Replace cracked fuel pipes immediately as it is a fire hazard.",
                    "petrol", "smell", "leak"),
            new Entry("Loss of Power on Hills",
                    "Clean air filter and spark plug. Check fuel flow. Decarbonize the exhaust silencer if clogged. Check clutch slipping. Service the carburetor.",
                    "loss", "power", "hills"),
            new Entry("Side Stand Not Cutting Ignition",
                    "Clean the side stand switch. Check wiring. Many bikes have a safety switch — if faulty, the bike won’t start in gear. Bypass temporarily only for testing.",
                    "side", "stand", "switch"),

            // --- ids 31-60 (second block) ---
            new Entry("Engine Vibrates Excessively",
                    "Excessive vibration can be due to loose engine mounting bolts, unbalanced wheels, or worn engine mounts. Check and tighten all engine mounting bolts. Inspect wheel balance and chain alignment. If vibration is very high at certain RPM, the crankshaft or engine bearings may need professional servicing.",
                    "vibration", "vibrates", "mounting", "bolts"),
            new Entry("CDI or Ignition Coil Failure",
                    "If there is spark but weak or no spark at all, the CDI unit or ignition coil may be faulty. Check spark plug cap and high tension wire for cracks. Replace the spark plug first. If problem continues, test with a known good coil or take it to an electrician.",
                    "cdi", "coil", "ignition", "spark"),
            new Entry("Clogged Exhaust Silencer",
                    "Reduced power and loud exhaust noise often indicate a clogged silencer. Remove the silencer and clean the internal baffle with petrol or wire brush. For catalytic converter models, decarbonize or replace the exhaust. This greatly improves performance.",
                    "exhaust", "silencer", "clogged"),
            new Entry("Fuel Leak from Carburetor",
                    "Check the float needle valve and gasket. Clean the float chamber thoroughly. Replace the needle if worn. Tighten all carburetor joints and ensure the overflow pipe is clear. Fuel leak is dangerous — fix immediately.",
                    "fuel", "leak", "carburetor"),
            new Entry("Brake Fluid Leak",
                    "Check brake hoses and caliper seals for leaks. Top up brake fluid only with recommended DOT 3 or DOT 4. If leak is visible, do not ride the bike. Get the brake system repaired at a service center immediately.",
                    "brake", "fluid", "leak"),
            new Entry("Tyre Puncture Frequent",
                    "Frequent punctures mean poor road conditions or low quality tube. Use puncture resistant tubes or tubeless tyres with sealant. Always maintain correct air pressure and avoid riding over sharp objects.",
                    "frequent", "puncture", "sealant"),
            new Entry("Accelerator Cable Broken or Stuck",
                    "Lubricate the throttle cable with cable lube. Check for broken strands inside the cable. Replace the cable if it is frayed or stuck. Adjust free play after replacement.",
                    "accelerator", "cable", "stuck"),
            new Entry("Engine Not Revving Above 4000 RPM",
                    "Usually caused by dirty air filter, clogged fuel jet, or restricted exhaust. Clean air filter, service carburetor (main jet and pilot jet), and clean exhaust silencer.",
                    "revving", "4000", "rpm", "jet"),
            new Entry("Loose Rear Shock Absorber",
                    "Tighten the shock absorber mounting bolts. Check for oil leakage from the shock. If the shock is damaged or leaking, replace it. Bad shocks affect handling and safety.",
                    "rear", "shock", "absorber"),
            new Entry("Gearbox Oil Leak",
                    "Check gearbox oil level and drain plug tightness. Replace the oil seal if leaking. Use recommended gearbox oil grade. Low oil can cause gear damage.",
                    "gearbox", "oil", "leak"),
            new Entry("Weak Pickup / Slow Acceleration",
                    "Clean spark plug, air filter, and carburetor jets. Check ignition timing. Ensure good fuel quality. Weak pickup is mostly due to dirty carburetor or worn spark plug.",
                    "weak", "pickup", "acceleration"),
            new Entry("Dashboard Lights Not Working",
                    "Check the dashboard fuse. Inspect all bulb connections. Clean the contacts. On digital dashboards, check the wiring harness for loose plugs.",
                    "dashboard", "lights", "fuse"),
            new Entry("Overheating in Traffic",
                    "Clean engine fins or radiator thoroughly. Check coolant level. Ensure proper oil level. Avoid riding in low gear for long periods in heavy traffic. Stop and cool down if temperature rises too high.",
                    "overheating", "traffic"),
            new Entry("Chain Making Noise",
                    "Clean the chain thoroughly. Apply good quality chain lube. Adjust chain tension correctly (2-3 cm slack). Replace the chain and sprockets if they are badly worn.",
                    "chain", "noise"),
            new Entry("Front Fork Oil Leak",
                    "Oil leakage from front forks means damaged oil seals. Replace the fork seals. This job requires proper tools — better to visit a service center.",
                    "fork", "oil", "leak"),
            new Entry("Bike Pulling to One Side",
                    "Check tyre pressure on both sides. Inspect wheel alignment. Check if frame is bent after an accident. Tighten steering bearings if loose.",
                    "pulling", "one", "side", "alignment"),
            new Entry("Starter Motor Clicking Sound",
                    "This usually means low battery or faulty starter relay. Charge or replace the battery. Clean relay contacts. If clicking continues, the starter motor may need repair.",
                    "starter", "clicking", "relay"),
            new Entry("Rich Fuel Mixture (Black Smoke)",
                    "Clean air filter. Check float level in carburetor. Clean or replace main jet. Adjust air-fuel mixture screw according to manual.",
                    "rich", "mixture", "black", "smoke"),
            new Entry("Loose Handlebar",
                    "Tighten the handlebar clamp bolts evenly. Check steering head bearings. Do not ride with loose handlebar as it is very dangerous.",
                    "handlebar", "loose"),
            new Entry("Engine Knocking Sound",
                    "Check engine oil level and quality. Use correct octane petrol. Avoid overloading the bike. Persistent knocking needs immediate mechanic attention as it can damage the engine.",
                    "knocking", "sound"),
            new Entry("Indicator Relay Faulty",
                    "If indicators blink very fast or not at all, replace the flasher relay. Check all indicator bulbs first before replacing the relay.",
                    "indicator", "relay", "faulty"),
            new Entry("Petrol Tank Rust Particles",
                    "Drain the tank completely. Clean inside or use fuel tank rust remover. Install a new fuel filter. Avoid keeping low fuel in the tank for long time.",
                    "tank", "rust", "particles"),
            new Entry("Brake Pad Replacement Needed",
                    "If brake lever goes too close to the handle, check pad thickness. Replace pads when less than 2mm thick. Always bed in new pads properly.",
                    "brake", "pad", "replacement"),
            new Entry("Magneto / Charging Coil Issue",
                    "If battery is not charging, check the magneto coils and regulator rectifier. This usually requires professional testing with a multimeter.",
                    "magneto", "charging", "coil"),
            new Entry("Speedometer Not Working",
                    "Check speedometer cable for breakage or dryness. Lubricate or replace the cable. On digital speedometers, check sensor and wiring.",
                    "speedometer", "not", "working"),
            new Entry("Engine Compression Low",
                    "Low compression causes hard starting and low power. Check piston rings, valves and head gasket. This usually needs engine overhaul.",
                    "compression", "low"),
            new Entry("Choke Not Working",
                    "Clean the choke plunger and cable. Ensure the choke closes and opens properly. Lubricate the choke cable.",
                    "choke", "not", "working"),
            new Entry("Rear Tyre Wear Uneven",
                    "Check chain alignment and tyre pressure. Avoid overloading. Rotate or replace tyre if wear is excessive on one side.",
                    "tyre", "wear", "uneven"),
            new Entry("Fuel Gauge Not Working",
                    "Check wiring connection to the fuel sender unit in the tank. Clean the contacts. The float inside the tank may be stuck.",
                    "fuel", "gauge", "not", "working"),
            new Entry("Engine Oil Pressure Warning Light",
                    "Stop the engine immediately. Check oil level. If low, top up. If light stays on even after topping up, do not ride — take the bike to a mechanic as it can cause major engine damage.",
                    "oil", "pressure", "warning", "light")
    );

    public static String respond(String userText) {
        String q = userText != null ? userText.trim() : "";
        if (q.isEmpty()) {
            return "Tell me what happened: injury or bike issue?";
        }

        Match bestFirstAid = bestMatch(FIRST_AID, q);
        Match bestBike = bestMatch(BIKE_REPAIR, q);

        // If both are weak, keep it short and ask for specifics.
        if (bestFirstAid.score <= 0 && bestBike.score <= 0) {
            return "I can help with first aid or bike fixes. Say the injury or the bike problem (for example: bleeding, head injury, puncture, not starting).";
        }

        // Decide between first-aid and bike repair.
        if (bestFirstAid.score >= bestBike.score) {
            return bestFirstAid.entry.title + ": " + bestFirstAid.entry.description;
        }
        return bestBike.entry.title + ": " + bestBike.entry.description;
    }

    private static final class Match {
        final Entry entry;
        final int score;

        Match(Entry entry, int score) {
            this.entry = entry;
            this.score = score;
        }
    }

    private static Match bestMatch(List<Entry> entries, String userText) {
        Set<String> qTokens = new HashSet<>(tokenize(userText));
        Entry best = null;
        int bestScore = 0;

        for (Entry e : entries) {
            int score = 0;
            for (String kw : e.keywords) {
                if (qTokens.contains(kw)) {
                    score += 3;
                } else if (userText.toLowerCase(Locale.US).contains(kw)) {
                    score += 1;
                }
            }
            // prefer more specific matches
            if (score > bestScore) {
                bestScore = score;
                best = e;
            }
        }
        if (best == null) {
            // should never happen with non-empty list
            best = entries.get(0);
        }
        return new Match(best, bestScore);
    }

    private static List<String> tokenize(String s) {
        if (s == null) {
            return new ArrayList<>();
        }
        String lower = s.toLowerCase(Locale.US);
        String cleaned = lower.replaceAll("[^a-z0-9+ ]", " ");
        String[] parts = cleaned.split("\\s+");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (t.isEmpty()) continue;
            out.add(t);
        }
        return out;
    }
}

