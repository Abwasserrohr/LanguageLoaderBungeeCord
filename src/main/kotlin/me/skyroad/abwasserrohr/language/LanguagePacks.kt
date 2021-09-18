package me.skyroad.abwasserrohr.language

import kotlinx.serialization.Serializable

@Serializable
data class LanguagePacks(var packs: HashMap<String, String>)
