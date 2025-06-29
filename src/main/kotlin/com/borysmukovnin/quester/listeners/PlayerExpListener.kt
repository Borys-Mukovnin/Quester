package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.dataclasses.Mode
import com.borysmukovnin.quester.models.objectives.ExpObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent

class PlayerExpListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onExpChange(event: PlayerExpChangeEvent) {
        val player = event.player
        val expChange = event.amount

        if (expChange == 0) return

        QuestEventProcessor.handle<ExpObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                when (obj.Mode) {
                    Mode.GAIN -> expChange > 0
                    Mode.LOSE -> expChange < 0
                }
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent += kotlin.math.abs(expChange)
            }
        )
    }
}
