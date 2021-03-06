package org.openecard.mdlw.sal.cryptoki;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : pkcs11_v2.40/pkcs11t.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class CK_CMS_SIG_PARAMS extends Structure {
	/** C type : CK_OBJECT_HANDLE */
	public long certificateHandle;
	public long getCertificateHandle() {
		return certificateHandle;
	}
	public void setCertificateHandle(long certificateHandle) {
		this.certificateHandle = certificateHandle;
	}
	/** C type : CK_MECHANISM_PTR */
	public org.openecard.mdlw.sal.cryptoki.CK_MECHANISM.ByReference pSigningMechanism;
	public org.openecard.mdlw.sal.cryptoki.CK_MECHANISM.ByReference getPSigningMechanism() {
		return pSigningMechanism;
	}
	public void setPSigningMechanism(org.openecard.mdlw.sal.cryptoki.CK_MECHANISM.ByReference pSigningMechanism) {
		this.pSigningMechanism = pSigningMechanism;
	}
	/** C type : CK_MECHANISM_PTR */
	public org.openecard.mdlw.sal.cryptoki.CK_MECHANISM.ByReference pDigestMechanism;
	public org.openecard.mdlw.sal.cryptoki.CK_MECHANISM.ByReference getPDigestMechanism() {
		return pDigestMechanism;
	}
	public void setPDigestMechanism(org.openecard.mdlw.sal.cryptoki.CK_MECHANISM.ByReference pDigestMechanism) {
		this.pDigestMechanism = pDigestMechanism;
	}
	/** C type : CK_UTF8CHAR_PTR */
	public Pointer pContentType;
	public Pointer getPContentType() {
		return pContentType;
	}
	public void setPContentType(Pointer pContentType) {
		this.pContentType = pContentType;
	}
	/** C type : CK_BYTE_PTR */
	public Pointer pRequestedAttributes;
	public Pointer getPRequestedAttributes() {
		return pRequestedAttributes;
	}
	public void setPRequestedAttributes(Pointer pRequestedAttributes) {
		this.pRequestedAttributes = pRequestedAttributes;
	}
	/** C type : CK_ULONG */
	public long ulRequestedAttributesLen;
	public long getUlRequestedAttributesLen() {
		return ulRequestedAttributesLen;
	}
	public void setUlRequestedAttributesLen(long ulRequestedAttributesLen) {
		this.ulRequestedAttributesLen = ulRequestedAttributesLen;
	}
	/** C type : CK_BYTE_PTR */
	public Pointer pRequiredAttributes;
	public Pointer getPRequiredAttributes() {
		return pRequiredAttributes;
	}
	public void setPRequiredAttributes(Pointer pRequiredAttributes) {
		this.pRequiredAttributes = pRequiredAttributes;
	}
	/** C type : CK_ULONG */
	public long ulRequiredAttributesLen;
	public long getUlRequiredAttributesLen() {
		return ulRequiredAttributesLen;
	}
	public void setUlRequiredAttributesLen(long ulRequiredAttributesLen) {
		this.ulRequiredAttributesLen = ulRequiredAttributesLen;
	}
	public CK_CMS_SIG_PARAMS() {
		super();
	}
	 protected List<String> getFieldOrder() {
		return Arrays.asList("certificateHandle", "pSigningMechanism", "pDigestMechanism", "pContentType", "pRequestedAttributes", "ulRequestedAttributesLen", "pRequiredAttributes", "ulRequiredAttributesLen");
	}
	/**
	 * @param certificateHandle C type : CK_OBJECT_HANDLE<br>
	 * @param pSigningMechanism C type : CK_MECHANISM_PTR<br>
	 * @param pDigestMechanism C type : CK_MECHANISM_PTR<br>
	 * @param pContentType C type : CK_UTF8CHAR_PTR<br>
	 * @param pRequestedAttributes C type : CK_BYTE_PTR<br>
	 * @param ulRequestedAttributesLen C type : CK_ULONG<br>
	 * @param pRequiredAttributes C type : CK_BYTE_PTR<br>
	 * @param ulRequiredAttributesLen C type : CK_ULONG
	 */
	public CK_CMS_SIG_PARAMS(long certificateHandle, org.openecard.mdlw.sal.cryptoki.CK_MECHANISM.ByReference pSigningMechanism, org.openecard.mdlw.sal.cryptoki. CK_MECHANISM.ByReference pDigestMechanism, Pointer pContentType, Pointer pRequestedAttributes, long ulRequestedAttributesLen, Pointer pRequiredAttributes, long ulRequiredAttributesLen) {
		super();
		this.certificateHandle = certificateHandle;
		this.pSigningMechanism = pSigningMechanism;
		this.pDigestMechanism = pDigestMechanism;
		this.pContentType = pContentType;
		this.pRequestedAttributes = pRequestedAttributes;
		this.ulRequestedAttributesLen = ulRequestedAttributesLen;
		this.pRequiredAttributes = pRequiredAttributes;
		this.ulRequiredAttributesLen = ulRequiredAttributesLen;
	}
	public CK_CMS_SIG_PARAMS(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends CK_CMS_SIG_PARAMS implements Structure.ByReference {
		
	};
	public static class ByValue extends CK_CMS_SIG_PARAMS implements Structure.ByValue {
		
	};
}
