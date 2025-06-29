package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.dataclasses.TravelMode
import com.borysmukovnin.quester.models.objectives.TravelObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerTravelListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player

        val from = event.from
        val to = event.to

        val distance = from.distance(to)

        if (distance == 0.0) return

        QuestEventProcessor.handle<TravelObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                val mode = obj.Mode ?: return@handle false
                when (mode) {
                    TravelMode.WALK -> !player.isSprinting && !player.isSwimming && !player.isFlying && !player.isInsideVehicle
                    TravelMode.SPRINT -> player.isSprinting
                    TravelMode.SWIM -> player.isSwimming
                    TravelMode.FLY -> player.isFlying && !player.isInsideVehicle
                    TravelMode.MOUNT -> player.isInsideVehicle
                }
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent += distance.toInt()
            }
        )
    }
}
