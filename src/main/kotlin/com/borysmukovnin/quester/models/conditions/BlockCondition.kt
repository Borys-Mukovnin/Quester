package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
import org.bukkit.Material
import org.bukkit.entity.Player

class BlockCondition : Condition {
    private var _block: MutableList<Material> = mutableListOf(Material.DIRT)

    var Block: MutableList<Material>
        get() = _block
        set(value) {
            _block = value
        }

    override fun isMet(player: Player): Boolean {
        val blockMatch = _block.any { b ->
            player.location.block.type == b
        }
        return blockMatch
    }
    override fun deepCopy(): Condition {
        val copy = BlockCondition()
        copy.Block = this._block.toMutableList()
        return copy
    }

}