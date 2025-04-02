package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemCondition : Condition {
    private var _location: MutableList<String> = mutableListOf("ANY")
    private var _itemType: MutableList<ItemStack> = mutableListOf()
    private var _amount: Int = 1

    var Location: MutableList<String>
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

    override fun isFulfiled(player: Player): Boolean {
        var counter = 0

        if (_location[0] == "ANY") {
            _location = mutableListOf("MAINHAND", "OFFHAND", "INVENTORY")
        }

        for (location in _location) {
            when (location) {
                "MAINHAND" -> {
                    counter += checkItem(player.inventory.itemInMainHand)
                }
                "OFFHAND" -> {
                    counter += checkItem(player.inventory.itemInOffHand)
                }
                "INVENTORY" -> {
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