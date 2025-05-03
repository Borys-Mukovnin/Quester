package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import org.bukkit.entity.Player

class PermissionCondition : Condition {
    private var _permission: MutableList<String> = mutableListOf()

    var Permission: MutableList<String>
        get() = _permission
        set(value) {
            _permission = value
        }

    override fun isFulfilled(player: Player): Boolean {
        val permissionMatch = _permission.any {perm ->
            player.hasPermission(perm)
        }
        return permissionMatch
    }
}