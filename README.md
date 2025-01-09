# Vim Teacher

An Android application designed to help users learn Vim commands through an interactive question-based learning system.

## Features

- Interactive question sets to learn Vim commands
- Progress tracking with color-coded questions (green for solved, yellow for unsolved)
- Cheat sheet for quick reference
- Leaderboard system to track user progress
- Firebase integration for user authentication and progress tracking
- Material Design UI with custom styling
- RecyclerViews with animations

## Technology Stack

- Kotlin
- Android SDK
- Firebase Authentication
- Cloud Firestore
- Navigation Component
- ViewBinding
- MVVM Architecture
- RecyclerView with ListAdapter
- Material Design Components

## Getting Started

* **Clone the Repository**
  Open a terminal or command prompt, navigate to your desired directory, and run:

  git clone https://github.com/atadagg/vim-teacher
* **Open the Project in Android Studio**

  * Launch Android Studio.
  * Click on  **File > Open** , and navigate to the folder where you cloned the repository.
  * Select the project and click  **OK** .
* **Build the Project**

  * Ensure that you have the required Android SDK and dependencies installed.
  * Sync the project with Gradle files by clicking on **Sync Now** if prompted.
* **Run the Project**

  * Connect an Android device via USB or set up an emulator in Android Studio.
  * Click the **Run** button (green triangle) in the toolbar or use the shortcut `Shift + F10`.
  * Select your device or emulator from the list, and the app will be built and deployed.

## Project Structure

- `adapter/` - RecyclerView adapters
- `model/` - Data models
- `repositories/` - Firebase Repository
- `viewmodel/` - ViewModels for MVVM architecture
- `fragments/` - UI fragments

## Screenshots

![1736455500289](image/README/1736455500289.png)
![1736455375658](image/README/1736455375658.png)
![1736455465626](image/README/1736455465626.png)

### Firestore Collections:

- `users` - Tracks users (email: String, questions_solved:Int)
- `questions` - Tracks questions (provided in the photos below)
- `userQuestions` - Tracks solved questions (questionId: Int, userId: String)

## Contributing

Feel free to submit issues and pull requests.
