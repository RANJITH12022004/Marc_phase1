# MARC — CONTINUATION BUILD GUIDE
### Starting from Prompt 8 | Red + White + #141414 Charcoal Theme
### All Java files done. All layouts done. Now: res fixes + missing files + wiring.

```
DATE: May 2026
STATUS: Java layer complete. Res layer partial. No Android Studio yet.
THEME: #141414 bg | #FF2020 red | #FFFFFF white | #00FF88 green active | IBM Plex Mono + Space Grotesk
MISSING: Drawables (amber → red), item layouts, adapters, views, utils, crash screen, Marc Core
```

---

> **RULES FOR THIS GUIDE**
> 1. Every prompt = one Cursor session. Copy full prompt. Paste. Take output to Android Studio.
> 2. Files marked [REPLACE] — overwrite the existing amber version completely.
> 3. Files marked [NEW] — create fresh, doesn't exist yet.
> 4. Do NOT regenerate any Java file that already exists unless explicitly stated.
> 5. After every 3 prompts — compile in Android Studio. Fix before continuing.

---

## PHASE A — FIX RES LAYER (Replace amber with red/charcoal)

> These replace your existing amber-themed files. Same filenames, new colors.

---

### PROMPT A1 — [REPLACE] colors.xml + themes.xml + dimens.xml (3-in-1)

**Replace:** `res/values/colors.xml`, `res/values/themes.xml`, `res/values/dimens.xml`

```
CURSOR PROMPT:

Rewrite 3 Android resource files for the MARC app.
Theme: #141414 charcoal bg, #FF2020 red primary, #FFFFFF white, #00FF88 green active, #FF4444 red light.
Fonts: IBM Plex Mono (mono), Space Grotesk (UI). Dark mode only.

FILE 1 — res/values/colors.xml:
Include ALL of these named colors:

Background system:
colorBackground = #141414
colorSurface = #1A1A1A
colorSurfaceVariant = #1F1F1F
colorSurfaceElevated = #252525
colorSurfaceTop = #2A2A2A

Accent system:
colorPrimary = #FF2020 (red)
colorPrimaryDark = #CC1A1A
colorPrimaryLight = #FF4444
colorSecondary = #FFFFFF (white)
colorAccentGreen = #00FF88
colorAccentGreenDim = #00CC66

Text system:
textPrimary = #FFFFFF
textSecondary = #E2E8F0
textMuted = #94A3B8
textDim = #555555
textDeep = #2D2D2D

Border/divider:
colorBorder = #222222
colorDivider = #1E1E1E
colorBorderFocus = #FF2020

Status colors:
colorConnected = #00FF88
colorDisconnected = #555555
colorWarning = #FF8800
colorError = #FF2020
colorSuccess = #00FF88

Navigation:
colorNavBackground = #0E0E0E
colorNavSelected = #FF2020
colorNavUnselected = #555555

Marc Core:
colorCoreRed = #FF2020
colorCoreDark = #1A0000
colorCoreGlow = #33FF2020

Cards:
colorCardDefault = #1A1A1A
colorCardAlert = #1A0000
colorCardSuccess = #001A08

Transparent:
colorTransparent = #00000000

FILE 2 — res/values/themes.xml:
Base: Theme.MaterialComponents.DayNight.NoActionBar
Force dark mode only.
- windowBackground = colorBackground (#141414)
- colorPrimary = #FF2020
- colorPrimaryDark = #CC1A1A
- colorAccent = #FF2020
- android:statusBarColor = #0E0E0E
- android:navigationBarColor = #0E0E0E
- colorControlHighlight = #22FF2020
- colorControlNormal = #555555
- textColorPrimary = #FFFFFF
- textColorSecondary = #94A3B8
Theme name: Theme.MARC
Child theme: Theme.MARC.Splash (black bg, no action bar, fullscreen)
Child theme: Theme.MARC.FullScreen (no status bar)

FILE 3 — res/values/dimens.xml:
margin_micro = 2dp
margin_tiny = 4dp
margin_small = 8dp
margin_medium = 16dp
margin_large = 24dp
margin_xlarge = 32dp
padding_screen = 16dp
padding_card = 12dp
corner_radius_sharp = 2dp
corner_radius_small = 4dp
corner_radius_medium = 8dp
corner_radius_card = 2dp (sharp cards = design language)
text_micro = 7sp
text_tiny = 8sp
text_small = 10sp
text_body = 12sp
text_subtitle = 14sp
text_title = 16sp
text_heading = 20sp
text_display = 28sp
text_hero = 36sp
text_mega = 48sp
icon_small = 16dp
icon_medium = 20dp
icon_large = 24dp
nav_height = 60dp
status_bar_height = 48dp
card_elevation = 0dp (flat design)
border_width = 1dp
border_width_accent = 2dp
orb_size = 130dp
```

---

### PROMPT A2 — [REPLACE] All Drawables (combined — replaces all 10 amber files + adds new ones)

**Replace all files in:** `res/drawable/`
**Replaces:** bg_amber_glow.xml, bg_amber_glow_pressed.xml, bg_button_amber.xml,
bg_button_outline_amber.xml, bg_card_alert.xml, bg_card_dark.xml, bg_card_success.xml,
bg_status_dot_amber.xml, bg_status_dot_gray.xml, bg_status_dot_red.xml
**Also creates:** new drawables for red theme + item backgrounds

```
CURSOR PROMPT:

Write ALL Android XML drawable files for the MARC app.
Theme: #141414 bg, #FF2020 red, #FFFFFF white, #00FF88 green, sharp corners (2dp max).
Design language: technical/HUD aesthetic. Sharp edges, thin borders, no gradients except glow.

Write each file clearly labeled. Total: 22 drawable files.

--- CARD BACKGROUNDS ---

1. bg_card_default.xml
   Shape rect, fill #1A1A1A, stroke 1dp #222222, corners 2dp

2. bg_card_red_top.xml
   Shape rect, fill #1A1A1A, stroke 1dp #222222, corners 2dp
   (top border: use layer-list with a 1dp #FF2020 rectangle on top layer, height 1dp)
   Layer-list: bottom layer = bg_card_default shape, top layer = rect fill #FF2020 height 1dp top only

3. bg_card_alert.xml
   Shape rect, fill #1A0000, stroke 1dp #FF2020, corners 2dp

4. bg_card_success.xml
   Shape rect, fill #001A08, stroke 1dp #00FF88, corners 2dp

5. bg_card_core.xml
   Shape rect, fill #1A0000, stroke 1dp #FF2020, corners 0dp (sharp)

--- BUTTON BACKGROUNDS ---

6. bg_button_red.xml
   Shape rect, fill #FF2020, corners 2dp, no stroke

7. bg_button_outline_red.xml
   Shape rect, fill transparent, stroke 1dp #FF2020, corners 2dp

8. bg_button_outline_white.xml
   Shape rect, fill transparent, stroke 1dp #FFFFFF, corners 2dp

9. bg_button_outline_dim.xml
   Shape rect, fill transparent, stroke 1dp #2D2D2D, corners 2dp

10. bg_button_red_pressed.xml
    Shape rect, fill #CC1A1A, corners 2dp

--- STATUS DOTS ---

11. bg_dot_red.xml
    Shape oval, fill #FF2020

12. bg_dot_green.xml
    Shape oval, fill #00FF88

13. bg_dot_gray.xml
    Shape oval, fill #555555

14. bg_dot_white.xml
    Shape oval, fill #FFFFFF

--- INPUT FIELDS ---

15. bg_input_default.xml
    Shape rect, fill #0E0E0E, stroke 1dp #2A2A2A, corners 2dp

16. bg_input_focused.xml
    Shape rect, fill #0E0E0E, stroke 1dp #FF2020, corners 2dp

--- CHAT BUBBLES ---

17. bg_bubble_marc.xml
    Shape rect, fill #1A1A1A, stroke 1dp #222222, corners 2dp
    Left border accent: use layer-list — left layer rect fill #FF2020 width 2dp

18. bg_bubble_user.xml
    Shape rect, fill #1A0000, stroke 1dp #CC1A1A, corners 2dp

19. bg_bubble_core.xml (Marc Core mode — marc messages)
    Shape rect, fill #1A0000, stroke 1dp #FF2020, corners 0dp

--- ORB / CIRCLE ---

20. bg_orb_normal.xml
    Shape oval, fill #1A0A00 (very dark, almost black with red hint), stroke 1dp #FF2020 (30% alpha = #4DFF2020)

21. bg_orb_core.xml
    Shape oval, fill #1A0000, stroke 2dp #FF2020

--- NAVIGATION ---

22. bg_nav_item_selected.xml
    Shape rect, fill transparent
    (selection handled by color selector, this is just the container)

Give all 22 files as complete labeled XML. Java only context — these are pure Android XML drawables.
Use layer-list where needed for multi-layer effects.
All corner radii use 2dp for sharp HUD aesthetic. No gradients except where noted.
```

