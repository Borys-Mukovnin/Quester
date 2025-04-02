package com.borysmukovnin.quester.Models.Conditions

import com.borysmukovnin.quester.Models.Condition
import org.bukkit.Material
import org.bukkit.entity.Player

class BlockCondition : Condition {
    private var _block: MutableList<Material> = mutableListOf(Material.DIRT)

    var Block: MutableList<Material>
        get() = _block
        set(value) {
            _block = value
        }

    override fun isFulfiled(player: Player): Boolean {
        val blockMatch = _block.any { b ->
            player.location.block.type == b
        }
        return blockMatch
    }
}