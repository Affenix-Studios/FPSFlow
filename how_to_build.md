# How to Build FPSFlow

This project supports a matrix build pipeline to generate separate artifacts for different Minecraft generations and Java versions. 

To prevent PowerShell script-chaining issues, run the following commands individually.

## 1. Clean the previous builds (Optional but Recommended)
Before building new versions, it is recommended to clear out the old build directory:
```powershell
gradle clean
```

## 2. Build for Minecraft 1.21.11 (Java 21)
This command compiles the mod using **Java 21** and **Yarn Mappings**:
```powershell
gradle buildJava21
```
*Output artifact (expected):* `build/libs/fpsflow-<version>-mc1.21.11-java21.jar`

## 3. Build for Minecraft 26.1.2+ (Java 25)
This command compiles the mod using **Java 25** and official **Mojang Mappings**:
```powershell
gradle buildJava25
```
*Output artifact:* `build/libs/fpsflow-<version>-mc26.1.2-java25.jar`

---
*Note: Make sure your IDE (like VS Code or IntelliJ) is not locking the `.gradle` or loom caches during compilation if you encounter a "Waiting for lock to be released" message.*