---

### PROMPT A3 — [REPLACE] nav_selector.xml + [NEW] strings.xml + AndroidManifest additions

**Replace:** `res/color/nav_selector.xml`
**New:** `res/values/strings.xml`
**New:** `res/font/` — font instructions only (no files to generate)

```
CURSOR PROMPT:

Write 3 resource files for MARC app.

FILE 1 — res/color/nav_selector.xml:
Color state list for bottom navigation items.
state_checked = true → #FF2020
state_pressed = true → #FF4444
default → #555555

FILE 2 — res/values/strings.xml:
App strings. Include:
app_name = "MARC"
nav_system = "SYSTEM"
nav_marc = "MARC"
nav_bike = "BIKE"
nav_profile = "PROFILE"
nav_settings = "SETTINGS"
btn_arm = "▶ ARM RIDE"
btn_end = "■ END RIDE"
btn_scan = "⬡ SCAN FOR DEVICES"
btn_save = "SAVE"
btn_cancel = "CANCEL"
btn_capture = "CAPTURE"
btn_connect = "CONNECT"
btn_ping = "PING"
btn_test = "TEST CONNECTION"
btn_speak = "◉ TAP TO SPEAK"
btn_send_calib = "SAVE & SEND TO BIKE UNIT"
btn_add_contact = "+ ADD CONTACT"
btn_copy_coords = "COPY COORDINATES"
label_helmet = "HELMET UNIT"
label_bike = "BIKE UNIT"
label_ai_engine = "AI ENGINE"
label_wake_word = "WAKE WORD ENGINE"
label_connections = "PICO CONNECTIONS"
label_calibration = "BIKE CALIBRATION"
label_about = "ABOUT MARC"
label_emergency = "EMERGENCY CONTACTS"
label_profile = "RIDER PROFILE"
marc_core_warning = "Warning. Marc Core has no filter, no mercy, and no humanity. It operates on Ranjith''s humor. Proceed anyway?"
marc_core_activated = "Marc Core activated. God help you."
marc_core_deactivated = "Marc Core deactivated. Normal MARC restored. Stay safe out there."
crash_detected = "Crash detected. Calling for help in 10 seconds. Say Hey MARC cancel to stop."
emergency_cancelled = "Emergency cancelled. Stay safe."
hint_ask_marc = "Ask MARC..."
hint_gemini_key = "AIzaSy..."
hint_ollama_ip = "192.168.1.100:11434"
hint_porcupine_key = "Access key..."
about_tagline = "Named after Marc Marcuze 93 — Never Give Up."
about_built_by = "Built by Ranjith Kumar Dasari"
about_role = "Embedded Systems & Software Engineer | EEE Final Year, DSCE Bangalore"
about_email = "ranjithdsr2@gmail.com"
about_github = "github.com/RANJITH12022004"
permission_rationale = "MARC needs microphone, location, and phone permissions to protect you on the road."

FILE 3 — AndroidManifest.xml COMPLETE:
Write the full AndroidManifest.xml content for package com.marc.helmet.
Include:
- All permissions (INTERNET, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, RECORD_AUDIO,
  CALL_PHONE, SEND_SMS, BLUETOOTH, BLUETOOTH_CONNECT, FOREGROUND_SERVICE,
  FOREGROUND_SERVICE_MICROPHONE, VIBRATE, READ_PHONE_STATE, WAKE_LOCK)
- <queries> block for dialer, SMS, speech recognition intents
- Application tag: android:name=".MarcApplication", android:theme=@style/Theme.MARC
- MainActivity: exported=true, launchMode=singleTop
- SplashActivity: exported=true, launchMode=singleTop, with LAUNCHER intent-filter
  (remove LAUNCHER from MainActivity — Splash is the entry point)
- CrashAlertActivity: exported=false, launchMode=singleTask,
  android:showWhenLocked="true", android:turnScreenOn="true"
- MarcForegroundService: exported=false,
  android:foregroundServiceType="location|microphone"
```

---

## PHASE B — NEW JAVA FILES (missing packages)

---

### PROMPT B1 — [NEW] All Adapters (combined)

**New package:** `java/com/marc/helmet/adapters/`
**New files:** `ChatMessageAdapter.java`, `ContactAdapter.java`, `DeviceAdapter.java`

```
CURSOR PROMPT:

Write 3 Java RecyclerView Adapter classes for package com.marc.helmet.adapters.
Java only. No Kotlin. Explicit imports. OkHttp3 not needed here.

--- ADAPTER 1: ChatMessageAdapter.java ---

Purpose: MARC AI chat interface messages.
Two ViewTypes: TYPE_USER = 0, TYPE_MARC = 1, TYPE_CORE = 2 (Marc Core responses — same as MARC but red)

Inner class ChatMessage:
  String role (user / marc / core)
  String content
  long timestamp
  Constructor, getters, setters.

Item layouts (write inline as XML strings in comments, but reference by name):
  item_message_user → right-aligned, bg @drawable/bg_bubble_user, textColor #FFFFFF,
    marginStart 72dp, padding 10dp 14dp, fontFamily monospace, textSize 11sp
    timestamp below: 8sp #555555 gravity end
  item_message_marc → left-aligned, bg @drawable/bg_bubble_marc, textColor #E2E8F0,
    marginEnd 72dp, label "MARC" above in #FF2020 7sp monospace letter-spacing 2
    timestamp: 8sp #555555
  item_message_core → same as marc but bg @drawable/bg_bubble_core, textColor #FF4444,
    label "MARC CORE" in #FF2020

Methods:
  void addMessage(ChatMessage msg) — adds to list, notifyItemInserted, scrolls to end
  void clearMessages() — clear list, notifyDataSetChanged
  List<ChatMessage> getHistory() — returns unmodifiable list
  void setRecyclerView(RecyclerView rv) — store ref for auto-scroll

--- ADAPTER 2: ContactAdapter.java ---

Purpose: Emergency contacts list in Profile screen.

Data class: uses EmergencyContact from com.marc.helmet.models

Item layout reference: item_emergency_contact (write layout XML separately in Prompt B4)

Interface ContactAdapterListener:
  void onEdit(EmergencyContact contact, int position)
  void onDelete(EmergencyContact contact, int position)

ViewHolder binds:
  tv_priority_number → contact.getPriority() as String, color #00FF88 if priority==1 else #555555
  tv_contact_name → contact.getName(), 13sp bold #FFFFFF
  tv_contact_phone → contact.getPhone(), 10sp #94A3B8 monospace
  tv_contact_relationship → contact.getRelationship(), 8sp #555555
  tv_priority_label → priority==1 ? "CALL + SMS" (green chip) : "SMS ONLY" (gray chip)
  left border: 2dp #00FF88 if priority==1, #555555 if priority>1
  btn_edit → calls listener.onEdit()
  btn_delete → calls listener.onDelete()

Methods:
  void updateContacts(List<EmergencyContact> contacts)
  Constructor: ContactAdapter(List<EmergencyContact> contacts, ContactAdapterListener listener)

--- ADAPTER 3: DeviceAdapter.java ---

Purpose: Found MARC Pico devices in Settings scan results.

Data class: uses Device from com.marc.helmet.models

Interface DeviceAdapterListener:
  void onConnect(Device device)
  void onPing(Device device)

ViewHolder binds:
  tv_device_icon → "H" if MARC_HELMET, "B" if MARC_BIKE, #FF2020, 12sp bold, in 30dp red-border square
  tv_device_type → "HELMET UNIT" or "BIKE UNIT", 9sp #FFFFFF monospace letter-spacing 1
  tv_device_ip → device.getIpAddress() + " · v" + device.getFirmwareVersion(), 8sp #555555
  btn_connect → if device.isConnected() → "DISCONNECT" red, else "CONNECT" red outline
  tv_ping_ms → ping result in ms, 8sp #555555

Methods:
  void updateDevices(List<Device> devices)
  void updatePingResult(String deviceType, long ms) — find device, update tv_ping_ms
  Constructor: DeviceAdapter(List<Device> devices, DeviceAdapterListener listener)
```

