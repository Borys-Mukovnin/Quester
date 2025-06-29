package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.PlaceObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class PlayerPlaceListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val placedBlock = event.blockPlaced.type

        QuestEventProcessor.handle<PlaceObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                obj.Block?.contains(placedBlock) ?: true
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent++
            }
        )
    }
}
