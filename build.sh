#!/bin/bash

# Остановка скрипта при ошибке
set -e

# Настройки проекта
PROJECT_NAME="JumpSimulation"
MAIN_CLASS="app.MainApp"
MAIN_JAR="JumpSimulation-1.0-SNAPSHOT.jar"
VERSION="1.0.0"
#ICON_PATH="src/main/resources/assets/app_icon.icns" # Укажите путь к иконке
JAVA_FX_SDK="/Users/ed.kiselev/javafx-sdk-23.0.1"   # Укажите ваш путь к JavaFX SDK
RUNTIME_DIR="custom-runtime"

# Проверяем наличие необходимых инструментов
echo "Проверяем JDK..."
if ! command -v java &> /dev/null; then
    echo "Java не установлена. Установите JDK 14 или выше."
    exit 1
fi

echo "Проверяем Maven..."
if ! command -v mvn &> /dev/null; then
    echo "Maven не установлен. Установите Maven и повторите попытку."
    exit 1
fi

echo "Проверяем jlink..."
if ! command -v jlink &> /dev/null; then
    echo "jlink не найден. Убедитесь, что JDK установлена правильно."
    exit 1
fi

echo "Проверяем jpackage..."
if ! command -v jpackage &> /dev/null; then
    echo "jpackage не найден. Убедитесь, что JDK поддерживает jpackage."
    exit 1
fi

# Шаг 1: Сборка JAR-файла с помощью Maven
echo "Собираем проект с помощью Maven..."
mvn clean package

# Проверяем, существует ли скомпилированный JAR-файл
if [ ! -f "target/${MAIN_JAR}" ]; then
    echo "JAR-файл не найден. Проверьте ошибки сборки."
    exit 1
fi

# Шаг 2: Создание Runtime-образа с помощью jlink
echo "Создаём Runtime-образ с помощью jlink..."
jlink \
    --module-path "${JAVA_FX_SDK}/lib:${JAVA_HOME}/jmods" \
    --add-modules java.base,javafx.controls,javafx.fxml \
    --output "${RUNTIME_DIR}" \
    --strip-debug \
    --no-header-files \
    --no-man-pages

# Проверяем, создан ли Runtime-образ
if [ ! -d "${RUNTIME_DIR}" ]; then
    echo "Runtime-образ не создан. Проверьте ошибки команды jlink."
    exit 1
fi

# Шаг 3: Создание папки для jpackage
echo "Подготавливаем папку для jpackage..."
mkdir -p build

# Копируем JAR-файл и ресурсы
cp target/${MAIN_JAR} build/
cp -r src/main/resources/assets build/

# Шаг 4: Создание DMG-файла с помощью jpackage
echo "Создаём DMG-файл с помощью jpackage..."
jpackage \
    --input build/ \
    --name "${PROJECT_NAME}" \
    --main-jar "${MAIN_JAR}" \
    --main-class "${MAIN_CLASS}" \
    --runtime-image "${RUNTIME_DIR}" \
    --type dmg \
    --app-version "${VERSION}" \
    --description "Симуляция прыжка с парашютом" \
    --vendor "Edward Kiselev" \
    ${ICON_PATH:+--icon "${ICON_PATH}"} \
    --java-options "--module-path /Users/ed.kiselev/javafx-sdk-23.0.1/lib --add-modules javafx.controls,javafx.fxml"

# Шаг 5: Очистка временных файлов
echo "Очистка временных файлов..."
rm -rf build/
rm -rf "${RUNTIME_DIR}"

echo "Создание DMG завершено. Ваш файл находится в текущей директории."