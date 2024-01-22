package ru.rutoken.tech.bouncycastle

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jcajce.provider.asymmetric.ecgost12.KeyFactorySpi
import java.io.IOException
import java.security.PublicKey

/**
 * Workaround of the Bouncy Castle issue [#1586](https://github.com/bcgit/bc-java/issues/1586). Should be removed when
 * the problem is resolved.
 *
 * Adds the missing digestParamSet algorithm parameter if it isn't set, since Bouncy Castle always expects at least two
 * parameters to be present: publicKeyParamSet and digestParamSet.
 */
class Gost2012KeyFactorySpi : KeyFactorySpi() {
    override fun generatePublic(keyInfo: SubjectPublicKeyInfo): PublicKey {
        val algOid = keyInfo.algorithm.algorithm
        if (algOid.isValid()) {
            val parameters = ASN1Sequence.getInstance(keyInfo.algorithm.parameters)
            // Bouncy Castle works fine if SubjectPublicKey has two or more parameters.
            if (parameters.size() > 1)
                return super.generatePublic(keyInfo)

            // Adding the digest algorithm to the parameters field of SubjectPublicKeyInfo structure.
            val desiredParameters = ASN1EncodableVector(2).apply {
                add(parameters.getObjectAt(0))
                add(algOid.getDigestAlgorithm())
            }
            val desiredAlgId = AlgorithmIdentifier(keyInfo.algorithm.algorithm, DERSequence(desiredParameters))
            val desiredSubPubKeyInfo = SubjectPublicKeyInfo(desiredAlgId, keyInfo.publicKeyData)

            return super.generatePublic(desiredSubPubKeyInfo)
        } else {
            throw IOException("algorithm identifier $algOid in key not recognised")
        }
    }
}

private fun ASN1ObjectIdentifier.isValid(): Boolean {
    return this == RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256 ||
            this == RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512 ||
            this == RosstandartObjectIdentifiers.id_tc26_agreement_gost_3410_12_256 ||
            this == RosstandartObjectIdentifiers.id_tc26_agreement_gost_3410_12_512
}

private fun ASN1ObjectIdentifier.getDigestAlgorithm(): ASN1ObjectIdentifier {
    return when (this) {
        RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256,
        RosstandartObjectIdentifiers.id_tc26_agreement_gost_3410_12_256 ->
            RosstandartObjectIdentifiers.id_tc26_gost_3411_12_256

        RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512,
        RosstandartObjectIdentifiers.id_tc26_agreement_gost_3410_12_512 ->
            RosstandartObjectIdentifiers.id_tc26_gost_3411_12_512

        else -> throw IllegalArgumentException("Unknown algorithm $this")
    }
}
