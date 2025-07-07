package com.borysmukovnin.quester.guis

import com.borysmukovnin.quester.utils.applyVariables
import com.borysmukovnin.quester.utils.asFormattedComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class MainGui(player: Player) : GuiClass(player) {

    override fun build(page: Int) {
        val name = "Quests menu".applyVariables(player).asFormattedComponent()
        _inventory = Bukkit.createInventory(this, 54, name)
    }
}