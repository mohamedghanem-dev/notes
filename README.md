# Notes

A private, offline notes app for Android — built with Kotlin and Jetpack Compose.

## Features

- Rich note list with search, pin, favorite, folders, tags, and trash
- Per-note background colors
- Optional app-wide password lock
- Grid or list view
- Arabic / English UI
- Share a note as plain text
- Fully offline — notes are stored locally in a Room/SQLite database

## Build

This project has no local Gradle wrapper — builds run through GitHub Actions.

1. Push to `main` (or run the workflow manually from the **Actions** tab).
2. The **Build Android APK** workflow builds a debug APK automatically.
3. Download it from the workflow run's **Artifacts** section.

For a signed release build, add these repository secrets first, then run the
workflow manually (`workflow_dispatch`):

- `RELEASE_KEYSTORE_BASE64` — your `.jks` keystore, base64-encoded
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_PASSWORD`

## Run locally (optional)

Requires [Android Studio](https://developer.android.com/studio). Open the
project folder directly — Android Studio will generate its own local Gradle
wrapper automatically on first sync.
