@echo off
chcp 65001 >nul
echo Запуск сервера...
echo.

REM Проверяем, скомпилирован ли проект
if not exist "target\classes" (
    echo Компиляция проекта...
    call mvn compile -q
    if errorlevel 1 (
        echo Ошибка компиляции!
        pause
        exit /b 1
    )
)

echo Сервер запускается...
echo Для остановки закройте окно или нажмите Ctrl+C
echo.

REM Запускаем сервер
java -cp "target\classes" ServerWindow

pause