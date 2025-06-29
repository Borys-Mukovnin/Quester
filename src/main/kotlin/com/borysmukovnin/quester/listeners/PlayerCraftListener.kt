package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.CraftObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryType

class PlayerCraftListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onCraft(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        if (event.inventory.type != InventoryType.WORKBENCH) return

        val crafted = event.recipe.result

        QuestEventProcessor.handle<CraftObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                obj.Item?.any { crafted.isSimilar(it) } == true
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent += crafted.amount
            }
        )
    }
}
