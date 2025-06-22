@echo off
setlocal enabledelayedexpansion

REM Download delle risorse dalla repository pubblica GitHub
set "JDK_URL=https://download.oracle.com/java/24/latest/jdk-24_windows-x64_bin.msi"
set "MYSQL_URL=https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-community-8.0.39.0.msi"
set "ZIP_FILE=.\risorse.zip"
set "EXTRACT_PATH=.\risorse\"
set "7Z_PATH=C:\Program Files\7-Zip\7z.exe"

chcp 65001>nul
cls
echo ██████╗  ██████╗ ██╗    ██╗███╗   ██╗██╗      ██████╗  █████╗ ██████╗     ██████╗ ███████╗██╗     ██║     ███████╗
echo ██╔══██╗██╔═══██╗██║    ██║████╗  ██║██║     ██╔═══██╗██╔══██╗██╔══██╗    ██╔══██╗██╔════╝██║     ██║     ██╔════╝
echo ██║  ██║██║   ██║██║ █╗ ██║██╔██╗ ██║██║     ██║   ██║███████║██║  ██║    ██║  ██║█████╗  ██║     ██║     █████╗  
echo ██║  ██║██║   ██║██║███╗██║██║╚██╗██║██║     ██║   ██║██╔══██║██║  ██║    ██║  ██║██╔══╝  ██║     ██║     ██╔══╝  
echo ██████╔╝╚██████╔╝╚███╔███╔╝██║ ╚████║███████╗╚██████╔╝██║  ██║██████╔╝    ██████╔╝███████╗███████╗███████╗███████╗
echo ╚═════╝  ╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═══╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═════╝     ╚═════╝ ╚══════╝╚══════╝╚══════╝╚══════╝                                                                                                                 
echo ██████╗ ██╗███████╗ ██████╗ ██████╗ ███████╗███████╗                                                              
echo ██╔══██╗██║██╔════╝██╔═══██╗██╔══██╗██╔════╝██╔════╝                                                              
echo ██████╔╝██║███████╗██║   ██║██████╔╝███████╗█████╗                                                                
echo ██╔══██╗██║╚════██║██║   ██║██╔══██╗╚════██║██╔══╝                                                                
echo ██║  ██║██║███████║╚██████╔╝██║  ██║███████║███████╗                                                              
echo ╚═╝  ╚═╝╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝╚══════╝                                                              

echo.
echo Verifico la connessione di rete...

REM Test connessione
ping -n 1 github.com >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Impossibile raggiungere GitHub. Verifica la connessione internet.
    goto error
)
echo [OK] Connessione a GitHub attiva

echo.
echo Scaricamento risorse...
mkdir risorse 
powershell -Command "Invoke-WebRequest -Uri '%JDK_URL%' -OutFile './risorse/jdk-24_windows-x64_bin.msi'"

powershell -Command "Invoke-WebRequest -Uri '%MYSQL_URL%' -OutFile './risorse/mysql-installer-community-8.0.39.0.msi'"

echo.

REM Rimuovi file precedente se esiste
if exist "%ZIP_FILE%" (
    echo Rimuovo file precedente...
    del "%ZIP_FILE%"
)

REM Download con gestione errori migliorata
echo Inizio download...
powershell -NoProfile -ExecutionPolicy Bypass -Command "& {try { $ProgressPreference = 'SilentlyContinue'; $webClient = New-Object System.Net.WebClient; $webClient.Headers.Add('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'); $webClient.DownloadFile('%FILE_URL%', '%ZIP_FILE%'); Write-Host '[OK] Download completato' } catch { Write-Host '[ERRORE] Download fallito:' $_.Exception.Message; exit 1 }}"

REM Verifica risultato download
if errorlevel 1 (
    echo.
    echo [ERRORE] Download fallito!
    goto error
)

REM Controllo esistenza e dimensione file
if not exist "%ZIP_FILE%" (
    echo [ERRORE] File non trovato dopo il download!
    goto error
)

REM Verifica dimensione file (deve essere > 0 bytes)
for %%F in ("%ZIP_FILE%") do set "filesize=%%~zF"
if %filesize% LSS 1000 (
    echo [ERRORE] File scaricato troppo piccolo ^(%%filesize%% bytes^). Possibile errore.
    type "%ZIP_FILE%"
    goto error
)
echo [OK] File scaricato correttamente ^(%filesize% bytes^)

echo.
echo Decompressione archivio...

REM Crea cartella se non esiste
if not exist "%EXTRACT_PATH%" mkdir "%EXTRACT_PATH%"

REM Decompressione con gestione errori
if exist "%7Z_PATH%" (
    echo Uso 7-Zip per la decompressione...
    "%7Z_PATH%" x "%ZIP_FILE%" -o"%EXTRACT_PATH%" -y
    set "decompress_result=!errorlevel!"
) else (
    echo Uso PowerShell per la decompressione...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%EXTRACT_PATH%' -Force; Write-Host '[OK] Decompressione completata' } catch { Write-Host '[ERRORE] Decompressione fallita:' $_.Exception.Message; exit 1 }"
    set "decompress_result=!errorlevel!"
)

if !decompress_result! neq 0 (
    echo [ERRORE] Decompressione fallita!
    goto error
)

REM Verifica contenuto estratto
if not exist "%EXTRACT_PATH%\jdk-24_windows-x64_bin.msi" (
    echo [ERRORE] File JDK non trovato dopo l'estrazione!
    echo Contenuto cartella risorse:
    dir "%EXTRACT_PATH%" /b
    goto error
)