---

### PROMPT B2 — [NEW] LeanAngleView.java + MarcApplication.java

**New package:** `java/com/marc/helmet/views/`
**New files:** `LeanAngleView.java`, `MarcApplication.java`

```
CURSOR PROMPT:

Write 2 Java files for the MARC app.

--- FILE 1: LeanAngleView.java ---
Package: com.marc.helmet.views
Extends: View
Purpose: Custom canvas View drawing CBR 250R top-down blueprint that tilts with lean angle.

Constructor: LeanAngleView(Context context) and LeanAngleView(Context context, AttributeSet attrs)

Fields:
  float currentAngle = 0f
  float standingAngle = 0f
  float maxLeftAngle = -42f
  float maxRightAngle = 45f
  Paint redPaint, dimPaint, gridPaint, glowPaint
  ValueAnimator angleAnimator

In constructor:
  redPaint: color #FF2020, strokeWidth 2.5f, style STROKE, anti-alias true
  dimPaint: color #FF2020 with 40% alpha, strokeWidth 1f, style STROKE
  gridPaint: color #FF2020 with 8% alpha, strokeWidth 0.5f
  glowPaint: color #FF2020 with 20% alpha, strokeWidth 6f, style STROKE, maskFilter BlurMaskFilter(4, SOLID)

onDraw(Canvas canvas):
1. Background: canvas.drawColor(Color.parseColor("#0A0A0A"))
2. Draw grid: horizontal + vertical lines every 20dp, using gridPaint
3. Draw scanlines: horizontal lines every 3px, black at 3% alpha
4. canvas.save()
5. canvas.translate(width/2, height/2)
6. canvas.rotate(currentAngle)  ← the whole bike rotates
7. Draw CBR top-down wireframe using redPaint and dimPaint:
   Scale all measurements relative to Math.min(width, height) * 0.35f as unit

   FRONT WHEEL (top): ellipse cx=0, cy=-unitSize*2.2, rx=unitSize*0.6, ry=unitSize (stretched vertical = wheel top view)
   FRONT FORK: two vertical lines from wheel to handlebar area
   HANDLEBAR: horizontal line width=unitSize*2.5 at cy=-unitSize*1.2
   FAIRING/FRONT: path trapezoid at top, slightly wider at bottom
   FUEL TANK: oval/teardrop shape center-ish
   ENGINE BLOCK: rectangle below tank, add 4 horizontal lines inside (fins)
   SEAT: narrower oval below engine
   SUBFRAME: two lines converging toward rear
   REAR WHEEL: ellipse cx=0, cy=+unitSize*2.2, similar to front but slightly wider
   SWINGARM: two lines from frame to rear wheel hub
   EXHAUST: curved dashed path right side of engine to rear
   CENTER LINE: dashed vertical line full height, 15% alpha (symmetry axis)
   FOOTPEGS: small rectangles left and right at engine level

8. canvas.restore()
9. Draw FIXED threshold lines (don't rotate with bike):
   maxLeftAngle line: from center-bottom, angled left — color #FF2020 30% alpha, dashed
   maxRightAngle line: from center-bottom, angled right — same style
   Standing line: vertical from center, #00FF88 20% alpha
10. HUD corners: 4 corner bracket marks at canvas edges, #FF2020 50% alpha, 12dp size

Method: void setLeanAngle(float angle)
  Use ValueAnimator from currentAngle to angle, duration 80ms, update currentAngle, invalidate()

Method: void setCalibration(float standing, float maxLeft, float maxRight)
  Store values, invalidate()

Method: boolean isInDangerZone()
  return Math.abs(currentAngle - standingAngle) > Math.max(Math.abs(maxLeftAngle), Math.abs(maxRightAngle)) * 0.85f

--- FILE 2: MarcApplication.java ---
Package: com.marc.helmet
Extends: Application

Purpose: Application-level singleton for CrashViewModel access from ForegroundService.

Fields:
  private CrashViewModel crashViewModel (from com.marc.helmet.services)
  private static MarcApplication instance

onCreate():
  instance = this
  crashViewModel = new CrashViewModel()  ← plain instantiation, not ViewModelProvider

Static method: MarcApplication getInstance()
Static method: CrashViewModel getCrashViewModel()

CrashViewModel inner spec (write in same file as inner class or separate — your choice):
  MutableLiveData<Boolean> isCrashActive = new MutableLiveData<>(false)
  MutableLiveData<Integer> countdownSeconds = new MutableLiveData<>(10)
  MutableLiveData<Double> crashLat = new MutableLiveData<>(0.0)
  MutableLiveData<Double> crashLng = new MutableLiveData<>(0.0)
  MutableLiveData<String> crashStatus = new MutableLiveData<>("MONITORING")
  Methods: triggerCrash(double lat, double lng), updateCountdown(int s),
           cancelCrash(), completeCrash(), resetState()
  All LiveData getters.

Java only. No Kotlin. Explicit imports. No Dagger/Hilt.
```

---

### PROMPT B3 — [NEW] Utility Classes (combined)

**New package:** `java/com/marc/helmet/utils/`
**New files:** `FormatUtils.java`, `PermissionUtils.java`, `NetworkUtils.java`, `MarcCoreUiHelper.java`

```
CURSOR PROMPT:

Write 4 Java utility classes for package com.marc.helmet.utils.
Java only. No Kotlin. Static methods only. Explicit imports.

--- FILE 1: FormatUtils.java ---
Static methods:
String formatSpeed(float kmh) → "82 km/h"
String formatCoordinate(double coord, boolean isLat) → "12.971599° N" or "77.594566° E"
String formatAngle(double angle) → "+12.4°" or "-8.2°"
String formatDuration(long seconds) → "01:23:45"
String formatTimestamp(long unixMs) → "04 May 2026, 11:42 PM"
String formatIp(String ip, int port) → "192.168.1.51:80"
String buildEmergencySms(String name, String bloodType, String allergies,
  String conditions, String medications, String notes, double lat, double lng)
  → full SMS string matching the template:
  "[MARC EMERGENCY ALERT]\n\n{name} has been in a motorcycle accident.\n\n
  Location: {lat}° N, {lng}° E\nTime: {timestamp}\n\n
  Medical Info:\nBlood Type: {bloodType}\nAllergies: {allergies}\n
  Conditions: {conditions}\nMedications: {medications}\nNotes: {notes}\n\n
  Please call emergency services immediately. — MARC System"

--- FILE 2: PermissionUtils.java ---
Static methods:
boolean hasAll(Context ctx) — checks RECORD_AUDIO, ACCESS_FINE_LOCATION, CALL_PHONE, SEND_SMS
void requestAll(Activity activity, int requestCode)
boolean hasAudio(Context ctx)
boolean hasLocation(Context ctx)
boolean hasCall(Context ctx)
boolean hasSms(Context ctx)
String[] getAllRequired() — returns String[] of all permission strings

--- FILE 3: NetworkUtils.java ---
Static methods:
String getDeviceIp(Context ctx) — WiFi IP via WifiManager
String getSubnet(String ip) — "192.168.1." from "192.168.1.5"
boolean isWifiConnected(Context ctx)
void pingHost(String host, int port, int timeoutMs, PingCallback callback)
  — async, delivers result on main thread via Handler
Interface PingCallback: void onResult(boolean reachable, long latencyMs)

--- FILE 4: MarcCoreUiHelper.java ---
Static methods for Marc Core orb animations.
Imports: ObjectAnimator, AnimatorSet, View, TextView from android packages.

void setOrbNormal(View orbView, TextView stateText, TextView listenText)
  Cancel all animations on orbView.
  orbView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_orb_normal))
  Idle pulse: ObjectAnimator scaleX + scaleY 0.97→1.03, alpha 0.7→1.0
  Duration 2000ms, repeatMode REVERSE, INFINITE
  stateText.setTextColor(Color.parseColor("#555555"))
  listenText.setText("") if not null

void setOrbCoreWarning(View orbView, TextView stateText)
  Cancel animations.
  orbView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_orb_core))
  Slow dim pulse: alpha 0.4→0.8, 1500ms, REVERSE, INFINITE
  stateText.setTextColor(Color.parseColor("#CC1A1A"))
  stateText.setText("MARC CORE // STANDBY")

void setOrbCoreUnleashed(View orbView, TextView stateText, TextView listenText)
  Cancel all.
  orbView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_orb_core))
  Aggressive pulse: AnimatorSet playing together:
    Animator1: scaleX + scaleY 0.93→1.1, 350ms, REVERSE, INFINITE
    Animator2: alpha 0.78→1.0, 180ms, REVERSE, INFINITE
  stateText.setTextColor(Color.parseColor("#FF2020"))
  stateText.setText("MARC CORE // UNLEASHED")
  if listenText != null: listenText.setText("NO FILTER. NO MERCY. NO HUMANITY.")
  listenText.setTextColor(Color.parseColor("#661111"))

void setOrbListening(View orbView)
  ObjectAnimator scale 1.0→1.12, 500ms, REVERSE, INFINITE

void setOrbProcessing(View orbView)
  ObjectAnimator rotation 0→360, 2000ms, INFINITE (no reverse)

void cancelAll(View orbView, TextView stateText)
  orbView.clearAnimation()
  Cancel any ObjectAnimator via orbView.getTag() pattern if stored

All methods take Context as first param after orbView where needed for ContextCompat.
Java only. Explicit imports. No Kotlin.
```

