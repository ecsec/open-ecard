#define appName "${app.name}"
#define appVersion "${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}"
#define publisher "${app.vendor}"
#define appURL "${app.url}"
#define appSupportURL "https://dev.openecard.org/projects/open-ecard/boards"
#define appExeName "Open-eCard-App.exe"
#define appIdentifier "${app.identifier}"
#define dirName "Open-eCard-App"

[Setup]
AppId={{CB11CB66-71B5-42C1-8076-15F1FEDCC22A}}
AppName={#appName}
AppVersion={#appVersion}
AppPublisher={#publisher}
AppPublisherURL={#appURL}
AppSupportURL={#appSupportURL}
DefaultDirName={autopf}\{#dirName}
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=No
DisableFinishedPage=No
DisableWelcomePage=No
DefaultGroupName={#appName}{#appVersion}
LicenseFile=${project.basedir}\src\main\resources\windows\license.txt
PrivilegesRequired=admin
OutputDir=${project.basedir}\target\iscc
OutputBaseFilename={#appIdentifier}-{#appVersion}
SetupIconFile=${project.basedir}\src\main\resources\windows\Open-eCard-App.ico
Compression=lzma
SolidCompression=yes
WizardStyle=modern
WizardSmallImageFile=${project.basedir}\src\main\resources\windows\Open-eCard-App-setup-icon.bmp

UninstallDisplayIcon={app}\Open-eCard-App.ico
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
Source: "${project.basedir}\target\jpackage-build\images\win-msi.image\{#dirName}\{#appExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "${project.basedir}\target\jpackage-build\images\win-msi.image\{#dirName}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{autoprograms}\{#appName}"; Filename: "{app}\{#appExeName}"
Name: "{autodesktop}\{#appName}"; Filename: "{app}\{#appExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#appExeName}"; Description: "{cm:LaunchProgram,{#StringChange(appName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallRun]
Filename: "{cmd}"; Parameters: "/C ""taskkill /im {#appExeName} /f /t"
Filename: "{app}\{#appExeName} "; Parameters: "-uninstall -svcName Open-eCard-App -stopOnUninstall"; Check: returnFalse()

[Registry]
Root: HKLM; Subkey: "SOFTWARE\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "Open-eCard-App"; ValueData: """{app}\{#appExeName}"""; Flags: uninsdeletevalue
Root: HKLM; Subkey: "SYSTEM\CurrentControlSet\services\SCardSvr"; ValueType: dword; ValueName: "Start"; ValueData: "2"; Flags: uninsdeletekeyifempty

[Code]
function returnFalse(): Boolean;
begin
	Result := False;
end;

//
// Uninstall previous versions
//
function GetUninstallString(): String;
var
	uninstallPath: String;
	uninstallStr: String;
begin
	uninstallPath := ExpandConstant('Software\Microsoft\Windows\CurrentVersion\Uninstall\{{CB11CB66-71B5-42C1-8076-15F1FEDCC22A}}_is1');
	uninstallStr := '';
	// Also possible to use QuietUninstallString
	if not RegQueryStringValue(HKLM, uninstallPath, 'UninstallString', uninstallStr) then
		RegQueryStringValue(HKCU, uninstallPath, 'UninstallString', uninstallStr);
	Result := uninstallStr;
end;

function IsUpgrade(): Boolean;
begin
	Result := (GetUninstallString() <> '');
end;

function UninstallOldVersion(): Integer;
var
	uninstallStr: String;
	resultCode: Integer;
begin
	Result := 0;
	uninstallStr := GetUninstallString();
	if uninstallStr <> '' then begin
		uninstallStr := RemoveQuotes(uninstallStr);
		if Exec(uninstallStr, '/SILENT /NORESTART /SUPPRESSMSGBOXES','', SW_HIDE, ewWaitUntilTerminated, resultCode) then
			Result := 3
		else
			Result := 2;
	end else
		Result := 1;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
	if (CurStep=ssInstall) then
	begin
		if (IsUpgrade()) then
		begin
			UninstallOldVersion();
		end;
	end;
end;
