package com.callcatcher.next.screening

import org.junit.Assert.*
import org.junit.Test

class SpamIntelligenceTest {
    @Test fun explicitlyBlockedNumberIsHighRiskAndBlocked() {
        val result = SpamIntelligence.assess("+441234567890", setOf("+441234567890"))
        assertEquals(100, result.score)
        assertTrue(result.shouldBlock)
    }

    @Test fun failedVerificationIsFlaggedButNotAutomaticallyBlocked() {
        val result = SpamIntelligence.assess("+441234567890", emptySet(), verificationFailed = true)
        assertEquals(75, result.score)
        assertFalse(result.shouldBlock)
    }

    @Test fun repeatedDigitPatternIsOnlyAReviewSignal() {
        val result = SpamIntelligence.assess("1111111111", emptySet())
        assertEquals(35, result.score)
        assertFalse(result.shouldBlock)
    }

    @Test fun unknownCallerIsNotAutomaticallyBlocked() {
        val result = SpamIntelligence.assess(null, emptySet())
        assertFalse(result.shouldBlock)
    }

    @Test fun internationalPrefixIsCanonicalized() {
        assertEquals("+4412345", BlockRules.normalizeNumber("0044 12345"))
    }
}
