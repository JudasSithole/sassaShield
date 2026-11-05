package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScamDetectorTest {

    @Test
    fun `detects SASSA PIN expiry scam`() {
        val result = ScamDetector.analyzeMessage("Your SASSA PIN has expired. Click here to verify: bit.ly/fake-sassa")
        assertTrue(result.isScam)
        assertNotNull(result.reason)
        assertTrue(result.confidence >= 0.6f)
    }

    @Test
    fun `detects SASSA grant scam with unofficial link`() {
        val result = ScamDetector.analyzeMessage("Collect your R350 grant here: http://scam-site.com/sassa")
        assertTrue(result.isScam)
        assertNotNull(result.reason)
        assertTrue(result.reason!!.contains("Unofficial SASSA link"))
    }

    @Test
    fun `considers official gov za link as safe`() {
        val result = ScamDetector.analyzeMessage("Visit www.sassa.gov.za for official info")
        assertFalse(result.isScam)
    }

    @Test
    fun `detects bank details update scam`() {
        val result = ScamDetector.analyzeMessage("Update your bank details immediately to receive your SASSA grant")
        assertTrue(result.isScam)
    }

    @Test
    fun `detects urgent threatening language`() {
        val result = ScamDetector.analyzeMessage("Final notice: your SASSA PIN will be cancelled immediately")
        assertTrue(result.isScam)
    }

    @Test
    fun `detects SASSA plus OTP combo`() {
        val result = ScamDetector.analyzeMessage("SASSA OTP verification required. Enter your PIN now.")
        assertTrue(result.isScam)
    }

    @Test
    fun `safe message is not flagged`() {
        val result = ScamDetector.analyzeMessage("Hello, your grant payment has been deposited into your account.")
        assertFalse(result.isScam)
        assertNull(result.reason)
    }

    @Test
    fun `empty message is not flagged`() {
        val result = ScamDetector.analyzeMessage("")
        assertFalse(result.isScam)
        assertNull(result.reason)
    }

    @Test
    fun `confidence is clamped between 0 and 1`() {
        val result = ScamDetector.analyzeMessage("SASSA grant pin verify bank details update login collect immediately cancel final notice expired r350 payday bonus gold card postbank otp re-register srd https://evil.com/scam")
        assertTrue(result.isScam)
        assertTrue(result.confidence <= 1.0f)
        assertTrue(result.confidence >= 0.0f)
    }

    @Test
    fun `isOfficialGovLink returns true for gov za domains`() {
        assertTrue(ScamDetector.isOfficialGovLink("https://www.sassa.gov.za"))
        assertTrue(ScamDetector.isOfficialGovLink("http://sassa.gov.za/services"))
        assertTrue(ScamDetector.isOfficialGovLink("www.gov.za"))
    }

    @Test
    fun `isOfficialGovLink returns false for non gov za domains`() {
        assertFalse(ScamDetector.isOfficialGovLink("https://bit.ly/fake-sassa"))
        assertFalse(ScamDetector.isOfficialGovLink("http://scam-site.com"))
        assertFalse(ScamDetector.isOfficialGovLink("sassa.com"))
    }
}
