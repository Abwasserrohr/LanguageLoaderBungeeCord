package me.skyroad.abwasserrohr

import net.md_5.bungee.api.connection.ProxiedPlayer

fun getLangCode(player: ProxiedPlayer): String {
    return player.langCode()
}

fun ProxiedPlayer.langCode(): String {
    return this.locale.toString().split("_")[0]
}
