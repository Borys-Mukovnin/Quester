package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
import com.borysmukovnin.quester.models.dataclasses.ItemLocation
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemCondition : Condition {
    private var _location: ItemLocation = ItemLocation.ANY
    private var _itemType: MutableList<ItemStack> = mutableListOf()

    var Location: ItemLocation
        get() = _location
        set(value) {
            _location = value
        }

    var ItemType: MutableList<ItemStack>
        get() = _itemType
        set(value) {
            _itemType = value
        }

    override fun isMet(player: Player): Boolean {
        val location = _location
        var counter = 0

        when (location) {
            ItemLocation.MAIN_HAND -> counter += checkItem(player.inventory.itemInMainHand)
            ItemLocation.OFF_HAND -> counter += checkItem(player.inventory.itemInOffHand)
            ItemLocation.INVENTORY -> player.inventory.contents.forEach { counter += checkItem(it) }
            ItemLocation.ANY -> {
                counter += checkItem(player.inventory.itemInMainHand)
                counter += checkItem(player.inventory.itemInOffHand)
                player.inventory.contents.forEach { counter += checkItem(it) }
            }
            else -> {}
        }

        return counter >= 1
    }

    override fun deepCopy(): Condition {
        val copy = ItemCondition()

        copy.Location = this._location

        // Deep copy ItemStacks using .clone() to avoid shared references
        copy.ItemType = this._itemType.map { it.clone() }.toMutableList()

        return copy
    }

    private fun checkItem(itemStack: ItemStack?): Int {
        if (itemStack == null || itemStack.isEmpty) {
            return 0
        }

        var matchCount = 0
        for (requiredItem in _itemType) {
            if (itemStack.isSimilar(requiredItem)) {
                matchCount++
            }
        }
        return matchCount
    }
}