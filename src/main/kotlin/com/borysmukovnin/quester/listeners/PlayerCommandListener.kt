package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.CommandObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class PlayerCommandListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player

        QuestEventProcessor.handle<CommandObjective>(
            player,
            event,
            objectiveMatcher = objectiveMatcher@{ obj, e ->
                val msg = (e as? PlayerCommandPreprocessEvent)?.message ?: return@objectiveMatcher false
                msg.startsWith("/${obj.Command}")
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent += 1
            }
        )
    }
}
