package com.borysmukovnin.quester.models.actions

import com.borysmukovnin.quester.models.dataclasses.Action
import com.borysmukovnin.quester.models.dataclasses.Mode
import org.bukkit.entity.Player

class ExpAction : Action {

    private var amount: Int = 0
    private var mode: Mode = com.borysmukovnin.quester.models.dataclasses.Mode.GAIN

    var Amount: Int
        get() = amount
        set(value) {
            amount = value
        }
    var Mode: Mode
        get() = mode
        set(value) {
            mode = value
        }

    override fun execute(player: Player) {
        if (mode == com.borysmukovnin.quester.models.dataclasses.Mode.GAIN) {
            player.giveExp(amount)
        } else if (mode == com.borysmukovnin.quester.models.dataclasses.Mode.LOSE) {
            val newTotal = (player.totalExperience - amount).coerceAtLeast(0)
            player.totalExperience = 0
            player.level = 0
            player.exp = 0f
            player.giveExp(newTotal)
        }
    }
    override fun deepCopy(): Action {
        val copy = ExpAction()
        copy.Amount = this.amount
        return copy
    }

}