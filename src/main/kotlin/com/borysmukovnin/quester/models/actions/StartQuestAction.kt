package com.borysmukovnin.quester.models.actions

import com.borysmukovnin.quester.models.dataclasses.Action
import com.borysmukovnin.quester.quests.QuestManager
import org.bukkit.entity.Player

class StartQuestAction : Action {
    private var quest: String = ""

    var Quest: String
        get() = quest
        set(value) {
            quest = value
        }

    override fun execute(player: Player) {
        QuestManager.startPlayerQuest(player,quest)
    }

    override fun deepCopy(): Action {
        val copy = StartQuestAction()
        copy.quest = this.quest
        return copy
    }

}