<div align="center">

  <a href="https://github.com/advanced-progamming-sut-2026/phase-0-null-ussr-pointer">
    <img src="/banner.png" alt="Null USSR Pointer Banner" width="100%">
  </a>

  <br>

  <h1>🌻 PVZ 2: NULL USSR POINTER 🧠</h1>

  <p>
    <b>We Plant. They Zombie. We Code.</b>
    <br>
    <i>A Java-based Plants vs. Zombies 2 clone developed for the Advanced Programming course.</i>
  </p>

  <br>

  <p>
    <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25">
    <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle">
    <img src="https://img.shields.io/badge/Architecture-MVC-4B275F?style=for-the-badge" alt="MVC Architecture">
  </p>

  <br>

</div>

> *"There's a zombie on your lawn... and a null pointer in your stack trace."*

---

## 🧟‍♂️ About the Project

Welcome to **NULL USSR POINTER**, the official repository of **Group 4's** *Plants vs. Zombies 2* clone.

This project is a ground-up recreation of the core mechanics of *Plants vs. Zombies 2*, implemented entirely in **Java** as part of the **Advanced Programming** course.

Our goal was not only to recreate the gameplay experience, but also to build a clean, maintainable, and extensible software architecture.

The project follows the **MVC (Model-View-Controller)** architectural pattern and makes use of several established **Design Patterns**, including **Factory**, **Strategy**, and **Builder**, where appropriate.

The result is a codebase designed to keep the gameplay logic organized, the components reusable, and the inevitable army of zombies under control.

---

## 🧠 The Brains Behind the Operation

Every zombie apocalypse needs people who can write code while everything else is on fire.

<table align="center">
  <tr>
    <th>Avatar</th>
    <th>Operative</th>
    <th>ID</th>
  </tr>
  <tr>
    <td align="center">🎯</td>
    <td><b>Reza Hazrati</b></td>
    <td><code>404105767</code></td>
  </tr>
  <tr>
    <td align="center">💻</td>
    <td><b>Sepehr Gholami</b></td>
    <td><code>404106152</code></td>
  </tr>
  <tr>
    <td align="center">🧠</td>
    <td><b>Sepehr Asadinejad</b></td>
    <td><code>404101419</code></td>
  </tr>
</table>

---

## 🪴 Key Features & Mechanics

### ⚔️ Grid-Based Combat

Accurate grid-based gameplay with collision detection, lane management, plant placement, and zombie movement.


### ☀️ Sun Economy

A dynamic resource system for generating, collecting, and spending Sun to deploy plants.


### 🧟 Horde AI

Zombies feature different movement speeds, health values, behaviors, and special abilities.


### 🏗️ Clean Architecture

The codebase follows MVC principles and utilizes appropriate design patterns to improve scalability, readability, and maintainability.

## 🏛️ Architecture

The project is structured around the **MVC architectural pattern**:

<div align="center">

|       Layer       | Responsibility                                                                |
| :---------------: | :---------------------------------------------------------------------------- |
|    🧠 **Model**   | Contains the game state, entities, mechanics, and core gameplay logic.        |
|    🖥️ **View**   | Responsible for presenting the current state of the game to the player.       |
| 🎮 **Controller** | Handles player input and coordinates interactions between the Model and View. |

</div>

The project also incorporates several design patterns where they provide a meaningful architectural benefit:

* 🏭 **Factory Pattern** — Used for creating different types of game entities.
* 🎯 **Strategy Pattern** — Used to encapsulate interchangeable behaviors.
* 🧱 **Builder Pattern** — Used where complex object construction benefits from a structured approach.

---

## ⚙️ Getting Started

The following instructions explain how to download, configure, and run the project locally.

### 1. Clone the Repository

Clone the public repository using Git:

```bash
git clone https://github.com/advanced-progamming-sut-2026/phase-0-null-ussr-pointer.git
```

Then navigate to the project directory:

```bash
cd phase-0-null-ussr-pointer
```

---

### 2. Download the Required Assets

The game assets are distributed separately from the source code.

Download the asset package from the following Google Drive folder:

**[📦 Download PVZ Assets](https://drive.google.com/drive/folders/11r0Wh6EqS6-KpSNM58sEJeUzI-jC-uUw?usp=drive_link)**

After downloading and extracting the package, locate the folder named:

```text
pvz-assets
```

The folder should contain the game's image atlases and other required graphical assets.

---

### 3. Place the Assets in the Project

Move the entire `pvz-assets` folder into the **root directory of the project**.

The final directory structure should look approximately like this:

```text
phase-0-null-ussr-pointer/
│
├── pvz-assets/
│   ├── ...
│   ├── ...
│   └── ...
│
├── src/
├── build.gradle
├── gradlew
├── gradlew.bat
└── ...
```

> **Important:** The `pvz-assets` directory must be located directly in the project's root directory. The application expects the assets at this location.

---

### 4. Start the Server

Start the game server using the Gradle wrapper:

#### Linux / macOS

```bash
./gradlew server.run
```

#### Windows

Double-click `run-server.bat` in the project root, or run manually:

```bat
gradlew.bat server.run
```

The server must be running before launching the clients.

---

### 5. Launch the Client

Once the server is running, launch a client instance:

#### Linux / macOS

```bash
./gradlew run
```

#### Windows

Double-click `run-client.bat` in the project root, or run manually:

```bat
gradlew.bat run
```

> On Windows, you can also double-click `play.bat` to start the server and a client together in one step.

Multiple client instances can be launched simultaneously if required.

---

## 🚀 Running Multiple Clients

The client and server architecture allows multiple client instances to connect to the running server.

Simply execute the client command again in another terminal:

```bash
./gradlew run
```

or, on Windows:

```bat
gradlew.bat run
```

Repeat the command as needed to launch additional clients.

---

<div align="center">

  <br>

<h2>🌻 Defend the Lawn. Destroy the Zombies. Avoid the NullPointerException.</h2>

  <p>
    <i>Built with Java, Gradle, design patterns, questionable amounts of caffeine, and an unreasonable number of zombies.</i>
  </p>

  <br>

  <img src="https://img.shields.io/badge/Made%20with-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/For-Advanced%20Programming-4B275F?style=for-the-badge">
  <img src="https://img.shields.io/badge/Status-Ready%20for%20Battle-2EA44F?style=for-the-badge">

</div>