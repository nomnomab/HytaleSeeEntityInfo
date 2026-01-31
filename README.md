# See Entity Info Plugin

<img width="1273" height="775" alt="HytaleClient_EozUDWjPpx" src="https://github.com/user-attachments/assets/c3758798-1b5d-44dc-8ec2-9e3fd96dd81b" />

> Until the ui system hytale uses better supports "realtime" guis, the shown world/entity data is not always accurate.
> 
> **Note**: not fully tested in a multi-player environment so usage bugs may occur.

A plugin for Hytale that lets you open a gui to view a list of entities per world.

---

## Features

### Entity List

- Open with `/entityinfo` or with the Debug Stick.
- Can see a list of entities per world.
- Can select an entity to view basic information:
  - Name
  - Component list
  - Last position
  - Last rotation
  - Model path (if available)
  - Model asset id (if available)
  - UUID

### Actions

- Tp to the selected world.
- Tp to a player.
- Tp to a teleporter.
- Tp to an entity.
- Tp an entity to you.
- Kill an entity.

### Debug Stick

- Smack an entity to open it in the gui.
- Sneak click to open the gui.

## Building

### Prerequisites

- **Java 25 JDK** - [Download here](https://www.oracle.com/java/technologies/downloads/)
- **IntelliJ IDEA** - [Download here](https://www.jetbrains.com/idea/download/) (Community Edition is fine)
- **Git** - [Download here](https://git-scm.com/)

### 1. Clone or Download

```bash
git clone https://github.com/nomnomab/HytaleSeeEntityInfo.git
cd HytaleSeeEntityInfo
```

### 2. Build

```bash
# Windows
gradlew.bat shadowJar

# Linux/Mac
./gradlew shadowJar
```

Your plugin JAR will be in: `build/libs/HytaleSeeEntityInfo-X.X.X.jar`

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request
