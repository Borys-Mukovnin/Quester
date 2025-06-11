package com.borysmukovnin.quester.models.actions

import com.borysmukovnin.quester.models.Action
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemAction : Action {

    private var item: ItemStack = ItemStack(Material.DIRT)

    var Item: ItemStack
        get() = item
        set(value) {
            item = value
        }

    override fun execute(player: Player) {
        player.inventory.addItem(item)
    }
    override fun deepCopy(): Action {
        val copy = ItemAction()
        copy.Item = this.item.clone() // clone() makes a deep copy of ItemStack
        return copy
    }


}