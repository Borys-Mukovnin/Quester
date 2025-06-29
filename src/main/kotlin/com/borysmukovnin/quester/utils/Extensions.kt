package com.borysmukovnin.quester.utils

import com.borysmukovnin.quester.models.dataclasses.Objective
import com.borysmukovnin.quester.models.dataclasses.Options
import com.borysmukovnin.quester.models.dataclasses.Quest
import com.borysmukovnin.quester.models.dataclasses.Stage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

fun Quest.deepCopy(): Quest {
    return Quest(
        Name = this.Name,
        Description = this.Description.toList(),
        Options = this.Options,
        StartConditions = this.StartConditions,
        StartActions = this.StartActions,
        Stages = this.Stages.mapValues { it.value.deepCopy() }.toMutableMap()
    )
}
fun Quest.nextIncompleteStage(): Stage? {
    return Stages.values
        .firstOrNull { !it.IsComplete }
}
fun Stage.deepCopy(): Stage {
    return Stage(
        Name = this.Name,
        Description = this.Description.toList(),
        Objectives = this.Objectives.map { it.deepCopy() }.toMutableList(),
        Options = this.Options,
        Actions = this.Actions
    )
}
fun Options.deepCopy(): Options {
    return Options(
        Repeatable = this.Repeatable,
        Cancelable = this.Cancelable,
        TimeLimit = this.TimeLimit,
        StartDate = this.StartDate,
        EndDate = this.EndDate
    )
}
fun InventoryType.isContainer(): Boolean {
    return when (this) {
        InventoryType.CHEST,
        InventoryType.BARREL,
        InventoryType.ENDER_CHEST,
        InventoryType.SHULKER_BOX,
        InventoryType.DROPPER,
        InventoryType.DISPENSER,
        InventoryType.HOPPER,
        InventoryType.FURNACE,
        InventoryType.BLAST_FURNACE,
        InventoryType.SMOKER,
        InventoryType.BREWING,
        InventoryType.BEACON,
        InventoryType.CARTOGRAPHY,
        InventoryType.GRINDSTONE,
        InventoryType.LOOM,
        InventoryType.SMITHING,
        InventoryType.STONECUTTER,
        InventoryType.ANVIL,
        InventoryType.ENCHANTING,
        InventoryType.LECTERN,
        InventoryType.MERCHANT -> true
        else -> false
    }
}


fun ConfigurationSection.getRequiredString(key: String, fileName: String): String? =
    getString(key).takeUnless { it.isNullOrBlank() } ?: run {
        PluginLogger.logWarning("Missing or invalid '$key' in file '$fileName'. Replacing with default value")
        null
    }

fun ConfigurationSection.getRequiredStringList(key: String, fileName: String): List<String>? =
    getStringList(key).takeUnless { it.isEmpty() } ?: run {
        PluginLogger.logWarning("Missing or empty list '$key' in file '$fileName'.")
        null
    }

fun ConfigurationSection.getRequiredConfigurationSection(key: String, fileName: String): ConfigurationSection? =
    getConfigurationSection(key) ?: run {
        PluginLogger.logWarning("Missing or invalid section '$key' in file '$fileName'.")
        null
    }
fun String.applyVariables(player: Player? = null,quest: Quest? = null,stage: Stage? = null,objective: Objective? = null): String {
    var result = this
    player?.let { result = result.replace("{player_name}", it.name) }

    quest?.let {
        result = result.replace("{quest_name}",it.Name) }

    stage?.let {
        result = result.replace("{stage_name}",it.Name)
    }

    objective?.let {
        result = result.replace("{objective_progress_current}",it.ProgressCurrent.toString())
        result = result.replace("{objective_progress_goal}",it.ProgressGoal.toString())
    }

    return result
}
fun String.asFormattedComponent(): Component {
    return MiniMessage.miniMessage().deserialize(this)
}


