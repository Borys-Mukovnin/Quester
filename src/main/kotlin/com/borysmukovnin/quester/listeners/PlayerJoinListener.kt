package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.dialogs.DialogManager
import com.borysmukovnin.quester.quests.QuestManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        QuestManager.loadActivePlayerQuestsAsync(event.player) {
            DialogManager.loadPlayerCompletedDialogAsync(event.player)
        }
    }
}