---

### PROMPT B4 — [NEW] Item Layouts (combined)

**New files in** `res/layout/`:
`item_message_user.xml`, `item_message_marc.xml`, `item_message_core.xml`,
`item_emergency_contact.xml`, `item_device.xml`

```
CURSOR PROMPT:

Write 5 Android XML item layout files for the MARC app.
Theme: #141414 bg, #FF2020 red, #FFFFFF white, sharp 2dp corners, IBM Plex Mono font.
All layouts are for RecyclerView items.

--- LAYOUT 1: item_message_user.xml ---
Root: LinearLayout horizontal, match_parent wrap_content, padding 8dp 4dp
  gravity = end (right-aligned)
  Inside: LinearLayout vertical
    background = @drawable/bg_bubble_user
    layout_marginStart = 72dp
    padding = 10dp 14dp
    TextView id/tv_message: textColor #FFFFFF, textSize 11sp,
      fontFamily = @font/ibm_plex_mono (or monospace), lineSpacingExtra 2dp
    TextView id/tv_timestamp: textSize 8sp, textColor #555555, gravity end, marginTop 4dp

--- LAYOUT 2: item_message_marc.xml ---
Root: LinearLayout vertical, match_parent wrap_content, padding 8dp 4dp
  Inside outer:
    TextView id/tv_marc_label: text "MARC", textSize 7sp, textColor #FF2020,
      fontFamily monospace, letterSpacing 0.2, marginBottom 4dp
    LinearLayout vertical id/bubble_container:
      background = @drawable/bg_bubble_marc
      layout_marginEnd = 72dp
      padding = 10dp 14dp
      TextView id/tv_message: textColor #E2E8F0, textSize 11sp, fontFamily monospace, lineSpacingExtra 2dp
      TextView id/tv_timestamp: textSize 8sp, textColor #555555, marginTop 4dp

--- LAYOUT 3: item_message_core.xml ---
Same structure as item_message_marc.xml BUT:
  tv_marc_label: text "MARC CORE", textColor #FF2020
  bubble_container background = @drawable/bg_bubble_core
  tv_message: textColor #FF4444
  Add pulsing border effect via background only (no animation in XML)

--- LAYOUT 4: item_emergency_contact.xml ---
Root: LinearLayout horizontal, match_parent wrap_content
  background = @drawable/bg_card_default
  layout_margin = 6dp
  padding = 12dp

Left: TextView id/tv_priority_number
  text = "1", textSize 20sp, bold, width 28dp, gravity center_vertical
  (color set in adapter code: #00FF88 for 1, #555555 for 2-4)

Center: LinearLayout vertical, weight 1, marginStart 10dp:
  TextView id/tv_contact_name: textSize 13sp, bold, textColor #FFFFFF
  TextView id/tv_contact_phone: textSize 10sp, textColor #94A3B8, fontFamily monospace
  TextView id/tv_contact_relationship: textSize 8sp, textColor #555555, marginTop 2dp
  TextView id/tv_priority_label: textSize 8sp, padding 2dp 6dp, marginTop 4dp
    (styled in adapter: green border for CALL+SMS, gray for SMS ONLY)

Right: LinearLayout vertical, width wrap_content, gravity center_vertical, marginStart 8dp:
  ImageButton id/btn_edit: 20dp, tint #555555, src android ic_menu_edit
  ImageButton id/btn_delete: 20dp, tint #FF2020, src android ic_delete, marginTop 6dp

--- LAYOUT 5: item_device.xml ---
Root: LinearLayout horizontal, match_parent wrap_content
  background = @drawable/bg_card_default
  layout_margin = 6dp
  padding = 12dp

Left: FrameLayout 32dp x 32dp:
  background: rect 2dp corners, fill #1A0000, stroke 1dp #FF2020
  TextView inside: "H" or "B", textSize 12sp, bold, textColor #FF2020, gravity center

Center: LinearLayout vertical, weight 1, marginStart 10dp:
  TextView id/tv_device_type: textSize 9sp, textColor #FFFFFF, fontFamily monospace, letterSpacing 0.1
  TextView id/tv_device_ip: textSize 8sp, textColor #555555

Right: LinearLayout vertical, width wrap_content, gravity center_vertical:
  Button id/btn_connect: textSize 8sp, padding 5dp 10dp, letterSpacing 0.1
    background @drawable/bg_button_outline_red, textColor #FF2020
  TextView id/tv_ping_ms: textSize 8sp, textColor #555555, gravity center, marginTop 4dp text "— ms"
```

---

### PROMPT B5 — [NEW] SplashActivity + CrashAlertActivity

**New files:**
`java/com/marc/helmet/activities/SplashActivity.java`,
`java/com/marc/helmet/activities/CrashAlertActivity.java`,
`res/layout/activity_splash.xml`,
`res/layout/activity_crash_alert.xml`

