#!/bin/bash

echo "Запуск клиента..."
echo ""

# Проверяем, скомпилирован ли проект
if [ ! -d "target/classes" ]; then
    echo "Компиляция проекта..."
    mvn compile -q
    if [ $? -ne 0 ]; then
        echo "Ошибка компиляции!"
        exit 1
    fi
fi

echo "Клиент запускается..."
echo "Для остановки закройте окно или нажмите Ctrl+C"
echo ""

# Запускаем клиента
java -cp "target/classes" ClientWindow