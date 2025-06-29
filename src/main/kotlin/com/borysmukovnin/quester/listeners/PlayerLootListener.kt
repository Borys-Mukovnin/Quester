package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.LootObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import com.borysmukovnin.quester.utils.isContainer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent

class PlayerLootListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onLoot(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clickedInventory = event.clickedInventory ?: return
        val clickedItem = event.currentItem ?: return
        if (!event.isShiftClick && event.action != InventoryAction.MOVE_TO_OTHER_INVENTORY) return

        if (event.view.bottomInventory != player.inventory) return
        if (!clickedInventory.type.isContainer()) return

        QuestEventProcessor.handle<LootObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                val itemList = obj.Item
                itemList.isNullOrEmpty() || itemList.any { it.isSimilar(clickedItem) }
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent++
            }
        )
    }
}