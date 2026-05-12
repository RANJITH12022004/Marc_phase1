# MARC — BUILD GUIDE v1.0
### Motorcycle Accident Response Companion
### Cursor → Android Studio Pipeline

```
STATUS: ACTIVE BUILD
STACK:  Java + XML + SQLite + OkHttp3 + Gemini API + Google Cloud STT + Android TTS + Porcupine
THEME:  Iron Man / Jarvis — Black #0A0A0A | Amber #E8750A | Red #C0392B
NAMED:  Marc Marcuze 93 — Never Give Up
```

---

> **READ THIS BEFORE YOU TOUCH CURSOR**
>
> This guide gives you prompts in exact order. Each prompt = one file or one feature.
> Copy the prompt → paste into Cursor → copy the output → paste into Android Studio.
> Do NOT skip phases. Do NOT start Phase 12 before Phase 9 works.
> You've written 3 PRDs for things you haven't shipped yet. This one ships.

---

## PART 0 — ANDROID STUDIO SETUP (DO THIS FIRST, CURSOR LATER)

### 0.1 — Create the Project

```
1. Open Android Studio
2. New Project → Empty Views Activity
3. Name: MARC
4. Package: com.marc.helmet
5. Language: Java
6. Min SDK: API 26 (Android 8.0)
7. Finish
```

### 0.2 — Add Dependencies to build.gradle (Module: app)

Open `app/build.gradle` and add inside `dependencies {}`:

```groovy
// Networking
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// JSON
implementation 'com.google.code.gson:gson:2.10.1'

// Google Cloud STT
implementation 'com.google.cloud:google-cloud-speech:4.33.0'

// Porcupine Wake Word
implementation 'ai.picovoice:porcupine-android:3.0.1'

// Google Play Services Location (GPS)
implementation 'com.google.android.gms:play-services-location:21.2.0'

// Navigation Component
implementation 'androidx.navigation:navigation-fragment:2.7.7'
implementation 'androidx.navigation:navigation-ui:2.7.7'

// ViewModel + LiveData
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'

// CardView + RecyclerView
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'androidx.recyclerview:recyclerview:1.3.2'

// Material Components
implementation 'com.google.android.material:material:1.12.0'
```

Also add to `build.gradle` (Module: app) inside `android {}`:

```groovy
compileOptions {
    sourceCompatibility JavaVersion.VERSION_11
    targetCompatibility JavaVersion.VERSION_11
}
```

Click **Sync Now**.

### 0.3 — AndroidManifest.xml Permissions

Open `app/src/main/AndroidManifest.xml`. Add inside `<manifest>` tag before `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

Inside `<application>` tag add:

```xml
android:theme="@style/Theme.MARC"
```

### 0.4 — Folder Structure to Create in Android Studio

Right-click `java/com.marc.helmet` → New → Package. Create these packages:

```
com.marc.helmet
├── activities/
├── fragments/
│   ├── dashboard/
│   ├── marc/
│   ├── bike/
│   ├── profile/
│   └── settings/
├── adapters/
├── database/
├── models/
├── network/
│   ├── pico/
│   └── ai/
├── services/
├── speech/
├── utils/
└── views/
```

### 0.5 — res/ Folder Structure

```
res/
├── layout/
├── drawable/
├── values/
│   ├── colors.xml      ← create this
│   ├── strings.xml     ← already exists
│   ├── themes.xml      ← already exists, replace content
│   └── dimens.xml      ← create this
├── font/               ← create this folder
├── anim/               ← create this folder
└── navigation/         ← create this folder
```

---

## PART 1 — THEME & DESIGN SYSTEM

> **These files go in res/values/ and res/drawable/**
> Generate in Cursor, paste in Android Studio.

---

### PROMPT 1.1 — colors.xml

**Save as:** `res/values/colors.xml`

```
CURSOR PROMPT:

Write an Android colors.xml file for an app called MARC (Motorcycle Accident Response Companion).
Theme: Iron Man / Jarvis — dark mode only. Inspired by this web design:
- Background: #0A0A0A (near black)
- Surface/card: #1A1A1A
- Surface elevated: #242424
- Primary accent: #E8750A (amber/orange — Jarvis gold)
- Primary dark: #B85E08
- Primary light: #FFA040
- Error/alert: #C0392B (red)
- Error bright: #FF2020 (warning flash)
- Success: #27AE60
- Text primary: #DDDDDD
- Text secondary: #888888
- Text muted: #444444
- Divider: #2A2A2A
- Amber glow: #E8750A with 40% alpha for glow effects
- White: #FFFFFF
- Transparent: #00000000

Include color names: colorPrimary, colorPrimaryDark, colorPrimaryVariant, colorSecondary,
colorBackground, colorSurface, colorSurfaceVariant, colorError, colorSuccess,
colorOnPrimary, colorOnBackground, colorOnSurface, textPrimary, textSecondary, textMuted,
colorDivider, colorAmber, colorRed, colorRedBright, colorCardBackground, colorNavBackground.
```

---

### PROMPT 1.2 — themes.xml

**Save as:** `res/values/themes.xml`

```
CURSOR PROMPT:

Write an Android themes.xml for app MARC. Requirements:
- Base theme: Theme.MaterialComponents.DayNight.NoActionBar
- Force dark mode only (no light mode variant needed)
- Override: windowBackground = #0A0A0A
- Override: colorPrimary = #E8750A
- Override: colorPrimaryDark = #B85E08
- Override: colorAccent = #E8750A
- Override: statusBarColor = #000000
- Override: navigationBarColor = #0A0A0A
- Override: colorControlHighlight = #E8750A with 20% alpha
- Override: colorControlNormal = #888888
- Override: textColorPrimary = #DDDDDD
- Override: textColorSecondary = #888888
- Include a child theme called Theme.MARC.Splash for the splash screen with a black background
- Include a child theme called Theme.MARC.FullScreen with no status bar
App theme name: Theme.MARC
```

---

### PROMPT 1.3 — dimens.xml

**Save as:** `res/values/dimens.xml`

```
CURSOR PROMPT:

Write an Android dimens.xml for app MARC. Include:
- margin_tiny: 4dp
- margin_small: 8dp
- margin_medium: 16dp
- margin_large: 24dp
- margin_xlarge: 32dp
- padding_card: 16dp
- padding_screen: 20dp
- corner_radius_small: 4dp
- corner_radius_medium: 8dp
- corner_radius_large: 16dp
- corner_radius_card: 12dp
- text_size_micro: 10sp
- text_size_small: 12sp
- text_size_body: 14sp
- text_size_subtitle: 16sp
- text_size_title: 20sp
- text_size_heading: 24sp
- text_size_display: 32sp
- text_size_hero: 48sp
- icon_size_small: 16dp
- icon_size_medium: 24dp
- icon_size_large: 32dp
- nav_bar_height: 60dp
- card_elevation: 4dp
- status_bar_extra: 24dp
- bottom_nav_height: 64dp
```

---

### PROMPT 1.4 — Amber Glow Drawable

**Save as:** `res/drawable/bg_amber_glow.xml`

```
CURSOR PROMPT:

Write an Android XML drawable called bg_amber_glow.xml.
It should be a shape drawable: rounded rectangle with corner radius 12dp,
background color #1A1A1A, and a stroke of 1dp with color #E8750A.
Also write a second drawable bg_amber_glow_pressed.xml — same but stroke 2dp
and background #2A1500 (dark amber tint).
Give both drawables as separate XML files clearly labeled.
```

---

### PROMPT 1.5 — Card Drawables

**Save as:** `res/drawable/bg_card_dark.xml`, `res/drawable/bg_card_alert.xml`, `res/drawable/bg_card_success.xml`

```
CURSOR PROMPT:

Write 3 Android XML shape drawables:

1. bg_card_dark.xml — rounded rect, corners 12dp, fill #1A1A1A, stroke 1dp #2A2A2A
2. bg_card_alert.xml — rounded rect, corners 12dp, fill #1A0500, stroke 1dp #C0392B
3. bg_card_success.xml — rounded rect, corners 12dp, fill #001A08, stroke 1dp #27AE60
4. bg_status_dot_amber.xml — oval shape, fill #E8750A, no stroke
5. bg_status_dot_red.xml — oval shape, fill #C0392B, no stroke
6. bg_status_dot_gray.xml — oval shape, fill #444444, no stroke
7. bg_button_amber.xml — rounded rect corners 8dp, fill #E8750A
8. bg_button_outline_amber.xml — rounded rect corners 8dp, fill transparent, stroke 1dp #E8750A

Give all 8 drawables as clearly labeled XML.
```

---

### PROMPT 1.6 — Animations

**Save as:** `res/anim/fade_in.xml`, `res/anim/slide_up.xml`, `res/anim/pulse.xml`

```
CURSOR PROMPT:

Write 4 Android XML animation files:

1. fade_in.xml — alpha from 0.0 to 1.0, duration 300ms, interpolator: decelerateInterpolator
2. fade_out.xml — alpha from 1.0 to 0.0, duration 200ms
3. slide_up.xml — translateY from 60dp to 0, alpha 0 to 1, duration 350ms, decelerateInterpolator
4. slide_in_left.xml — translateX from -80dp to 0, alpha 0 to 1, duration 350ms

Give all 4 as labeled XML files.
```

---

## PART 2 — DATABASE LAYER

> **These files go in java/com/marc/helmet/database/**

---

### PROMPT 2.1 — DatabaseHelper.java

**Save as:** `database/DatabaseHelper.java`

```
CURSOR PROMPT:

Write a Java Android SQLiteOpenHelper class called DatabaseHelper for package com.marc.helmet.database.

Create these 7 tables:

TABLE user_profile:
- id INTEGER PRIMARY KEY AUTOINCREMENT
- name TEXT
- age INTEGER
- blood_type TEXT
- allergies TEXT
- medical_conditions TEXT
- medications TEXT
- emergency_notes TEXT
- profile_photo_path TEXT
- updated_at INTEGER (unix timestamp)

TABLE emergency_contacts:
- id INTEGER PRIMARY KEY AUTOINCREMENT
- priority INTEGER (1-4, 1 = call + sms, 2-4 = sms only)
- name TEXT NOT NULL
- phone TEXT NOT NULL
- relationship TEXT
- created_at INTEGER

TABLE devices:
- id INTEGER PRIMARY KEY AUTOINCREMENT
- device_type TEXT (MARC_HELMET or MARC_BIKE)
- ip_address TEXT NOT NULL
- port INTEGER DEFAULT 80
- firmware_version TEXT
- last_connected INTEGER
- is_connected INTEGER DEFAULT 0

TABLE calibration:
- id INTEGER PRIMARY KEY AUTOINCREMENT
- device_id INTEGER
- standing_roll REAL
- standing_pitch REAL
- max_left_roll REAL
- max_right_roll REAL
- calibrated_at INTEGER

TABLE settings:
- key TEXT PRIMARY KEY
- value TEXT

TABLE marc_conversations:
- id INTEGER PRIMARY KEY AUTOINCREMENT
- role TEXT (user or marc)
- message TEXT NOT NULL
- timestamp INTEGER
- session_id TEXT

TABLE ride_sessions:
- id INTEGER PRIMARY KEY AUTOINCREMENT
- start_time INTEGER
- end_time INTEGER
- max_speed_kmh REAL
- crash_detected INTEGER DEFAULT 0
- crash_time INTEGER
- notes TEXT

Requirements:
- DATABASE_NAME = "marc.db", VERSION = 1
- In onCreate() create all 7 tables
- In onUpgrade() drop all and recreate
- Add a static synchronized getInstance(Context) singleton method
- Include a method insertDefaultSettings() that inserts default key-value pairs:
  ai_engine=gemini, gemini_api_key=, ollama_ip=192.168.1.100:11434,
  ollama_model=llama3.2:3b-instruct-q4_K_M, wake_word_engine=porcupine,
  porcupine_access_key=, porcupine_sensitivity=0.5, speed_alert_threshold_kmh=80
- Call insertDefaultSettings() in onCreate() only if settings table is empty
```

---

### PROMPT 2.2 — Model Classes

**Save as:** `models/UserProfile.java`, `models/EmergencyContact.java`, `models/Device.java`, `models/Calibration.java`

```
CURSOR PROMPT:


```Write 4 Java model/POJO classes for package com.marc.helmet.models.
All classes need: full constructor, no-arg constructor, all getters and setters, toString().

1. UserProfile.java — fields: id(int), name(String), age(int), bloodType(String),
   allergies(String), medicalConditions(String), medications(String), emergencyNotes(String),
   profilePhotoPath(String), updatedAt(long)

2. EmergencyContact.java — fields: id(int), priority(int), name(String), phone(String),
   relationship(String), createdAt(long)
   Add method: boolean isPrimary() { return priority == 1; }
   Add method: String getPriorityLabel() { return priority == 1 ? "CALL + SMS" : "SMS ONLY"; }

3. Device.java — fields: id(int), deviceType(String), ipAddress(String), port(int),
   firmwareVersion(String), lastConnected(long), isConnected(boolean)
   Add constants: HELMET = "MARC_HELMET", BIKE = "MARC_BIKE"
   Add method: String getBaseUrl() { return "http://" + ipAddress + ":" + port; }
   Add method: boolean isHelmet(), isConnected()

4. Calibration.java — fields: id(int), deviceId(int), standingRoll(double), standingPitch(double),
   maxLeftRoll(double), maxRightRoll(double), calibratedAt(long)
   Add method: boolean isCrash(double currentRoll) that returns true if
   Math.abs(currentRoll - standingRoll) > Math.max(Math.abs(maxLeftRoll), Math.abs(maxRightRoll))
   Add method: double getCrashThreshold()
   Add method: boolean isCalibrated() returns true if calibratedAt > 0

---

### PROMPT 2.3 — DAO Classes

**Save as:** `database/UserProfileDao.java`, `database/EmergencyContactDao.java`, `database/DeviceDao.java`, `database/CalibrationDao.java`, `database/SettingsDao.java`

```
CURSOR PROMPT:

Write 5 Java DAO (Data Access Object) classes for package com.marc.helmet.database.
Each takes a DatabaseHelper instance in constructor. Use SQLiteDatabase from DatabaseHelper.getWritableDatabase().
Import all model classes from com.marc.helmet.models.

