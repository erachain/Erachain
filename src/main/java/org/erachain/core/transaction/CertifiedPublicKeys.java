package org.erachain.core.transaction;

import org.erachain.core.account.PublicKeyAccount;

import java.util.List;

public interface CertifiedPublicKeys {
    List<PublicKeyAccount> getCertifiedPublicKeys();
}
