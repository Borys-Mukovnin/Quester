package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.PickObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class PlayerPickListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val pickedItem = event.item.itemStack

        QuestEventProcessor.handle<PickObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                obj.Item?.any { pickedItem.isSimilar(it) } ?: true
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent += pickedItem.amount
            }
        )
    }
}
