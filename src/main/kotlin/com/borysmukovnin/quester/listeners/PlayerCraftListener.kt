package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.dataclasses.Status
import com.borysmukovnin.quester.models.objectives.CommandObjective
import com.borysmukovnin.quester.models.objectives.CraftObjective
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.nextIncompleteStage
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class PlayerCraftListener : Listener {

    @EventHandler
    fun onCommand(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        if (event.inventory.type != InventoryType.WORKBENCH) return
        val craftedItem = event.recipe.result

        val quests = QuestManager.getActivePlayerQuests(player.uniqueId)

        quests.filter { it.value.Status == Status.ACTIVE }.forEach { (name,pqd) ->
            val quest = pqd.Quest
            val stage = quest.nextIncompleteStage() ?: return
            stage.Objectives.forEach { objective ->
                if (objective is CraftObjective) {

                    objective.Item?.forEach { item ->
                        if (craftedItem.isSimilar(item)) {
                            objective.ProgressCurrent += craftedItem.amount
                            player.sendMessage("${quest.Name} | ${stage.Name} Progress: ${objective.ProgressCurrent} / ${objective.ProgressGoal}")
                        }
                    }
                }
            }
        }
    }
}