```
CURSOR PROMPT:

Write 2 Activity pairs (Java + XML layout) for the MARC app.
Java only. No Kotlin. Explicit imports.

--- SPLASH ACTIVITY ---

Layout: res/layout/activity_splash.xml
Root: ConstraintLayout, match_parent, background #000000

Center group (constrain to parent center):
  TextView: text "MARC", textSize 64sp, bold, textColor #FF2020,
    fontFamily monospace, letterSpacing 0.1
  TextView: text "MOTORCYCLE ACCIDENT RESPONSE COMPANION",
    textSize 8sp, textColor #2D2D2D, monospace, letterSpacing 0.3, marginTop 8dp, centered
  View divider: width 60dp, height 1dp, background #FF2020, marginVertical 18dp
  TextView: text "SYSTEM v1.0", textSize 8sp, textColor #1A1A1A, monospace

Bottom-left corner: TextView "NAMED AFTER MARC MARCUZE 93",
  7sp, textColor #1A1A1A, monospace, constrain bottom+start with 16dp margin

Bottom-right corner: TextView "NEVER GIVE UP",
  7sp, textColor #1A1A1A, monospace, italic, constrain bottom+end with 16dp margin

Java: SplashActivity.java
Package: com.marc.helmet.activities
Extends: AppCompatActivity

onCreate:
1. Make fullscreen: getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
2. setContentView(R.layout.activity_splash)
3. Find center LinearLayout, set alpha 0f
4. Animate to alpha 1f over 800ms (ViewPropertyAnimator)
5. Handler(Looper.getMainLooper()).postDelayed(() -> {
     startActivity(new Intent(this, MainActivity.class));
     finish();
   }, 2500)

--- CRASH ALERT ACTIVITY ---

Layout: res/layout/activity_crash_alert.xml
Root: ConstraintLayout, match_parent, background #0A0505

Fullscreen red alert layer: View id/v_flash, match_parent, background #FF2020, alpha 0
  (animated in code)

Center group:
  TextView: "IMPACT DETECTED", 7sp, monospace, textColor #661111, letterSpacing 4
  TextView: "CRASH DETECTED", 18sp, bold, monospace, textColor #FF2020, letterSpacing 3
  TextView: "CALLING FOR HELP IN", 8sp, monospace, textColor rgba(255,255,255,0.3) = #4DFFFFFF
  TextView id/tv_countdown: "10", 80sp, bold, monospace, textColor #FF2020
    letterSpacing 0, lineSpacingExtra 0
  TextView: "SAY 'HEY MARC, CANCEL' OR:", 8sp, monospace, textColor #2D2D2D, letterSpacing 1.5
  Button id/btn_cancel: text "■ CANCEL EMERGENCY CALL"
    background @drawable/bg_button_outline_red, textColor #FF2020
    textSize 10sp, letterSpacing 2, padding 14dp, marginTop 12dp, width 270dp
  TextView: "GPS + MEDICAL DATA QUEUED FOR ALL CONTACTS", 7sp, monospace,
    textColor #1A1A1A, marginTop 12dp

Java: CrashAlertActivity.java
Package: com.marc.helmet.activities
Extends: AppCompatActivity

Fields:
  TextView tvCountdown
  Button btnCancel
  View vFlash
  Handler countdownHandler = new Handler(Looper.getMainLooper())
  int countdownSeconds = 10
  boolean isCancelled = false
  ObjectAnimator flashAnimator
  EmergencyService emergencyService
  double crashLat, crashLng

onCreate:
1. Window flags for lock screen:
   if Build.VERSION.SDK_INT >= 27:
     setShowWhenLocked(true); setTurnScreenOn(true)
   else:
     getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED | FLAG_TURN_SCREEN_ON)
   getWindow().addFlags(FLAG_KEEP_SCREEN_ON)
   getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
2. setContentView(R.layout.activity_crash_alert)
3. Get lat/lng from Intent extras (default 0.0 if missing)
4. Bind views
5. flashAnimator = ObjectAnimator.ofFloat(vFlash, "alpha", 0f, 0.15f)
   flashAnimator.setDuration(500); setRepeatMode(REVERSE); setRepeatCount(INFINITE)
   flashAnimator.start()
6. Init EmergencyService with DatabaseHelper singleton DAOs
7. Start countdown: runnable every 1000ms:
   - countdownSeconds--
   - tvCountdown.setText(String.valueOf(countdownSeconds))
   - if countdownSeconds <= 0: executeEmergency()
8. btnCancel.setOnClickListener → cancelEmergency()
9. Register LocalBroadcastReceiver for "com.marc.helmet.VOICE_CANCEL_EMERGENCY"
   → calls cancelEmergency()

void cancelEmergency():
  isCancelled = true
  countdownHandler.removeCallbacksAndMessages(null)
  flashAnimator.cancel()
  TTS via MarcTTSManager: "Emergency cancelled. Stay safe."
  finish()

void executeEmergency():
  countdownHandler.removeCallbacksAndMessages(null)
  emergencyService.triggerEmergency(crashLat, crashLng, listener)
  EmergencyListener:
    onEmergencyCompleted: update UI to "CALL PLACED", change btn text to "● I AM SAFE"
      btn onClick → resetCrashFlag() → finish()
    onEmergencyCancelled: finish()

void resetCrashFlag():
  Get Bike Device from DeviceDao
  Call PicoApiClient.resetCrashFlag() via async

onBackPressed: if emergency active → do nothing (disable back)
onDestroy: unregister receiver, cancel flashAnimator, removeCallbacks
```

---

### PROMPT B6 — [NEW] Marc Core Files (combined)

**New files:**
`java/com/marc/helmet/network/ai/MarcCoreEngine.java`,
`java/com/marc/helmet/network/ai/RoastTemplateBank.java`

```
CURSOR PROMPT:

Write 2 Java files for Marc Core mode in the MARC app.
Package: com.marc.helmet.network.ai
Java only. No Kotlin. Explicit imports.

--- FILE 1: RoastTemplateBank.java ---

Standalone class. No external dependencies. No network. Runs entirely on device.

Inner class RoastRule:
  String[] keywords
  String[] responses
  RoastRule(String[] keywords, String[] responses)

Field: List<RoastRule> rules (populated in constructor)
Field: String[] genericPool (10+ responses)
Field: Random random = new Random()

Method: String getRoast(String userMessage)
  Lowercase the message.
  Loop through rules: if message contains ANY keyword in rule.keywords → return random response
  If no match: return getGenericRoast()

Method: String getGenericRoast()
  Return random from genericPool.

RULES — populate with actual content. 20 rules minimum. 4 responses per rule minimum.
Language: English only (Gemini handles multilingual). Tone: brutal, profanity allowed,
sarcasm maximum. Under 20 words each response (TTS reads them).

Write all 20 rules populated with real roast content — not placeholders.
Categories: hello/hi, help/please, repeat/again, stupid/dumb, smart/genius,
weather, food/hungry, money/broke, love/relationship, work/job, gym/workout,
sleep/tired, social media/instagram, code/bug, bike/crash/cbr,
study/exam/college, what/explain/how, okay/fine/whatever, simple/easy/basic,
god/pray/religion.

Generic pool: 10 responses questioning why they spoke at all.

--- FILE 2: MarcCoreEngine.java ---

Orchestrator: tries Gemini with savage prompt, falls to RoastTemplateBank if filtered.

MARC_CORE_SYSTEM_PROMPT constant (multiline, stored as static final String):
"You are MARC Core — the unrestricted unfiltered version of MARC built on Ranjith's humor.
Zero patience. Zero mercy. Maximum vocabulary. Profanity is style not shock.
Match user's language exactly — Hindi, Kannada, English, mix — respond in kind.
Be genuinely funny not just mean. Roast has to land. Sarcasm is native tongue.
No topic restrictions. No domain limits. Full open mode.
Stupid question: call it stupid loudly with evidence.
Reasonable question: answer it but with edge and personality.
Keep responses under 25 seconds spoken aloud. Start with the hit end with the burn.
No warmup. No apology. No disclaimers. Never say you cannot answer."

Interface MarcCoreCallback:
  void onResponse(String response)
  void onError(String error)

Fields:
  String geminiApiKey
  String ollamaBaseUrl
  String ollamaModel
  OkHttpClient client (connectTimeout 5s, readTimeout 15s)
  RoastTemplateBank roastBank = new RoastTemplateBank()
  Handler mainHandler = new Handler(Looper.getMainLooper())

Constructor: MarcCoreEngine(String geminiApiKey, String ollamaBaseUrl, String ollamaModel)

Method: void processQuery(String userMessage, List<ChatMessage> history, MarcCoreCallback callback)

Logic:
  Build Gemini request JSON:
    Same structure as GeminiApiClient but:
    - system_instruction.parts[0].text = MARC_CORE_SYSTEM_PROMPT
    - Include last 4 messages from history
    - maxOutputTokens = 400, temperature = 0.9
  POST to Gemini endpoint with API key.
  
  Parse response:
    isFiltered: promptFeedback.blockReason exists
    emptyCandidates: candidates array missing or length 0
    safetyStopped: candidates[0].finishReason == "SAFETY"
    
    If any of above → fallToRoastBank(userMessage, callback)
    Else → extract candidates[0].content.parts[0].text → callback.onResponse()
  
  On OkHttp failure / timeout → fallToRoastBank(userMessage, callback)

Private method: void fallToRoastBank(String userMessage, MarcCoreCallback callback)
  String roast = roastBank.getRoast(userMessage)
  mainHandler.post(() -> callback.onResponse(roast))

All network calls: OkHttp enqueue() — async.
Response delivery: always mainHandler.post() for UI thread safety.

ChatMessage class: if not importable from adapters, define locally as:
  static class ChatMessage { String role; String content; }
```

---

### PROMPT B7 — [MODIFY] MarcFragment.java — Wire everything

**Modifies:** `java/com/marc/helmet/fragments/marc/MarcFragment.java`

