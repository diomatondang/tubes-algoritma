@echo off
echo Menjalankan Sundara CoffeeSpace POS...

set "JAVA_CMD=java"
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Greenfoot\jdk\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\Greenfoot\jdk\bin\java.exe"
    ) else (
        for /d %%i in ("C:\Program Files\Java\jdk*") do (
            if exist "%%i\bin\java.exe" set "JAVA_CMD=%%i\bin\java.exe"
        )
    )
)

"%JAVA_CMD%" -cp "lib/*;out" sundara.Main
