# iOS libs

For iOS and iOSSimulators we have to link libssl.a and libcrypto.a statically. 
One cannot use static libaries for macOS which might be installed on the host machine, since those are macOs only. 

To build ios and iossim compatible libraries proceed as follows:

1. clone https://github.com/apotocki/openssl-iosx.git
1. cd to project
1. bash scripts/build.sh -p=iossim-arm64,ios
1. find the specific .a-files (libssl.a and libcrypto.a) and copy to libs folder in the corresponding subfolders

