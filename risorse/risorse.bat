@echo off
setlocal
 
REM Download delle risorse dalla repository pubblica GitHub
set "FILE_URL=https://github.com/Vsb23/Risorse-da-scaricare/raw/refs/heads/main/risorse.zip"
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

echo Scaricamento risorse ...

REM Download con parametri ottimizzati
powershell -Command "try { $ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri '%FILE_URL%' -OutFile '%ZIP_FILE%' -UserAgent 'Mozilla/5.0' -TimeoutSec 300 } catch { Write-Host 'Errore durante il download'; exit 1 }"
if errorlevel 1 goto error

REM Controllo esistenza file
if not exist "%ZIP_FILE%" (
    echo File non scaricato!
    goto error
)

echo Decompressione archivio...

REM Decompressione con 7-Zip (modifica il percorso se necessario)
if exist "%7Z_PATH%" (
    "%7Z_PATH%" x "%ZIP_FILE%" -o"%EXTRACT_PATH%" -y
) else (
    powershell -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%EXTRACT_PATH%' -Force"
)
if errorlevel 1 goto error

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

echo Avvio installazioni...
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

echo Installazione completata! Adesso è possibile chiudere il terminale.

REM Pulizia file temporaneo
del "%ZIP_FILE%"
timeout /t 3 /nobreak >nul
exit /b 0

:error
echo Errore durante l'operazione. Verifica:
echo - Connessione internet
echo - Accessibilità repository GitHub
echo - Disponibilita' del file risorse.zip
echo.
echo Repository utilizzata: %FILE_URL%
pause
exit /b 1