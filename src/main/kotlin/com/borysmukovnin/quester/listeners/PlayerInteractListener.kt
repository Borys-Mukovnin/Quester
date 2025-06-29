package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.InteractObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock ?: return

        QuestEventProcessor.handle<InteractObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                obj.Block?.contains(block.type) == true
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent += 1
            }
        )
    }
}
