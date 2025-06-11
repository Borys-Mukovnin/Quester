package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.Condition
import org.bukkit.entity.Player

class HealthCondition : Condition {
    private var _minHealth: Double = 0.0
    private var _maxHealth: Double = 99999.0

    var MinHealth: Double
        get() = _minHealth
        set(value) {
            _minHealth = value
        }
    var MaxHealth: Double
        get() = _maxHealth
        set(value) {
            _maxHealth = value
        }

    override fun isMet(player: Player): Boolean {
        val health = player.health

        return health in _minHealth.._maxHealth
    }
    override fun deepCopy(): Condition {
        val copy = HealthCondition()
        copy.MinHealth = this._minHealth
        copy.MaxHealth = this._maxHealth
        return copy
    }

}