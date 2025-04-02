package com.borysmukovnin.quester.Models.Objectives

import com.borysmukovnin.quester.Models.Objective
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class PickObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 0
    private var _item: MutableList<ItemStack>? = null
    private var _amount: Int? = 1

    override var ProgressCurrent: Int
        get() = _progressCurrent
        set(value) {
            _progressCurrent = value
        }
    override var ProgressGoal: Int
        get() = _progressGoal
        set(value) {
            _progressGoal = value
        }
    var Item: MutableList<ItemStack>?
        get() = _item
        set(value) {
            _item = value
        }
    var Amount: Int?
        get() = _amount
        set(value) {
            _amount = value
        }
    override fun isComplete(): Boolean {
        return ProgressCurrent == ProgressGoal
    }
}