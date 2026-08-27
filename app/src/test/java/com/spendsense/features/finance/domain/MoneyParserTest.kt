package com.spendsense.features.finance.domain

import com.spendsense.core.utils.MoneyParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyParserTest {
    @Test
    fun parsesInrAmountToMinorUnits() {
        assertEquals(42855L, MoneyParser.firstAmountMinor("INR 428.55 debited"))
    }

    @Test
    fun parsesCommaSeparatedRupeeAmount() {
        assertEquals(124000L, MoneyParser.firstAmountMinor("₹1,240.00 paid to SWIGGY"))
    }

    @Test
    fun verifiesEquivalentAmountAppearsInText() {
        assertTrue(MoneyParser.amountAppearsInText(68000L, "Rs. 680 debited at SHELL"))
    }
}
