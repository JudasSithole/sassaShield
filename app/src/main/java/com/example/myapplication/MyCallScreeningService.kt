package com.example.myapplication

import android.telecom.Call
import android.telecom.CallScreeningService
import com.example.myapplication.data.ScamCall
import com.example.myapplication.data.ScamDatabase
import com.example.myapplication.repository.PhoneRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyCallScreeningService : CallScreeningService() {

    private val repository = PhoneRepository()

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            val db = ScamDatabase.getDatabase(applicationContext)
            val dao = db.scamDao()

            var shouldBlock = false
            var wasFlagged = false
            var riskLevel = "unknown"
            var reason = ""

            // 1. Check Manual Blocklist (100% Offline)
            if (dao.isManuallyBlocked(phoneNumber)) {
                shouldBlock = true
                reason = "Manually blocked by user"
                riskLevel = "high"
            }

            // 2. Check Local Cache (100% Offline)
            if (!shouldBlock) {
                val cachedCall = dao.getScamCallByNumber(phoneNumber)
                if (cachedCall != null && (cachedCall.riskLevel == "high" || cachedCall.wasBlocked)) {
                    shouldBlock = true
                    reason = "Known scammer (cached)"
                    riskLevel = cachedCall.riskLevel
                }
            }

            // 3. International Heuristic (100% Offline)
            // SASSA is South African (+27). If a call is international and not from +27, flag it.
            if (!shouldBlock && !phoneNumber.startsWith("+27") && phoneNumber.startsWith("+")) {
                wasFlagged = true
                reason = "International caller (Potential Scam)"
                riskLevel = "medium"
            }

            // 4. API Check (Requires Network)
            if (!shouldBlock) {
                val response = repository.validatePhone(phoneNumber)
                if (response != null && response.valid) {
                    val risk = response.riskAssessment
                    riskLevel = risk?.riskLevel ?: "low"
                    val isAbuse = risk?.isAbuseDetected == true

                    if (riskLevel == "high" || isAbuse) {
                        shouldBlock = true
                        reason = "High risk detected by AI"
                    } else if (riskLevel == "medium") {
                        wasFlagged = true
                        reason = "Medium risk detected by AI"
                    }

                    // Update Cache
                    dao.insertScamCall(
                        ScamCall(
                            phoneNumber = phoneNumber,
                            riskLevel = riskLevel,
                            carrier = response.carrier,
                            location = response.location,
                            wasBlocked = shouldBlock
                        )
                    )
                }
            }

            val responseBuilder = CallResponse.Builder()
            if (shouldBlock) {
                responseBuilder.setDisallowCall(true)
                responseBuilder.setRejectCall(true)
                responseBuilder.setSkipCallLog(false)
                responseBuilder.setSkipNotification(true)
            }

            if (shouldBlock || wasFlagged) {
                NotificationHelper.showScamNotification(
                    applicationContext,
                    if (shouldBlock) "Call Blocked" else "Scam Warning",
                    "$reason: $phoneNumber"
                )
            }

            respondToCall(callDetails, responseBuilder.build())
        }
    }
}
