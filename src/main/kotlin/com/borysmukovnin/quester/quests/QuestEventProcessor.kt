package com.borysmukovnin.quester.quests

import com.borysmukovnin.quester.models.dataclasses.Objective
import com.borysmukovnin.quester.models.dataclasses.Status
import com.borysmukovnin.quester.utils.Configurator
import com.borysmukovnin.quester.utils.applyVariables
import com.borysmukovnin.quester.utils.asFormattedComponent
import com.borysmukovnin.quester.utils.nextIncompleteStage
import org.bukkit.entity.Player
import org.bukkit.event.Event

object QuestEventProcessor {
    inline fun <reified T : Objective> handle(
        player: Player,
        event: Event,
        crossinline objectiveMatcher: (T, Event) -> Boolean,
        crossinline objectiveProcessor: (T, Player, Event) -> Unit
    ) {
        val quests = QuestManager.getActivePlayerQuests(player.uniqueId)

        quests.filter { it.value.Status == Status.ACTIVE }.forEach { (name, pqd) ->
            val quest = pqd.Quest
            val stage = quest.nextIncompleteStage() ?: return@forEach

            stage.Objectives
                .filterIsInstance<T>()
                .filter { !it.isComplete() }
                .forEach objectiveLoop@{ objective ->

                    val conditionsMet = objective.Conditions?.all { it.isMet(player) } ?: true
                    if (!conditionsMet) return@objectiveLoop

                    if (objectiveMatcher(objective, event)) {
                        objectiveProcessor(objective, player, event)

                        player.sendMessage(
                            Configurator.lang.progressMessage
                                .applyVariables(player, quest, stage, objective)
                                .asFormattedComponent()
                        )
                    }

                    if (stage.IsComplete) {
                        stage.Actions.forEach { it.execute(player) }
                    }
                }

            if (quest.IsComplete) {
                quests[name] = pqd.copy(Status = Status.COMPLETED, TimesCompleted = pqd.TimesCompleted + 1)
            }
        }
    }
}
