package com.marc.helmet.utils;

import java.util.Locale;

/**
 * When Gemini hits quota/rate limits (HTTP 429, RESOURCE_EXHAUSTED, etc.), we short-circuit the error
 * with keyword-based canned first-aid / roadside motorcycle tips so riders still hear something useful.
 */
public final class MarcOfflineGuidance {

    private MarcOfflineGuidance() {}

    /** True when {@code error} looks like Gemini quota overflow or HTTP rate limiting. */
    public static boolean isRateOrQuotaLimited(String error) {
        if (error == null) {
            return false;
        }
        String e = error.toLowerCase(Locale.US);
        return e.contains("http 429")
                || e.contains("\"code\":429")
                || e.contains("code 429")
                || e.contains("resource_exhausted")
                || e.contains("too many requests")
                || (e.contains("quota") && (e.contains("exceed") || e.contains("limit")))
                || (e.contains("rate") && e.contains("limit"))
                || e.contains("rate_limit");
    }

    /**
     * Picks concise spoken guidance from the rider's question text. Includes a reminder that cloud AI
     * is paused and emergencies need professional services.
     */
    public static String replyFor(String userQuestion) {
        String q =
                userQuestion != null ? userQuestion.toLowerCase(Locale.US).trim() : "";

        boolean firstAidPriority = mentionsFirstAid(q);
        boolean bikePriority = mentionsBikeMechanical(q);

        if (firstAidPriority && bikePriority) {
            if (strongFirstAidCue(q)) {
                return preamble() + categorizeFirstAid(q);
            }
            if (strongBikeCue(q)) {
                return preamble() + categorizeBike(q);
            }
            return preamble() + combineSplit();
        }
        if (firstAidPriority) {
            return preamble() + categorizeFirstAid(q);
        }
        if (bikePriority) {
            return preamble() + categorizeBike(q);
        }
        return preamble() + genericBoth();
    }

    private static boolean strongFirstAidCue(String q) {
        return q.contains("blood")
                || q.contains("bleed")
                || q.contains("unconscious")
                || q.contains("cpr")
                || q.contains("burn")
                || q.contains("allerg")
                || q.contains("snake")
                || q.contains("heat stroke")
                || q.contains("hypothermia");
    }

    private static boolean strongBikeCue(String q) {
        return q.contains("flat")
                || q.contains("puncture")
                || q.contains("chain")
                || q.contains("battery")
                || q.contains("brake")
                || q.contains("overheat")
                || q.contains("oil leak")
                || q.contains("coolant")
                || q.contains("won't start")
                || q.contains("wont start");
    }

    private static boolean mentionsFirstAid(String q) {
        return q.contains("hurt")
                || q.contains("injur")
                || q.contains("pain")
                || q.contains("bleed")
                || q.contains("blood")
                || q.contains("cut")
                || q.contains("wound")
                || q.contains("burn")
                || q.contains("fract")
                || q.contains("broken")
                || q.contains("sprain")
                || q.contains("cpr")
                || q.contains("choke")
                || q.contains("unconscious")
                || q.contains("shock")
                || q.contains("dizzy")
                || q.contains("dehydra")
                || q.contains("heat stroke")
                || q.contains("hypothermia")
                || q.contains("sting")
                || q.contains("bite")
                || q.contains("snake")
                || q.contains("allerg")
                || q.contains("first aid")
                || q.contains("medical");
    }

    private static boolean mentionsBikeMechanical(String q) {
        return q.contains("bike")
                || q.contains("motorcycle")
                || q.contains("tyre")
                || q.contains("tire")
                || q.contains("flat")
                || q.contains("puncture")
                || q.contains("tube")
                || q.contains("chain")
                || q.contains("clutch")
                || q.contains("brake")
                || q.contains("battery")
                || q.contains("start")
                || q.contains("stall")
                || q.contains("smoke")
                || q.contains("overheat")
                || q.contains("oil")
                || q.contains("coolant")
                || q.contains("leak")
                || q.contains("fix")
                || q.contains("tool")
                || q.contains("roadside")
                || q.contains("mechanic");
    }

    private static String preamble() {
        return "My cloud link is full right now, so here is a quick offline tip. "
                + "For life-threatening situations, call emergency services right away. ";
    }

