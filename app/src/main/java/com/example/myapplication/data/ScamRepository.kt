package com.example.myapplication.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScamRepository(context: Context) {
    private val prefs = context.getSharedPreferences("scams", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _scams = MutableStateFlow<List<ScamMessage>>(loadScams())
    val scams: StateFlow<List<ScamMessage>> = _scams

    private fun loadScams(): List<ScamMessage> {
        val json = prefs.getString("scam_list", null) ?: return emptyList()
        val type = object : TypeToken<List<ScamMessage>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addScam(scam: ScamMessage) {
        val current = _scams.value.toMutableList()
        current.add(0, scam)
        saveScams(current)
        _scams.value = current
    }

    fun deleteScam(scam: ScamMessage) {
        val current = _scams.value.toMutableList()
        current.remove(scam)
        saveScams(current)
        _scams.value = current
    }

    private fun saveScams(list: List<ScamMessage>) {
        prefs.edit().putString("scam_list", gson.toJson(list)).apply()
    }
}
