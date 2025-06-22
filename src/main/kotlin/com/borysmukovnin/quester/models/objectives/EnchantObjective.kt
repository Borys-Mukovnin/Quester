package com.borysmukovnin.quester.models.objectives

import com.borysmukovnin.quester.models.dataclasses.Condition
import com.borysmukovnin.quester.models.dataclasses.Objective
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class EnchantObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 1
    private var _enchantment: List<Enchantment>? = null
    private var _item: List<ItemStack>? = null
    private var conditions: List<Condition>? = null

    override var Conditions: List<Condition>?
        get() = conditions
        set(value) {
            conditions = value
        }

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
    var Enchant: List<Enchantment>?
        get() = _enchantment
        set(value) {
            _enchantment = value
        }
    var Item: List<ItemStack>?
        get() = _item
        set(value) {
            _item = value
        }

    override fun isComplete(): Boolean {
        return ProgressCurrent == ProgressGoal
    }
    override fun deepCopy(): Objective {
        val copy = EnchantObjective()
        copy.ProgressCurrent = this._progressCurrent
        copy.ProgressGoal = this._progressGoal
        copy.Enchant = this._enchantment?.toList()
        copy.Item = this._item?.map { it.clone() }?.toList()
        return copy
    }

}