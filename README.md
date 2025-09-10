# 🧹 Clone Cleaner

**Clone Cleaner** is a Java CLI that detects and removes duplicate files by *content* (SHA-256), helping you reclaim disk space and keep folders tidy. It prints an actionable plan, generates TXT/CSV reports, and (optionally) deletes duplicates after an explicit `YES` confirmation.

[![Build](https://img.shields.io/badge/build-maven-blue)]()
[![Java](https://img.shields.io/badge/java-21%2B-red)]()
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

---

## 📑 Table of Contents

- [Features](#-features)
- [Demo](#-demo)
- [Installation](#-installation)
- [Usage](#-usage)
    - [Command-line Options](#command-line-options)
    - [Examples](#examples)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Running Tests](#-running-tests)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🚀 Features

- Content-based detection (SHA-256) safe against misleading filenames.
- Clear plan output: which file to **KEEP** and which to **DEL** for each hash group.
- Two keep policies: `FIRST` (default) or `NEWEST`.
- Dry-run by default; deletion only after typing `YES`.
- Reports:
    - **TXT**: human-friendly, grouped by content hash.
    - **CSV**: machine-friendly (`hash,keep,action,path`).
- Relative or absolute paths in output.
- Colored console output (disable with `--no-color`).
- Unit and integration tests (JUnit 5).

---

## 📸 Demo

```
=======================================
            CLONE CLEANER
       Duplicate Files Detector
=======================================
Scan root: /path/to/scan
Duplicate groups (by content): 1
Files in duplicate groups: 2
Keep policy: FIRST | MODE: DELETE | PATHS: RELATIVE

Hash: 3603a81777e23656716d12c42d8600a9222402e00f25bbbbc192d53bd2755870 | Files: 2
  KEEP -> file1.pdf
  DEL  -> file1 (copy).pdf

Planned deletions: 1
Type YES to confirm deletion:
```

---

## ⚙️ Installation

### Prerequisites
- Java **21+**
- Maven **3.9+**
- Git (to clone the repository)

### Clone & Build
```bash
git clone https://github.com/MatheusBarbosaSE/clone-cleaner.git
cd clone-cleaner
mvn clean package
```

---

## 💻 Usage

### Command-line Options

| Option | Description | Default |
|-------|-------------|---------|
| `--delete` | Perform deletion after confirmation (`YES`). | Dry-run (no deletion) |
| `--keep=first\|newest` | Keep first occurrence or newest file in each group. | `first` |
| `--report=<path>` | Save a TXT summary report to `<path>`. | none |
| `--csv=<path>` | Save a CSV report to `<path>`. | none |
| `--absolute` | Print absolute paths instead of relative. | Relative |
| `--no-color` | Disable ANSI colors in console output. | Colors enabled |

### Examples

Detect duplicates (dry-run):
```bash
java -cp target/classes com.matheusbarbosase.clonecleaner.ui.ConsoleApp "/path/to/scan"
```

Delete duplicates (requires typing `YES`):
```bash
java -cp target/classes com.matheusbarbosase.clonecleaner.ui.ConsoleApp "/path/to/scan" --delete
```

Keep newest file in each duplicate group:
```bash
java -cp target/classes com.matheusbarbosase.clonecleaner.ui.ConsoleApp "/path/to/scan" --keep=newest
```

Generate TXT and CSV reports:
```bash
java -cp target/classes com.matheusbarbosase.clonecleaner.ui.ConsoleApp "/path/to/scan" --report=report.txt --csv=report.csv --no-color
```

Print absolute paths:
```bash
java -cp target/classes com.matheusbarbosase.clonecleaner.ui.ConsoleApp "/path/to/scan" --absolute
```

---

## 🛠️ Technology Stack

- **Java 21**, standard libraries (`java.nio.file`, `MessageDigest`).
- **Maven 3.9+** (build/test lifecycle).
- **JUnit 5** (unit + integration tests).

---

## 📂 Project Structure

```
clone-cleaner/
├── src/
│   ├── main/
│   │   └── java/com/matheusbarbosase/clonecleaner/
│   │       ├── core/        # Hashing (SHA-256), duplicate detection, deletion services
│   │       └── ui/          # ConsoleApp (CLI, colors, confirmation, reports)
│   └── test/
│       └── java/com/matheusbarbosase/clonecleaner/
│           ├── core/        # Unit tests (hasher, finder, cleaner)
│           └── ui/          # Integration tests (CLI, reports)
├── target/                  # Build artifacts (ignored by Git)
├── LICENSE                  # MIT license
├── pom.xml                  # Maven configuration
└── README.md                # Project documentation
```

---

## 🧪 Running Tests

The project includes unit and integration tests (JUnit 5) to validate hashing, duplicate detection, cleaning, and the CLI behavior.

### Run all tests with Maven
```bash
mvn clean test
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'feat: add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

Feel free to open **issues** for bug reports or suggestions.

---

## 📄 License

This project is licensed under the **[MIT License](LICENSE)**.  
You are free to use, copy, modify, and distribute this software, provided you keep the original credits.

