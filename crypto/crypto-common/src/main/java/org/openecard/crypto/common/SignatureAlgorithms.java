/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.crypto.common;

import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.openecard.crypto.common.HashAlgorithms.*;


/**
 * List of algorithms based mainly on the PKCS11 mechanisms.
 *
 * @author Tobias Wich
 */
public enum SignatureAlgorithms {

//    CKM_RSA_PKCS_KEY_PAIR_GEN          (0x00000000L),
    CKM_RSA_PKCS                       (0x00000001L, "NONEwithRSA", "http://ws.openecard.org/alg/rsa", KeyTypes.CKK_RSA, null),
//    CKM_RSA_9796                       (0x00000002L),
//    CKM_RSA_X_509                      (0x00000003L),

//    CKM_MD2_RSA_PKCS                   (0x00000004L),
//    CKM_MD5_RSA_PKCS                   (0x00000005L),
    CKM_SHA1_RSA_PKCS                  (0x00000006L, "SHA1withRSA", "http://www.w3.org/2000/09/xmldsig#rsa-sha1", KeyTypes.CKK_RSA, CKM_SHA_1),

//    CKM_RIPEMD128_RSA_PKCS             (0x00000007L),
//    CKM_RIPEMD160_RSA_PKCS             (0x00000008L),
//    CKM_RSA_PKCS_OAEP                  (0x00000009L),

//    CKM_RSA_X9_31_KEY_PAIR_GEN         (0x0000000AL),
//    CKM_RSA_X9_31                      (0x0000000BL),
//    CKM_SHA1_RSA_X9_31                 (0x0000000CL),
    CKM_RSA_PKCS_PSS                   (0x0000000DL, "NONEwithRSAandMGF1", "http://ws.openecard.org/alg/rsa-MGF1", KeyTypes.CKK_RSA, null),
    CKM_SHA1_RSA_PKCS_PSS              (0x0000000EL, "SHA1withRSAandMGF1", "http://www.w3.org/2007/05/xmldsig-more#sha1-rsa-MGF1", KeyTypes.CKK_RSA, CKM_SHA_1),

//    CKM_DSA_KEY_PAIR_GEN               (0x00000010L),
//    CKM_DSA                            (0x00000011L),
//    CKM_DSA_SHA1                       (0x00000012L),
//    CKM_DSA_SHA224                     (0x00000013L),
//    CKM_DSA_SHA256                     (0x00000014L),
//    CKM_DSA_SHA384                     (0x00000015L),
//    CKM_DSA_SHA512                     (0x00000016L),

//    CKM_DH_PKCS_KEY_PAIR_GEN           (0x00000020L),
//    CKM_DH_PKCS_DERIVE                 (0x00000021L),

//    CKM_X9_42_DH_KEY_PAIR_GEN          (0x00000030L),
//    CKM_X9_42_DH_DERIVE                (0x00000031L),
//    CKM_X9_42_DH_HYBRID_DERIVE         (0x00000032L),
//    CKM_X9_42_MQV_DERIVE               (0x00000033L),

