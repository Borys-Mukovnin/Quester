package com.borysmukovnin.quester.guis

import com.borysmukovnin.quester.utils.Configurator
import com.borysmukovnin.quester.utils.applyVariables
import com.borysmukovnin.quester.utils.asFormattedComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class MainGui(player: Player) : GuiClass(player) {

    override fun build() {
        val gui = Configurator.mainGui
        val name = gui.name.applyVariables(player).asFormattedComponent()
        _inventory = Bukkit.createInventory(this, gui.size, name)

        gui.items.forEach { item ->
            val clonedItem = item.itemStack.clone()
            val meta = clonedItem.itemMeta
            if (meta != null) {
                meta.displayName(meta.displayName()?.let { component ->
                    val text = (component as? net.kyori.adventure.text.TextComponent)?.content() ?: return@let component
                    text.applyVariables(player).asFormattedComponent()
                })

                meta.lore(meta.lore()?.map { component ->
                    val text = (component as? net.kyori.adventure.text.TextComponent)?.content() ?: ""
                    text.applyVariables(player).asFormattedComponent()
                })

                clonedItem.itemMeta = meta
            }

            item.position.forEach { slot ->
                if (slot in 0 until gui.size) {
                    inventory.setItem(slot, clonedItem)
                }
            }
        }
    }
}