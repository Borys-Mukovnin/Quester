package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.MineObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class PlayerMineListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val brokenBlock = event.block

        QuestEventProcessor.handle<MineObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                obj.Block?.contains(brokenBlock.type) ?: true
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent++
            }
        )
    }
}