1. UserProfileDao.java:
   - UserProfile getProfile() — returns single row or null
   - long insertOrUpdateProfile(UserProfile profile) — upsert using id
   - void clearProfile()

2. EmergencyContactDao.java:
   - List<EmergencyContact> getAllContacts() — ordered by priority ASC
   - EmergencyContact getPrimaryContact() — priority = 1
   - long insertContact(EmergencyContact contact)
   - void updateContact(EmergencyContact contact)
   - void deleteContact(int id)
   - void reorderContacts(List<Integer> orderedIds) — update priority for each id in list order
   - int getContactCount()

3. DeviceDao.java:
   - List<Device> getAllDevices()
   - Device getHelmet() — deviceType = MARC_HELMET
   - Device getBike() — deviceType = MARC_BIKE
   - long insertOrUpdateDevice(Device device) — upsert by deviceType
   - void setConnected(String deviceType, boolean connected)
   - void updateIp(String deviceType, String ip)

4. CalibrationDao.java:
   - Calibration getCalibrationForDevice(int deviceId)
   - long insertOrUpdateCalibration(Calibration calibration)
   - boolean isCalibrated(int deviceId)

5. SettingsDao.java:
   - String getSetting(String key)
   - String getSetting(String key, String defaultValue)
   - void setSetting(String key, String value)
   - Map<String, String> getAllSettings()
   - boolean isGeminiMode() — returns ai_engine == "gemini"
   - String getOllamaIp()
   - String getGeminiApiKey()
   - float getPorcupineSensitivity()
   - float getSpeedThreshold()
```

---

## PART 3 — NETWORK LAYER

> **Files go in java/com/marc/helmet/network/**

---

### PROMPT 3.1 — PicoApiClient.java

**Save as:** `network/pico/PicoApiClient.java`

```
CURSOR PROMPT:

Write a Java class PicoApiClient for package com.marc.helmet.network.pico.
Uses OkHttp3 for all HTTP calls. All calls are async (use OkHttp Callback).

Constructor: PicoApiClient(String baseUrl)
— baseUrl example: "http://192.168.1.50:80"

Define these callback interfaces inside the class:
- interface PicoCallback<T> { void onSuccess(T result); void onError(String error); }
- class PicoStatus { String deviceType; double roll; double pitch; boolean crashFlag;
  boolean speedAlertActive; boolean initialized; String firmwareVersion; }

Implement these methods:

1. void identify(PicoCallback<PicoStatus> callback)
   — GET /identify, parse JSON response into PicoStatus

2. void getStatus(PicoCallback<PicoStatus> callback)
   — GET /status, parse full JSON into PicoStatus

3. void calibrate(double standingRoll, double standingPitch, double maxLeft, double maxRight,
   PicoCallback<Boolean> callback)
   — POST /calibrate with JSON body, return true on 200

4. void setLedAlert(boolean active, float thresholdKmh, PicoCallback<Boolean> callback)
   — POST /led with JSON { "threshold_kmh": x, "mode": "alert" or "clear" }

5. void confirmInit(PicoCallback<Boolean> callback)
   — GET /init_confirm

6. void resetCrashFlag(PicoCallback<Boolean> callback)
   — POST /reset_crash with empty body

7. void ping(PicoCallback<Long> callback)
   — GET /identify, measure latency in ms, return it

Use Gson for JSON parsing. Timeout: connectTimeout 3s, readTimeout 5s.
Handle all exceptions and return error string via onError.
All network calls must be executed on OkHttp background threads.
Responses must be delivered on the main thread using Handler(Looper.getMainLooper()).post().
```

---

### PROMPT 3.2 — DeviceScanner.java

**Save as:** `network/pico/DeviceScanner.java`

```
CURSOR PROMPT:

Write a Java class DeviceScanner for package com.marc.helmet.network.pico.
Purpose: scan local network subnet for MARC Pico W devices.

Constructor: DeviceScanner(Context context)

interface ScanCallback {
    void onDeviceFound(String ip, String deviceType, String firmwareVersion);
    void onScanComplete(int devicesFound);
    void onScanProgress(int current, int total);
}

Method: void startScan(ScanCallback callback)
Logic:
1. Get device's current IP from WifiManager
2. Extract subnet (e.g. 192.168.1.x)
3. Scan IPs .1 to .254 in parallel using ExecutorService (32 threads)
4. For each IP, try GET http://{ip}/identify with 500ms connect timeout, 1s read timeout
5. If response is 200 and contains MARC_HELMET or MARC_BIKE in JSON, call onDeviceFound
6. Track progress and call onScanProgress every 10 IPs
7. After all done, call onScanComplete
8. Skip device's own IP

Method: void stopScan() — shuts down executor

Handle all exceptions silently (most IPs won't respond).
Use OkHttp3 for requests. Parse response JSON with Gson.
```

---

### PROMPT 3.3 — GeminiApiClient.java

**Save as:** `network/ai/GeminiApiClient.java`

```
CURSOR PROMPT:

Write a Java class GeminiApiClient for package com.marc.helmet.network.ai.
Uses OkHttp3 for HTTP. Calls Gemini 1.5 Flash REST API.

Constants:
- BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent"
- MAX_TOKENS = 512
- TEMPERATURE = 0.3f

MARC_SYSTEM_PROMPT constant (multiline String):
"You are MARC (Motorcycle Accident Response Companion), an AI embedded in a smart helmet.
Your ONLY domains are: (1) First aid for motorcycle accident injuries — bleeding control, fracture
stabilization, shock, CPR, burns, head trauma. (2) Motorcycle troubleshooting and roadside repairs
— won't start, puncture, chain, electrical, overheating.
Rules: Keep responses under 20 seconds when spoken aloud. Use simple direct language — assume user
may be injured or stressed. Never recommend riding with serious injuries. Always end critical first
aid steps with: Call emergency services if condition worsens.
If asked anything outside these domains, respond: I'm MARC — I handle injuries and bike repairs. What's wrong?"

Interface MarcResponseCallback { void onResponse(String response); void onError(String error); }

Method: void sendMessage(String apiKey, List<ChatMessage> history, String userMessage, MarcResponseCallback callback)

ChatMessage inner class: String role (user/model), String content

Build request JSON:
{
  "system_instruction": { "parts": [{ "text": MARC_SYSTEM_PROMPT }] },
  "contents": [ ...history as { "role": "user"/"model", "parts": [{"text": content}] } ],
  "generationConfig": { "maxOutputTokens": 512, "temperature": 0.3 }
}

Parse response: extract candidates[0].content.parts[0].text

Timeout: 15s read. Deliver response on main thread.
Include method: boolean isConfigured(String apiKey) — returns apiKey != null && !apiKey.isEmpty()
```

---

### PROMPT 3.4 — OllamaApiClient.java

**Save as:** `network/ai/OllamaApiClient.java`

```
CURSOR PROMPT:

Write a Java class OllamaApiClient for package com.marc.helmet.network.ai.
Uses OkHttp3. Calls local Ollama API.

Constructor: OllamaApiClient(String ollamaBaseUrl)
— ollamaBaseUrl example: "http://192.168.1.100:11434"

Same MARC_SYSTEM_PROMPT string constant as GeminiApiClient.

Interface MarcResponseCallback { void onResponse(String response); void onError(String error); }

Method: void sendMessage(String model, List<ChatMessage> history, String userMessage, MarcResponseCallback callback)
— POST to {baseUrl}/api/chat
— Body: { "model": model, "messages": [ {role, content} ... ], "stream": false }
— Prepend system message: { "role": "system", "content": MARC_SYSTEM_PROMPT }
— Parse response: message.content field
— Timeout: 30s (Ollama is slow)
— Deliver on main thread

Method: void testConnection(PicoApiClient.PicoCallback<List<String>> callback)
— GET {baseUrl}/api/tags
— Parse models array, return list of model names

Method: boolean isConfigured(String baseUrl)
— returns baseUrl != null && !baseUrl.isEmpty() && baseUrl.contains(":")
```

---

## PART 4 — SERVICES

> **Files go in java/com/marc/helmet/services/**

---

### PROMPT 4.1 — MarcForegroundService.java

**Save as:** `services/MarcForegroundService.java`

```
CURSOR PROMPT:

Write a Java Android ForegroundService called MarcForegroundService
for package com.marc.helmet.services.

This service:
1. Runs in foreground with a persistent notification (channel: "MARC_SYSTEM", amber color)
2. Notification: "MARC is Armed", "Monitoring your ride. Stay safe.", amber icon
3. Exposes a LocalBroadcastManager system with these intents:
   - ACTION_CRASH_DETECTED = "com.marc.helmet.CRASH_DETECTED"
   - ACTION_SPEED_UPDATE = "com.marc.helmet.SPEED_UPDATE" (extra: float speed_kmh)
   - ACTION_LOCATION_UPDATE = "com.marc.helmet.LOCATION_UPDATE" (extras: double lat, double lng)
   - ACTION_DEVICE_STATUS = "com.marc.helmet.DEVICE_STATUS" (extras: String device_type, boolean connected)

4. Uses a Handler to poll Bike Pico /status every 200ms when bikeClient is set
   - Reads roll angle, checks against calibration thresholds (from CalibrationDao)
   - If crash detected: sends ACTION_CRASH_DETECTED broadcast, stops polling temporarily
   - Stores current calibration in memory (reload from DB on start)

5. Manages GPS via FusedLocationProviderClient, updates every 1 second
   - Computes speed from GPS speed field (m/s → km/h)
   - Broadcasts ACTION_SPEED_UPDATE and ACTION_LOCATION_UPDATE
   - If speed > threshold from settings: sends LED alert command to helmet pico client

6. Static methods: startService(Context), stopService(Context)
7. Binder for activity binding: class MarcBinder extends Binder
8. Methods: setHelmetClient(PicoApiClient), setBikeClient(PicoApiClient),
   setCalibration(Calibration), setSpeedThreshold(float)
9. boolean isRunning() static method using a static flag

Import: com.marc.helmet.network.pico.PicoApiClient, com.marc.helmet.database.*,
com.marc.helmet.models.*
Use CHANNEL_ID = "MARC_SERVICE_CHANNEL"
```

---

### PROMPT 4.2 — EmergencyService.java

**Save as:** `services/EmergencyService.java`

```
CURSOR PROMPT:

Write a Java class EmergencyService for package com.marc.helmet.services.
This is NOT a Service — it's a manager class. Constructor: EmergencyService(Context context).

Inject: EmergencyContactDao contactDao, UserProfileDao profileDao.

Constants:
- COUNTDOWN_SECONDS = 10
- ACTION_EMERGENCY_STARTED = "com.marc.helmet.EMERGENCY_STARTED"
- ACTION_EMERGENCY_CANCELLED = "com.marc.helmet.EMERGENCY_CANCELLED"
- ACTION_EMERGENCY_COMPLETED = "com.marc.helmet.EMERGENCY_COMPLETED"
- ACTION_COUNTDOWN_TICK = "com.marc.helmet.COUNTDOWN_TICK" (extra: int seconds_remaining)

Interface EmergencyListener {
    void onCountdownTick(int secondsRemaining);
    void onEmergencyStarted();
    void onEmergencyCancelled();
    void onEmergencyCompleted();
}

Method: void triggerEmergency(double lat, double lng, EmergencyListener listener)
1. Set isEmergencyActive = true
2. Notify listener.onEmergencyStarted()
3. Start 10-second countdown using Handler, tick every 1 second
4. On each tick: listener.onCountdownTick(remaining)
5. At 0: call executeEmergency(lat, lng), listener.onEmergencyCompleted()

Method: void cancelEmergency()
- Sets isCancelled = true, stops countdown Handler
- Listener.onEmergencyCancelled()

Method: private void executeEmergency(double lat, double lng)
- Get primary contact (priority = 1) from contactDao
- Place phone call: Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone))
- Get all contacts from contactDao
- For each contact: send SMS via SmsManager
- SMS content: buildEmergencySms(lat, lng)

Method: private String buildEmergencySms(double lat, double lng)
- Get UserProfile from profileDao
- Return: "[MARC EMERGENCY ALERT]\n\n{name} has been in a motorcycle accident.\n\n" +
  "Location: {lat}° N, {lng}° E\n" +
  "Time: {formatted timestamp}\n\n" +
  "Medical Info:\nBlood Type: {bloodType}\nAllergies: {allergies}\n" +
  "Conditions: {medicalConditions}\nMedications: {medications}\n" +
  "Notes: {emergencyNotes}\n\nPlease call emergency services immediately. — MARC System"

boolean isEmergencyActive()
boolean isCountingDown()
```

---

## PART 5 — SPEECH SYSTEM

> **Files go in java/com/marc/helmet/speech/**

---

### PROMPT 5.1 — MarcTTSManager.java

**Save as:** `speech/MarcTTSManager.java`

```
CURSOR PROMPT:

Write a Java class MarcTTSManager for package com.marc.helmet.speech.
Wraps Android TextToSpeech engine.

Constructor: MarcTTSManager(Context context, OnReadyListener listener)
Interface OnReadyListener { void onReady(); void onError(String msg); }

Initialize TextToSpeech with Locale("en", "IN").
Set pitch = 0.9f, speech rate = 0.9f.
On init: if SUCCESS → listener.onReady() else listener.onError()

LOADING_PHRASES string array (pick randomly when called):
- "MARC is thinking..."
- "Analyzing, hold on..."
- "Checking database..."
- "Running parallel search..."
- "Cross-referencing medical data..."
- "Scanning bike diagnostics..."
- "Processing request..."

Method: void speak(String text) — speaks given text, interrupts current speech
Method: void speakEmergency(String text) — sets stream to STREAM_ALARM, max volume, speaks text
Method: void speakLoadingPhrase() — picks random phrase from LOADING_PHRASES array, speaks it
Method: void speakOllamaLoading() — speaks "MARC BACK is thinking locally..."
Method: void countDown(int seconds) — speaks "{seconds} seconds..."
Method: void stop() — stops current TTS
Method: void shutdown() — releases TTS engine
Method: boolean isReady()

Route audio to Bluetooth headset if connected, fallback to phone speaker.
Check BluetoothAdapter for connected headset profile.
```

---

### PROMPT 5.2 — GoogleSTTManager.java

**Save as:** `speech/GoogleSTTManager.java`

```
CURSOR PROMPT:

