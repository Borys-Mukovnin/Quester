package com.borysmukovnin.quester.Models.Objectives

import com.borysmukovnin.quester.Models.Objective
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class KillObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 0
    private var _item: MutableList<ItemStack>? = null
    private var _target: MutableList<EntityType>? = null
    private var _targetAmount: Int? = 1

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
    var Target: MutableList<EntityType>?
        get() = _target
        set(value) {
            _target = value
        }
    var TargetAmount : Int?
        get() = _targetAmount
        set(value) {
            _targetAmount = value
        }
    override fun isComplete(): Boolean {
        return ProgressCurrent == ProgressGoal
    }
}