/*
 * R3 Proprietary and Confidential
 *
 * Copyright (c) 2018 R3 Limited.  All rights reserved.
 *
 * The intellectual and technical concepts contained herein are proprietary to R3 and its suppliers and are protected by trade secret law.
 *
 * Distribution of this file or any portion thereof via any medium without the express permission of R3 is strictly prohibited.
 */

package net.corda.notaryhealthcheck.contract

import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

/**
 * Minimal contract to use for checking that notarisation works
 */
class NullContract : Contract {
    override fun verify(tx: LedgerTransaction) {}
}