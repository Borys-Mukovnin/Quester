package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.KillObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class PlayerKillListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityKill(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val weapon = killer.inventory.itemInMainHand
        val targetType = event.entity.type

        QuestEventProcessor.handle<KillObjective>(
            killer,
            event,
            objectiveMatcher = { obj, _ ->
                val targetMatches = obj.Target.isNullOrEmpty() || obj.Target!!.contains(targetType)
                val itemMatches = obj.Item.isNullOrEmpty() || obj.Item!!.any { it.isSimilar(weapon) }
                targetMatches && itemMatches
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent += 1
            }
        )
    }
}
