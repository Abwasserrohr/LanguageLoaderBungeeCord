package me.skyroad.abwasserrohr

import me.skyroad.abwasserrohr.language.LanguageContainer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.IOException
import java.nio.file.Files

class LanguageLoader: Plugin() {

    companion object {
        lateinit var instance: Plugin
        lateinit var container: LanguageContainer
        lateinit var pluginFolder: File
        var fallbackLanguageCode: String = "en"
        val packs: ArrayList<String> = arrayListOf()
        var onlyLocal = false
    }

    override fun onEnable() {
        val file = File(dataFolder, "config.yml")
        if (!file.exists()) {
            try {
                getResourceAsStream("config.yml").use { `in` -> Files.copy(`in`, file.toPath()) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(dataFolder, "config.yml"))

        onlyLocal = config.getBoolean("localload")
        packs.addAll(config.getStringList("loadpacks"))

        config.getString("fallbackCode")?.let { fallbackLanguageCode = it }

        pluginFolder = dataFolder
        instance = this
        container = LanguageContainer()

    }

    override fun onDisable() {
    }
}
