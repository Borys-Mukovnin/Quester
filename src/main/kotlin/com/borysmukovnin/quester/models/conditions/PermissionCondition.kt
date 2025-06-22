package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
import org.bukkit.entity.Player

class PermissionCondition : Condition {
    private var _permission: MutableList<String> = mutableListOf()

    var Permission: MutableList<String>
        get() = _permission
        set(value) {
            _permission = value
        }

    override fun isMet(player: Player): Boolean {
        val permissionMatch = _permission.any {perm ->
            player.hasPermission(perm)
        }
        return permissionMatch
    }

    override fun deepCopy(): Condition {
        val copy = PermissionCondition()
        copy.Permission = this._permission.toMutableList()
        return copy
    }

}