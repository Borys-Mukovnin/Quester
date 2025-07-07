package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.guis.QuestsGui
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class QuestGuiListener : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder

        if (holder is QuestsGui) {
            event.isCancelled = true

            val clickedSlot = event.slot
            val player = holder.player
        }
    }
}