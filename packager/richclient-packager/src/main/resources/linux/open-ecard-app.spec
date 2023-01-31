Summary: open-ecard-app
Name: open-ecard-app
Version: ${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}
Release: 1
License: GPL v3
Vendor: ecsec GmbH

%if "x" != "x"
URL:
%endif

%if "x/opt" != "x"
Prefix: /opt
%endif

Provides: open-ecard-app

%if "x" != "x"
Group:
%endif

Autoprov: 0
Autoreq: 0
%if "xxdg-utils" != "x" || "x" != "x"
Requires: xdg-utils
%endif

#comment line below to enable effective jar compression
#it could easily get your package size from 40 to 15Mb but
#build time will substantially increase and it may require unpack200/system java to install
%define __jar_repack %{nil}

%define package_filelist %{_tmppath}/%{name}.files
%define app_filelist %{_tmppath}/%{name}.app.files
%define filesystem_filelist %{_tmppath}/%{name}.filesystem.files

%define default_filesystem / /opt /usr /usr/bin /usr/lib /usr/local /usr/local/bin /usr/local/lib

%description
Client side implementation of the eCard-API-Framework (BSI TR-03112)

%global __os_install_post %{nil}

%prep

%build

%install
rm -rf %{buildroot}
install -d -m 755 %{buildroot}/opt/open-ecard-app
cp -r %{_sourcedir}/opt/open-ecard-app/* %{buildroot}/opt/open-ecard-app
%if "x${project.basedir}/src/main/resources/licenses/LICENSE.GPL" != "x"
  %define license_install_file %{_defaultlicensedir}/%{name}-%{version}/%{basename:${project.basedir}/src/main/resources/licenses/LICENSE.GPL}
  install -d -m 755 "%{buildroot}%{dirname:%{license_install_file}}"
  install -m 644 "${project.basedir}/src/main/resources/licenses/LICENSE.GPL" "%{buildroot}%{license_install_file}"
%endif
(cd %{buildroot} && find . -type d) | sed -e 's/^\.//' -e '/^$/d' | sort > %{app_filelist}
{ rpm -ql filesystem || echo %{default_filesystem}; } | sort > %{filesystem_filelist}
comm -23 %{app_filelist} %{filesystem_filelist} > %{package_filelist}
sed -i -e 's/.*/%dir "&"/' %{package_filelist}
(cd %{buildroot} && find . -not -type d) | sed -e 's/^\.//' -e 's/.*/"&"/' >> %{package_filelist}
%if "x${project.basedir}/src/main/resources/licenses/LICENSE.GPL" != "x"
  sed -i -e 's|"%{license_install_file}"||' -e '/^$/d' %{package_filelist}
%endif

%files -f %{package_filelist}
%if "x${project.basedir}/src/main/resources/licenses/LICENSE.GPL" != "x"
  %license "%{license_install_file}"
%endif

%post
xdg-desktop-menu install /opt/open-ecard-app/lib/open-ecard-app-open-ecard-app.desktop

%postun

# Don't remove the settings on upgrade, only on uninstall.
case "$1" in
  0)
    xdg-desktop-menu uninstall /opt/open-ecard-app/lib/open-ecard-app-open-ecard-app.desktop
  ;;
esac

%clean
