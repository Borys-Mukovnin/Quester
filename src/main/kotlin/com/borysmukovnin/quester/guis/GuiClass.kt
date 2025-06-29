package com.borysmukovnin.quester.guis

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class GuiClass(protected val player: Player) : InventoryHolder {
    protected var _inventory: Inventory = Bukkit.createInventory(this, 54, Component.text(""))

    protected abstract fun build()

    fun open() {
        build()
        player.openInventory(_inventory)
    }

    override fun getInventory(): Inventory = _inventory
}
