package com.borysmukovnin.quester.utils

import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.models.dataclasses.*
import com.borysmukovnin.quester.quests.QuestManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

object Configurator {
    lateinit var plugin: Quester
    lateinit var mainGui: Gui
    lateinit var lang: Lang

    fun init(plugin: Quester) {
        this.plugin = plugin
        this.reload()
    }

    fun reload(sender: CommandSender? = null, onComplete: (() -> Unit)? = null) {
        if (sender == null) {
            this.loadConfig()
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(QuestManager.plugin, Runnable {
            this.loadConfig()

            Bukkit.getScheduler().runTask(QuestManager.plugin, Runnable {
                sender.sendMessage("Main configuration reload complete.")
            })

            onComplete?.invoke()
        })
    }

    private fun loadConfig() {
        val configFile = File(plugin.dataFolder, "config.yml")
        val config: YamlConfiguration = YamlConfiguration.loadConfiguration(configFile)


        val langc = config.getRequiredString("language",configFile.name) ?: "en"
        val langFile = File(plugin.dataFolder, "lang/$langc.yml")

        lang = parseLanguage(langFile)


        mainGui = parseMainGui(File(plugin.dataFolder, "gui/main.yml"))
    }

    private fun parseLanguage(configFile: File) : Lang {
        val config = YamlConfiguration.loadConfiguration(configFile)
        val progressMessage = config.getRequiredString("progress_message",configFile.name) ?: "{quest_name} | {stage_name} Progress: {objective_progress_current} / {objective_progress_goal}"

        return Lang(
            progressMessage = progressMessage
        )
    }

    private fun parseMainGui(configFile: File): Gui {
        val config = YamlConfiguration.loadConfiguration(configFile)
        val name = config.getString("name") ?: "Quests"
        val size = config.getInt("size")
        val itemList = mutableListOf<GuiItem>()

        val itemsSection = config.getMapList("items")
        for (itemMap in itemsSection) {
            val materialName = itemMap["material"] as? String ?: continue
            val material = Material.matchMaterial(materialName) ?: continue
            val amount = (itemMap["amount"] as? Int) ?: 1
            val model = (itemMap["model"] as? Int) ?: 0
            val displayName = itemMap["display_name"] as? String
            val description = (itemMap["description"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            val itemStack = ItemStack(material, amount)
            val meta = itemStack.itemMeta
            if (meta != null) {
                if (displayName != null) meta.displayName(displayName.asFormattedComponent())
                meta.lore(description.map { it.asFormattedComponent() })
                if (model > 0) meta.setCustomModelData(model)
                itemStack.itemMeta = meta
            }

            val positions = when (val positionString = itemMap["position"]) {
                is Int -> listOf(positionString)
                is String -> parseSlotPositions(positionString)
                is List<*> -> positionString.mapNotNull {
                    when (it) {
                        is Int -> it
                        is String -> parseSlotPositions(it).firstOrNull()
                        else -> null
                    }
                }
                else -> emptyList()
            }

            val leftClick = itemMap["left_click"] as? String
            val rightClick = itemMap["right_click"] as? String
            val middleClick = itemMap["middle_click"] as? String

            itemList.add(
                GuiItem(
                    itemStack = itemStack,
                    position = positions,
                    leftClick = leftClick,
                    rightClick = rightClick,
                    middleClick = middleClick
                )
            )
        }

        return Gui(name = name, size = size, items = itemList)
    }

    private fun parseSlotPositions(positionString: String): List<Int> {
        val positions = mutableListOf<Int>()
        val parts = positionString.split(",")
        for (part in parts) {
            if ("-" in part) {
                val (start, end) = part.split("-").mapNotNull { it.toIntOrNull() }
                if (start <= end) {
                    positions.addAll(start..end)
                }
            } else {
                part.toIntOrNull()?.let { positions.add(it) }
            }
        }
        return positions
    }
}