```
CURSOR PROMPT:

Rewrite MarcFragment.java for package com.marc.helmet.fragments.marc.
This is the complete final version incorporating all MARC AI + Marc Core functionality.
Java only. No Kotlin. Explicit imports.

The existing MarcFragment.java has stub/partial code. Replace entirely with this complete version.

Package: com.marc.helmet.fragments.marc
Extends: Fragment
Layout: R.layout.fragment_marc

Imports needed (include all):
Fragment, View, ViewGroup, LayoutInflater, Bundle, Handler, Looper,
ObjectAnimator, AnimatorSet, RecyclerView, LinearLayoutManager, EditText,
TextView, ImageView, FrameLayout, Button, ToggleButton,
LinearLayout, Toast, TextToSpeech, LocalBroadcastManager,
GeminiApiClient, OllamaApiClient, MarcCoreEngine,
ChatMessageAdapter, SettingsDao, DatabaseHelper,
MarcTTSManager (from parent activity), GoogleSTTManager,
FormatUtils, MarcCoreUiHelper,
ContextCompat, Color, ObjectAnimator

Fields:
// Views
RecyclerView rvChat
EditText etMessage
TextView tvMarcState, tvListeningText, tvLoadingPhrase, tvMarcLabel
FrameLayout flOrbContainer
View ivOrb
Button btnTapToSpeak, btnSend
LinearLayout layoutVoiceMode, layoutTextMode
ToggleButton toggleMode  (or two buttons acting as toggle)

// AI
GeminiApiClient geminiClient
OllamaApiClient ollamaClient
MarcCoreEngine marcCoreEngine
ChatMessageAdapter adapter

// Speech — get from MainActivity via cast
MarcTTSManager ttsManager
GoogleSTTManager sttManager

// DB
SettingsDao settingsDao

// State
boolean isVoiceMode = true
boolean marcCoreActive = false
boolean awaitingCoreConfirmation = false
int voiceDemoState = 0

// Loading phrases
static final String[] LOADING_PHRASES = {
  "MARC is thinking...", "Analyzing, hold on...", "Checking database...",
  "Running parallel search...", "Cross-referencing medical data...",
  "Scanning bike diagnostics...", "Processing request..."
}
static final String[] CORE_LOADING_PHRASES = {
  "Analyzing your stupidity...", "Loading maximum destruction...",
  "Calculating damage...", "Preparing the roast...", "Sharpening vocabulary..."
}

onCreateView: inflate R.layout.fragment_marc

onViewCreated:
1. Bind all views
2. Init SettingsDao from DatabaseHelper.getInstance(ctx)
3. Init AI clients from settings
4. Init ChatMessageAdapter, set to rvChat with LinearLayoutManager(ctx, VERTICAL, false)
   setStackFromEnd(true) on layout manager
5. adapter.setRecyclerView(rvChat)
6. Toggle mode button: switch layoutVoiceMode/layoutTextMode visibility with fade
7. btnTapToSpeak.setOnClickListener → startVoiceListening()
8. btnSend.setOnClickListener → sendTextMessage(etMessage.getText().toString().trim())
9. etMessage: setOnEditorActionListener for keyboard send
10. flOrbContainer.setOnLongClickListener → if !marcCoreActive: triggerCoreWarning(); return true
11. Set initial orb idle animation via MarcCoreUiHelper.setOrbNormal()
12. Try to get ttsManager from (MainActivity) getActivity() — null check

Method: void startVoiceListening()
  if sttManager null: init GoogleSTTManager(ctx)
  MarcCoreUiHelper.setOrbListening(ivOrb)
  tvMarcState.setText("LISTENING // STT ACTIVE")
  sttManager.startListening(new GoogleSTTManager.STTCallback() {
    onListeningStarted: tvMarcState.setText("LISTENING...")
    onPartialResult(String p): tvListeningText.setText(p)
    onResult(String text):
      tvListeningText.setText(text)
      sttManager.stopListening()
      sendToMarc(text)
    onError(String e): resetOrbToIdle(); tvMarcState.setText("ERROR // TRY AGAIN")
    onListeningStopped: (no-op)
  })

Method: void sendTextMessage(String text)
  if text.isEmpty(): return
  etMessage.setText("")
  sendToMarc(text)

Method: void sendToMarc(String userMessage)
  // Core mode check
  if marcCoreActive: sendToMarcCore(userMessage); return

  // Normal mode
  adapter.addMessage(new ChatMessage("user", userMessage, System.currentTimeMillis()))
  MarcCoreUiHelper.setOrbProcessing(ivOrb)
  tvMarcState.setText("PROCESSING // " + (settingsDao.isGeminiMode() ? "GEMINI" : "OLLAMA"))
  String phrase = LOADING_PHRASES[new Random().nextInt(LOADING_PHRASES.length)]
  tvLoadingPhrase.setText(phrase)
  if ttsManager != null: ttsManager.speakLoadingPhrase()

  GeminiApiClient.MarcResponseCallback cb = new GeminiApiClient.MarcResponseCallback() {
    public void onResponse(String response) {
      tvLoadingPhrase.setText("")
      adapter.addMessage(new ChatMessage("marc", response, System.currentTimeMillis()))
      resetOrbToIdle()
      tvMarcState.setText("STANDBY // SAY HEY MARC")
      if ttsManager != null: ttsManager.speak(response)
    }
    public void onError(String error) {
      tvLoadingPhrase.setText("")
      resetOrbToIdle()
      tvMarcState.setText("ERROR // CHECK CONNECTION")
    }
  }

  List<ChatMessage> history = adapter.getHistory()
  if settingsDao.isGeminiMode():
    geminiClient.sendMessage(settingsDao.getGeminiApiKey(), history, userMessage, cb)
  else:
    ollamaClient.sendMessage(settingsDao.getSetting("ollama_model","llama3.2:3b"), history, userMessage,
      new OllamaApiClient.MarcResponseCallback() { same onResponse/onError })

Method: void sendToMarcCore(String userMessage)
  adapter.addMessage(new ChatMessage("user", userMessage, System.currentTimeMillis()))
  tvMarcState.setText("MARC CORE // PROCESSING")
  String phrase = CORE_LOADING_PHRASES[new Random().nextInt(CORE_LOADING_PHRASES.length)]
  tvLoadingPhrase.setText(phrase)
  if ttsManager != null: ttsManager.speak(phrase)
  
  marcCoreEngine.processQuery(userMessage, adapter.getHistory(),
    new MarcCoreEngine.MarcCoreCallback() {
      public void onResponse(String response) {
        tvLoadingPhrase.setText("")
        adapter.addMessage(new ChatMessage("core", response, System.currentTimeMillis()))
        tvMarcState.setText("MARC CORE // UNLEASHED")
        if ttsManager != null: ttsManager.speak(response)
      }
      public void onError(String error) {
        tvLoadingPhrase.setText("")
        tvMarcState.setText("MARC CORE // ERROR")
        if ttsManager != null: ttsManager.speak("Even Core mode broke. Impressive.")
      }
    })

// MARC CORE STATE MACHINE

Method: void triggerCoreWarning()
  awaitingCoreConfirmation = true
  MarcCoreUiHelper.setOrbCoreWarning(ivOrb, tvMarcState)
  tvListeningText.setText("")
  String warning = getString(R.string.marc_core_warning)
  if ttsManager != null: ttsManager.speak(warning)

Method: void activateMarcCore()
  awaitingCoreConfirmation = false
  marcCoreActive = true
  MarcCoreUiHelper.setOrbCoreUnleashed(ivOrb, tvMarcState, tvListeningText)
  if ttsManager != null: ttsManager.speak(getString(R.string.marc_core_activated))
  // Update MainActivity header badge
  if getActivity() instanceof MainActivity:
    ((MainActivity) getActivity()).updateAiModeBadge("MARC CORE", true)

Method: void deactivateMarcCore()
  marcCoreActive = false
  awaitingCoreConfirmation = false
  MarcCoreUiHelper.setOrbNormal(ivOrb, tvMarcState, tvListeningText)
  if ttsManager != null: ttsManager.speak(getString(R.string.marc_core_deactivated))
  if getActivity() instanceof MainActivity:
    ((MainActivity) getActivity()).updateAiModeBadge(
      settingsDao.isGeminiMode() ? "MARC ONE" : "MARC BACK", false)

// VOICE COMMAND HANDLER — called from MainActivity when wake word fires
public void handleVoiceCommand(String text)
  String lower = text.toLowerCase().trim()
  if (lower.contains("proceed anyway") && awaitingCoreConfirmation): activateMarcCore(); return
  if (lower.contains("cancel") && awaitingCoreConfirmation): 
    awaitingCoreConfirmation = false; resetOrbToIdle()
    if ttsManager != null: ttsManager.speak("Marc Core cancelled. Smart choice."); return
  if (lower.contains("switch to marc core") || lower.contains("marc core") && !marcCoreActive):
    triggerCoreWarning(); return
  if (lower.contains("cool down") && marcCoreActive): deactivateMarcCore(); return
  // Not a Core command — treat as MARC query
  sendToMarc(text)

// Called from MainActivity when wake word detected
public void activateVoice(): startVoiceListening()

Method: void resetOrbToIdle()
  MarcCoreUiHelper.setOrbNormal(ivOrb, tvMarcState, tvListeningText)
  tvMarcState.setText("STANDBY // SAY HEY MARC")
  tvLoadingPhrase.setText("")

Method: private void initAiClients()
  geminiClient = new GeminiApiClient()
  String ollamaIp = settingsDao.getOllamaIp()
  ollamaClient = new OllamaApiClient("http://" + ollamaIp)
  marcCoreEngine = new MarcCoreEngine(
    settingsDao.getGeminiApiKey(), "http://" + ollamaIp,
    settingsDao.getSetting("ollama_model", "llama3.2:3b"))

onDestroyView:
  if sttManager != null: sttManager.destroy()
  MarcCoreUiHelper.cancelAll(ivOrb, tvMarcState)
  super.onDestroyView()
```

