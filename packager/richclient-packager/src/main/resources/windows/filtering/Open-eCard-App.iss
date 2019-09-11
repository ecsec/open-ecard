#define appName "${app.name}"
#define appVersion "${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}"
#define publisher "${app.vendor}"
#define appURL "${app.url}"
#define appExeName "open-ecard.exe"

[Setup]
AppId={{CB11CB66-71B5-42C1-8076-15F1FEDCC22A}}
AppName={#appName}
AppVersion={#appVersion}
AppPublisher={#publisher}
AppPublisherURL={#appURL}
AppSupportURL={#appURL}
AppUpdatesURL={#appURL}
DefaultDirName={autopf}\{#appName}
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=No
DisableFinishedPage=No
DisableWelcomePage=No
DefaultGroupName={#appName}{#appVersion}
LicenseFile=${project.basedir}\src\main\resources\windows\license.txt
PrivilegesRequiredOverridesAllowed=dialog
OutputDir=${project.basedir}\target\jpackager-out
OutputBaseFilename={#appName}{#appVersion}
SetupIconFile=${project.basedir}\src\main\resources\windows\Open-eCard-App.ico
Compression=lzma
SolidCompression=yes
WizardStyle=modern
WizardSmallImageFile=${project.basedir}\src\main\resources\windows\Open-eCard-App-setup-icon.bmp

UninstallDisplayIcon=${project.basedir}\src\main\resources\windows\Open-eCard-App.ico
UninstallDisplayName={#appName}
WizardImageStretch=No

ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

LanguageDetectionMethod=uilanguage
ShowLanguageDialog=no

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "${project.basedir}\target\jpackager-build\images\win-msi.image\open-ecard\open-ecard.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "${project.basedir}\target\jpackager-build\images\win-msi.image\open-ecard\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{autoprograms}\{#appName}"; Filename: "{app}\{#appExeName}"
Name: "{autodesktop}\{#appName}"; Filename: "{app}\{#appExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#appExeName}"; Description: "{cm:LaunchProgram,{#StringChange(appName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
