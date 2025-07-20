# OfflineLandAreaApp

Offline Android app to estimate land area by walking around the perimeter **without GPS or internet**. 
It uses step counting + direction (sensor-based) to approximate the area in multiple units like square meters, square feet, guntha, acres, and hectares.

---

## Features
- **Offline:** Works with no GPS or internet.
- **Simple UI:** Start/Stop buttons to begin and end tracking.
- **Area Calculation:** Displays results in Sq. Meters, Sq. Feet, Guntha, Acres, Hectares.
- **No logs or personal data saved.**

---

## Build & Get APK (GitHub Actions)

This repository is pre-configured with **GitHub Actions** to automatically build the APK.

### Steps:

1. **Create a new GitHub repository**:
   - Go to [https://github.com/new](https://github.com/new).
   - Name it **OfflineLandAreaApp** (or any name you like).
   - Click **Create repository**.

2. **Push this code to GitHub**:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/USERNAME/OfflineLandAreaApp.git
   git push -u origin main
   ```

3. **Wait for GitHub Actions**:
   - Open the **Actions** tab in your repository.
   - The **Android CI** workflow will run automatically.
   - Once done, download the APK from **Actions → Last workflow run → Artifacts → OfflineLandAreaApp-APK**.

---

## How to Use the App
1. Install the generated APK on your phone.
2. Enter your stride length (meters) – default is **0.75m**.
3. Press **Start** and walk the boundary of the land (one loop).
4. Press **Stop** – it will calculate and show area.

---

## Notes & Limitations
- Accuracy depends on your stride length and consistent walking.
- Dead-reckoning method – expect some drift.
- Best for rough estimates only (not legal measurement).

---

## License
Free to use for personal projects. No warranty.
