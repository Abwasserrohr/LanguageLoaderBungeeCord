package me.skyroad.abwasserrohr

import net.md_5.bungee.api.connection.ProxiedPlayer


fun getMessage(pack: String, lang: String, messageKey: String): String {
    return LanguageLoader.container.get(pack, lang, messageKey, null)
}

fun getMessage(pack: String, lang: String, messageKey: String, replaceMap: HashMap<String, String>): String {
    return LanguageLoader.container.get(pack, lang, messageKey, replaceMap)
}

fun ProxiedPlayer.sendTranslatedMessage(pack: String, messageKey: String, sendPrefix: Boolean, replaceMap: HashMap<String, String>) {
    var message = ""
    val langCode = this.locale.toString().split("_")[0]
    if (sendPrefix) { message += getMessage("general", langCode, "prefix") + " " }
    message += getMessage(pack, langCode, messageKey, replaceMap)
    this.sendMessage(message)
}

fun ProxiedPlayer.sendTranslatedMessage(pack: String, messageKey: String, sendPrefix: Boolean = true) {
    var message = ""
    val langCode = this.locale.toString().split("_")[0]
    if (sendPrefix) { message += getMessage("general", langCode, "prefix") + " " }
    message += getMessage(pack, langCode, messageKey)
    this.sendMessage(message)
}

fun sendMessage(player: ProxiedPlayer, pack: String, messageKey: String, sendPrefix: Boolean, replaceMap: HashMap<String, String>) {
    player.sendTranslatedMessage(pack, messageKey, sendPrefix, replaceMap)
}

fun sendMessage(player: ProxiedPlayer, pack: String, messageKey: String, sendPrefix: Boolean = true) {
    player.sendTranslatedMessage(pack, messageKey, sendPrefix)
}

fun sendMessage(player: ProxiedPlayer, pack: String, messageKey: String) {
    player.sendTranslatedMessage(pack, messageKey)
}
