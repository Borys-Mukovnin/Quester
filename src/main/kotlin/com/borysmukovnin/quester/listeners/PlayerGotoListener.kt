package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.GotoObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerGotoListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        val to = event.to

        QuestEventProcessor.handle<GotoObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                val target = obj.Goto
                to.world?.name == target.world?.name &&
                        to.blockX == target.blockX &&
                        to.blockY == target.blockY &&
                        to.blockZ == target.blockZ
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent = obj.ProgressGoal
            }
        )
    }
}
