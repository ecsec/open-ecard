module openecard.app.ifd.scio.backend.pcsc.jvmMain {
	requires java.smartcardio;
	requires kotlin.stdlib;
	requires apdu4j.jnasmartcardio;
//	requires ifd.common.jvm;
//	requires common.jvm;
	requires io.github.oshai.kotlinlogging;
}
