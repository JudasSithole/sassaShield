package com.example.myapplication.network

import com.google.gson.annotations.SerializedName

data class AbstractPhoneResponse(
    val phone: String,
    val valid: Boolean,
    val type: String?, // mobile, landline, voip
    val carrier: String?,
    val location: String?,
    @SerializedName("risk_assessment")
    val riskAssessment: RiskAssessment?
)

data class RiskAssessment(
    @SerializedName("risk_level")
    val riskLevel: String?, // low, medium, high
    @SerializedName("is_disposable")
    val isDisposable: Boolean?,
    @SerializedName("is_abuse_detected")
    val isAbuseDetected: Boolean?,
    @SerializedName("is_voip")
    val isVoip: Boolean?
)
