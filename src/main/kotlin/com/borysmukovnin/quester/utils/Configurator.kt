package com.borysmukovnin.quester.utils

import com.borysmukovnin.quester.Quester
import com.borysmukovnin.quester.models.dataclasses.*
import com.borysmukovnin.quester.quests.QuestManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

object Configurator {
    lateinit var plugin: Quester
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


    }

    private fun parseLanguage(configFile: File) : Lang {
        val config = YamlConfiguration.loadConfiguration(configFile)
        val progressMessage = config.getRequiredString("progress_message",configFile.name) ?: "{quest_name} | {stage_name} Progress: {objective_progress_current} / {objective_progress_goal}"

        return Lang(
            progressMessage = progressMessage
        )
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