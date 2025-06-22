package com.borysmukovnin.quester.models.conditions

import com.borysmukovnin.quester.models.dataclasses.Condition
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

class AdvancementCondition : Condition {
    private var _condtions: MutableList<String> = mutableListOf("minecraft:story/root")

    var Advancements: MutableList<String>
        get() = _condtions
        set(value) {
            _condtions = value.map { "minecraft:$it" }.toMutableList()
        }

    override fun isMet(player: Player): Boolean {
        val advancementMatch = _condtions.any {adv ->
            val advancement = Bukkit.getAdvancement(NamespacedKey.minecraft(adv)) ?: return false
            player.getAdvancementProgress(advancement).isDone
        }
        return advancementMatch
    }

    override fun deepCopy(): Condition {
        val copy = AdvancementCondition()
        copy.Advancements = this._condtions.map { it.removePrefix("minecraft:") }.toMutableList()
        return copy
    }
}