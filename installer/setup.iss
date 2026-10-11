; Inno Setup script for the Windows installer.
; Build first: run EmpSystem\build.bat, then launch4j with launch4j.xml (creates "EM System.exe").
; Then compile this script with Inno Setup; the installer is written to installer\output.

#define MyAppName "EM System"
#define MyAppVersion "1.6"
#define MyAppExeName "EM System.exe"

[Setup]
; AppId must stay the same so new versions replace older installs.
AppId={{F0AC9518-0DD7-440A-98EE-FEC30B6FE4C2}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
DefaultDirName={autopf}\{#MyAppName}
DisableProgramGroupPage=yes
OutputDir=output
OutputBaseFilename=EMS Setup
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "..\EmpSystem\dist\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\EmpSystem\dist\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
