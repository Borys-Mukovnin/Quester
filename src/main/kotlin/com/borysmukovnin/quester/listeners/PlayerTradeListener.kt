package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.TradeObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class PlayerTradeListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTrade(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory
        if (inventory.type != InventoryType.MERCHANT) return

        if (event.rawSlot != inventory.size - 1) return
        val result = inventory.getItem(inventory.size - 1) ?: return

        QuestEventProcessor.handle<TradeObjective>(
            player,
            event,
            objectiveMatcher = { _, _ -> true },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent++
            }
        )
    }
}
