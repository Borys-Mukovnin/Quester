package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.Condition
import com.borysmukovnin.quester.models.ItemLocation
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

    override fun isMet(player: Player): Boolean {
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

    override fun deepCopy(): Condition {
        val copy = ItemCondition()

        // Copy locations (enums, safe with toMutableList)
        copy.Location = this._location.toMutableList()

        // Deep copy ItemStacks using .clone() to avoid shared references
        copy.ItemType = this._itemType.map { it.clone() }.toMutableList()

        // Copy amount (primitive)
        copy.Amount = this._amount

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