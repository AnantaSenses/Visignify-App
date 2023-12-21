<h1 align="center">Visignify App</h1> <br>
<p align="center">
  <a href="https://github.com/AnantaSenses/Visignify-App/blob/main/Banner%20Visignify.png">
    <img alt="Visignify" title="Visignify" src="https://github.com/AnantaSenses/Visignify-App/blob/main/Banner%20Visignify.png" width="500">
  </a>
</p>

# Visignify

## <a name="introduction"></a> Introduction :
Visignify is a unique, user-centric platform that offers a transformative solution by revolutionizing how individuals with disabilities communicate, access information, and engage with the world. This app includes features such as object, sign detection using machine learning, OCR for text-to-speech functionality, voice output for location, battery, weather information, learn feature, and a "Deaf Note" feature.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Guide to use the App](#guide)
- [Libraries](#libraries)
- [Project Structure](#project-structures)

## <a name="features"></a> Features :
* Blind Features :
  - <b>Object Detection</b>
  - <b>OCR Reader</b>
  - <b>Battery Information</b>
  - <b>Weather Information</b>
  - <b>Calculator</b>
  - <b>Location Information</b>

* Deaf Features :
  - <b>Sign Detection</b>
  - <b>Deaf Note</b>
  - <b>Learn Sign</b>
  - <b>Weather Information</b>

## <a name="guide"></a> How to Use This App :

### 1. Onboarding Screen
![Onboarding Screen](https://github.com/AnantaSenses/Visignify-App/assets/92041571/f32427bc-83ef-47d8-bb39-c5b0cbe36957)
- Deaf users can directly access the Deaf feature by pressing the "Deaf" button on the screen.
- Mute users can input voice commands to the desired feature by swiping left. Swipe right to enter the Blind feature.
  
### 2. Choose Screen
![Choose Screen](https://github.com/AnantaSenses/Visignify-App/assets/92041571/93abfe03-1e4c-4f4c-957a-b4d85c8023cc)
- Blind users can input voice commands by swiping left if familiar with Blind features. Swipe right to access the Home Blind screen for instructions on available features.
### 3. Blind Features
![home Blind](https://github.com/AnantaSenses/Visignify-App/assets/92041571/5edbfb00-8910-471f-b4d0-4d4de7d5f25e)
On the Home Blind screen, users can listen to explanations of various features and interact with them using voice commands or screen gestures. Blind users can input voice commands by swiping left if familiar with Blind features. Swipe right to access the Home Blind screen for instructions on available features.

- **Object Detection**
  ![Object Detection](https://github.com/AnantaSenses/Visignify-App/assets/92041571/ea94e45e-8936-4beb-b709-80b9c52d1097)
  - Say "object" to enter this feature and immediately detect objects.
  - When finished, swipe right to return to the Home Blind screen.

- **OCR Reader**
  ![OCR Reader](https://github.com/AnantaSenses/Visignify-App/assets/92041571/e89dc7bf-9508-41e1-af01-b58e3273cedf)
  - Say "read" to enter this feature.
  - Swipe left to input voice and say "yes" to confirm and reading and say "no" to return to the Home Blind screen.

- **Battery Information**
  - Say "battery" to enter this feature and listen to the current battery status.
  - Swipe right to replay the information.
  - Swipe left to return to the Home Blind screen.

- **Weather Information**
  - Say "weather" to enter this feature.
  - Tap the screen to input voice the desired city for weather information.
  - Tap again and say "exit" to return to the Home Blind screen.

- **Calculator**
  - Say "calculator" to enter this feature.
  - Tap the screen to input voice a calculation (e.g., "one plus one").
  - Tap again and say "exit" to return to the Home Blind screen.

- **Location Information**
  - Say "location" to enter this feature and immediately listen to the current location information.
  - Swipe right to replay the information.
  - Swipe left to return to the Home Blind screen.

### 4. Home Deaf Feature
![home deaf](https://github.com/AnantaSenses/Visignify-App/assets/92041571/82cab8df-2fd4-441a-945c-4d9d0ee73bcf)
- On the Home Deaf screen, users can find:
  - **Sign Detection**
  - **Deaf Note**
  - **Learn Sign**
  - **Weather Information**
  - **Settings**

## <a name="libraries"></a> Libraries :
  - [Lifecycle & Livedata](https://developer.android.com/jetpack/androidx/releases/lifecycle)
  - [Kotlin Flow](https://developer.android.com/kotlin/flow)
  - [Navigation Component](https://developer.android.com/jetpack/androidx/releases/navigation)
  - [Kotlin Coroutines](https://developer.android.com/kotlin/coroutines)
  - [Room Database](https://developer.android.com/jetpack/androidx/releases/room)

## <a name="project-structures"></a> Project Structure :
* `adapter`
* `data`
* `db`
* `helper`
* `model`
* `ObjectDetection`
  - `fragment`
  - `model`
  - `utils`
* `repository`
* `SignDetection`
  - `fragment`
  - `model`
  - `utils`
* `ui`
  - `fragments`
* `viewmodel`
