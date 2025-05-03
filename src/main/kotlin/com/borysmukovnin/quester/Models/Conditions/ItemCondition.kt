package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import com.borysmukovnin.quester.Models.ItemLocation
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemCondition : Condition {
    private var _location: MutableList<ItemLocation> = mutableListOf(ItemLocation.ANY)
    private var _itemType: MutableList<ItemStack> = mutableListOf()
    private var _amount: Int = 1

    var Location: MutableList<ItemLocation>
        get() = _location
        set(value) {
            _location = value
        }

    var ItemType: MutableList<ItemStack>
        get() = _itemType
        set(value) {
            _itemType = value
        }

    var Amount: Int
        get() = _amount
        set(value) {
            _amount = value
        }

    override fun isFulfilled(player: Player): Boolean {
        var counter = 0

        if (_location[0] == ItemLocation.ANY) {
            _location = mutableListOf(ItemLocation.MAIN_HAND, ItemLocation.OFF_HAND, ItemLocation.INVENTORY)
        }

        for (location in _location) {
            when (location) {
                ItemLocation.MAIN_HAND -> {
                    counter += checkItem(player.inventory.itemInMainHand)
                }
                ItemLocation.OFF_HAND -> {
                    counter += checkItem(player.inventory.itemInOffHand)
                }
                ItemLocation.INVENTORY -> {
                    for (itemStack in player.inventory.contents) {
                        counter += checkItem(itemStack)
                    }
                }
                else -> {
                    continue
                }
            }
        }

        return counter >= _amount
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