    private static String categorizeFirstAid(String q) {
        if (q.contains("bleed") || q.contains("blood") || q.contains("cut") || q.contains("wound")) {
            return "For bleeding: apply firm direct pressure with a clean cloth or dressing. "
                    + "Keep the injured person still, elevate the limb if practical, and add more layers "
                    + "if blood soaks through—do not remove the first layer. If bleeding is severe or "
                    + "does not slow, seek emergency help.";
        }
        if (q.contains("burn")) {
            return "For burns: cool the area under lukewarm running water for ten to twenty minutes. "
                    + "Remove tight items near the burn before swelling starts. Cover loosely with "
                    + "clean non-fluffy cloth. Do not use ice directly. Large, deep, or facial burns "
                    + "need emergency care.";
        }
        if (q.contains("fract") || q.contains("broken") || q.contains("sprain")) {
            return "Suspected fracture or bad sprain: stop moving the limb, support it in the position "
                    + "found, apply cold packs wrapped in cloth for swelling, and get professional care. "
                    + "Watch for numbness or pale fingertips or toes—that needs urgent assessment.";
        }
        if (q.contains("choke")) {
            return "If someone cannot breathe or cough, shout for help and begin choking protocols you "
                    + "were trained in. Follow local emergency dispatcher instructions and use back blows "
                    + "and abdominal thrusts only when appropriate for conscious adults.";
        }
        if (q.contains("cpr") || q.contains("unconscious") || q.contains("collapsed")) {
            return "If unresponsive and not breathing normally after a trauma, shout for help, call "
                    + "emergency services, and begin CPR only if trained. Hands-only chest compressions "
                    + "in the centre of the chest at steady depth and rate save lives.";
        }
        if (q.contains("heat stroke") || q.contains("hypothermia") || q.contains("dehydra")) {
            return "Heat illness: move to shade, loosen gear, sip water if awake, cool skin gradually. "
                    + "Hypothermia: get dry, warmed slowly, sugary warm drink if alert. Serious confusion, "
                    + "stopped sweating in heat, or violent shivering needs emergency care.";
        }
        if (q.contains("bite") || q.contains("snake") || q.contains("sting") || q.contains("allerg")) {
            return "Animal or insect sting: rinse the wound, immobilise a bitten limb below heart level "
                    + "unless policy says otherwise, watch for tightening airway or rash from allergy "
                    + "which needs emergency treatment. Snake bites vary by species—prioritise evacuation.";
        }
        if (q.contains("shock") || q.contains("dizzy") || q.contains("faint")) {
            return "If someone feels faint: lay them down legs slightly raised when safe, loosen tight "
                    + "equipment, reassure calmly, sip water if dehydration is likely. Persistent chest "
                    + "pain or confusion needs emergency evaluation.";
        }
        return genericFirstAid();
    }

    private static String genericFirstAid() {
        return "Basic first-aid flow: scene safe, summon help when serious, calm the patient, "
                + "control bleeding with pressure, stabilise fractures without forcing alignment, monitor "
                + "breathing, and reassess frequently until professionals arrive.";
    }

    private static String categorizeBike(String q) {
        if (q.contains("flat") || q.contains("puncture") || q.contains("tube") || q.contains("tyre") || q.contains("tire")) {
            return "Flat or puncture: roll to a stable shoulder away from traffic, stand the bike upright, "
                    + "inflate with a compressor or patch the tube according to kit instructions, reinstall "
                    + "checking bead seating, torque wheel nuts evenly, verify pressure before rolling.";
        }
        if (q.contains("chain")) {
            return "Chain trouble: inspect master link clips, tighten slack within spec, clean grime if stuck, "
                    + "rear stand helps; if snapped, roadside links can be temporary until a shop weld or "
                    + "replacement—do not ride with binding or grinding.";
        }
        if (q.contains("brake")) {
            return "Brake loss or fade: pump lever to build pressure—if spongy inspect fluid reservoir and leaks. "
                    + "Cable bikes: centre pads and tighten cable carefully. Fade from heat lets you cool "
                    + "pads before riding slowly to help—never descend fast with doubtful brakes.";
        }
        if (q.contains("battery") || q.contains("start") || q.contains("won't start") || q.contains("wont start")) {
            return "Starter or electrical: toggle kill switch on, clutch in neutral, jumper if another bike "
                    + "pack is available respecting polarity—brief crank cycles to avoid frying starter. Loose "
                    + "grounds are common once gear vibrates kilometers.";
        }
        if (q.contains("smoke") || q.contains("overheat") || q.contains("coolant") || q.contains("oil leak")) {
            return "Smoke or coolant loss: safely stop off the road—do not open a hot radiator. "
                    + "Shut down, let cool unless oil is dripping onto headers. Top only when cold with "
                    + "matching fluid; tow if coolant or oil pours under your feet.";
        }
        if (q.contains("fuel") || q.contains("gas") || q.contains("petrol")) {
            return "Fuel issues: triple-check petcock/reserve valve, clogged vent in cap vents cause surge "
                    + "vacuum lock cracked open solves—inspect filter and lines for kinks after a tumble.";
        }
        return genericBike();
    }

    private static String genericBike() {
        return "Roadside checklist: stabilize on center or side stand away from lanes, helmets on while "
                + "troubleshooting roadside, tyres and chain slack first, spills second, tighten obvious "
                + "hardware with care, ride gently to the nearest mechanic if unresolved.";
    }

    private static String combineSplit() {
        return genericFirstAid() + " " + genericBike();
    }

    private static String genericBoth() {
        return genericFirstAid() + " " + genericBike();
    }
}
