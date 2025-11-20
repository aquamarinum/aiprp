@echo off
chcp 65001 >nul
echo Запуск клиента...
echo.

REM Проверяем, скомпилирован ли проект
if not exist "target\classes\com\example" (
    echo Компиляция проекта...
    call mvn compile -q
    if errorlevel 1 (
        echo Ошибка компиляции!
        pause
        exit /b 1
    )
)

echo Клиент запускается...
echo Для остановки закройте окно или нажмите Ctrl+C
echo.

REM Запускаем клиента
java -cp "target\classes\com\example" ClientWindow

pause