    CKM_SHA256_RSA_PKCS                (0x00000040L, "SHA256withRSA", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", KeyTypes.CKK_RSA, CKM_SHA256),
    CKM_SHA384_RSA_PKCS                (0x00000041L, "SHA384withRSA", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", KeyTypes.CKK_RSA, CKM_SHA384),
    CKM_SHA512_RSA_PKCS                (0x00000042L, "SHA512withRSA", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", KeyTypes.CKK_RSA, CKM_SHA512),
    CKM_SHA256_RSA_PKCS_PSS            (0x00000043L, "SHA256withRSAandMGF1", "http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1", KeyTypes.CKK_RSA, CKM_SHA256),
    CKM_SHA384_RSA_PKCS_PSS            (0x00000044L, "SHA384withRSAandMGF1", "http://www.w3.org/2007/05/xmldsig-more#sha384-rsa-MGF1", KeyTypes.CKK_RSA, CKM_SHA384),
    CKM_SHA512_RSA_PKCS_PSS            (0x00000045L, "SHA512withRSA", "http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1", KeyTypes.CKK_RSA, CKM_SHA512),

    CKM_SHA224_RSA_PKCS                (0x00000046L, "SHA224withRSA", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha224", KeyTypes.CKK_RSA, CKM_SHA224),
    CKM_SHA224_RSA_PKCS_PSS            (0x00000047L, "SHA224withRSAandMGF1", "http://www.w3.org/2007/05/xmldsig-more#sha224-rsa-MGF1", KeyTypes.CKK_RSA, CKM_SHA224),

//    CKM_SHA512_224                     (0x00000048L),
//    CKM_SHA512_224_HMAC                (0x00000049L),
//    CKM_SHA512_224_HMAC_GENERAL        (0x0000004AL),
//    CKM_SHA512_224_KEY_DERIVATION      (0x0000004BL),
//    CKM_SHA512_256                     (0x0000004CL),
//    CKM_SHA512_256_HMAC                (0x0000004DL),
//    CKM_SHA512_256_HMAC_GENERAL        (0x0000004EL),
//    CKM_SHA512_256_KEY_DERIVATION      (0x0000004FL),

//    CKM_SHA512_T_HMAC                  (0x00000051L),
//    CKM_SHA512_T_HMAC_GENERAL          (0x00000052L),
//    CKM_SHA512_T_KEY_DERIVATION        (0x00000053L),

//    CKM_RC2_KEY_GEN                    (0x00000100L),
//    CKM_RC2_ECB                        (0x00000101L),
//    CKM_RC2_CBC                        (0x00000102L),
//    CKM_RC2_MAC                        (0x00000103L),
//
//    CKM_RC2_MAC_GENERAL                (0x00000104L),
//    CKM_RC2_CBC_PAD                    (0x00000105L),
//
//    CKM_RC4_KEY_GEN                    (0x00000110L),
//    CKM_RC4                            (0x00000111L),
//    CKM_DES_KEY_GEN                    (0x00000120L),
//    CKM_DES_ECB                        (0x00000121L),
//    CKM_DES_CBC                        (0x00000122L),
//    CKM_DES_MAC                        (0x00000123L),
//
//    CKM_DES_MAC_GENERAL                (0x00000124L),
//    CKM_DES_CBC_PAD                    (0x00000125L),
//
//    CKM_DES2_KEY_GEN                   (0x00000130L),
//    CKM_DES3_KEY_GEN                   (0x00000131L),
//    CKM_DES3_ECB                       (0x00000132L),
//    CKM_DES3_CBC                       (0x00000133L),
//    CKM_DES3_MAC                       (0x00000134L),
//
//    CKM_DES3_MAC_GENERAL               (0x00000135L),
//    CKM_DES3_CBC_PAD                   (0x00000136L),
//    CKM_DES3_CMAC_GENERAL              (0x00000137L),
//    CKM_DES3_CMAC                      (0x00000138L),
//    CKM_CDMF_KEY_GEN                   (0x00000140L),
//    CKM_CDMF_ECB                       (0x00000141L),
//    CKM_CDMF_CBC                       (0x00000142L),
//    CKM_CDMF_MAC                       (0x00000143L),
//    CKM_CDMF_MAC_GENERAL               (0x00000144L),
//    CKM_CDMF_CBC_PAD                   (0x00000145L),
//
//    CKM_DES_OFB64                      (0x00000150L),
//    CKM_DES_OFB8                       (0x00000151L),
//    CKM_DES_CFB64                      (0x00000152L),
//    CKM_DES_CFB8                       (0x00000153L),
//
//    CKM_MD2_HMAC                       (0x00000201L),
//    CKM_MD2_HMAC_GENERAL               (0x00000202L),
//
//    CKM_MD5_HMAC                       (0x00000211L),
//    CKM_MD5_HMAC_GENERAL               (0x00000212L),
//
//    CKM_SHA_1_HMAC                     (0x00000221L),
//    CKM_SHA_1_HMAC_GENERAL             (0x00000222L),
//
//    CKM_RIPEMD128_HMAC                 (0x00000231L),
//    CKM_RIPEMD128_HMAC_GENERAL         (0x00000232L),
//    CKM_RIPEMD160_HMAC                 (0x00000241L),
//    CKM_RIPEMD160_HMAC_GENERAL         (0x00000242L),
//
//    CKM_SHA256_HMAC                    (0x00000251L),
//    CKM_SHA256_HMAC_GENERAL            (0x00000252L),
//    CKM_SHA224_HMAC                    (0x00000256L),
//    CKM_SHA224_HMAC_GENERAL            (0x00000257L),
//    CKM_SHA384_HMAC                    (0x00000261L),
//    CKM_SHA384_HMAC_GENERAL            (0x00000262L),
//    CKM_SHA512_HMAC                    (0x00000271L),
//    CKM_SHA512_HMAC_GENERAL            (0x00000272L),
//    CKM_SECURID_KEY_GEN                (0x00000280L),
//    CKM_SECURID                        (0x00000282L),
//    CKM_HOTP_KEY_GEN                   (0x00000290L),
//    CKM_HOTP                           (0x00000291L),
//    CKM_ACTI                           (0x000002A0L),
//    CKM_ACTI_KEY_GEN                   (0x000002A1L),

//    CKM_CAST_KEY_GEN                   (0x00000300L),
//    CKM_CAST_ECB                       (0x00000301L),
//    CKM_CAST_CBC                       (0x00000302L),
//    CKM_CAST_MAC                       (0x00000303L),
//    CKM_CAST_MAC_GENERAL               (0x00000304L),
//    CKM_CAST_CBC_PAD                   (0x00000305L),
//    CKM_CAST3_KEY_GEN                  (0x00000310L),
//    CKM_CAST3_ECB                      (0x00000311L),
//    CKM_CAST3_CBC                      (0x00000312L),
//    CKM_CAST3_MAC                      (0x00000313L),
//    CKM_CAST3_MAC_GENERAL              (0x00000314L),
//    CKM_CAST3_CBC_PAD                  (0x00000315L),
//    CKM_CAST5_KEY_GEN                  (0x00000320L),
//    CKM_CAST128_KEY_GEN                (0x00000320L),
//    CKM_CAST5_ECB                      (0x00000321L),
//    CKM_CAST128_ECB                    (0x00000321L),
//    CKM_CAST5_CBC                      (0x00000322L),
//    CKM_CAST128_CBC                    (0x00000322L),
//    CKM_CAST5_MAC                      (0x00000323L),
//    CKM_CAST128_MAC                    (0x00000323L),
//    CKM_CAST5_MAC_GENERAL              (0x00000324L),
//    CKM_CAST128_MAC_GENERAL            (0x00000324L),
//    CKM_CAST5_CBC_PAD                  (0x00000325L),
//    CKM_CAST128_CBC_PAD                (0x00000325L),
//    CKM_RC5_KEY_GEN                    (0x00000330L),
//    CKM_RC5_ECB                        (0x00000331L),
//    CKM_RC5_CBC                        (0x00000332L),
//    CKM_RC5_MAC                        (0x00000333L),
//    CKM_RC5_MAC_GENERAL                (0x00000334L),
//    CKM_RC5_CBC_PAD                    (0x00000335L),
//    CKM_IDEA_KEY_GEN                   (0x00000340L),
//    CKM_IDEA_ECB                       (0x00000341L),
//    CKM_IDEA_CBC                       (0x00000342L),
//    CKM_IDEA_MAC                       (0x00000343L),
//    CKM_IDEA_MAC_GENERAL               (0x00000344L),
//    CKM_IDEA_CBC_PAD                   (0x00000345L),
//    CKM_GENERIC_SECRET_KEY_GEN         (0x00000350L),
//    CKM_CONCATENATE_BASE_AND_KEY       (0x00000360L),
//    CKM_CONCATENATE_BASE_AND_DATA      (0x00000362L),
//    CKM_CONCATENATE_DATA_AND_BASE      (0x00000363L),
//    CKM_XOR_BASE_AND_DATA              (0x00000364L),
//    CKM_EXTRACT_KEY_FROM_KEY           (0x00000365L),
//    CKM_SSL3_PRE_MASTER_KEY_GEN        (0x00000370L),
//    CKM_SSL3_MASTER_KEY_DERIVE         (0x00000371L),
//    CKM_SSL3_KEY_AND_MAC_DERIVE        (0x00000372L),
//
//    CKM_SSL3_MASTER_KEY_DERIVE_DH      (0x00000373L),
//    CKM_TLS_PRE_MASTER_KEY_GEN         (0x00000374L),
//    CKM_TLS_MASTER_KEY_DERIVE          (0x00000375L),
//    CKM_TLS_KEY_AND_MAC_DERIVE         (0x00000376L),
//    CKM_TLS_MASTER_KEY_DERIVE_DH       (0x00000377L),
//
//    CKM_TLS_PRF                        (0x00000378L),
//
//    CKM_SSL3_MD5_MAC                   (0x00000380L),
//    CKM_SSL3_SHA1_MAC                  (0x00000381L),
//    CKM_MD5_KEY_DERIVATION             (0x00000390L),
//    CKM_MD2_KEY_DERIVATION             (0x00000391L),
//    CKM_SHA1_KEY_DERIVATION            (0x00000392L),
//
//    CKM_SHA256_KEY_DERIVATION          (0x00000393L),
//    CKM_SHA384_KEY_DERIVATION          (0x00000394L),
//    CKM_SHA512_KEY_DERIVATION          (0x00000395L),
//    CKM_SHA224_KEY_DERIVATION          (0x00000396L),
//
//    CKM_PBE_MD2_DES_CBC                (0x000003A0L),
//    CKM_PBE_MD5_DES_CBC                (0x000003A1L),
//    CKM_PBE_MD5_CAST_CBC               (0x000003A2L),
//    CKM_PBE_MD5_CAST3_CBC              (0x000003A3L),
//    CKM_PBE_MD5_CAST5_CBC              (0x000003A4L),
//    CKM_PBE_MD5_CAST128_CBC            (0x000003A4L),
//    CKM_PBE_SHA1_CAST5_CBC             (0x000003A5L),
//    CKM_PBE_SHA1_CAST128_CBC           (0x000003A5L),
//    CKM_PBE_SHA1_RC4_128               (0x000003A6L),
//    CKM_PBE_SHA1_RC4_40                (0x000003A7L),
//    CKM_PBE_SHA1_DES3_EDE_CBC          (0x000003A8L),
//    CKM_PBE_SHA1_DES2_EDE_CBC          (0x000003A9L),
//    CKM_PBE_SHA1_RC2_128_CBC           (0x000003AAL),
//    CKM_PBE_SHA1_RC2_40_CBC            (0x000003ABL),
//
//    CKM_PKCS5_PBKD2                    (0x000003B0L),
//
//    CKM_PBA_SHA1_WITH_SHA1_HMAC        (0x000003C0L),
//
//    CKM_WTLS_PRE_MASTER_KEY_GEN        (0x000003D0L),
//    CKM_WTLS_MASTER_KEY_DERIVE         (0x000003D1L),
//    CKM_WTLS_MASTER_KEY_DERIVE_DH_ECC  (0x000003D2L),
//    CKM_WTLS_PRF                       (0x000003D3L),
//    CKM_WTLS_SERVER_KEY_AND_MAC_DERIVE (0x000003D4L),
//    CKM_WTLS_CLIENT_KEY_AND_MAC_DERIVE (0x000003D5L),

//    CKM_TLS10_MAC_SERVER               (0x000003D6L),
//    CKM_TLS10_MAC_CLIENT               (0x000003D7L),
//    CKM_TLS12_MAC                      (0x000003D8L),
//    CKM_TLS12_KDF                      (0x000003D9L),
//    CKM_TLS12_MASTER_KEY_DERIVE        (0x000003E0L),
//    CKM_TLS12_KEY_AND_MAC_DERIVE       (0x000003E1L),
//    CKM_TLS12_MASTER_KEY_DERIVE_DH     (0x000003E2L),
//    CKM_TLS12_KEY_SAFE_DERIVE          (0x000003E3L),
//    CKM_TLS_MAC                        (0x000003E4L),
//    CKM_TLS_KDF                        (0x000003E5L),
//
//    CKM_KEY_WRAP_LYNKS                 (0x00000400L),
//    CKM_KEY_WRAP_SET_OAEP              (0x00000401L),
//
//    CKM_CMS_SIG                        (0x00000500L),
//    CKM_KIP_DERIVE                     (0x00000510L),
//    CKM_KIP_WRAP                       (0x00000511L),
//    CKM_KIP_MAC                        (0x00000512L),
//
//    CKM_CAMELLIA_KEY_GEN               (0x00000550L),
//    CKM_CAMELLIA_ECB                   (0x00000551L),
//    CKM_CAMELLIA_CBC                   (0x00000552L),
//    CKM_CAMELLIA_MAC                   (0x00000553L),
//    CKM_CAMELLIA_MAC_GENERAL           (0x00000554L),
//    CKM_CAMELLIA_CBC_PAD               (0x00000555L),
//    CKM_CAMELLIA_ECB_ENCRYPT_DATA      (0x00000556L),
//    CKM_CAMELLIA_CBC_ENCRYPT_DATA      (0x00000557L),
//    CKM_CAMELLIA_CTR                   (0x00000558L),
//
//    CKM_ARIA_KEY_GEN                   (0x00000560L),
//    CKM_ARIA_ECB                       (0x00000561L),
//    CKM_ARIA_CBC                       (0x00000562L),
//    CKM_ARIA_MAC                       (0x00000563L),
//    CKM_ARIA_MAC_GENERAL               (0x00000564L),
//    CKM_ARIA_CBC_PAD                   (0x00000565L),
//    CKM_ARIA_ECB_ENCRYPT_DATA          (0x00000566L),
//    CKM_ARIA_CBC_ENCRYPT_DATA          (0x00000567L),

//    CKM_SEED_KEY_GEN                   (0x00000650L),
//    CKM_SEED_ECB                       (0x00000651L),
//    CKM_SEED_CBC                       (0x00000652L),
//    CKM_SEED_MAC                       (0x00000653L),
//    CKM_SEED_MAC_GENERAL               (0x00000654L),
//    CKM_SEED_CBC_PAD                   (0x00000655L),
//    CKM_SEED_ECB_ENCRYPT_DATA          (0x00000656L),
//    CKM_SEED_CBC_ENCRYPT_DATA          (0x00000657L),
//
//    CKM_SKIPJACK_KEY_GEN               (0x00001000L),
//    CKM_SKIPJACK_ECB64                 (0x00001001L),
//    CKM_SKIPJACK_CBC64                 (0x00001002L),
//    CKM_SKIPJACK_OFB64                 (0x00001003L),
//    CKM_SKIPJACK_CFB64                 (0x00001004L),
//    CKM_SKIPJACK_CFB32                 (0x00001005L),
//    CKM_SKIPJACK_CFB16                 (0x00001006L),
//    CKM_SKIPJACK_CFB8                  (0x00001007L),
//    CKM_SKIPJACK_WRAP                  (0x00001008L),
//    CKM_SKIPJACK_PRIVATE_WRAP          (0x00001009L),
//    CKM_SKIPJACK_RELAYX                (0x0000100aL),
//    CKM_KEA_KEY_PAIR_GEN               (0x00001010L),
//    CKM_KEA_KEY_DERIVE                 (0x00001011L),
//    CKM_KEA_DERIVE                     (0x00001012L),
//    CKM_FORTEZZA_TIMESTAMP             (0x00001020L),
//    CKM_BATON_KEY_GEN                  (0x00001030L),
//    CKM_BATON_ECB128                   (0x00001031L),
//    CKM_BATON_ECB96                    (0x00001032L),
//    CKM_BATON_CBC128                   (0x00001033L),
//    CKM_BATON_COUNTER                  (0x00001034L),
//    CKM_BATON_SHUFFLE                  (0x00001035L),
//    CKM_BATON_WRAP                     (0x00001036L),
//
//    CKM_ECDSA_KEY_PAIR_GEN             (0x00001040L), /* Deprecated */
//    CKM_EC_KEY_PAIR_GEN                (0x00001040L),

    CKM_ECDSA                          (0x00001041L, "NONEwithECDSA", "http://ws.openecard.org/alg/ecdsa", KeyTypes.CKK_EC, null),
    CKM_ECDSA_SHA1                     (0x00001042L, "SHA1withECDSA", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1", KeyTypes.CKK_EC, CKM_SHA_1),
    CKM_ECDSA_SHA224                   (0x00001043L, "SHA224withECDSA", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224", KeyTypes.CKK_EC, CKM_SHA224),
    CKM_ECDSA_SHA256                   (0x00001044L, "SHA256withECDSA", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", KeyTypes.CKK_EC, CKM_SHA256),
    CKM_ECDSA_SHA384                   (0x00001045L, "SHA384withECDSA", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384", KeyTypes.CKK_EC, CKM_SHA384),
    CKM_ECDSA_SHA512                   (0x00001046L, "SHA512withECDSA", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512", KeyTypes.CKK_EC, CKM_SHA512);

//    CKM_ECDH1_DERIVE                   (0x00001050L),
//    CKM_ECDH1_COFACTOR_DERIVE          (0x00001051L),
//    CKM_ECMQV_DERIVE                   (0x00001052L),
//
//    CKM_ECDH_AES_KEY_WRAP              (0x00001053L),
//    CKM_RSA_AES_KEY_WRAP               (0x00001054L),
//
//    CKM_JUNIPER_KEY_GEN                (0x00001060L),
//    CKM_JUNIPER_ECB128                 (0x00001061L),
//    CKM_JUNIPER_CBC128                 (0x00001062L),
//    CKM_JUNIPER_COUNTER                (0x00001063L),
//    CKM_JUNIPER_SHUFFLE                (0x00001064L),
//    CKM_JUNIPER_WRAP                   (0x00001065L),
//    CKM_FASTHASH                       (0x00001070L),
//
//    CKM_AES_KEY_GEN                    (0x00001080L),
//    CKM_AES_ECB                        (0x00001081L),
//    CKM_AES_CBC                        (0x00001082L),
//    CKM_AES_MAC                        (0x00001083L),
//    CKM_AES_MAC_GENERAL                (0x00001084L),
//    CKM_AES_CBC_PAD                    (0x00001085L),
//    CKM_AES_CTR                        (0x00001086L),
//    CKM_AES_GCM                        (0x00001087L),
//    CKM_AES_CCM                        (0x00001088L),
//    CKM_AES_CTS                        (0x00001089L),
//    CKM_AES_CMAC                       (0x0000108AL),
//    CKM_AES_CMAC_GENERAL               (0x0000108BL),
//
//    CKM_AES_XCBC_MAC                   (0x0000108CL),
//    CKM_AES_XCBC_MAC_96                (0x0000108DL),
//    CKM_AES_GMAC                       (0x0000108EL),
//
//    CKM_BLOWFISH_KEY_GEN               (0x00001090L),
//    CKM_BLOWFISH_CBC                   (0x00001091L),
//    CKM_TWOFISH_KEY_GEN                (0x00001092L),
//    CKM_TWOFISH_CBC                    (0x00001093L),
//    CKM_BLOWFISH_CBC_PAD               (0x00001094L),
//    CKM_TWOFISH_CBC_PAD                (0x00001095L),

//    CKM_DES_ECB_ENCRYPT_DATA           (0x00001100L),
//    CKM_DES_CBC_ENCRYPT_DATA           (0x00001101L),
//    CKM_DES3_ECB_ENCRYPT_DATA          (0x00001102L),
//    CKM_DES3_CBC_ENCRYPT_DATA          (0x00001103L),
//    CKM_AES_ECB_ENCRYPT_DATA           (0x00001104L),
//    CKM_AES_CBC_ENCRYPT_DATA           (0x00001105L),
//
//    CKM_GOSTR3410_KEY_PAIR_GEN         (0x00001200L),
//    CKM_GOSTR3410                      (0x00001201L),
//    CKM_GOSTR3410_WITH_GOSTR3411       (0x00001202L),
//    CKM_GOSTR3410_KEY_WRAP             (0x00001203L),
//    CKM_GOSTR3410_DERIVE               (0x00001204L),
//    CKM_GOSTR3411                      (0x00001210L),
//    CKM_GOSTR3411_HMAC                 (0x00001211L),
//    CKM_GOST28147_KEY_GEN              (0x00001220L),
//    CKM_GOST28147_ECB                  (0x00001221L),
//    CKM_GOST28147                      (0x00001222L),
//    CKM_GOST28147_MAC                  (0x00001223L),
//    CKM_GOST28147_KEY_WRAP             (0x00001224L),
//
//    CKM_DSA_PARAMETER_GEN              (0x00002000L),
//    CKM_DH_PKCS_PARAMETER_GEN          (0x00002001L),
//    CKM_X9_42_DH_PARAMETER_GEN         (0x00002002L),
//    CKM_DSA_PROBABLISTIC_PARAMETER_GEN (0x00002003L),
//    CKM_DSA_SHAWE_TAYLOR_PARAMETER_GEN (0x00002004L),
//
//    CKM_AES_OFB                        (0x00002104L),
//    CKM_AES_CFB64                      (0x00002105L),
//    CKM_AES_CFB8                       (0x00002106L),
//    CKM_AES_CFB128                     (0x00002107L),

//    CKM_AES_CFB1                       (0x00002108L),
//    CKM_AES_KEY_WRAP                   (0x00002109L),     /* WAS: 0x00001090 */
//    CKM_AES_KEY_WRAP_PAD               (0x0000210AL),     /* WAS: 0x00001091 */

//    CKM_RSA_PKCS_TPM_1_1               (0x00004001L),
//    CKM_RSA_PKCS_OAEP_TPM_1_1          (0x00004002L);

//    CKM_VENDOR_DEFINED                 (0x80000000L);


    private static final HashMap<Long, SignatureAlgorithms> mechanismLookup;
    private static final HashMap<String, SignatureAlgorithms> jcaLookup;
    private static final HashMap<String, SignatureAlgorithms> algIdLookup;

    static {
	SignatureAlgorithms[] values = values();

	mechanismLookup = new HashMap<>(values.length);
	jcaLookup = new HashMap<>(values.length);
	algIdLookup = new HashMap<>(values.length);

	for (SignatureAlgorithms next : values) {
	    mechanismLookup.put(next.getPkcs11Mechanism(), next);
	    if (next.jcaAlg != null) {
		jcaLookup.put(next.jcaAlg, next);
	    }
	    if (next.algId != null) {
		algIdLookup.put(next.algId, next);
	    }
	}
    }

    private final long pkcs11MechanismId;
    private final String jcaAlg;
    private final String algId;
    private final KeyTypes keyType;
    private final HashAlgorithms hashAlg;

    private SignatureAlgorithms(long id, @Nonnull String jcaAlg, @Nonnull String algId, @Nonnull KeyTypes keyType,
	    @Nullable HashAlgorithms hashAlg) {
	this.pkcs11MechanismId = id;
	this.jcaAlg = jcaAlg;
	this.algId = algId;
	this.keyType = keyType;
	this.hashAlg = hashAlg;
    }

    @Nonnull
    public long getPkcs11Mechanism() {
	return pkcs11MechanismId;
    }

    @Nonnull
    public String getJcaAlg() {
	return jcaAlg;
    }

    @Nonnull
    public String getAlgId() {
	return algId;
    }

    @Nonnull
    public KeyTypes getKeyType() {
	return keyType;
    }

    @Nullable
    public HashAlgorithms getHashAlg() {
	return hashAlg;
    }

    public boolean isRsaPss() {
	switch (this) {
	case CKM_SHA1_RSA_PKCS_PSS:
	case CKM_SHA256_RSA_PKCS_PSS:
	case CKM_SHA384_RSA_PKCS_PSS:
	case CKM_SHA512_RSA_PKCS_PSS:
	    return true;
	default:
	    return false;
	}
    }

    public boolean isRsaSsa() {
	return getKeyType() == KeyTypes.CKK_RSA && getHashAlg() != null && !isRsaPss();
    }

    @Nonnull
    public static SignatureAlgorithms fromMechanismId(long id) throws UnsupportedAlgorithmException {
	SignatureAlgorithms result = mechanismLookup.get(id);
	if (result == null) {
	    throw new UnsupportedAlgorithmException(String.format("No mechanism defined for ID %08x.", id));
	} else {
	    return result;
	}
    }

    @Nonnull
    public static SignatureAlgorithms fromJcaName(String jcaName) throws UnsupportedAlgorithmException {
	SignatureAlgorithms a = jcaLookup.get(jcaName);
	if (a == null) {
	    throw new UnsupportedAlgorithmException("No JCA Name " + jcaName + " available.");
	} else {
	    return a;
	}
    }

    @Nonnull
    public static SignatureAlgorithms fromAlgId(String algId) throws UnsupportedAlgorithmException {
	SignatureAlgorithms a = algIdLookup.get(algId);
	if (a == null) {
	    throw new UnsupportedAlgorithmException("No Algorithm ID " + algId + " available.");
	} else {
	    return a;
	}
    }


    @Nonnull
    public static String algIdtoJcaName(@Nonnull String oid) throws UnsupportedAlgorithmException {
	SignatureAlgorithms m = algIdLookup.get(oid);
	if (m == null) {
	    throw new UnsupportedAlgorithmException("No JCA Name available for OID " + oid + ".");
	} else {
	    return m.getJcaAlg();
	}
    }

    @Nonnull
    public static String jcaNameToAlgId(@Nonnull String jcaAlgName) throws UnsupportedAlgorithmException {
	SignatureAlgorithms m = jcaLookup.get(jcaAlgName);
	if (m == null) {
	    throw new UnsupportedAlgorithmException("No OID available for JCA Name " + jcaAlgName + ".");
	} else {
	    return m.getAlgId();
	}
    }

}
