package me.skyroad.abwasserrohr.language

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.skyroad.abwasserrohr.LanguageLoader
import net.md_5.bungee.api.ChatColor
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.net.URL
import kotlin.Exception

class LanguageContainer {
    private val loadedPacks = hashMapOf<String, String>()
    // HashMap<Pack,HashMap<LanguageCode,HashMap<MessageKey, MessageString>>>
    private val container: HashMap<String,HashMap<String, HashMap<String, String>>> = hashMapOf()
    private lateinit var languages: Map<String, ArrayList<String>>

    init {

        println("[LanguageLoader] Trying to load language packs: ${LanguageLoader.packs}")

        val packList = loadText(URL("https://raw.githubusercontent.com/Abwasserrohr/LanguageLoader/main/Language/packs.json"))
        val availablePacks = Json.decodeFromString<LanguagePacks>(packList)

        LanguageLoader.packs.forEach { requestedPack ->
            if (requestedPack == "all") {
                availablePacks.packs.forEach { pack ->
                    loadedPacks[pack.key] = pack.value
                }
            } else {
                availablePacks.packs.forEach { pack ->
                    if (pack.key == requestedPack) {
                        loadedPacks[pack.key] = pack.value
                    }
                }
            }
        }
        var loadedMessages = 0
        loadedPacks.forEach { pack ->
            val yaml = Yaml()
            languages = yaml.load<Map<String, ArrayList<String>>>(loadText(URL(pack.value)))
            (languages["languages"])?.forEach { languageCode ->
                val packURL = pack.value.replace(".yml", "_$languageCode.yml")
                val data = yaml.load<Map<String, String>>(loadText(URL(packURL)))

                container[pack.key] = container[pack.key] ?: hashMapOf()

                val languageContainer = container[pack.key]?.get(languageCode) ?: hashMapOf()
                container[pack.key]?.set(languageCode, languageContainer)

                container[pack.key]?.get(languageCode)?.let {
                    data.forEach { languageData ->
                        it[languageData.key] = ChatColor.translateAlternateColorCodes('&', languageData.value)
                        loadedMessages++
                    }
                }

                val loopContainer = container.clone() as HashMap<String, HashMap<String, HashMap<String, String>>>
                loopContainer[pack.key]?.get(languageCode)?.forEach {
                    it.value
                    if (it.value.contains("%{")) {
                        val replaceInnerData = it.value.split("%{")
                        replaceInnerData.forEach { replacement ->
                            val replaceString = replacement.split("}%")[0]
                            val replaceKeys = replaceString.split("/")
                            loopContainer[replaceKeys[0]]?.get(languageCode)?.get(replaceKeys[1])?.let { translatedValue ->
                                container[replaceKeys[0]]?.get(languageCode)?.set(replaceKeys[1], translatedValue)
                            }
                        }
                    }
                }
            }
        }

        // Trying to apply all %{packName/translationKey}% variables in the files.
        (languages["languages"])?.forEach { languageCode ->
            loadedPacks.forEach { pack ->
                val loopContainer = container.clone() as HashMap<String, HashMap<String, HashMap<String, String>>>
                loopContainer[pack.key]?.get(languageCode)?.forEach {
                    if (it.value.contains("%{")) {
                        val replaceInnerData = it.value.split("%{")
                        replaceInnerData.forEach { replacement ->
                            try {
                                val replaceString = replacement.split("}%")[0]
                                val replaceKeys = replaceString.split("/")
                                if (replaceKeys.size == 2) {
                                    loopContainer[replaceKeys[0]]?.get(languageCode)?.get(replaceKeys[1])
                                        ?.let { translatedValue ->
                                            container[replaceKeys[0]]?.get(languageCode)?.get(it.key)?.replace(
                                                "%{${replaceKeys[0]}/${replaceKeys[1]}}%",
                                                translatedValue
                                            )?.let { newValue ->
                                                container[replaceKeys[0]]?.get(languageCode)?.set(it.key, newValue)
                                            }
                                        }
                                }
                            } catch (e: Exception) { println("Error while applying internal translations: $e") }
                        }
                    }
                }
            }
        }
        println("[LanguageLoader] Loaded $loadedMessages messages.")
    }

    fun get(pack: String, lang: String, messageKey: String, replaceMap: HashMap<String, String>?): String {
        container[pack]?.get(lang)?.get(messageKey)?.let {
            var message = it
            replaceMap?.forEach { replaceValue ->
                message = message.replace(replaceValue.key, replaceValue.value)
            }
            return message
        } ?: run {
            // The requested translation is not available, try for the fallback language code instead.
            container[pack]?.get(LanguageLoader.fallbackLanguageCode)?.get(messageKey)?.let {
                var message = it
                replaceMap?.forEach { replaceValue ->
                    message = message.replace(replaceValue.key, replaceValue.value)
                }
                return message
            }
        }
        // There is no entry for the requested language code or the fallback language code, send error.
        return "[missing: \"$pack\" \"$lang\" \"$messageKey\"]"
    }

    private fun loadText(url: URL): String {
        if (LanguageLoader.onlyLocal) {
            return File("${LanguageLoader.pluginFolder}/packs/${url.file.split("/").last()}").readText(Charsets.UTF_8)
        }
        return try {
            val text = url.readText(Charsets.UTF_8)
            File("${LanguageLoader.pluginFolder}/packs/").mkdirs()
            File("${LanguageLoader.pluginFolder}/packs/${url.file.split("/").last()}").writeText(text, Charsets.UTF_8)
            text
        } catch (e: Exception) {
            val text = File("${LanguageLoader.pluginFolder}/packs/${url.file.split("/").last()}").readText(Charsets.UTF_8)
            println("Couldn't load language data  from web: $e")
            text
        }
    }
}