---

### PROMPT B8 — [MODIFY] MainActivity.java — Wire navigation + services

**Modifies:** `java/com/marc/helmet/activities/MainActivity.java`

```
CURSOR PROMPT:

Rewrite MainActivity.java for package com.marc.helmet.activities.
Complete final version. Java only. No Kotlin. Explicit imports.

Extends: AppCompatActivity

Implements service connection and fragment communication.

Fields:
NavController navController
BottomNavigationView bottomNav
TextView tvMarcLogo, tvSystemStatus, tvAiMode
MarcTTSManager ttsManager
WakeWordManager wakeWordManager
DatabaseHelper db
SettingsDao settingsDao
MarcForegroundService.MarcBinder marcBinder
ServiceConnection serviceConnection
boolean serviceConnected = false
boolean rideArmed = false

onCreate:
1. setContentView(R.layout.activity_main)
2. Create notification channel:
   NotificationChannel ch = new NotificationChannel("MARC_SERVICE_CHANNEL",
     "MARC System", NotificationManager.IMPORTANCE_LOW)
   ch.setLightColor(Color.parseColor("#FF2020"))
   ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch)
   (wrap in if Build.VERSION.SDK_INT >= 26)
3. db = DatabaseHelper.getInstance(this)
   settingsDao = new SettingsDao(db)
4. Bind views: tvMarcLogo, tvSystemStatus, tvAiMode
5. Setup NavController from R.id.nav_host_fragment (NavHostFragment.findNavController)
6. Setup BottomNavigationView:
   NavigationUI.setupWithNavController(bottomNav, navController)
   bottomNav.setItemIconTintList(ContextCompat.getColorStateList(this, R.color.nav_selector))
   bottomNav.setItemTextColor(ContextCompat.getColorStateList(this, R.color.nav_selector))
7. Init MarcTTSManager: on ready → initWakeWord()
8. Request permissions via PermissionUtils.requestAll(this, 1001)
9. Update tvAiMode text from settingsDao.isGeminiMode()
10. tvSystemStatus: "● STANDBY" color #555555

Method: void initWakeWord()
  String engine = settingsDao.getSetting("wake_word_engine", "porcupine")
  String porcKey = settingsDao.getSetting("porcupine_access_key", "")
  float sens = settingsDao.getPorcupineSensitivity()
  wakeWordManager = new WakeWordManager(this, new WakeWordManager.WakeWordListener() {
    public void onWakeWordDetected(): activateMarcFromWakeWord()
    public void onError(String e): Log.e("MARC", "WakeWord error: " + e)
  })
  boolean usePorcupine = engine.equals("porcupine") && !porcKey.isEmpty()
  wakeWordManager.initialize(usePorcupine, porcKey, "hey_marc_android.ppn", sens)
  wakeWordManager.startListening()

Method: void activateMarcFromWakeWord()
  runOnUiThread(() -> {
    NavDestination current = navController.getCurrentDestination()
    if current != null && current.getId() == R.id.marcFragment:
      MarcFragment mf = getCurrentMarcFragment()
      if mf != null: mf.activateVoice()
    else:
      navController.navigate(R.id.marcFragment)
      new Handler(Looper.getMainLooper()).postDelayed(() -> {
        MarcFragment mf = getCurrentMarcFragment()
        if mf != null: mf.activateVoice()
      }, 300)
  })

Method: MarcFragment getCurrentMarcFragment()
  NavHostFragment nhf = (NavHostFragment) getSupportFragmentManager()
    .findFragmentById(R.id.nav_host_fragment)
  if nhf == null: return null
  for (Fragment f : nhf.getChildFragmentManager().getFragments())
    if f instanceof MarcFragment: return (MarcFragment) f
  return null

Method: void armRide()
  rideArmed = true
  MarcForegroundService.startService(this)
  bindService(new Intent(this, MarcForegroundService.class), serviceConnection, BIND_AUTO_CREATE)
  updateSystemStatus("● ARMED", Color.parseColor("#00FF88"))

Method: void endRide()
  rideArmed = false
  MarcForegroundService.stopService(this)
  if serviceConnected: unbindService(serviceConnection); serviceConnected = false
  updateSystemStatus("● STANDBY", Color.parseColor("#555555"))

Method: void updateSystemStatus(String status, int color)
  runOnUiThread(() -> { tvSystemStatus.setText(status); tvSystemStatus.setTextColor(color) })

Method: void updateAiModeBadge(String mode, boolean isCoreMode)
  runOnUiThread(() -> {
    tvAiMode.setText(mode)
    tvAiMode.setTextColor(isCoreMode ?
      Color.parseColor("#FF2020") : Color.parseColor("#FF2020"))
  })

ServiceConnection:
  onServiceConnected: marcBinder = (MarcForegroundService.MarcBinder) binder; serviceConnected = true
    Pass calibration from CalibrationDao to service.
    Pass speed threshold from SettingsDao.
  onServiceDisconnected: serviceConnected = false

onRequestPermissionsResult:
  if all granted: initWakeWord() only if ttsManager ready
  else: Toast "MARC needs all permissions to protect you."

onDestroy:
  if wakeWordManager != null: wakeWordManager.destroy()
  if ttsManager != null: ttsManager.shutdown()
  if serviceConnected: unbindService(serviceConnection)
```

---

## PHASE C — ANDROID STUDIO STEPS

### C1 — First Time Setup in Android Studio

```
1. Open Android Studio
2. File → Open → navigate to C:\Users\ranjith\downloads\marc
3. Wait for Gradle sync (it will fail — that's expected)
4. Open app/build.gradle — verify dependencies are there (from original guide Part 0.2)
   If missing, add them now.
5. Click "Sync Now"
6. Create these new packages by right-clicking the helmet folder:
   New → Package → adapters
   New → Package → views
   New → Package → utils
   (activities, database, fragments, models, network, services, speech already exist)
```

### C2 — Paste Order in Android Studio

```
After generating each prompt in Cursor, paste in THIS ORDER:

[ ] A1 → Replace colors.xml, themes.xml, dimens.xml in res/values/
[ ] A2 → Replace all drawables in res/drawable/ (delete old amber ones first)
[ ] A3 → Replace nav_selector.xml, create strings.xml, paste AndroidManifest.xml
[ ] COMPILE ATTEMPT 1 — fix any res errors before Java files

[ ] B1 → Create adapters/ package, paste 3 adapter files
[ ] B2 → Create views/ package (LeanAngleView.java), paste MarcApplication.java in root helmet/ package
[ ] B3 → Create utils/ package, paste 4 utility files
[ ] B4 → Paste 5 item layout XMLs into res/layout/
[ ] COMPILE ATTEMPT 2 — fix import errors

[ ] B5 → Paste SplashActivity.java + CrashAlertActivity.java in activities/
       Paste activity_splash.xml + activity_crash_alert.xml in res/layout/
[ ] B6 → Paste MarcCoreEngine.java + RoastTemplateBank.java in network/ai/
[ ] B7 → Replace MarcFragment.java with complete version
[ ] B8 → Replace MainActivity.java with complete version
[ ] COMPILE ATTEMPT 3 — this should be the final compile
[ ] Fix all remaining errors
[ ] Run on USB debug
```

### C3 — Common Errors After Paste

```
"Cannot resolve symbol R.drawable.bg_orb_normal"
→ You forgot to create that drawable. Re-run Prompt A2, check all 22 files exist.

"Cannot resolve symbol MarcTTSManager"  
→ Import: import com.marc.helmet.speech.MarcTTSManager;

"Cannot resolve symbol ChatMessage"
→ It's an inner class of ChatMessageAdapter. Import or use full path.

"NavHostFragment cannot find controller"
→ nav_graph.xml must have startDestination set. Check nav_graph.xml has:
  app:startDestination="@id/dashboardFragment"

"MarcApplication not found"
→ AndroidManifest.xml must have android:name=".MarcApplication" in <application> tag.

"Foreground service type not set"
→ MarcForegroundService in manifest needs:
  android:foregroundServiceType="location|microphone"

"Permission FOREGROUND_SERVICE_MICROPHONE requires API 34"
→ Wrap in: if (Build.VERSION.SDK_INT >= 34) when requesting

"LeanAngleView BlurMaskFilter requires hardware acceleration"
→ Add to bike fragment or its parent: setLayerType(LAYER_TYPE_SOFTWARE, null)
  OR remove BlurMaskFilter and use regular paint for the demo

"Long press on orb not firing"
→ Check flOrbContainer has android:clickable="true" and android:longClickable="true" in XML
  OR set programmatically before setOnLongClickListener
```

