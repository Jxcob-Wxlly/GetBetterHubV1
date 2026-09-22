# GetBetterHub

> **A modern Android application built to help users work towards becoming a better version of themselves.**

GetBetterHub is an Android application developed using **Kotlin and Jetpack Compose**, with a focus on modern Android development practices, local data persistence, API integration, authentication, and accessibility through multilingual support.

The application is designed around a combination of online and offline functionality, allowing locally cached information to remain available when an internet connection is unavailable.

---

## Overview

GetBetterHub is a personal-development focused Android application that brings together user-oriented functionality within a modern mobile interface.

The application uses a hybrid data approach:

```text
                    ┌──────────────────┐
                    │   GetBetterHub   │
                    │   Android App    │
                    └────────┬─────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
        ┌───────▼───────┐       ┌─────────▼─────────┐
        │   Local Data  │       │    Remote API     │
        │     Room      │       │ Retrofit / Gson   │
        └───────┬───────┘       └─────────┬─────────┘
                │                         │
                └────────────┬────────────┘
                             │
                     ┌───────▼───────┐
                     │  Compose UI   │
                     │  Material 3   │
                     └───────────────┘
```

This approach allows the application to work with locally persisted information while also communicating with a remote ASP.NET Core API.

---

## Key Features & Capabilities

### Modern Android UI

* Built with **Jetpack Compose**
* Material 3 design components
* Compose Navigation for screen-to-screen navigation
* Extended Material icons
* Responsive Android interface

### Offline Data Support

GetBetterHub incorporates **Room Database** for local data persistence.

This provides the foundation for:

* Local caching
* Offline access
* Persistent application data
* Synchronisation between local and remote data

### API Integration

The application communicates with a remote backend using:

* **Retrofit**
* **Gson**
* **OkHttp**
* Logging interceptors

The backend integration is designed around an **ASP.NET Core API**.

### Authentication

The application includes support for **Google Sign-In / SSO**, providing users with an alternative authentication method through their Google account.

### Multilingual Support

GetBetterHub provides language resources for:

* 🇬🇧 English
* 🇿🇦 isiZulu
* 🇿🇦 Afrikaans

The application is configured to select language resources according to the device locale, with support for changing the language through the application.

### Notifications

The application requests notification permissions where required by the Android platform.

### Audio Support

The application includes Android microphone access through the `RECORD_AUDIO` permission, supporting functionality that requires audio input.

---

## Technology Stack

| Technology             | Purpose                                |
| ---------------------- | -------------------------------------- |
| **Kotlin**             | Primary programming language           |
| **Android SDK**        | Mobile application platform            |
| **Jetpack Compose**    | Declarative UI framework               |
| **Material 3**         | UI components and design system        |
| **Navigation Compose** | Application navigation                 |
| **Room**               | Local database and offline persistence |
| **Retrofit**           | REST API communication                 |
| **Gson**               | JSON serialization/deserialization     |
| **OkHttp**             | HTTP client and network logging        |
| **Google Sign-In**     | Authentication / SSO                   |
| **Gradle Kotlin DSL**  | Build configuration                    |
| **JUnit**              | Unit testing                           |
| **AndroidX Testing**   | Android instrumentation/UI testing     |

---

## Technical Architecture

GetBetterHub follows a modern Android architecture centred around separation between the user interface, local persistence, and remote services.



## Android Configuration

| Configuration    | Value                  |
| ---------------- | ---------------------- |
| Application ID   | `com.getbetterhub.app` |
| Minimum SDK      | 26                     |
| Target SDK       | 34                     |
| Compile SDK      | 34                     |
| Version          | 1.0                    |
| Primary Language | Kotlin                 |
| UI Framework     | Jetpack Compose        |

The project also limits packaged language resources to English, isiZulu, and Afrikaans.

---

## Getting Started

### Prerequisites

Before building GetBetterHub, install:

* [Android Studio](https://developer.android.com/studio)
* Android SDK
* A compatible JDK
* An Android emulator or physical Android device

### Clone the Repository

```bash
git clone https://github.com/Jxcob-Wxlly/GetBetterHubV1.git
```

Navigate into the project:

```bash
cd GetBetterHubV1
```

### Open in Android Studio

1. Open Android Studio.
2. Select **Open**.
3. Select the cloned `GetBetterHubV1` directory.
4. Allow Gradle to synchronise.
5. Connect an Android device or launch an emulator.
6. Run the `app` configuration.

---

## Build Configuration

The application uses **Gradle Kotlin DSL** for build configuration.

The Android module is configured with:

* Kotlin Android support
* Kotlin annotation processing
* Jetpack Compose
* Room
* Retrofit
* OkHttp
* Google authentication
* AndroidX testing libraries

The current release configuration has code shrinking disabled, making the project suitable for development and academic demonstration.

---

## Testing

Testing dependencies are included for both unit and Android instrumentation testing.

### Unit Testing

```text
JUnit 4.13.2
```

### Android Testing

```text
AndroidX Test JUnit
Espresso
Compose UI Testing
```

This provides a foundation for testing application logic, Android components, and Compose-based interfaces.

---

## Backend Communication

GetBetterHub is designed to communicate with an **ASP.NET Core API** through Retrofit.

```text
Android Application
       │
       │ HTTP Requests
       ▼
    Retrofit
       │
       ▼
      Gson
       │
       ▼
   ASP.NET Core API
```

OkHttp's logging interceptor is included to assist with monitoring and debugging network requests during development.

---


## Localization

GetBetterHub is configured with three supported languages:

```text
English
isiZulu
Afrikaans
```

Language resources are separated using Android's resource-qualifier system.

This allows the application to provide localized content based on the user's device language while also supporting an in-app language selection mechanism.

---

## Permissions

The application currently declares permissions for:

| Permission           | Purpose                            |
| -------------------- | ---------------------------------- |
| `INTERNET`           | Communication with remote services |
| `RECORD_AUDIO`       | Audio/microphone functionality     |
| `POST_NOTIFICATIONS` | Application notifications          |

Permissions are declared in the Android application manifest.

---

## 📸 Screenshots
### Home Screen



### Authentication



### Main Application Interface



### Additional Features


---

## Project Goals

GetBetterHub was developed as a practical Android application demonstrating the integration of multiple technologies within a single mobile project.

The project provides experience with:

* Android application development
* Kotlin
* Jetpack Compose
* Modern UI development
* REST API integration
* Local database persistence
* Offline data handling
* Authentication
* Localization
* Android permissions
* Automated testing foundations
* Gradle-based project configuration

---

## Potential Future Improvements

Potential future development could include:

* Expanded automated test coverage
* Improved offline synchronisation strategies
* Enhanced error handling and network recovery
* Additional language support
* Production release configuration
* CI/CD integration
* Improved application analytics
* Additional accessibility features
* Further backend optimisation

---

## Author

**Jacob Wally**

Software Development Student

GitHub: [@Jxcob-Wxlly](https://github.com/Jxcob-Wxlly)

---

## Project Status

**Version:** 1.0
**Platform:** Android
**Status:** Development / Academic Project

---
