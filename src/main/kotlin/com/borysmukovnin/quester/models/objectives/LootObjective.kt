package com.borysmukovnin.quester.models.objectives

import com.borysmukovnin.quester.models.Objective
import org.bukkit.inventory.ItemStack

class LootObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 0
    private var _item: MutableList<ItemStack>? = null

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

    override fun isComplete(): Boolean {
        return ProgressCurrent == ProgressGoal
    }
    override fun deepCopy(): Objective {
        val copy = LootObjective()
        copy.ProgressCurrent = this._progressCurrent
        copy.ProgressGoal = this._progressGoal
        copy.Item = this._item?.map { it.clone() }?.toMutableList()
        return copy
    }

}