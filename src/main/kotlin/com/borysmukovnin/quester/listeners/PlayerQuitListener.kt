package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.dialogs.DialogManager
import com.borysmukovnin.quester.quests.QuestManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        QuestManager.saveQuestProgressAsync(event.player) {
            DialogManager.savePlayerCompletedDialogAsync(event.player)
        }

    }
}