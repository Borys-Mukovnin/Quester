package com.borysmukovnin.quester.models.objectives

import com.borysmukovnin.quester.models.dataclasses.Condition
import com.borysmukovnin.quester.models.dataclasses.Objective
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

class KillObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 1
    private var _item: List<ItemStack>? = null
    private var _target: List<EntityType>? = null
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
    var Item: List<ItemStack>?
        get() = _item
        set(value) {
            _item = value
        }
    var Target: List<EntityType>?
        get() = _target
        set(value) {
            _target = value
        }

    override fun isComplete(): Boolean {
        return ProgressCurrent >= ProgressGoal
    }
    override fun deepCopy(): Objective {
        val copy = KillObjective()
        copy.ProgressCurrent = this._progressCurrent
        copy.ProgressGoal = this._progressGoal
        copy.Item = this._item?.map { it.clone() }?.toMutableList()
        copy.Target = this._target?.toMutableList()  // EntityType is enum-like, shallow copy is fine
        return copy
    }

}