if not exist "%EXTRACT_PATH%\mysql-installer-community-8.0.39.0.msi" (
    echo [ERRORE] File MySQL non trovato dopo l'estrazione!
    echo Contenuto cartella risorse:
    dir "%EXTRACT_PATH%" /b
    goto error
)

cls
echo ██████╗  ██████╗ ██╗    ██╗███╗   ██╗██╗      ██████╗  █████╗ ██████╗                     
echo ██╔══██╗██╔═══██╗██║    ██║████╗  ██║██║     ██╔═══██╗██╔══██╗██╔══██╗                    
echo ██║  ██║██║   ██║██║ █╗ ██║██╔██╗ ██║██║     ██║   ██║███████║██║  ██║                    
echo ██║  ██║██║   ██║██║███╗██║██║╚██╗██║██║     ██║   ██║██╔══██║██║  ██║                    
echo ██████╔╝╚██████╔╝╚███╔███╔╝██║ ╚████║███████╗╚██████╔╝██║  ██║██████╔╝                    
echo ╚═════╝  ╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═══╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═════╝                                                                                                               
echo  ██████╗ ██████╗ ███╗   ███╗██████╗ ██╗     ███████╗████████╗ █████╗ ████████╗ ██████╗ ██╗
echo ██╔════╝██╔═══██╗████╗ ████║██╔══██╗██║     ██╔════╝╚══██╔══╝██╔══██╗╚══██╔══╝██╔═══██╗██║
echo ██║     ██║   ██║██╔████╔██║██████╔╝██║     █████╗     ██║   ███████║   ██║   ██║   ██║██║
echo ██║     ██║   ██║██║╚██╔╝██║██╔═══╝ ██║     ██╔══╝     ██║   ██╔══██║   ██║   ██║   ██║╚═╝
echo ╚██████╗╚██████╔╝██║ ╚═╝ ██║██║     ███████╗███████╗   ██║   ██║  ██║   ██║   ╚██████╔╝██╗
echo  ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚══════╝╚══════╝   ╚═╝   ╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚═╝

echo.
echo [OK] Download e decompressione completati con successo!
echo.
echo Avvio installazioni...
echo - JDK 24
echo - MySQL Community Server 8.0.39
echo.

start /d ".\risorse\" jdk-24_windows-x64_bin.msi
start /d ".\risorse\" mysql-installer-community-8.0.39.0.msi

cls
echo ██╗███╗   ██╗███████╗████████╗ █████╗ ██╗     ██╗      █████╗ ███████╗██╗ ██████╗ ███╗   ██╗███████╗
echo ██║████╗  ██║██╔════╝╚══██╔══╝██╔══██╗██║     ██║     ██╔══██╗╚══███╔╝██║██╔═══██╗████╗  ██║██╔════╝
echo ██║██╔██╗ ██║███████╗   ██║   ███████║██║     ██║     ███████║  ███╔╝ ██║██║   ██║██╔██╗ ██║█████╗  
echo ██║██║╚██╗██║╚════██║   ██║   ██╔══██║██║     ██║     ██╔══██║ ███╔╝  ██║██║   ██║██║╚██╗██║██╔══╝  
echo ██║██║ ╚████║███████║   ██║   ██║  ██║███████╗███████╗██║  ██║███████╗██║╚██████╔╝██║ ╚████║███████╗
echo ╚═╝╚═╝  ╚═══╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═╝╚══════╝╚═╝ ╚═════╝ ╚═╝  ╚═══╝╚══════╝                                                                                                    
echo  ██████╗ ██████╗ ███╗   ███╗██████╗ ██╗     ███████╗████████╗ █████╗ ████████╗ █████╗ ██╗           
echo ██╔════╝██╔═══██╗████╗ ████║██╔══██╗██║     ██╔════╝╚══██╔══╝██╔══██╗╚══██╔══╝██╔══██╗██║           
echo ██║     ██║   ██║██╔████╔██║██████╔╝██║     █████╗     ██║   ███████║   ██║   ███████║██║           
echo ██║     ██║   ██║██║╚██╔╝██║██╔═══╝ ██║     ██╔══╝     ██║   ██╔══██║   ██║   ██╔══██║╚═╝           
echo ╚██████╗╚██████╔╝██║ ╚═╝ ██║██║     ███████╗███████╗   ██║   ██║  ██║   ██║   ██║  ██║██╗           
echo  ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚══════╝╚══════╝   ╚═╝   ╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝           

echo.
echo [OK] Installazione completata! 
echo Adesso è possibile chiudere il terminale.

REM Pulizia file temporaneo
echo Pulizia file temporanei...
del "%ZIP_FILE%" 2>nul
timeout /t 5 /nobreak >nul
exit /b 0

:error
echo.
echo ================================
echo         ERRORE RILEVATO        
echo ================================
echo.
echo Possibili cause:
echo 1. Connessione internet instabile
echo 2. Repository GitHub non accessibile
echo 3. File risorse.zip non presente o corrotto
echo 4. Antivirus che blocca il download
echo 5. Proxy aziendale che impedisce l'accesso
echo.
echo Repository utilizzata: 
echo %FILE_URL%
echo.
echo Suggerimenti:
echo - Controlla la connessione internet
echo - Disabilita temporaneamente l'antivirus
echo - Verifica che GitHub sia accessibile
echo - Prova a scaricare manualmente il file
echo.
pause
exit /b 1