package com.borysmukovnin.quester.listeners

import com.borysmukovnin.quester.models.objectives.EnchantObjective
import com.borysmukovnin.quester.quests.QuestEventProcessor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent

class PlayerEnchantListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEnchant(event: EnchantItemEvent) {
        val player = event.enchanter
        val enchantedItem = event.item
        val enchants = event.enchantsToAdd.keys

        QuestEventProcessor.handle<EnchantObjective>(
            player,
            event,
            objectiveMatcher = { obj, _ ->
                val enchantOk = obj.Enchant?.any { it in enchants } ?: true
                val itemOk = obj.Item?.any { enchantedItem.isSimilar(it) } ?: true
                enchantOk && itemOk
            },
            objectiveProcessor = { obj, _, _ ->
                obj.ProgressCurrent += 1
            }
        )
    }
}