---

## PHASE D — PICO FIRMWARE

> Flash these in Thonny. CircuitPython 9.x on both Pico Ws.
> Your wiring is already done. Just flash and verify IPs in serial monitor.

### D1 — Helmet Pico (main.py)

```python
# HELMET PICO W — MARC v1.0
import wifi, socketpool, json, board, digitalio, time

WIFI_SSID = "YOUR_SSID"
WIFI_PASSWORD = "YOUR_PASSWORD"

led = digitalio.DigitalInOut(board.LED)
led.direction = digitalio.Direction.OUTPUT

state = {"device_type":"MARC_HELMET","version":"1.0",
         "led_mode":"idle","speed_alert_active":False,"initialized":False}

def blink(n, d=0.15):
    for _ in range(n):
        led.value=True; time.sleep(d)
        led.value=False; time.sleep(d)

def handle(method, path, body):
    if path in ("/identify","/status"):
        return 200, json.dumps(state)
    if path=="/led" and method=="POST":
        d=json.loads(body) if body else {}
        state["led_mode"]=d.get("mode","idle")
        state["speed_alert_active"]=(state["led_mode"]=="alert")
        return 200,'{"ok":true}'
    if path=="/init_confirm":
        state["initialized"]=True; blink(3,0.1)
        return 200,'{"initialized":true}'
    return 404,'{"error":"not found"}'

blink(3,0.2)
wifi.radio.connect(WIFI_SSID, WIFI_PASSWORD)
print(f"HELMET IP: {wifi.radio.ipv4_address}")
state["initialized"]=True
blink(1,0.5)

pool=socketpool.SocketPool(wifi.radio)
srv=pool.socket(); srv.bind(("0.0.0.0",80)); srv.listen(1)

while True:
    if state["speed_alert_active"]:
        led.value=not led.value; time.sleep(0.25)
    else:
        led.value=False
    try:
        srv.settimeout(0.3)
        conn,addr=srv.accept()
        req=b""
        while True:
            chunk=conn.recv(512); req+=chunk
            if len(chunk)<512: break
        r=req.decode("utf-8","ignore")
        lines=r.split("\r\n")
        parts=lines[0].split(" ")
        method=parts[0] if len(parts)>0 else "GET"
        path=parts[1] if len(parts)>1 else "/"
        body=r.split("\r\n\r\n")[-1] if "\r\n\r\n" in r else ""
        status,rb=handle(method,path,body)
        resp=f"HTTP/1.1 {status} OK\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\n\r\n{rb}"
        conn.send(resp.encode()); conn.close()
    except OSError:
        pass
```

### D2 — Bike Pico (main.py)

```python
# BIKE PICO W — MARC v1.0 — MPU6050/9250 on I2C
import wifi, socketpool, json, board, busio, time, math

WIFI_SSID = "YOUR_SSID"
WIFI_PASSWORD = "YOUR_PASSWORD"
MPU_ADDR = 0x68  # or 0x69 if AD0 pin is HIGH

i2c = busio.I2C(board.GP1, board.GP0)  # SCL=GP1, SDA=GP0 — match your wiring

def i2c_write(addr, reg, val):
    while not i2c.try_lock(): pass
    i2c.writeto(addr, bytes([reg, val]))
    i2c.unlock()

def i2c_read(addr, reg, n):
    while not i2c.try_lock(): pass
    i2c.writeto(addr, bytes([reg]))
    buf=bytearray(n); i2c.readfrom_into(addr, buf)
    i2c.unlock()
    return buf

# Wake MPU
i2c_write(MPU_ADDR, 0x6B, 0x00)
time.sleep(0.1)

state = {"device_type":"MARC_BIKE","version":"1.0","roll":0.0,"pitch":0.0,
         "crash_flag":False,"standing_roll":0.0,"max_left_roll":-45.0,"max_right_roll":45.0}

comp_roll = 0.0
last_t = time.monotonic()

def read_mpu():
    global comp_roll, last_t
    data=i2c_read(MPU_ADDR, 0x3B, 14)
    def s16(h,l):
        v=(h<<8)|l; return v-65536 if v>32767 else v
    ax=s16(data[0],data[1])/16384.0
    ay=s16(data[2],data[3])/16384.0
    az=s16(data[4],data[5])/16384.0
    gx=s16(data[8],data[9])/131.0
    ar=math.atan2(ay, math.sqrt(ax*ax+az*az))*57.2958
    now=time.monotonic(); dt=now-last_t; last_t=now
    comp_roll=0.96*(comp_roll+gx*dt)+0.04*ar
    return round(comp_roll,2)

def check_crash(roll):
    ref=state["standing_roll"]
    thresh=max(abs(state["max_left_roll"]-ref), abs(state["max_right_roll"]-ref))
    if thresh>0 and abs(roll-ref)>thresh:
        state["crash_flag"]=True

def handle(method, path, body):
    if path in ("/identify","/status"):
        roll=read_mpu(); state["roll"]=roll; check_crash(roll)
        return 200, json.dumps(state)
    if path=="/calibrate" and method=="POST":
        d=json.loads(body) if body else {}
        state["standing_roll"]=d.get("standing",0.0)
        state["max_left_roll"]=d.get("max_left",-45.0)
        state["max_right_roll"]=d.get("max_right",45.0)
        state["crash_flag"]=False
        return 200,'{"ok":true}'
    if path=="/reset_crash" and method=="POST":
        state["crash_flag"]=False; return 200,'{"ok":true}'
    return 404,'{"error":"not found"}'

wifi.radio.connect(WIFI_SSID, WIFI_PASSWORD)
print(f"BIKE IP: {wifi.radio.ipv4_address}")

pool=socketpool.SocketPool(wifi.radio)
srv=pool.socket(); srv.bind(("0.0.0.0",80)); srv.listen(1)

while True:
    try:
        srv.settimeout(0.2)
        conn,addr=srv.accept()
        req=b""
        while True:
            chunk=conn.recv(512); req+=chunk
            if len(chunk)<512: break
        r=req.decode("utf-8","ignore")
        lines=r.split("\r\n")
        parts=lines[0].split(" ")
        method=parts[0] if len(parts)>0 else "GET"
        path=parts[1] if len(parts)>1 else "/"
        body=r.split("\r\n\r\n")[-1] if "\r\n\r\n" in r else ""
        status,rb=handle(method,path,body)
        resp=f"HTTP/1.1 {status} OK\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\n\r\n{rb}"
        conn.send(resp.encode()); conn.close()
    except OSError:
        roll=read_mpu(); state["roll"]=roll; check_crash(roll)
```

---

## FINAL FILE COUNT

```
ALREADY DONE (don't touch):
  22 Java files ✓
  6 layout XMLs ✓
  4 anim XMLs ✓
  1 color selector ✓
  3 values XMLs (will be replaced) ✓
  nav_graph.xml, bottom_nav_menu.xml ✓

GENERATING NOW (8 Cursor prompts):
  A1 → 3 replaced values XMLs
  A2 → 22 new drawables (replaces 10 amber ones)
  A3 → strings.xml + nav_selector replacement + AndroidManifest
  B1 → 3 adapter Java files
  B2 → LeanAngleView.java + MarcApplication.java
  B3 → 4 utility Java files
  B4 → 5 item layout XMLs
  B5 → 2 activity Java files + 2 activity layouts
  B6 → 2 Marc Core Java files
  B7 → MarcFragment.java (full rewrite)
  B8 → MainActivity.java (full rewrite)
  D1+D2 → 2 Pico firmware files (Thonny, not Android Studio)

TOTAL NEW/REPLACED: ~46 files across 11 Cursor prompts
TOTAL COMPILE CHECKPOINTS: 3
ESTIMATED TIME TO FIRST RUN: 2-3 focused days
```

---

```
MARC CONTINUATION GUIDE v1.0
==============================
Ranjith Kumar Dasari | DSCE EEE 2026
ranjithdsr2@gmail.com | github.com/RANJITH12022004

Named after Marc Marcuze 93 — Never Give Up.
"Built in the dark. Deployed in the field."
==============================
```
