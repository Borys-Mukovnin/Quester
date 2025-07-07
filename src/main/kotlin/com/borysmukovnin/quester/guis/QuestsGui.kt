package com.borysmukovnin.quester.guis

import com.borysmukovnin.quester.models.dataclasses.PlayerQuestData
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.applyVariables
import com.borysmukovnin.quester.utils.asFormattedComponent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuestsGui(player: Player) : GuiClass(player) {

    override fun build(page: Int) {
        val name = "{player_name}'s Quests".applyVariables(player).asFormattedComponent()
        _inventory = Bukkit.createInventory(this, 54, name)

        val quests: Map<String, PlayerQuestData> = QuestManager.getActivePlayerQuests(player.uniqueId)
        val emptySlots = getEmptySlots()
//        val emptySlots = listOf(
//            10, 11, 12, 13, 14, 15, 16,
//            19, 20, 21, 22, 23, 24, 25,
//            28, 29, 30, 31, 32, 33, 34,
//            37, 38, 39, 40, 41, 42, 43
//        )

        val questItems = quests.values.map { playerQuestData ->
            val quest = playerQuestData.Quest

            val lore: MutableList<Component> = mutableListOf()

            for ((_, stage) in quest.Stages) {
                lore += "<gray><bold>Stage:</bold> ${stage.Name}".asFormattedComponent()

                for (objective in stage.Objectives) {
                    val     className = try {
                        objective::class.simpleName ?: ""
                    } catch (_: Exception) {
                        ""
                    }

                    val progressLine = if (className.isNotEmpty()) {
                        "<white>${className}</white>: {objective_progress_current}/{objective_progress_goal}"
                    } else {
                        "<white>{objective_progress_current}/{objective_progress_goal}"
                    }

                    lore += progressLine
                        .applyVariables(player, quest, stage, objective)
                        .asFormattedComponent()
                }
            }

            ItemStack(Material.BOOK).apply {
                itemMeta = itemMeta?.apply {
                    displayName(Component.text(quest.Name))
                    lore(lore)
                }
            }
        }

        for ((index, slot) in emptySlots.withIndex()) {
            if (index >= questItems.size) break
            _inventory.setItem(slot, questItems[index])
        }

    }
}