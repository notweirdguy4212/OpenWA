package com.callcatcher.next.screening

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockRulesTest {
    @Test fun exactNumberMatchIsBlocked() {
        assertTrue(BlockRules.isBlocked("+441234567890", setOf("+441234567890")))
    }

    @Test fun formattingDoesNotChangeMatch() {
        assertTrue(BlockRules.isBlocked("+44 1234 567890", setOf("+441234567890")))
    }

    @Test fun unrelatedNumberIsAllowed() {
        assertFalse(BlockRules.isBlocked("+441111111111", setOf("+442222222222")))
    }

    @Test fun unknownNumberIsNotBlockedByDefault() {
        assertFalse(BlockRules.isBlocked(null, setOf("+441234567890")))
    }

    @Test fun international00PrefixCanonicalizes() {
        assertTrue(BlockRules.isBlocked("0044 1234 567890", setOf("+441234567890")))
    }
}
