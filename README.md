# 🇲🇦 LLM-Powered Moroccan Darija Translator

An enterprise, multi-client translation platform that translates English (and other natural languages) into authentic **Moroccan Arabic Dialect (Darija)** using **Google Gemini LLM** and a centralized **Jakarta RESTful Web Service**.

---

## 📋 Table of Contents
1. [Project Overview](#1-project-overview)
2. [Technologies Used & Compatibility](#2-technologies-used--compatibility)
3. [System Architecture](#3-system-architecture)
4. [Project Directory Structure](#4-project-directory-structure)
5. [Prerequisites](#5-prerequisites)
6. [API Key & Environment Configuration](#6-api-key--environment-configuration)
7. [Building & Running the Backend](#7-building--running-the-backend)
8. [Authentication Configuration](#8-authentication-configuration)
9. [REST API Testing (Postman)](#9-rest-api-testing-postman)
10. [Client Applications Setup & Execution](#10-client-applications-setup--execution)
    - [Chrome Extension (Manifest V3)](#101-chrome-extension-manifest-v3)
    - [PHP Web Client](#102-php-web-client)
    - [Python Desktop Client (Tkinter)](#103-python-desktop-client-tkinter)
    - [React Native Mobile App (Expo)](#104-react-native-mobile-app-expo)
11. [UML Diagrams](#11-uml-diagrams)
12. [Common Errors & Solutions](#12-common-errors--solutions)

---

## 1. Project Overview

Moroccan Arabic (Darija) possesses distinct vocabulary, phonetic characteristics, and colloquial expressions that differ markedly from Modern Standard Arabic (MSA). Standard rule-based machine translation tools frequently fail to produce natural Darija phrases.

This system leverages **Large Language Models (Google Gemini)** fine-tuned via prompt engineering to translate natural language input into authentic everyday Moroccan Darija.

### Core Translation Example
- **Input**: `"Hello, how are you?"`
- **Output**: `"سلام، كيداير؟"`

---

## 2. Technologies Used & Compatibility

| Component | Technology | Version | Purpose / Compatibility |
| :--- | :--- | :--- | :--- |
| **Backend Language** | Java SE | 17 / 21 LTS | Compatible with Jakarta EE 10 |
| **Backend Framework** | Jakarta EE / JAX-RS | 10.0 (JAX-RS 3.1) | Standardized enterprise RESTful APIs (`jakarta.*`) |
| **App Server** | Payara Micro / WildFly | 6.2023+ / 30+ | Jakarta EE runtime with built-in JSON-B & CDI |
| **JSON Processing** | Jakarta JSON-B / Eclipse Yasson | 3.0 | Automatic JSON serialization/deserialization |
| **AI LLM** | Google Gemini API | `v1beta` (`gemini-1.5-flash`) | Fast, accurate Moroccan Darija generative translation |
| **Browser Extension** | Chrome Manifest V3 | MV3 | SidePanel API, Context Menus, Speech APIs, Storage |
| **Web Client** | PHP | 8.0+ / 8.1+ / 8.2+ | Lightweight web UI with `ext-curl` and `ext-json` |
| **Desktop Client** | Python | 3.9+ / 3.10+ / 3.11+ | Native desktop GUI using `tkinter` and `requests` |
| **Mobile Client** | React Native / Expo | SDK 50+ | Cross-platform mobile app with Speech & Clipboard |
| **Documentation** | PlantUML | 1.2023+ | UML Class, Deployment, Use Case, Sequence diagrams |

---

## 3. System Architecture

The architecture follows a decoupled **Central REST Web Service / Multi-Client Tier** model:

```
[ Chrome Extension ]   [ PHP Web App ]   [ Python Desktop App ]   [ React Native Mobile App ]
         │                    │                     │                         │
         └────────────────────┼─────────────────────┼─────────────────────────┘
                              │ HTTP Basic Auth & JSON
                              ▼
        ┌─────────────────────────────────────────────────────────┐
        │  Jakarta RESTful Web Service (POST /api/translator/...) │
        │  ├── BasicAuthFilter (Security Verification)            │
        │  ├── TranslatorResource (JAX-RS Endpoint)               │
        │  └── GeminiTranslationService (LLM Client)              │
        └──────────────────────────┬──────────────────────────────┘
                                   │ HTTPS / JSON
                                   ▼
                   ┌───────────────────────────────┐
                   │    Google Gemini LLM API      │
                   └───────────────────────────────┘
```

---

## 4. Project Directory Structure

```
DarijaTranslator/
│
├── backend/                                # Jakarta EE Maven RESTful API
│   ├── pom.xml                             # Dependencies & Payara Micro plugin
│   ├── src/main/java/com/translator/
│   │   ├── config/                         # Application & CORS configuration
│   │   ├── model/                          # DTOs (Request, Response, Error)
│   │   ├── resource/                       # TranslatorResource REST endpoint
│   │   ├── security/                       # BasicAuthFilter (Jakarta Security)
│   │   └── service/                        # GeminiTranslationService (AI Integration)
│   └── src/test/java/com/translator/       # Unit & Mock tests
│
├── chrome-extension/                       # Chrome Extension Manifest V3
│   ├── manifest.json                       # Extension manifest
│   ├── background.js                       # Service worker & context menu
│   ├── sidepanel.html                      # Side panel UI
│   ├── sidepanel.css                       # Responsive styling
│   └── sidepanel.js                        # Speech recognition, TTS, and API calls
│
├── php-client/                             # PHP Web Client
│   ├── config.php                          # Endpoint and credentials config
│   ├── translate.php                       # PHP cURL POST proxy
│   ├── index.php                           # Responsive HTML UI
│   └── style.css                           # Modern styling
│
├── python-client/                          # Python Desktop Client
│   ├── config.py                           # Endpoint and credentials config
│   ├── api_client.py                       # Requests library HTTP client wrapper
│   ├── main.py                             # Tkinter GUI application
│   └── requirements.txt                    # Python dependencies
│
├── mobile-client/                          # React Native Mobile Client
│   ├── app.json                            # Expo configuration
│   ├── package.json                        # Mobile dependencies
│   ├── App.js                              # Main React Native component
│   └── src/
│       ├── config/apiConfig.js             # Mobile network endpoint config
│       ├── services/translationService.js  # Mobile API client
│       ├── components/                     # Header, Input, Result, History UI
│       └── styles/theme.js                 # Theme palette & spacing
│
├── documentation/                          # Architectural Documentation & Testing
│   ├── class-diagram.puml                  # PlantUML Class Diagram
│   ├── deployment-diagram.puml             # PlantUML Deployment Diagram
│   ├── use-case-diagram.puml               # PlantUML Use Case Diagram
│   ├── sequence-diagram.puml               # PlantUML Sequence Diagram
│   └── postman_collection.json             # Postman test collection
│
└── README.md                               # Complete system documentation
```

---

## 5. Prerequisites

Before running the project, ensure the following are installed:
- **Java Development Kit (JDK)**: Version 17 or 21 (LTS)
- **Apache Maven**: Version 3.8+
- **Google Chrome Browser**: Version 114+ (for Side Panel API support)
- **PHP**: Version 8.0+ with cURL extension enabled
- **Python**: Version 3.9+ with `pip`
- **Node.js & npm**: Node 18+ (for React Native / Expo)

---

## 6. API Key & Environment Configuration

### 6.1 Obtaining a Google Gemini API Key
1. Visit the [Google AI Studio](https://aistudio.google.com/).
2. Sign in with your Google account.
3. Click **Get API key** and generate a new key.

### 6.2 Setting Environment Variables

#### On Linux / macOS / Git Bash:
```bash
export GEMINI_API_KEY="your_actual_gemini_api_key_here"
export GEMINI_MODEL="gemini-1.5-flash"
export API_USERNAME="user"
export API_PASSWORD="darija123"
```

#### On Windows (PowerShell):
```powershell
$env:GEMINI_API_KEY="your_actual_gemini_api_key_here"
$env:GEMINI_MODEL="gemini-1.5-flash"
$env:API_USERNAME="user"
$env:API_PASSWORD="darija123"
```

#### On Windows (CMD):
```cmd
set GEMINI_API_KEY=your_actual_gemini_api_key_here
set GEMINI_MODEL=gemini-1.5-flash
set API_USERNAME=user
set API_PASSWORD=darija123
```

---

## 7. Building & Running the Backend

Navigate to the `backend` directory:
```bash
cd backend
```

### 7.1 Build and Run Tests
```bash
mvn clean test
```

### 7.2 Run with Payara Micro (Embedded)
```bash
mvn clean package payara-micro:start
```
The RESTful Web Service will start at:
`http://localhost:8080/backend/api/translator/translate`

---

## 8. Authentication Configuration

The REST API is secured with **HTTP Basic Authentication**:
- **Default Username**: `user` (or configured via `API_USERNAME`)
- **Default Password**: `darija123` (or configured via `API_PASSWORD`)

Every request to `/api/translator/translate` must include the header:
```http
Authorization: Basic dXNlcjpkYXJpamExMjM=
```
*(Base64 representation of `user:darija123`)*

Requests with missing or invalid credentials receive `401 Unauthorized` and `WWW-Authenticate: Basic realm="DarijaTranslatorRealm"`.

---

## 9. REST API Testing (Postman)

Import `documentation/postman_collection.json` into Postman or Thunder Client.

### Test Cases Included:

1. **Successful Translation (`200 OK`)**:
   - **Method**: `POST`
   - **URL**: `http://localhost:8080/backend/api/translator/translate`
   - **Auth**: Basic Auth (`user` / `darija123`)
   - **Body**: `{"text": "Hello, how are you?"}`
   - **Expected Status**: `200 OK`
   - **Response**:
     ```json
     {
       "originalText": "Hello, how are you?",
       "translatedText": "سلام، كيداير؟",
       "sourceLanguage": "English",
       "targetLanguage": "Moroccan Darija"
     }
     ```

2. **Empty Text (`400 Bad Request`)**:
   - **Body**: `{"text": "   "}`
   - **Expected Status**: `400 Bad Request`

3. **Missing Authentication (`401 Unauthorized`)**:
   - Request without `Authorization` header.
   - **Expected Status**: `401 Unauthorized`

4. **Invalid Credentials (`401 Unauthorized`)**:
   - Auth: Basic Auth with wrong credentials.
   - **Expected Status**: `401 Unauthorized`

5. **Invalid JSON (`400 Bad Request`)**:
   - Malformed JSON payload.
   - **Expected Status**: `400 Bad Request`

---

## 10. Client Applications Setup & Execution

### 10.1 Chrome Extension (Manifest V3)
1. Open Google Chrome and navigate to `chrome://extensions/`.
2. Enable **Developer mode** (toggle in the top right).
3. Click **Load unpacked** and select the `chrome-extension/` directory.
4. Pin the extension icon in Chrome.
5. **Usage**:
   - **Side Panel**: Click the extension icon to open the Side Panel.
   - **Context Menu**: Select any text on any webpage, right-click, and select **"Translate to Darija"**.
   - **Speech**: Click 🎤 for voice input or 🔊 to hear the translation read aloud.
   - **Settings**: Click ⚙️ to update backend URL or credentials.

### 10.2 PHP Web Client
1. Ensure PHP is installed.
2. Navigate to the `php-client` directory:
   ```bash
   cd php-client
   ```
3. Start the built-in PHP development server:
   ```bash
   php -S localhost:8000
   ```
4. Open your browser at `http://localhost:8000`.

### 10.3 Python Desktop Client (Tkinter)
1. Navigate to the `python-client` directory:
   ```bash
   cd python-client
   ```
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Run the desktop application:
   ```bash
   python main.py
   ```

### 10.4 React Native Mobile App (Expo)
1. Navigate to the `mobile-client` directory:
   ```bash
   cd mobile-client
   ```
2. Install Node dependencies:
   ```bash
   npm install
   ```
3. Start the Expo development server:
   ```bash
   npx expo start
   ```
4. **Connecting Physical Device / Emulator**:
   - Click the ⚙️ settings icon inside the mobile app.
   - Change the API URL from `localhost` to your computer's local Wi-Fi IP address (e.g., `http://192.168.1.150:8080/backend/api/translator/translate`).
   - Scan the QR code using the **Expo Go** app on Android/iOS.

---

## 11. UML Diagrams

The `documentation/` folder contains PlantUML diagram source files:
- `class-diagram.puml`: Backend class architecture and relationships.
- `deployment-diagram.puml`: Physical deployment across Client Tier, Backend Server, and Google Cloud.
- `use-case-diagram.puml`: Use case modeling user interactions and system capabilities.
- `sequence-diagram.puml`: End-to-end translation and authentication message flow.

### Viewing UML Diagrams:
- Use IntelliJ IDEA / Eclipse PlantUML plugin, or
- Paste file contents into the online editor at [www.plantuml.com/plantuml](http://www.plantuml.com/plantuml).

---

## 12. Common Errors & Solutions

| Error | Cause | Solution |
| :--- | :--- | :--- |
| `HTTP 401 Unauthorized` | Missing or incorrect Basic Auth credentials. | Ensure credentials match `API_USERNAME` / `API_PASSWORD` (default: `user` / `darija123`). |
| `Gemini API key is not configured` | Environment variable `GEMINI_API_KEY` is not set. | Set `GEMINI_API_KEY` in your terminal or OS environment before starting Payara/WildFly. |
| `Connection refused (Mobile client)` | Mobile app trying to connect to `localhost`. | Set the API URL to your computer's LAN IP (e.g. `192.168.x.x`) instead of `localhost`. |
| `CORS error in browser` | Cross-origin request blocked. | `CorsFilter` is included in the backend; ensure Payara server is running. |
| `Speech synthesis not working` | Browser/device missing Arabic voice pack. | Install an Arabic voice pack in your OS language settings. |