Write a Java class GoogleSTTManager for package com.marc.helmet.speech.
Uses Android's built-in SpeechRecognizer (free, no Cloud API needed for STT fallback).
Note: This wraps SpeechRecognizer for on-device STT, NOT Cloud STT.
(Cloud STT integration is a v2 upgrade — for demo, Android's built-in is sufficient.)

Constructor: GoogleSTTManager(Context context)

Interface STTCallback {
    void onResult(String text);
    void onPartialResult(String partial);
    void onError(String error);
    void onListeningStarted();
    void onListeningStopped();
}

Method: void startListening(STTCallback callback)
- Creates SpeechRecognizer with RecognitionListener
- Uses EXTRA_LANGUAGE = "en-IN"
- Sets EXTRA_PARTIAL_RESULTS = true
- Max listen time 8 seconds (EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS = 1500)
- On final result: callback.onResult()
- On partial: callback.onPartialResult()
- On error: callback.onError()

Method: void stopListening()
Method: void destroy()
Method: boolean isAvailable(Context context) — checks SpeechRecognizer.isRecognitionAvailable()
```

---

### PROMPT 5.3 — WakeWordManager.java

**Save as:** `speech/WakeWordManager.java`

```
CURSOR PROMPT:

Write a Java class WakeWordManager for package com.marc.helmet.speech.
Manages two modes of wake word detection: Porcupine and Always-On SpeechRecognizer.

Constructor: WakeWordManager(Context context, WakeWordListener listener)

Interface WakeWordListener {
    void onWakeWordDetected();
    void onError(String error);
}

Fields: boolean usePorcupine (default true), boolean isListening

Method: void initialize(boolean usePorcupine, String porcupineAccessKey,
  String ppnFilePath, float sensitivity)
- If usePorcupine: initialize Picovoice Porcupine
  - Use PorcupineManager.Builder with accessKey, keywordPath, sensitivity
  - On detection: listener.onWakeWordDetected()
  - On error: listener.onError()
- If !usePorcupine: set flag for always-on mode

Method: void startListening()
- If Porcupine mode: porcupineManager.start()
- If Always-On mode: start SpeechRecognizer continuous loop
  - On result: check if text.toLowerCase().contains("marc") or contains("hey marc")
  - If yes: listener.onWakeWordDetected()
  - Restart recognition immediately after each result (continuous loop)

Method: void stopListening()
Method: void destroy()
Method: boolean isListening()

For Porcupine: catch PorcupineException, call listener.onError()
For Always-On: handle all RecognitionListener errors gracefully, auto-restart on error
```

---

## PART 6 — MAIN ACTIVITY & NAVIGATION

---

### PROMPT 6.1 — activity_main.xml

**Save as:** `res/layout/activity_main.xml`

```
CURSOR PROMPT:

Write an Android XML layout file activity_main.xml for the MARC app.
Design: Iron Man / Jarvis dark theme. Background #0A0A0A.

Structure:
- Root: ConstraintLayout, background #0A0A0A, match_parent x match_parent
- Top: custom status bar area — LinearLayout horizontal, height 56dp, background #000000,
  padding 0 16dp, contains:
  - TextView id/tv_marc_logo: text "MARC", font style bold, textSize 20sp, color #E8750A,
    fontFamily monospace
  - View (spacer, weight 1)
  - TextView id/tv_system_status: text "● ARMED", textSize 11sp, color #27AE60
  - Space 8dp
  - TextView id/tv_ai_mode: text "MARC ONE", textSize 11sp, color #E8750A

- Middle: FragmentContainerView id/nav_host_fragment, match_parent, 0dp height with
  constraint top=status_bar_bottom, bottom=bottom_nav_top

- Bottom: com.google.android.material.bottomnavigation.BottomNavigationView
  id/bottom_nav, height 64dp, background #000000, constraint bottom=parent_bottom
  app:menu="@menu/bottom_nav_menu"
  app:itemIconTint="@color/nav_selector" (we'll define later)
  app:itemTextColor="@color/nav_selector"
  app:labelVisibilityMode="labeled"
```

---

### PROMPT 6.2 — bottom_nav_menu.xml

**Save as:** `res/menu/bottom_nav_menu.xml`

```
CURSOR PROMPT:

Write an Android menu XML file bottom_nav_menu.xml for the MARC bottom navigation.
5 items:
1. id/nav_dashboard, title "SYSTEM", icon: use @android:drawable/ic_menu_compass (placeholder)
2. id/nav_marc, title "MARC", icon: use @android:drawable/ic_btn_speak_now (placeholder)
3. id/nav_bike, title "BIKE", icon: use @android:drawable/ic_menu_directions (placeholder)
4. id/nav_profile, title "PROFILE", icon: use @android:drawable/ic_menu_myplaces (placeholder)
5. id/nav_settings, title "SETTINGS", icon: use @android:drawable/ic_menu_manage (placeholder)

Note in comments: Replace icon references with custom vector drawables for production.
```

---

### PROMPT 6.3 — nav_graph.xml

**Save as:** `res/navigation/nav_graph.xml`

```
CURSOR PROMPT:

Write an Android Navigation Component nav_graph.xml for the MARC app.
startDestination: DashboardFragment

Fragments:
1. DashboardFragment — id/dashboardFragment, class com.marc.helmet.fragments.dashboard.DashboardFragment, layout fragment_dashboard
2. MarcFragment — id/marcFragment, class com.marc.helmet.fragments.marc.MarcFragment, layout fragment_marc
3. BikeFragment — id/bikeFragment, class com.marc.helmet.fragments.bike.BikeFragment, layout fragment_bike
4. ProfileFragment — id/profileFragment, class com.marc.helmet.fragments.profile.ProfileFragment, layout fragment_profile
5. SettingsFragment — id/settingsFragment, class com.marc.helmet.fragments.settings.SettingsFragment, layout fragment_settings

No actions needed between fragments (bottom nav handles it directly).
```

---

### PROMPT 6.4 — MainActivity.java

**Save as:** `activities/MainActivity.java`

```
CURSOR PROMPT:

Write a Java MainActivity for package com.marc.helmet.activities.
Extends AppCompatActivity. Uses Android Navigation Component.

Fields:
- NavController navController
- MarcForegroundService.MarcBinder marcBinder
- MarcTTSManager ttsManager
- WakeWordManager wakeWordManager
- DatabaseHelper db
- SettingsDao settingsDao
- TextView tvSystemStatus, tvAiMode

OnCreate:
1. setContentView(R.layout.activity_main)
2. Setup NavController from R.id.nav_host_fragment
3. Setup BottomNavigationView with NavigationUI.setupWithNavController()
4. Init DatabaseHelper singleton
5. Init MarcTTSManager — on ready: initWakeWord()
6. Show/hide bottom nav labels based on selected tab
7. Request permissions: RECORD_AUDIO, ACCESS_FINE_LOCATION, CALL_PHONE, SEND_SMS
   — use ActivityCompat.requestPermissions, handle onRequestPermissionsResult
8. Init tvSystemStatus, tvAiMode from layout
9. Update tvAiMode text based on settingsDao.isGeminiMode() → "MARC ONE" or "MARC BACK"

Method: void initWakeWord()
- Get wake word settings from SettingsDao
- Initialize WakeWordManager
- On wake word detected: activateMarcListening()

Method: void activateMarcListening()
- If current fragment is MarcFragment: call its activateVoice() method
- Else: navigate to MarcFragment then activate

Method: void updateSystemStatus(String status, int color)
- Update tvSystemStatus text and color on main thread

Method: void armRide() — starts MarcForegroundService, updates status to "● ARMED" green
Method: void endRide() — stops MarcForegroundService, updates status to "● STANDBY" amber

Bind to MarcForegroundService using ServiceConnection.
Handle onDestroy: shutdown ttsManager, wakeWordManager.
```

---

## PART 7 — DASHBOARD FRAGMENT

---

### PROMPT 7.1 — fragment_dashboard.xml

**Save as:** `res/layout/fragment_dashboard.xml`

```
CURSOR PROMPT:

Write an Android XML layout fragment_dashboard.xml for the MARC dashboard screen.
Theme: Iron Man dark. Background #0A0A0A. ScrollView root.

Inside ScrollView → ConstraintLayout or LinearLayout vertical:

SECTION 1 — Header row:
- TextView "SYSTEM STATUS", monospace, 10sp, color #444444, letter-spacing 0.2
- TextView id/tv_ride_timer "00:00:00", 28sp bold, color #E8750A (ride duration)

SECTION 2 — ARM/END buttons row:
- Button id/btn_arm_ride: text "▶ ARM RIDE", background @drawable/bg_button_amber,
  textColor #000000, bold, 14sp, corner radius 8dp, height 52dp, weight 1
- Space 12dp
- Button id/btn_end_ride: text "■ END RIDE", background @drawable/bg_button_outline_amber,
  textColor #E8750A, bold, 14sp, height 52dp, weight 1

SECTION 3 — Connection status cards (horizontal GridLayout or LinearLayout):
- Card id/card_helmet: title "HELMET UNIT", subtitle id/tv_helmet_status "DISCONNECTED",
  dot id/dot_helmet (8dp circle drawable), ip id/tv_helmet_ip ""
  background @drawable/bg_card_dark, corners 12dp, padding 16dp
- Card id/card_bike: same structure for bike unit id/tv_bike_status, id/dot_bike, id/tv_bike_ip

SECTION 4 — AI status card:
- Card background @drawable/bg_card_dark, padding 16dp
- Row: "AI ENGINE" label + TextView id/tv_ai_engine_status "MARC ONE — ONLINE" color #27AE60
- Row: "WAKE WORD" label + TextView id/tv_wake_word_status "● LISTENING" color #E8750A

SECTION 5 — Live telemetry row:
- Small card: "SPEED" + TextView id/tv_speed "— km/h", textSize 24sp, color #E8750A
- Small card: "BT HEADSET" + TextView id/tv_bt_status "DISCONNECTED"

SECTION 6 — GPS coordinates mini card:
- "LAST LOCATION" label
- TextView id/tv_dash_coordinates "Acquiring GPS..." color #888888, monospace, 12sp

Style all labels in monospace/Courier, 10sp, color #444444, UPPERCASE letter-spacing.
All values in 14sp-24sp, color #DDDDDD or #E8750A.
Use 12dp margins between cards. Background of each card: #1A1A1A.
```

---

### PROMPT 7.2 — DashboardFragment.java

**Save as:** `fragments/dashboard/DashboardFragment.java`

```
CURSOR PROMPT:

Write a Java DashboardFragment for package com.marc.helmet.fragments.dashboard.
Extends Fragment. Layout: R.layout.fragment_dashboard.

Fields:
- All views from layout (tvRideTimer, btnArmRide, btnEndRide, tvHelmetStatus, tvBikeStatus,
  tvAiEngineStatus, tvSpeed, tvBtStatus, tvDashCoordinates, dotHelmet, dotBike, tvHelmetIp,
  tvBikeIp, tvWakeWordStatus)
- Handler timerHandler, long rideStartTime, boolean isRiding
- BroadcastReceiver for MarcForegroundService broadcasts
- SettingsDao settingsDao
- DeviceDao deviceDao

OnViewCreated:
1. Bind all views
2. Init SettingsDao, DeviceDao from DatabaseHelper
3. Setup ARM/END buttons:
   - btn_arm_ride → calls parent MainActivity.armRide(), starts timer
   - btn_end_ride → calls parent MainActivity.endRide(), stops timer
4. Register BroadcastReceiver for:
   - ACTION_SPEED_UPDATE → update tvSpeed with speed_kmh value
   - ACTION_LOCATION_UPDATE → update tvDashCoordinates with lat/lng formatted to 6dp
   - ACTION_DEVICE_STATUS → update device cards
   - ACTION_CRASH_DETECTED → flash screen red (animate background to #1A0000)
5. Refresh device status cards from DeviceDao on resume
6. Update AI engine status text from SettingsDao
7. Check Bluetooth headset status via BluetoothAdapter

Timer: starts on ARM, ticks every second, formats as HH:MM:SS on tvRideTimer.
updateDeviceCard(Device device, TextView statusTv, View dot, TextView ipTv):
- If connected: status = "CONNECTED", dot background = bg_status_dot_amber, ip = device.ip
- If disconnected: status = "DISCONNECTED", dot = gray

Unregister receiver in onDestroyView.
```

---

## PART 8 — MARC FRAGMENT (AI Interface)

---

### PROMPT 8.1 — fragment_marc.xml

**Save as:** `res/layout/fragment_marc.xml`

```
CURSOR PROMPT:

Write an Android XML layout fragment_marc.xml for the MARC AI screen.
Theme: Iron Man/Jarvis. Pure black background. Two modes: VOICE and TEXT.

Root: ConstraintLayout, background #000000, match_parent

TOP BAR:
- TextView "MARC", monospace, 12sp, color #444444, letter-spacing 0.3, top-left
- ToggleButton id/toggle_mode: text "VOICE" / "TEXT", background transparent,
  textColor #E8750A, monospace, 11sp, top-right

VOICE MODE layout (id/layout_voice_mode, VISIBLE by default):
- Center of screen: FrameLayout id/fl_orb_container, 200dp x 200dp, centered
  - ImageView id/iv_orb: 200dp x 200dp, background = circle drawable,
    background color #1A0800 with amber border — this will be animated in code
  - TextView id/tv_orb_icon: text "◎", 64sp, color #E8750A, centered inside orb
- Below orb: TextView id/tv_marc_state "SAY HEY MARC", 10sp, monospace, color #444444,
  letter-spacing 0.3, centered
- Below state: TextView id/tv_listening_text "", 14sp, color #888888, centered, italic
  (shows partial STT results)
- Bottom strip: TextView id/tv_loading_phrase "", 11sp, color #E8750A, centered, monospace
  (shows Jarvis loading phrases)
- Bottom button: Button id/btn_tap_to_speak "◉ TAP TO SPEAK", outline amber, 44dp height

TEXT MODE layout (id/layout_text_mode, GONE by default):
- RecyclerView id/rv_chat, fill height, background #000000
- Bottom input row:
  - EditText id/et_message, background @drawable/bg_amber_glow, hint "Ask MARC...",
    hintColor #444444, textColor #DDDDDD, monospace, 14sp, corner 8dp
  - ImageButton id/btn_send: amber tint, send icon, 48dp x 48dp

Both layouts use constraint top/bottom/left/right to fill the screen area between top bar and bottom.
```

---

### PROMPT 8.2 — ChatMessageAdapter.java

**Save as:** `adapters/ChatMessageAdapter.java`

```
CURSOR PROMPT:

Write a Java RecyclerView.Adapter class ChatMessageAdapter for package com.marc.helmet.adapters.
Displays chat messages between user and MARC.

Inner class ChatMessage: String role (user/marc), String content, long timestamp

Two ViewTypes: TYPE_USER = 0, TYPE_MARC = 1

For TYPE_USER (right-aligned bubble):
- Background: rounded rect right corners, fill #E8750A, no left corners rounded
- Text: #000000, 14sp, padding 12dp 16dp
- Align to right (gravity end, marginStart 60dp)

For TYPE_MARC (left-aligned bubble):
- Background: rounded rect left corners, fill #1A1A1A, stroke #2A2A2A
- Text: #DDDDDD, 14sp, padding 12dp 16dp
- Small "MARC" label above bubble: 10sp, color #E8750A, monospace
- Align to left (gravity start, marginEnd 60dp)

Create layout files: item_message_user.xml and item_message_marc.xml as part of your answer.

Method: void addMessage(ChatMessage msg) — adds and scrolls to bottom
Method: void clearMessages()
Method: List<ChatMessage> getHistory() — returns list for API context
```

---

### PROMPT 8.3 — MarcFragment.java

**Save as:** `fragments/marc/MarcFragment.java`

```
CURSOR PROMPT:

Write a Java MarcFragment for package com.marc.helmet.fragments.marc.
Extends Fragment. Layout: R.layout.fragment_marc.

Fields:
- All views from layout
- MarcTTSManager ttsManager (get from MainActivity)
- GoogleSTTManager sttManager
- GeminiApiClient geminiClient
- OllamaApiClient ollamaClient
- ChatMessageAdapter adapter
- SettingsDao settingsDao
- boolean isVoiceMode = true
- boolean isMarcListening = false
- List<ChatMessage> conversationHistory (last 6 messages)
- Handler orbAnimHandler

LOADING_PHRASES string array (same as MarcTTSManager)

OnViewCreated:
1. Bind all views
2. Init SettingsDao, AI clients
3. Setup RecyclerView with ChatMessageAdapter (LinearLayoutManager, stack from end)
4. toggle_mode listener: switch between voice/text layouts with animation (fade_in/fade_out)
5. btn_tap_to_speak listener: startListening()
6. btn_send listener: sendTextMessage(et_message.getText())
7. Set initial orb state (idle pulsing animation)

Method: void activateVoice() — called from MainActivity on wake word
- Set tvMarcState to "LISTENING..."
- Start orb expand animation
- Call sttManager.startListening(sttCallback)

STT Callback:
- onListeningStarted: animate orb
- onPartialResult: update tvListeningText
- onResult: stop orb animation, tvMarcState = "PROCESSING...", tvListeningText = full text,
  call sendToMarc(text)
- onError: reset orb to idle

Method: void sendToMarc(String userMessage)
1. Add user message to RecyclerView and conversationHistory
2. Start orb processing animation (rotate, amber glow)
3. TTS: speakLoadingPhrase() (random Jarvis phrase)
4. tvLoadingPhrase: show one of the loading phrases (rotate text every 1.5s)
5. Call Gemini or Ollama based on SettingsDao.isGeminiMode()
6. On response: stop animations, addMarcMessage(response), TTS speak response
7. On error: TTS "MARC encountered an error. Try again.", show error state

Method: void sendTextMessage(String text) — same as sendToMarc but no TTS for input
Method: void addMarcMessage(String text) — add to adapter + history + DB
Method: void resetOrbToIdle()
Method: void setOrbListening() — pulse animation
Method: void setOrbProcessing() — rotation animation

Orb animations: use ObjectAnimator for scale and alpha. 
Idle: alpha 0.6↔1.0, scale 0.95↔1.05, duration 2000ms, repeat infinite.
Listening: scale 1.0↔1.15, duration 500ms, repeat.
Processing: rotation 0→360, duration 2000ms, repeat.
```

---

## PART 9 — BIKE FRAGMENT

---

### PROMPT 9.1 — fragment_bike.xml

**Save as:** `res/layout/fragment_bike.xml`

```
CURSOR PROMPT:

Write an Android XML layout fragment_bike.xml. Theme: Iron Man HUD.

Root: ConstraintLayout, background #000000

TOP TABS (TabLayout):
- 3 tabs: LEAN ANGLE | COORDINATES | SPEED
- Background #000000, tabIndicatorColor #E8750A, tabSelectedTextColor #E8750A,
  tabTextColor #444444, tabIndicatorHeight 2dp

CONTENT (ViewPager2 id/vp_bike, fill remaining space below tabs)

Each tab page is a separate fragment/layout.

Also write 3 child layout files:

--- layout_bike_lean.xml ---
- SurfaceView id/sv_lean_3d filling most of the screen (the 3D bike view — will render in code)
- Over the SurfaceView (overlay, bottom area):
  - Large TextView id/tv_current_angle "0.0°", 48sp bold, color #E8750A, monospace, centered
  - Small row: TextView "LEFT MAX" color #C0392B, small | TextView id/tv_max_left "-45.0°" |
    Space | TextView "RIGHT MAX" color #C0392B | TextView id/tv_max_right "+45.0°"
  - Status chip id/chip_crash_status "● SAFE", 12sp, color #27AE60
  - Button id/btn_recalibrate "RECALIBRATE", small outline amber button, bottom right

--- layout_bike_coordinates.xml ---
- Label "LATITUDE", 10sp, monospace, #444444
- TextView id/tv_latitude "Acquiring...", 28sp, monospace, bold, #E8750A
- Label "LONGITUDE"
- TextView id/tv_longitude "Acquiring...", 28sp, monospace, bold, #E8750A
- Divider
- Row: "ALTITUDE" + TextView id/tv_altitude "—m"
- Row: "ACCURACY" + TextView id/tv_accuracy "—m"
- Row: "GPS STATUS" + TextView id/tv_gps_status "● ACQUIRING" color #E8750A
- Button id/btn_copy_coords "COPY COORDINATES", small outline amber, bottom

--- layout_bike_speed.xml ---
- Large circular display area (FrameLayout 280dp x 280dp, centered)
  - TextView id/tv_speed_large "0", 96sp, bold, monospace, color #E8750A
  - TextView "km/h", 16sp, color #888888, below the large number
- Threshold row: "ALERT AT:" + SeekBar id/sb_speed_threshold (0-200 km/h) + TextView id/tv_threshold_val "80 km/h"
- Alert status: TextView id/tv_speed_alert_status "LED ALERT: OFF", 12sp, monospace
- Speed history label "LAST 60 SECONDS"
- LinearLayout id/ll_speed_graph (placeholder — we fill with custom view in code)
```

---

### PROMPT 9.2 — LeanAngleView.java (3D Iron Man bike visualization)

**Save as:** `views/LeanAngleView.java`

```
CURSOR PROMPT:

Write a Java custom View class LeanAngleView extending View for package com.marc.helmet.views.
This draws a 2D side-silhouette of a motorcycle that tilts based on lean angle. Iron Man aesthetic.
(Full 3D OpenGL is v2 — this is a compelling 2D canvas implementation for demo.)

Canvas drawing requirements:
- Background: #000000
- Draw a HUD grid: faint amber (#E8750A at 10% alpha) horizontal and vertical lines, 30dp apart
- Draw scanline effect: horizontal lines 2px tall, #000000 at 20% alpha, every 4px
- Draw a simplified motorcycle silhouette:
  - Two circles (wheels): radius 40dp, color #333333 stroke 3dp, amber fill 20% alpha
  - Body: rounded rectangle connecting the wheels, #222222 fill, #E8750A stroke 2dp
  - Rider silhouette: simple rounded shapes above body, #333333
  - Exhaust: small rectangle rear lower, #444444
- The ENTIRE drawing rotates around the center point by currentAngle degrees
  (use canvas.rotate(currentAngle, centerX, centerY))
- Draw threshold lines at maxLeftAngle and maxRightAngle:
  - Color: #C0392B (red), dashed effect (PathEffect), 2dp width
  - These lines do NOT rotate — they are fixed reference lines
- Draw center line at 0°: #27AE60 (green), 1dp, fixed
- In crash zone (within 10% of threshold): flash amber glow on bike body

Method: void setLeanAngle(float angle) — invalidates view
Method: void setCalibration(float standingAngle, float maxLeft, float maxRight)
Method: float getCurrentAngle()

Constructor accepting Context only, and Context + AttributeSet.
Use ValueAnimator for smooth angle transitions (50ms duration per update).
```

---

### PROMPT 9.3 — BikeFragment.java

**Save as:** `fragments/bike/BikeFragment.java`

```
CURSOR PROMPT:

Write a Java BikeFragment for package com.marc.helmet.fragments.bike.
Extends Fragment. Contains a ViewPager2 with 3 tabs: Lean Angle, Coordinates, Speed.

Use a FragmentStateAdapter with 3 inner Fragment classes:
- LeanAngleTabFragment
- CoordinatesTabFragment
- SpeedTabFragment

Each inner fragment inflates its respective layout (layout_bike_lean, layout_bike_coordinates, layout_bike_speed).

LeanAngleTabFragment:
- Add LeanAngleView programmatically to sv_lean_3d parent FrameLayout
- Register BroadcastReceiver for ACTION_DEVICE_STATUS and custom lean angle updates
- Receive lean angle data from parent activity (via shared ViewModel or broadcast)
  broadcast action: "com.marc.helmet.LEAN_UPDATE" extra: float roll
- Update LeanAngleView.setLeanAngle() on UI thread
- Update tv_current_angle text
- Update chip_crash_status: if in crash zone → "● DANGER" red else "● SAFE" green
- btn_recalibrate → navigate to Settings calibration section

CoordinatesTabFragment:
- Register BroadcastReceiver for ACTION_LOCATION_UPDATE
- Update tv_latitude and tv_longitude with 6 decimal places
- Update tv_altitude, tv_accuracy from Location object
- btn_copy_coords: copies "LAT: {lat}, LNG: {lng}" to clipboard

SpeedTabFragment:
- Register BroadcastReceiver for ACTION_SPEED_UPDATE
- Update tv_speed_large with current speed (round to int)
- SeekBar sb_speed_threshold: onProgressChanged → update tv_threshold_val,
  save to SettingsDao, send new threshold to MarcForegroundService
- Update tv_speed_alert_status when alert fires

All inner fragments: unregister receivers in onDestroyView.
```

---

## PART 10 — PROFILE FRAGMENT

---

### PROMPT 10.1 — fragment_profile.xml

**Save as:** `res/layout/fragment_profile.xml`

```
CURSOR PROMPT:

Write an Android XML layout fragment_profile.xml. Theme: Iron Man dark.

Root: ScrollView, background #0A0A0A

Inside: LinearLayout vertical, padding 20dp

SECTION HEADER "RIDER PROFILE":
- Monospace label 10sp #444444 letter-spacing 0.3

PROFILE CARD (background #1A1A1A, corners 12dp, padding 20dp):
- Row: ImageView id/iv_profile_photo 64dp circle, background #2A2A2A (placeholder avatar)
  + Column: TextView id/tv_profile_name "NO NAME SET" 18sp bold #DDDDDD,
    TextView id/tv_profile_age "AGE NOT SET" 12sp #888888
- Divider 1dp #2A2A2A, margin 16dp vertical
- Form fields (each: label above, EditText below):
  - id/et_name: hint "Full Name", label "NAME"
  - id/et_age: hint "Age", inputType number, label "AGE"
  - id/spinner_blood_type: label "BLOOD TYPE" — use Spinner with #1A1A1A bg, amber text
  - id/et_allergies: hint "Penicillin, Latex...", label "ALLERGIES", multiline 2 lines
  - id/et_medical_conditions: hint "Diabetes, Hypertension...", label "CONDITIONS", multiline 2 lines
  - id/et_medications: hint "Metformin 500mg...", label "MEDICATIONS", multiline 2 lines
  - id/et_emergency_notes: hint "Pacemaker fitted, do not defibrillate...", label "EMERGENCY NOTES", multiline 3 lines
- Button id/btn_save_profile "SAVE PROFILE", full width, amber button, margin top 16dp

Style all EditTexts: background @drawable/bg_amber_glow, textColor #DDDDDD,
hintTextColor #444444, textSize 14sp, fontFamily monospace, padding 12dp.

SECTION HEADER "EMERGENCY CONTACTS" (margin top 24dp):
- Monospace label + TextView "(1 = CALL + SMS, 2-4 = SMS ONLY)" 10sp #444444

RecyclerView id/rv_contacts (height WRAP_CONTENT, nestedScrollingEnabled false)

Button id/btn_add_contact "+ ADD CONTACT", outline amber, full width
TextView id/tv_contact_warning "⚠ Add at least 1 contact to enable ARM RIDE"
  color #C0392B, 11sp, GONE by default (show if no contacts)
```

---

### PROMPT 10.2 — ContactAdapter.java

**Save as:** `adapters/ContactAdapter.java`

```
CURSOR PROMPT:

Write a Java RecyclerView.Adapter class ContactAdapter for package com.marc.helmet.adapters.
Displays EmergencyContact items in the Profile screen.

Item layout (write inline XML in comments or as item_emergency_contact.xml):
- Background @drawable/bg_card_dark, corners 12dp, margin 6dp, padding 16dp
- Left: priority badge — large number "1", 24sp bold, color:
  - Priority 1: #E8750A (amber)
  - Priority 2-4: #444444 (gray)
- Center column:
  - Name: 15sp bold #DDDDDD
  - Phone: 13sp #888888 monospace
  - Relationship: 11sp #444444
  - Priority label badge: "CALL + SMS" (amber outline chip, 10sp) or "SMS ONLY" (gray chip)
- Right: ImageButton edit icon (pencil, 20dp, color #888888)
         ImageButton delete icon (trash, 20dp, color #C0392B)

Interface ContactAdapterListener:
    void onEdit(EmergencyContact contact, int position)
    void onDelete(EmergencyContact contact, int position)

Constructor: ContactAdapter(List<EmergencyContact> contacts, ContactAdapterListener listener)
Method: void updateContacts(List<EmergencyContact> contacts)
```

---

### PROMPT 10.3 — ProfileFragment.java

**Save as:** `fragments/profile/ProfileFragment.java`

```
CURSOR PROMPT:

Write a Java ProfileFragment for package com.marc.helmet.fragments.profile.
Extends Fragment. Layout: R.layout.fragment_profile.

Fields: all views, UserProfileDao profileDao, EmergencyContactDao contactDao, ContactAdapter adapter

OnViewCreated:
1. Bind all views
2. Init DAOs
3. Setup blood type spinner: ["Select...", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"]
   Custom spinner adapter with #1A1A1A background, #DDDDDD text, #E8750A selected item highlight
4. Load existing profile from DB → populate all EditText fields
5. Setup RecyclerView with ContactAdapter
6. Load contacts from DB, update adapter
7. Show/hide tv_contact_warning based on contact count

btn_save_profile onClick:
- Validate: name not empty, age > 0 and < 120
- Create UserProfile from fields
- Save via profileDao.insertOrUpdateProfile()
- Show success toast: "Profile saved. MARC has your back." (amber background toast)
- Update tv_profile_name and tv_profile_age

btn_add_contact onClick:
- Show AddContactDialog (write this dialog too — below)

ContactAdapterListener:
- onEdit: show AddContactDialog pre-filled with contact data
- onDelete: show confirmation dialog "Remove {name}?" → delete from DB, refresh list

AddContactDialog (write as AlertDialog or DialogFragment):
- Dark dialog (#1A1A1A background)
- Fields: Name (EditText), Phone (EditText, inputType phone), Relationship (EditText)
- Priority selection: "Make Primary Contact" CheckBox (only if less than 1 primary exists)
- Buttons: "CANCEL" (gray text) | "SAVE CONTACT" (amber button)
- On save: validate name + phone not empty, insert to DB, refresh parent RecyclerView
```

---

## PART 11 — SETTINGS FRAGMENT

---

### PROMPT 11.1 — fragment_settings.xml

**Save as:** `res/layout/fragment_settings.xml`

```
CURSOR PROMPT:

Write an Android XML layout fragment_settings.xml. Theme: Iron Man dark. Root: ScrollView.

Inside: LinearLayout vertical, padding 20dp, background #0A0A0A

5 settings sections, each as a card (#1A1A1A, corners 12dp, padding 20dp, margin-bottom 16dp):

SECTION 1 — AI ENGINE:
- Header "AI ENGINE" monospace 10sp #444444
- ToggleButton or Switch id/toggle_ai_mode: "MARC ONE" / "MARC BACK", amber when active
- Conditional layout id/layout_gemini (VISIBLE when MARC ONE):
  - Label "GEMINI API KEY" + EditText id/et_gemini_key (password input type, masked)
- Conditional layout id/layout_ollama (GONE when MARC ONE):
  - Label "OLLAMA SERVER IP" + EditText id/et_ollama_ip hint "192.168.1.100:11434"
  - Button id/btn_test_ollama "TEST CONNECTION" small outline amber
  - TextView id/tv_ollama_status "" (shows SUCCESS/FAILED after test)
  - Label "MODEL" + EditText id/et_ollama_model hint "llama3.2:3b-instruct-q4_K_M"

SECTION 2 — WAKE WORD:
- Header "WAKE WORD ENGINE" monospace 10sp #444444
- RadioGroup: RadioButton id/rb_porcupine "PORCUPINE" | RadioButton id/rb_always_on "ALWAYS-ON"
  (amber tint for selected)
- Conditional id/layout_porcupine (shows for porcupine):
  - Label "PORCUPINE ACCESS KEY" + EditText id/et_porcupine_key (masked)
  - Label "SENSITIVITY" + SeekBar id/sb_sensitivity + TextView id/tv_sensitivity_val "0.5"

SECTION 3 — CONNECTIONS:
- Header "PICO CONNECTIONS" monospace 10sp #444444
- Button id/btn_scan_devices "⬡ SCAN FOR DEVICES", full width, amber button
- ProgressBar id/pb_scanning GONE (shows during scan)
- RecyclerView id/rv_devices (wrap content, nested scroll false) — shows found devices

SECTION 4 — CALIBRATION:
- Header "BIKE UNIT CALIBRATION" monospace 10sp #444444
- Calibration status: TextView id/tv_calib_status "NOT CALIBRATED" red
- Step 1 card: "STANDING POSITION" + Button id/btn_capture_standing "CAPTURE" outline
  + TextView id/tv_standing_val "—°"
- Step 2 card: "MAX RIGHT LEAN" + Button id/btn_capture_right "CAPTURE"
  + TextView id/tv_right_val "—°"
- Step 3 card: "MAX LEFT LEAN" + Button id/btn_capture_left "CAPTURE"
  + TextView id/tv_left_val "—°"
- Button id/btn_save_calibration "SAVE & SEND TO BIKE UNIT" full width amber (disabled until all 3 captured)

SECTION 5 — INFO:
- Header "ABOUT MARC" monospace 10sp #444444
- ExpandableListView or AccordionView with FAQ items
- Developer card (amber border-left, dark bg):
  - "BUILT BY" label
  - "Ranjith Kumar Dasari" 16sp bold amber
  - "Embedded Systems & Software Engineer" 12sp gray
  - "EEE Final Year, DSCE Bangalore" 12sp gray
  - "ranjithdsr2@gmail.com" 11sp gray monospace
  - "github.com/RANJITH12022004" 11sp gray monospace, clickable → opens browser
  - Italic text: "MARC is named after Marc Marcuze 93 — Never Give Up." 11sp #444444 italic
```

---

### PROMPT 11.2 — SettingsFragment.java

**Save as:** `fragments/settings/SettingsFragment.java`

```
CURSOR PROMPT:

Write a Java SettingsFragment for package com.marc.helmet.fragments.settings.
Extends Fragment. Layout: R.layout.fragment_settings.

Fields: all views, SettingsDao settingsDao, DeviceDao deviceDao, CalibrationDao calibDao,
DeviceScanner scanner, DeviceAdapter deviceAdapter (write this adapter too)

OnViewCreated:
1. Load all settings from SettingsDao, populate fields
2. toggle_ai_mode: toggle visibility of layout_gemini / layout_ollama, save to DB
3. btn_test_ollama:
   - Get IP from et_ollama_ip
   - Call OllamaApiClient.testConnection()
   - Show "● CONNECTED" green or "✕ FAILED" red in tv_ollama_status
4. rb_porcupine / rb_always_on: save wake_word_engine setting
5. sb_sensitivity: update tv_sensitivity_val, save porcupine_sensitivity
6. btn_scan_devices:
   - Show pb_scanning
   - Start DeviceScanner.startScan()
   - onDeviceFound: create Device object, save to DeviceDao, update RecyclerView
   - onScanComplete: hide pb_scanning, show "Scan complete. Found X devices."
7. Calibration buttons:
   - btn_capture_standing: call getBikeStatus() → store roll/pitch in memory, update tv_standing_val
   - btn_capture_right: same, store as maxRight
   - btn_capture_left: same, store as maxLeft
   - Enable btn_save_calibration when all 3 captured
   - btn_save_calibration:
     - Build Calibration object
     - Save to CalibrationDao
     - Send to Bike Pico via PicoApiClient.calibrate()
     - Update tv_calib_status to "CALIBRATED ●" green
     - Show toast "Calibration saved. MARC knows your bike now."
8. Developer card: set github link clickable (Intent.ACTION_VIEW)

Private method getBikeStatus():
- Get Bike Device from DeviceDao
- Create PicoApiClient with bike URL
- Call getStatus(), return PicoStatus synchronously (or via callback with loading indicator)

DeviceAdapter (write inline):
- Item layout: card #1A1A1A, padding 16dp
- Left: device type icon (H for Helmet, B for Bike) in amber circle
- Center: device type label, IP address, firmware version
- Right: Button "CONNECT" (amber, 10sp) that calls connectDevice(device)
- connectDevice: update DeviceDao, notify parent service, show "Connected" state
```

---

## PART 12 — CRASH SCREEN

---

### PROMPT 12.1 — CrashAlertActivity.java & layout

**Save as:** `activities/CrashAlertActivity.java`, `res/layout/activity_crash_alert.xml`

```
CURSOR PROMPT:

Write a Java Activity CrashAlertActivity and its layout activity_crash_alert.xml
for package com.marc.helmet.activities.

This activity launches when crash is detected (from MarcForegroundService broadcast).
It should show on top of everything, even lock screen.

LAYOUT activity_crash_alert.xml:
- Root: ConstraintLayout, background #000000
- Full screen pulsing red overlay: View id/v_red_flash, match_parent, background #C0392B, alpha 0
  (will animate in/out via code)
- Center: large text "CRASH DETECTED", Bebas Neue style (bold 48sp), color #FF2020
  with amber glow animation (text shadow)
- Below: Text "CALLING FOR HELP IN:", 14sp, monospace, color #888888
- Large countdown: TextView id/tv_countdown "10", 96sp bold, color #FF2020, monospace
- Below: Text "SAY 'HEY MARC, CANCEL' OR:", 11sp, #444444, monospace
- Large cancel button id/btn_cancel_emergency: text "■ CANCEL EMERGENCY CALL",
  background #1A0000, border 2dp #C0392B, textColor #FF2020, bold, 52dp height, full width
- Bottom: "MARC is contacting your emergency contacts.", 11sp, #444444, italic, centered

ACTIVITY CrashAlertActivity.java:
- Show on lock screen: window flags FLAG_SHOW_WHEN_LOCKED, FLAG_TURN_SCREEN_ON, FLAG_KEEP_SCREEN_ON
- Launch in singleTask mode (declare in manifest)
- onCreate:
  1. Set window flags for lock screen display
  2. Init EmergencyService (inject DAOs from DatabaseHelper)
  3. Start red flash animation: ObjectAnimator on v_red_flash alpha 0↔0.3, repeat, duration 600ms
  4. Get GPS from Intent extras (lat, lng) OR get last known location from LocationManager
  5. Call emergencyService.triggerEmergency(lat, lng, listener)
  6. EmergencyListener:
     - onCountdownTick: update tv_countdown text
     - onEmergencyStarted: TTS "Crash detected. Calling for help in 10 seconds."
     - onEmergencyCancelled: finish activity
     - onEmergencyCompleted: TTS "Emergency call placed. MARC is with you. Stay calm.",
       show "CALL PLACED" state, change button to "● I AM SAFE" which finishes activity

- btn_cancel_emergency onClick: emergencyService.cancelEmergency()
- Wake word cancel: register broadcast for "com.marc.helmet.VOICE_CANCEL_EMERGENCY"
  (sent from WakeWordManager when user says "hey marc cancel")
- onBackPressed: disabled during active emergency countdown
- Set in AndroidManifest: showWhenLocked=true, turnScreenOn=true
```

---

## PART 13 — FOREGROUND SERVICE UPDATES

---

### PROMPT 13.1 — Update MarcForegroundService for crash trigger

**Save as:** Additions to `services/MarcForegroundService.java`

```
CURSOR PROMPT:

Add the following to the existing MarcForegroundService:

When crash is detected (either path A or B from the earlier spec):
1. Stop the 200ms polling temporarily (pause Handler)
2. Send broadcast ACTION_CRASH_DETECTED with extras: double lat, double lng (from current GPS)
3. Launch CrashAlertActivity using:
   Intent intent = new Intent(this, CrashAlertActivity.class);
   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   intent.putExtra("lat", currentLat);
   intent.putExtra("lng", currentLng);
   startActivity(intent);
4. After 15 seconds, resume polling (in case it was a false positive that user cancelled)

Also add:
- void sendLeanAngleBroadcast(float roll) — broadcasts "com.marc.helmet.LEAN_UPDATE" every 200ms
  so BikeFragment can update the 3D view in real-time
- void reloadCalibration() — refreshes calibration from DB (called after user recalibrates)
```

---

## PART 14 — ANDROIDMANIFEST.XML FINAL

---

### PROMPT 14.1 — Complete AndroidManifest additions

**Save as:** Paste into `AndroidManifest.xml`

```
CURSOR PROMPT:

Write the complete <application> section content for AndroidManifest.xml for the MARC app.
Package: com.marc.helmet. Include:

Activities:
- MainActivity: exported=true, launchMode=singleTop, theme=@style/Theme.MARC
- CrashAlertActivity: exported=false, launchMode=singleTask,
  showWhenLocked=true (API 27+: use android:showWhenLocked="true" and android:turnScreenOn="true")
  theme=@style/Theme.MARC.FullScreen

Services:
- MarcForegroundService: exported=false, foregroundServiceType=location|microphone

Receivers: (none required — using LocalBroadcastManager)

Also write the complete <queries> block for Android 11+ to allow:
- Launching dialer intent
- Sending SMS intent
- Checking if speech recognition is available

Also write the notification channel creation code snippet (to put in MainActivity.onCreate):
Channel ID: MARC_SERVICE_CHANNEL
Name: "MARC System"
Description: "MARC ride monitoring service"
Importance: IMPORTANCE_LOW
LightColor: #E8750A
```

---

## PART 15 — UTILITIES

---

### PROMPT 15.1 — Utility Classes

**Save as:** `utils/FormatUtils.java`, `utils/PermissionUtils.java`, `utils/NetworkUtils.java`

```
CURSOR PROMPT:

Write 3 Java utility classes for package com.marc.helmet.utils:

1. FormatUtils.java — static methods:
   - String formatSpeed(float kmh) → "82 km/h"
   - String formatCoordinate(double coord, boolean isLatitude) → "12.971599° N"
   - String formatAngle(double angle) → "+12.4°" or "-8.2°"
   - String formatDuration(long seconds) → "01:23:45"
   - String formatTimestamp(long unixMs) → "04 May 2026, 11:42 PM"

2. PermissionUtils.java — static methods:
   - boolean hasAllRequired(Context context) — checks RECORD_AUDIO, LOCATION, CALL_PHONE, SEND_SMS
   - void requestAll(Activity activity, int requestCode)
   - boolean hasAudio(Context context)
   - boolean hasLocation(Context context)
   - boolean hasCall(Context context)
   - boolean hasSms(Context context)

3. NetworkUtils.java — static methods:
   - String getDeviceIp(Context context) — returns WiFi IP as string
   - String getSubnet(String ip) — returns "192.168.1." from "192.168.1.5"
   - boolean isWifiConnected(Context context)
   - boolean isInternetReachable() — synchronous ping to 8.8.8.8 (run on background thread)
```

---

## PART 16 — BUILD VERIFICATION CHECKLIST

Run through these in Android Studio before calling it done:

### 16.1 — Compile Check
```
Build → Make Project
Fix any import errors (most will be missing imports — use Alt+Enter → Import)
Fix any resource reference errors (layout IDs, drawable names)
```

### 16.2 — Manifest Check
```
Ensure CrashAlertActivity is declared
Ensure MarcForegroundService has foregroundServiceType
Ensure all permissions are declared
```

### 16.3 — Run Order for First Test
```
Phase 1: Run app → Dashboard loads → ARM RIDE button visible → no crash
Phase 2: Profile screen → enter medical data → save → reopen → data persists
Phase 3: Settings → enter Gemini API key → MARC tab → type a message → get response
Phase 4: Settings → Connections → scan → connect Bike Pico → see in Dashboard
Phase 5: Bike tab → see lean angle update from Pico in real-time
Phase 6: Calibrate → capture 3 positions → save → verify in Bike tab threshold lines
Phase 7: Tilt Pico beyond threshold → CrashAlertActivity launches → countdown → cancel works
Phase 8: Say "Hey MARC, how do I stop bleeding?" → voice response plays
Phase 9: Speed > threshold → helmet LED fires
```

### 16.4 — Common Errors & Fixes
```
"NetworkOnMainThreadException" → You called OkHttp synchronously. Use async callbacks.
"IllegalStateException: Fragment not attached" → Check isAdded() before UI updates in callbacks.
"SecurityException: Missing permission" → Permission not declared in manifest or not requested at runtime.
"NullPointerException in Fragment" → Views accessed before onViewCreated or after onDestroyView.
"Porcupine: invalid access key" → Get key from console.picovoice.ai, it's free.
"Ollama: connection refused" → Run 'ollama serve' on laptop. Check IP. Check same WiFi.
"Gemini: 400 Bad Request" → Check API key in Settings. Check model name string.
"CrashAlertActivity not showing on lock screen" → Add window flags in onCreate before setContentView.
```

---

## PART 17 — PICO W FIRMWARE (BONUS — NOT CURSOR, WRITE THIS IN THONNY)

> Drop this in Thonny. Flash to each Pico. Not Android Studio work.

### Helmet Pico — main.py skeleton

```python
# HELMET PICO W — MARC System v1.0
# Flash this with CircuitPython 9.x

import wifi
import socketpool
import json
import board
import digitalio
import time

# ── CONFIG ─────────────────────────────────────────────────────────────────────
WIFI_SSID = "YOUR_SSID_HERE"
WIFI_PASSWORD = "YOUR_PASSWORD_HERE"
DEVICE_TYPE = "MARC_HELMET"
VERSION = "1.0"

# ── HARDWARE ────────────────────────────────────────────────────────────────────
led = digitalio.DigitalInOut(board.LED)
led.direction = digitalio.Direction.OUTPUT

# ── STATE ───────────────────────────────────────────────────────────────────────
state = {
    "device_type": DEVICE_TYPE,
    "version": VERSION,
    "led_mode": "idle",
    "speed_alert_active": False,
    "initialized": False
}

def blink(n, delay=0.15):
    for _ in range(n):
        led.value = True
        time.sleep(delay)
        led.value = False
        time.sleep(delay)

def connect_wifi():
    blink(3, 0.2)  # amber blink = connecting
    wifi.radio.connect(WIFI_SSID, WIFI_PASSWORD)
    print(f"Connected. IP: {wifi.radio.ipv4_address}")
    blink(1, 0.5)  # long blink = connected
    state["initialized"] = True

def handle_request(method, path, body):
    if path == "/identify" or path == "/status":
        return 200, json.dumps(state)
    elif path == "/led" and method == "POST":
        data = json.loads(body) if body else {}
        state["led_mode"] = data.get("mode", "idle")
        state["speed_alert_active"] = (state["led_mode"] == "alert")
        return 200, '{"ok": true}'
    elif path == "/init_confirm":
        state["initialized"] = True
        blink(3, 0.1)  # fast blinks = initialized
        return 200, '{"initialized": true}'
    return 404, '{"error": "not found"}'

def run_server(pool):
    server = pool.socket()
    server.bind(("0.0.0.0", 80))
    server.listen(1)
    print("HTTP server running on port 80")
    while True:
        if state["speed_alert_active"]:
            led.value = not led.value  # blink effect
            time.sleep(0.25)
        try:
            server.settimeout(0.5)
            conn, addr = server.accept()
            request = b""
            while True:
                chunk = conn.recv(512)
                request += chunk
                if len(chunk) < 512:
                    break
            req_str = request.decode("utf-8", errors="ignore")
            lines = req_str.split("\r\n")
            method, path = lines[0].split(" ")[:2]
            body = req_str.split("\r\n\r\n")[-1] if "\r\n\r\n" in req_str else ""
            status, response_body = handle_request(method, path, body)
            response = f"HTTP/1.1 {status} OK\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\n\r\n{response_body}"
            conn.send(response.encode())
            conn.close()
        except OSError:
            pass  # timeout, keep looping

connect_wifi()
pool = socketpool.SocketPool(wifi.radio)
run_server(pool)
```

### Bike Pico — main.py skeleton

```python
# BIKE PICO W — MARC System v1.0
# Flash this with CircuitPython 9.x
# Hardware: MPU6050 on I2C SDA=GP0, SCL=GP1 (or adjust pins)

import wifi, socketpool, json, board, busio, time, math

WIFI_SSID = "YOUR_SSID_HERE"
WIFI_PASSWORD = "YOUR_PASSWORD_HERE"
DEVICE_TYPE = "MARC_BIKE"
VERSION = "1.0"

# ── MPU6050 SETUP ───────────────────────────────────────────────────────────────
i2c = busio.I2C(board.GP1, board.GP0)
MPU_ADDR = 0x68
# Wake up MPU6050
i2c.try_lock()
i2c.writeto(MPU_ADDR, bytes([0x6B, 0x00]))
i2c.unlock()

# ── STATE ────────────────────────────────────────────────────────────────────────
state = {
    "device_type": DEVICE_TYPE, "version": VERSION,
    "roll": 0.0, "pitch": 0.0, "crash_flag": False,
    "standing_roll": 0.0, "max_left_roll": -45.0, "max_right_roll": 45.0
}
angle_buffer = []
complementary_roll = 0.0
last_time = time.monotonic()

def read_mpu():
    global complementary_roll, last_time
    i2c.try_lock()
    i2c.writeto(MPU_ADDR, bytes([0x3B]))
    data = bytearray(14)
    i2c.readfrom_into(MPU_ADDR, data)
    i2c.unlock()
    def s16(high, low):
        v = (high << 8) | low
        return v - 65536 if v > 32767 else v
    ax = s16(data[0], data[1]) / 16384.0
    ay = s16(data[2], data[3]) / 16384.0
    az = s16(data[4], data[5]) / 16384.0
    gx = s16(data[8], data[9]) / 131.0
    accel_roll = math.atan2(ay, math.sqrt(ax*ax + az*az)) * 57.2958
    now = time.monotonic()
    dt = now - last_time
    last_time = now
    complementary_roll = 0.96 * (complementary_roll + gx * dt) + 0.04 * accel_roll
    return complementary_roll

def check_crash(roll):
    ref = state["standing_roll"]
    threshold = max(abs(state["max_left_roll"] - ref), abs(state["max_right_roll"] - ref))
    if threshold > 0 and abs(roll - ref) > threshold:
        state["crash_flag"] = True

def handle_request(method, path, body):
    if path in ("/identify", "/status"):
        roll = read_mpu()
        state["roll"] = round(roll, 2)
        check_crash(roll)
        return 200, json.dumps(state)
    elif path == "/calibrate" and method == "POST":
        data = json.loads(body) if body else {}
        state["standing_roll"] = data.get("standing", 0.0)
        state["max_left_roll"] = data.get("max_left", -45.0)
        state["max_right_roll"] = data.get("max_right", 45.0)
        state["crash_flag"] = False
        return 200, '{"ok": true}'
    elif path == "/reset_crash" and method == "POST":
        state["crash_flag"] = False
        return 200, '{"ok": true}'
    return 404, '{"error": "not found"}'

# ── WIFI + SERVER (same pattern as helmet) ──────────────────────────────────────
wifi.radio.connect(WIFI_SSID, WIFI_PASSWORD)
print(f"Bike Pico online. IP: {wifi.radio.ipv4_address}")
pool = socketpool.SocketPool(wifi.radio)
server = pool.socket()
server.bind(("0.0.0.0", 80))
server.listen(1)
print("Bike HTTP server ready on port 80")

while True:
    try:
        server.settimeout(0.2)
        conn, addr = server.accept()
        request = b""
        while True:
            chunk = conn.recv(512)
            request += chunk
            if len(chunk) < 512:
                break
        req_str = request.decode("utf-8", errors="ignore")
        lines = req_str.split("\r\n")
        method, path = lines[0].split(" ")[:2]
        body = req_str.split("\r\n\r\n")[-1] if "\r\n\r\n" in req_str else ""
        status, rb = handle_request(method, path, body)
        response = f"HTTP/1.1 {status} OK\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\n\r\n{rb}"
        conn.send(response.encode())
        conn.close()
    except OSError:
        # Poll MPU even when no request comes in
        roll = read_mpu()
        state["roll"] = round(roll, 2)
        check_crash(roll)
```

---

## FINAL BUILD ORDER — EXECUTE THIS SEQUENCE

```
[ ] STEP 0  — Android Studio project setup (Part 0)
[ ] STEP 1  — Paste colors.xml, themes.xml, dimens.xml (Prompts 1.1–1.3)
[ ] STEP 2  — Paste all drawables and animations (Prompts 1.4–1.6)
[ ] STEP 3  — Paste DatabaseHelper + all models + all DAOs (Prompts 2.1–2.3)
[ ] STEP 4  — Paste PicoApiClient + DeviceScanner (Prompts 3.1–3.2)
[ ] STEP 5  — Paste GeminiApiClient + OllamaApiClient (Prompts 3.3–3.4)
[ ] STEP 6  — Paste MarcForegroundService + EmergencyService (Prompts 4.1–4.2)
[ ] STEP 7  — Paste TTS + STT + WakeWord managers (Prompts 5.1–5.3)
[ ] STEP 8  — Paste activity_main.xml + nav files + MainActivity (Prompts 6.1–6.4)
[ ] STEP 9  — COMPILE. Fix all import errors. Do not proceed until it compiles.
[ ] STEP 10 — Paste Dashboard layout + Fragment (Prompts 7.1–7.2)
[ ] STEP 11 — Paste MARC layout + Adapter + Fragment (Prompts 8.1–8.3)
[ ] STEP 12 — Paste Bike layout + LeanAngleView + Fragment (Prompts 9.1–9.3)
[ ] STEP 13 — Paste Profile layout + Adapter + Fragment (Prompts 10.1–10.3)
[ ] STEP 14 — Paste Settings layout + Fragment (Prompts 11.1–11.2)
[ ] STEP 15 — Paste CrashAlertActivity + layout (Prompt 12.1)
[ ] STEP 16 — Paste utility classes (Prompt 15.1)
[ ] STEP 17 — Update AndroidManifest (Prompt 14.1)
[ ] STEP 18 — COMPILE AGAIN. Fix everything.
[ ] STEP 19 — Flash Helmet Pico firmware (Part 17)
[ ] STEP 20 — Flash Bike Pico firmware (Part 17)
[ ] STEP 21 — Run on USB debug. Test Phase order from 16.3.
[ ] STEP 22 — Extract APK: Build → Build Bundle/APK → Build APK
```

---

```
MARC_BUILD_GUIDE v1.0
Ranjith Kumar Dasari | DSCE EEE 2026
Named after Marc Marcuze 93 — Never Give Up.
"Built in the dark. Deployed in the field."
```

---

## PART 18 — HOW TO USE CURSOR CORRECTLY (READ OR SUFFER)

> You're about to paste AI-generated Java into Android Studio like a raccoon
> stuffing garbage into a microwave. Here's how to not do that.

### 18.1 — Cursor Setup for This Project

```
1. Open Cursor
2. Open a new empty folder (NOT the Android Studio project folder)
3. Create a file called MARC_CONTEXT.md in that folder
4. Paste this into it:
```

**MARC_CONTEXT.md — paste this as your Cursor project context:**

```markdown
# MARC Project Context

## App: MARC (Motorcycle Accident Response Companion)
## Package: com.marc.helmet
## Language: Java (NOT Kotlin)
## Min SDK: 26 | Target SDK: 34
## Build System: Gradle

## Key Libraries:
- OkHttp3 4.12.0 (all HTTP)
- Gson 2.10.1 (all JSON)
- Porcupine Android SDK 3.0.1 (wake word)
- Google Play Services Location 21.2.0 (GPS)
- Material Components 1.12.0 (UI)
- Navigation Component 2.7.7 (fragments)
- Lifecycle ViewModel/LiveData 2.7.0

## Theme Colors:
- Background: #0A0A0A
- Surface: #1A1A1A
- Primary Accent (Amber): #E8750A
- Error/Alert (Red): #C0392B
- Text Primary: #DDDDDD
- Text Muted: #444444

## Architecture: Single Activity (MainActivity) + Navigation Component + Fragments
## Fragments: DashboardFragment, MarcFragment, BikeFragment, ProfileFragment, SettingsFragment
## Services: MarcForegroundService (crash monitor + GPS), EmergencyService (manager class)
## Database: SQLite via SQLiteOpenHelper (DatabaseHelper.java)
## NO Kotlin. NO Room. NO Retrofit. NO Dagger. Just Java + OkHttp + SQLite. Keep it simple.

## Important rules for all generated code:
- All network calls ASYNC (OkHttp callback), deliver results on main thread via Handler
- All DB calls can be on main thread (data is small, SQLite is fast enough for demo)
- No lambdas that require API > 26 without checking
- All Views null-checked before access
- All Fragment UI updates check isAdded() first
- Never use deprecated APIs without fallback
```

---

### 18.2 — Cursor Prompting Rules

**Rule 1: Always prefix with the file path**
```
BAD:  "Write a database helper class"
GOOD: "Write DatabaseHelper.java for package com.marc.helmet.database. [full prompt from guide]"
```

**Rule 2: Tell Cursor exactly what NOT to do**
```
Always add to every prompt:
"Use Java only. No Kotlin. No Room. No Retrofit. No Dagger. No lambdas requiring API 26+.
 All network calls must be asynchronous via OkHttp callbacks.
 Deliver all UI updates on main thread via Handler(Looper.getMainLooper()).post()."
```

**Rule 3: Ask for imports explicitly**
```
Add to every Java file prompt:
"Include all necessary import statements at the top. Do not use wildcard imports."
```

**Rule 4: One file per prompt, no exceptions**
```
BAD:  "Write the entire database layer"
GOOD: "Write DatabaseHelper.java" [separate prompt]
      "Write UserProfileDao.java" [separate prompt]
      "Write EmergencyContactDao.java" [separate prompt]
```

**Rule 5: Validate before pasting**
Quick scan checklist before pasting into Android Studio:
- [ ] Package declaration matches `com.marc.helmet.*`
- [ ] No `import kotlin.*` anywhere
- [ ] No `@Inject` annotations (no Dagger)
- [ ] No `suspend fun` or `coroutine` keywords
- [ ] All resource references use `R.id.*`, `R.layout.*`, `R.drawable.*` format
- [ ] No hardcoded hex colors in Java (use `getResources().getColor(R.color.colorAmber, null)`)

**Rule 6: When Cursor gives you garbage**
```
If the output is wrong, don't just re-ask.
Add to your next prompt: "The previous attempt [specific problem]. Fix it by [specific fix].
Keep everything else identical."

Common Cursor failures and how to redirect:
- Gives Kotlin → "Rewrite this in Java. I said Java. Not Kotlin. Java."
- Uses Room → "Remove all Room annotations. Use raw SQLiteDatabase with ContentValues."
- Uses Retrofit → "Remove Retrofit. Use OkHttp3 directly with OkHttpClient and Request.Builder."
- Missing imports → "Add all missing import statements. List every class import explicitly."
- Async done wrong → "Move the OkHttp call to be async using enqueue(). 
                       Deliver results via Handler(Looper.getMainLooper()).post()"
```

---

### 18.3 — Exact Cursor Prompt Templates (Copy-Paste Ready)

These are the ACTUAL prompts you type into Cursor. Copy verbatim, add the file-specific
content from the relevant section above.

**Template A — New Java Class:**
```
Write [ClassName].java for package [com.marc.helmet.package].

[PASTE THE SPECIFIC REQUIREMENTS FROM THE SECTION ABOVE]

Rules:
- Java only. No Kotlin.
- Include all import statements explicitly. No wildcard imports.
- No Room, no Retrofit, no Dagger, no Hilt.
- All HTTP calls asynchronous via OkHttp3 enqueue().
- All UI updates via Handler(Looper.getMainLooper()).post(Runnable).
- All Fragment UI updates check isAdded() before touching views.
- Use R.color.*, R.drawable.*, R.string.* for all resource references.
```

**Template B — New XML Layout:**
```
Write [filename].xml for the MARC Android app (package com.marc.helmet).

[PASTE THE SPECIFIC LAYOUT REQUIREMENTS FROM THE SECTION ABOVE]

Rules:
- Use ConstraintLayout as root unless ScrollView is specified.
- All colors must reference @color/* values (e.g. @color/colorBackground, @color/colorAmber).
- All dimensions must reference @dimen/* values where possible.
- All text must reference @string/* values OR be placeholder text clearly marked TODO.
- No hardcoded colors or dimensions in XML except 0dp for constraints.
- Include all view IDs exactly as specified.
- Use app:layout_constraintXxx for all ConstraintLayout positioning.
```

**Template C — Fixing a Generated File:**
```
Fix the following Java file [ClassName].java for the MARC Android app.

Problem: [describe exactly what's wrong]
Expected behavior: [describe what it should do]

Current broken code:
[paste the broken section]

Fix only the problematic section. Keep all other code identical.
Apply the same rules: Java only, async OkHttp, main thread UI updates, check isAdded().
```

---

## PART 19 — MISSING PROMPTS (ITEMS NOT YET WRITTEN)

These are the remaining files the guide referenced but didn't write full prompts for.

---

### PROMPT 19.1 — Color State List (Nav Selector)

**Save as:** `res/color/nav_selector.xml`

```
CURSOR PROMPT:

Write an Android color state list XML file nav_selector.xml for res/color/ folder
(not res/values/ — this goes in res/color/).

States:
- state_checked=true → #E8750A (amber, selected nav item)
- state_pressed=true → #FFA040 (lighter amber, pressed state)
- default → #444444 (gray, unselected nav item)

This is used for the BottomNavigationView itemIconTint and itemTextColor.
```

---

### PROMPT 19.2 — item_emergency_contact.xml

**Save as:** `res/layout/item_emergency_contact.xml`

```
CURSOR PROMPT:

Write an Android XML item layout item_emergency_contact.xml for the MARC app.
Used in a RecyclerView in the Profile screen. Theme: Iron Man dark.

Root: androidx.cardview.widget.CardView
  - cardBackgroundColor: #1A1A1A
  - cardCornerRadius: 12dp
  - cardElevation: 0dp
  - layout_margin: 6dp

Inside CardView → ConstraintLayout, padding 16dp:

LEFT (constrain start):
  - TextView id/tv_priority_number: text "1", textSize 28sp, bold, color #E8750A,
    width 40dp, centered vertical, constrain start=parent

CENTER (constrain start=tv_priority_number end, end=layout_actions start):
  - TextView id/tv_contact_name: textSize 15sp, bold, color #DDDDDD
  - TextView id/tv_contact_phone: textSize 13sp, color #888888, fontFamily monospace
  - TextView id/tv_contact_relationship: textSize 11sp, color #444444
  - TextView id/tv_priority_label: textSize 10sp, padding 2dp 6dp,
    background = rounded rect 4dp corners with stroke 1dp
    (amber border + amber text for CALL + SMS, gray border + gray text for SMS ONLY)

RIGHT (constrain end):
  - LinearLayout vertical, 32dp width:
    - ImageButton id/btn_edit: 24dp, tint #888888, src @android:drawable/ic_menu_edit
    - ImageButton id/btn_delete: 24dp, tint #C0392B, src @android:drawable/ic_menu_delete

Vertical chain: name → phone → relationship → label, centered in card height.
```

---

### PROMPT 19.3 — item_device.xml

**Save as:** `res/layout/item_device.xml`

```
CURSOR PROMPT:

Write an Android XML item layout item_device.xml for MARC device list in Settings.
Theme: Iron Man dark.

Root: LinearLayout horizontal, background @drawable/bg_card_dark,
  padding 16dp, layout_margin 6dp, cornerRadius handled by bg drawable.

LEFT: FrameLayout 44dp x 44dp, background = circle drawable #2A2A2A:
  - TextView inside: "H" or "B", 18sp bold, color #E8750A, centered

CENTER (weight 1, margin start 12dp): LinearLayout vertical:
  - TextView id/tv_device_type: "HELMET UNIT" or "BIKE UNIT", 14sp bold, #DDDDDD
  - TextView id/tv_device_ip: "192.168.1.50", 12sp, #888888, monospace
  - TextView id/tv_firmware: "v1.0", 10sp, #444444

RIGHT: LinearLayout vertical, width wrap_content, margin start 8dp:
  - Button id/btn_connect: text "CONNECT", textSize 10sp, padding 6dp 12dp,
    background @drawable/bg_button_amber, textColor #000000
  - TextView id/tv_ping: "— ms", 10sp, #444444, centered, margin top 4dp
```

---

### PROMPT 19.4 — item_message_user.xml & item_message_marc.xml

**Save as:** `res/layout/item_message_user.xml` and `res/layout/item_message_marc.xml`

```
CURSOR PROMPT:

Write TWO Android XML item layouts for the MARC chat RecyclerView.

FILE 1 — item_message_user.xml (user's message, right-aligned):
Root: LinearLayout, orientation horizontal, gravity end, padding 8dp 4dp,
  layout_width match_parent.

Inside: LinearLayout vertical, background = custom shape:
  - Shape: rounded rect all corners 12dp EXCEPT bottom-right = 2dp
  - Background color: #E8750A
  - padding: 10dp 14dp
  - layout_marginStart: 72dp (pushes it right)
  - TextView id/tv_message: textColor #000000, textSize 14sp, lineSpacingExtra 3dp
  - TextView id/tv_timestamp: textSize 9sp, color #00000080 (50% alpha black), gravity end

FILE 2 — item_message_marc.xml (MARC's message, left-aligned):
Root: LinearLayout, orientation horizontal, gravity start, padding 8dp 4dp,
  layout_width match_parent.

Inside outer layout:
  - TextView id/tv_marc_label: text "MARC", textSize 9sp, color #E8750A, monospace,
    letter-spacing 0.2, margin-bottom 4dp

  Below: LinearLayout vertical, background = custom shape:
    - Shape: rounded rect all corners 12dp EXCEPT bottom-left = 2dp
    - Background color: #1A1A1A
    - Stroke: 1dp #2A2A2A
    - padding: 10dp 14dp
    - layout_marginEnd: 72dp (keeps it left)
    - TextView id/tv_message: textColor #DDDDDD, textSize 14sp, lineSpacingExtra 3dp
    - TextView id/tv_timestamp: textSize 9sp, color #DDDDDD50, gravity start
```

---

### PROMPT 19.5 — Groq API Client (Drop-in Gemini Replacement)

**Save as:** `network/ai/GroqApiClient.java`

```
CURSOR PROMPT:

Write a Java class GroqApiClient for package com.marc.helmet.network.ai.
This is a drop-in replacement for GeminiApiClient — same interface, different API.
Uses OkHttp3.

Groq API endpoint: https://api.groq.com/openai/v1/chat/completions
This follows the OpenAI Chat Completions format.

Constants:
- BASE_URL = "https://api.groq.com/openai/v1/chat/completions"
- DEFAULT_MODEL = "llama-3.1-70b-versatile" (fast, free tier, excellent instruction following)
- MAX_TOKENS = 512
- TEMPERATURE = 0.3f

Same MARC_SYSTEM_PROMPT string as GeminiApiClient (copy verbatim).

Same MarcResponseCallback interface: onResponse(String), onError(String).

Method: void sendMessage(String apiKey, List<ChatMessage> history, String userMessage,
  MarcResponseCallback callback)

Build request JSON (OpenAI format):
{
  "model": "llama-3.1-70b-versatile",
  "messages": [
    { "role": "system", "content": MARC_SYSTEM_PROMPT },
    ...history as { "role": "user"/"assistant", "content": content },
    { "role": "user", "content": userMessage }
  ],
  "max_tokens": 512,
  "temperature": 0.3
}

Authorization header: "Bearer " + apiKey

Parse response: choices[0].message.content

Note: Groq uses "assistant" for model role, NOT "model" like Gemini.
Map ChatMessage role "marc" → "assistant" when building request.

Timeout: 10s (Groq is FAST — 500+ tokens/sec, fastest free inference available).
Deliver response on main thread.

Method: boolean isConfigured(String apiKey)

Add a comment at the top of the file:
// GROQ API CLIENT — Drop-in replacement for Gemini. Swap in SettingsFragment.
// Get free API key at: console.groq.com
// Model: llama-3.1-70b-versatile — better instruction following than Gemini Flash on constrained prompts.
// To switch: in SettingsDao, ai_engine = "groq". Update GeminiApiClient caller to check engine type.
```

---

### PROMPT 19.6 — Update SettingsFragment for Groq toggle

**Save as:** Addition to `fragments/settings/SettingsFragment.java`

```
CURSOR PROMPT:

Update the AI ENGINE section of SettingsFragment to support 3 modes instead of 2.

Change the toggle to a RadioGroup with 3 options:
- RadioButton id/rb_gemini: "MARC ONE (Gemini)" — amber when selected
- RadioButton id/rb_groq: "MARC GROQ (Groq)" — amber when selected
- RadioButton id/rb_ollama: "MARC BACK (Ollama)" — amber when selected

Show/hide corresponding input layouts:
- layout_gemini: Gemini API key field (shown for rb_gemini)
- layout_groq: NEW layout with Groq API key field id/et_groq_key (shown for rb_groq)
  Add note TextView: "Free at console.groq.com — 500+ tokens/sec inference"
  color #444444, 10sp, below the key field
- layout_ollama: Ollama IP field (shown for rb_ollama)

Save engine selection as: ai_engine = "gemini" | "groq" | "ollama" in SettingsDao.
Add key "groq_api_key" to default settings in DatabaseHelper.

In MarcFragment.sendToMarc(), add Groq branch:
  else if (settingsDao.getSetting("ai_engine").equals("groq")):
    use GroqApiClient with groq_api_key from settings

Loading phrase for Groq: "Running Groq inference..." (add to LOADING_PHRASES array)
```

---

### PROMPT 19.7 — MarcViewModel.java (Shared State)

**Save as:** `fragments/marc/MarcViewModel.java`

```
CURSOR PROMPT:

Write a Java ViewModel class MarcViewModel extending AndroidViewModel
for package com.marc.helmet.fragments.marc.

Purpose: Share MARC conversation state between MarcFragment and MainActivity
so wake word activation from any screen doesn't lose conversation history.

Fields (all MutableLiveData):
- MutableLiveData<List<ChatMessage>> conversationHistory = new MutableLiveData<>(new ArrayList<>())
- MutableLiveData<Boolean> isMarcListening = new MutableLiveData<>(false)
- MutableLiveData<Boolean> isMarcProcessing = new MutableLiveData<>(false)
- MutableLiveData<String> currentLoadingPhrase = new MutableLiveData<>("")
- MutableLiveData<String> lastResponse = new MutableLiveData<>("")
- MutableLiveData<String> errorMessage = new MutableLiveData<>("")

Methods:
- void addUserMessage(String text) — adds ChatMessage(role=user) to history list
- void addMarcMessage(String text) — adds ChatMessage(role=marc) to history list
- List<ChatMessage> getLast6Messages() — returns last 6 from history (3 user + 3 marc)
- void clearHistory() — clear conversation list
- void setListening(boolean listening) — update isMarcListening
- void setProcessing(boolean processing, String phrase) — update both fields
- LiveData<List<ChatMessage>> getHistory() — returns LiveData for observation
- LiveData<Boolean> getListeningState()
- LiveData<Boolean> getProcessingState()
- LiveData<String> getLoadingPhrase()

ChatMessage inner class (or import from adapters package):
  String role, String content, long timestamp

Constructor: MarcViewModel(@NonNull Application application) — call super(application)

Note in comment: "Observe this ViewModel from both MainActivity (for wake word) and
MarcFragment (for UI updates). Use ViewModelProvider with activity scope."
```

---

### PROMPT 19.8 — CrashViewModel.java (Shared Crash State)

**Save as:** `services/CrashViewModel.java`

```
CURSOR PROMPT:

Write a Java ViewModel class CrashViewModel extending ViewModel
for package com.marc.helmet.services.

Purpose: Expose crash state, countdown, and GPS to any observer (CrashAlertActivity,
DashboardFragment, MarcFragment).

Fields:
- MutableLiveData<Boolean> isCrashActive = new MutableLiveData<>(false)
- MutableLiveData<Integer> countdownSeconds = new MutableLiveData<>(10)
- MutableLiveData<Double> crashLat = new MutableLiveData<>(0.0)
- MutableLiveData<Double> crashLng = new MutableLiveData<>(0.0)
- MutableLiveData<String> crashStatus = new MutableLiveData<>("MONITORING")
  // States: MONITORING | CRASH_DETECTED | COUNTING_DOWN | CALL_PLACED | CANCELLED

Methods:
- void triggerCrash(double lat, double lng)
- void updateCountdown(int seconds)
- void cancelCrash()
- void completeCrash()
- void resetState()
- LiveData getters for all fields

This ViewModel is updated by MarcForegroundService via application-level shared instance.
Add static helper: CrashViewModel getInstance(Application app) using ViewModelStore.

Note: ForegroundService cannot use ViewModelProvider directly.
Instead, store the ViewModel in Application class and access it globally.
Write MarcApplication.java extending Application with:
  - CrashViewModel crashViewModel field
  - getter getCrashViewModel()
  - Register in AndroidManifest as android:name=".MarcApplication"
```

---

### PROMPT 19.9 — SplashActivity.java & layout

**Save as:** `activities/SplashActivity.java`, `res/layout/activity_splash.xml`

```
CURSOR PROMPT:

Write a Java SplashActivity and layout activity_splash.xml for the MARC app.
This is the launch screen. Shows for 2.5 seconds then goes to MainActivity.

LAYOUT activity_splash.xml:
- Root: ConstraintLayout, background #000000, match_parent
- Center: LinearLayout vertical, centered:
  - TextView "MARC", textSize 72sp, bold, color #E8750A, fontFamily monospace,
    letter-spacing 0.1 (simulate Bebas Neue — closest Android fallback is bold monospace)
  - TextView "MOTORCYCLE ACCIDENT RESPONSE COMPANION", textSize 10sp, color #444444,
    monospace, letter-spacing 0.2, centered, margin-top 8dp
  - View divider: width 60dp, height 1dp, background #E8750A, margin 20dp vertical
  - TextView "MARC SYSTEM v1.0", textSize 9sp, color #2A2A2A, monospace
- Bottom-left corner: TextView "NAMED AFTER MARC MARCUZE 93", 8sp, color #1A1A1A, monospace
- Bottom-right corner: TextView "NEVER GIVE UP", 8sp, color #1A1A1A, monospace italic

ACTIVITY SplashActivity.java:
- Extends AppCompatActivity
- Theme: @style/Theme.MARC.Splash
- onCreate:
  1. Hide status bar (fullscreen)
  2. Start fade-in animation on the center LinearLayout (from alpha 0 to 1, 800ms)
  3. After 2500ms: start MainActivity, call finish()
  4. Use Handler(Looper.getMainLooper()).postDelayed() for the delay
- In AndroidManifest: this is the LAUNCHER activity (intent-filter with MAIN + LAUNCHER)
  MainActivity should NOT have LAUNCHER — remove it from MainActivity.

Animation sequence:
- 0ms: MARC text fades in
- 300ms: subtitle fades in
- 600ms: divider slides in from left (translateX -200dp to 0)
- 900ms: version text fades in
- 2500ms: fade to black, start MainActivity
```

---

## PART 20 — ANDROID STUDIO SPECIFIC GOTCHAS

> Things that will bite you and aren't in any tutorial because they assume
> you've done this before. You haven't. Read this.

### 20.1 — Vector Drawables for Nav Icons

The bottom nav uses icon references. Android system drawables look terrible.
Create proper vector drawables. In Android Studio:

```
Right-click res/drawable → New → Vector Asset
Click "Clip Art" → search for:
  - "dashboard" → save as ic_nav_system.xml
  - "mic" → save as ic_nav_marc.xml
  - "motorcycle" (or "directions bike") → save as ic_nav_bike.xml
  - "person" → save as ic_nav_profile.xml
  - "settings" → save as ic_nav_settings.xml

Change fill color to #888888 (unselected state).
The nav_selector.xml color state list handles selected color automatically.

Update bottom_nav_menu.xml to reference these:
  android:icon="@drawable/ic_nav_system" etc.
```

### 20.2 — Google Cloud STT vs Android Built-in

The guide uses Android's built-in SpeechRecognizer (free, no setup).
If you want proper Google Cloud STT later:

```
1. Go to console.cloud.google.com
2. Create project → enable "Cloud Speech-to-Text API"
3. Create service account → download JSON key
4. Add to app/src/main/assets/google_credentials.json
5. In GoogleSTTManager: use GoogleCredentials.fromStream() to authenticate
6. This is a v2 upgrade — not needed for demo
```

### 20.3 — Porcupine Setup (Do This Before Running)

```
1. Go to console.picovoice.ai → sign up (free)
2. Create account → get Access Key (it's on the dashboard)
3. Go to "Wake Word" → Create Custom Wake Word
4. Type "Hey MARC" → Train → Download Android (.ppn file)
5. Place the .ppn file in: app/src/main/assets/hey_marc_android.ppn
6. In SettingsFragment: the porcupine_access_key field takes the key from step 2
7. In WakeWordManager: ppnFilePath = "hey_marc_android.ppn" (loads from assets)

The .ppn file is device-specific — you need the Android version.
Training takes about 1 minute on Picovoice servers.
```

### 20.4 — Gemini API Key Setup

```
1. Go to aistudio.google.com
2. Sign in with Google → Get API Key → Create API Key
3. Copy the key
4. In MARC app: Settings → AI ENGINE → MARC ONE → paste key in field
5. The app saves it to SQLite settings table
6. Free tier: 15 requests/minute, 1500/day — more than enough for demo

IMPORTANT: Do NOT hardcode the API key in Java files.
Let the user enter it in Settings as designed.
If you hardcode it and push to GitHub, Google will auto-revoke it within minutes.
```

### 20.5 — Pico W Wi-Fi Hardcoding

```
In both Pico firmware files (main.py):
  WIFI_SSID = "YOUR_SSID_HERE"     ← replace with your hotspot/router SSID
  WIFI_PASSWORD = "YOUR_PASS_HERE" ← replace with your password

For demo: use your phone's mobile hotspot.
Both Picos and the Android device on the SAME hotspot network.
Your phone IS the hotspot — it's also on that network.
This means Picos can reach your phone's Ollama laptop too IF the laptop is on the same hotspot.

Network topology for demo:
  Phone (hotspot) ← Pico W Helmet (client)
                  ← Pico W Bike (client)
                  ← Laptop (client, runs Ollama)
  Phone app → HTTP → Pico IPs
  Phone app → HTTP → Laptop Ollama IP:11434
  Phone app → HTTPS → Gemini API (through mobile data)
```

### 20.6 — Running Ollama for the Demo

```
On your laptop (must be on same network as phone hotspot):

1. Install Ollama: curl -fsSL https://ollama.ai/install.sh | sh
2. Pull model: ollama pull llama3.2:3b-instruct-q4_K_M
3. Start server: OLLAMA_HOST=0.0.0.0 ollama serve
   (The 0.0.0.0 makes it listen on all interfaces, not just localhost)
4. Check IP: ip addr show (Linux) or ipconfig (Windows) → find your local IP
5. In MARC app Settings → MARC BACK → enter: 192.168.x.x:11434
6. Press TEST CONNECTION → should show "● CONNECTED"

If TEST fails:
  - Check firewall: sudo ufw allow 11434 (Linux)
  - Check same network: both phone and laptop on same hotspot
  - Check OLLAMA_HOST: must be 0.0.0.0, not 127.0.0.1
```

### 20.7 — USB Debug & APK Extraction

```
Enable USB debugging on your Android phone:
  Settings → About Phone → tap "Build Number" 7 times
  Settings → Developer Options → USB Debugging → ON

In Android Studio:
  1. Connect phone via USB
  2. Select your device in the device dropdown (top of Android Studio)
  3. Click ▶ Run (Shift+F10)
  4. App installs and launches on phone automatically

To get the APK after testing:
  Build → Build Bundle(s) / APK(s) → Build APK(s)
  APK saved to: app/build/outputs/apk/debug/app-debug.apk
  Copy this file — it installs on any Android device (allow unknown sources)

To install on another device:
  adb install app-debug.apk
  OR copy APK to device, open with file manager → install
```

---

## PART 21 — DEMO DAY SCRIPT

> You've built it. Now don't fumble the demo because you forgot what to say.
> Practice this sequence. Exactly this sequence.

### 21.1 — Pre-Demo Setup (30 mins before)

```
[ ] Phone hotspot ON
[ ] Laptop connected to phone hotspot
[ ] Ollama running: OLLAMA_HOST=0.0.0.0 ollama serve
[ ] Both Pico Ws powered and connected (check LEDs blinking amber → solid = connected)
[ ] Note the IPs printed by each Pico in their serial output (or find via network scan in Settings)
[ ] Open MARC app → Settings → Connections → SCAN → connect both Picos
[ ] Settings → Calibration → run full calibration sequence
[ ] Settings → AI ENGINE → MARC ONE → verify Gemini API key is saved
[ ] Settings → MARC BACK → verify Ollama IP is saved
[ ] Profile → add blood type, one emergency contact (use your own number for demo)
[ ] ARM RIDE → verify "● ARMED" shows in header
[ ] Say "Hey MARC" → verify it responds
[ ] Bike tab → verify 3D lean angle is live-updating
[ ] Speed page → verify km/h reading (walk around with phone for GPS fix)
```

### 21.2 — Demo Sequence (5 minutes)

```
BEAT 1 — Hardware intro (30 seconds)
"This is MARC — Motorcycle Accident Response Companion. Two Pico Ws,
one on the helmet, one on the bike frame. Watch the app — everything is live."

BEAT 2 — Show dashboard (20 seconds)
Show connected devices. Show MARC ONE status. Show GPS coordinates.
"Both units online. AI engine ready. GPS locked."

BEAT 3 — Bike telemetry (40 seconds)
Go to Bike tab → Lean Angle view.
Slowly tilt the Bike Pico W by hand.
"Real-time MPU6050 data. The 3D model matches the physical angle."
Show coordinates tab. Show speed tab.

BEAT 4 — MARC AI voice (60 seconds)
Say out loud: "Hey MARC"
Wait for orb activation.
Say: "How do I stop severe bleeding from a leg wound?"
Let MARC respond via speaker/headset.
Then: "My bike won't start in the morning. What should I check first?"
Let MARC respond.
"Domain-constrained AI. First aid and bike repair only. Nothing else."

BEAT 5 — Crash detection (60 seconds)
"Now the main feature. Watch."
Go to Settings → temporarily lower the max lean angle threshold to trigger easily.
OR physically tilt the Bike Pico W beyond the calibrated threshold.
CrashAlertActivity launches on phone automatically.
Show the 10-second countdown.
"Crash detected. System is calling my emergency contact and sending SMS with
my GPS coordinates and blood type. I have 10 seconds to cancel."
Say "Hey MARC, cancel" — show it cancels.
"If I was unconscious, that call goes through."

BEAT 6 — Close (30 seconds)
"Gemini online. Llama 3.2 offline fallback. Porcupine wake word. CircuitPython firmware.
Built with a 2011 CBR 250R, two Pico Ws, and a reason."
```

### 21.3 — Questions They'll Ask

```
Q: "What if there's no internet?"
A: Toggle to MARC BACK in Settings. Show Ollama response.
   "Same AI, running locally on a laptop on the same network. Sub-8-second response."

Q: "What about false positives?"
A: "10-second countdown with voice cancellation. User says 'Hey MARC cancel' —
   show it working. Also, calibration sets THEIR specific bike's lean range —
   not a generic threshold."

Q: "What's the GPS accuracy?"
A: "Standard Android GPS, 3-5 meter accuracy. Enough for emergency coordinates.
   The SMS sends raw lat/lng — any first responder can paste it into Maps."

Q: "How does the AI know about your medical data?"
A: "It doesn't. The AI handles first aid questions. The emergency SMS is a separate
   system that reads from SQLite profile — it's template substitution, not AI."

Q: "Could this work on production hardware?"
A: "The Pico W runs this firmware unmodified. The AI switches to Gemini Flash for
   production — free tier handles 1500 queries/day. The app is production-ready
   minus Play Store signing."
```

---

```
MARC_BUILD_GUIDE v1.0 — COMPLETE
=====================================
Total prompts: 27 Cursor prompts
Total files: ~45 Java/XML/Python files
Build time: 17-25 days aggressive

Ranjith Kumar Dasari | DSCE EEE 2026
ranjithdsr2@gmail.com
github.com/RANJITH12022004

Named after Marc Marcuze 93.
Never Give Up.
"Built in the dark. Deployed in the field."
=====================================
```
