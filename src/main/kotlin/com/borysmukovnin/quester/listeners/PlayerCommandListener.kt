package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.dataclasses.Status
import com.borysmukovnin.quester.models.objectives.CommandObjective
import com.borysmukovnin.quester.quests.QuestManager
import com.borysmukovnin.quester.utils.nextIncompleteStage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class PlayerCommandListener : Listener {

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        val message = event.message
        val quests = QuestManager.getActivePlayerQuests(player.uniqueId)

        quests.filter { it.value.Status == Status.ACTIVE }.forEach { (name,pqd) ->
            val quest = pqd.Quest
            val stage = quest.nextIncompleteStage() ?: return
            stage.Objectives.forEach { objective ->
                if (objective is CommandObjective) {
                    if (message.startsWith("/${objective.Command}")) {
                        objective.ProgressCurrent += 1
                        player.sendMessage("${quest.Name} | ${stage.Name} Progress: ${objective.ProgressCurrent} / ${objective.ProgressGoal}")
                    }
                }
            }
        }
    }
}