@echo off
echo Compiling Sundara CoffeeSpace POS...
if not exist out mkdir out

set "JAVAC_CMD=javac"
where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Greenfoot\jdk\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\Greenfoot\jdk\bin\javac.exe"
    ) else (
        for /d %%i in ("C:\Program Files\Java\jdk*") do (
            if exist "%%i\bin\javac.exe" set "JAVAC_CMD=%%i\bin\javac.exe"
        )
    )
)

"%JAVAC_CMD%" -encoding UTF-8 -cp "lib/*" -d out ^
  src\sundara\model\Category.java ^
  src\sundara\model\MenuItem.java ^
  src\sundara\model\OrderItem.java ^
  src\sundara\data\DatabaseManager.java ^
  src\sundara\data\MenuData.java ^
  src\sundara\data\ReceiptSettings.java ^
  src\sundara\ui\Theme.java ^
  src\sundara\ui\SundaraLogo.java ^
  src\sundara\ui\MenuItemCard.java ^
  src\sundara\ui\MenuPanel.java ^
  src\sundara\ui\OrderPanel.java ^
  src\sundara\ui\PaymentDialog.java ^
  src\sundara\ui\ArchiveDialog.java ^
  src\sundara\ui\AdminSettingsDialog.java ^
  src\sundara\ui\MainFrame.java ^
  src\sundara\ui\LoginDialog.java ^
  src\sundara\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Kompilasi gagal. Periksa error di atas.
    pause
    exit /b 1
)

echo Menyalin assets...
if not exist out\sundara\assets mkdir out\sundara\assets
copy /Y src\sundara\assets\* out\sundara\assets\ >nul

echo.
echo Selesai! Jalankan: .\run.bat
pause
