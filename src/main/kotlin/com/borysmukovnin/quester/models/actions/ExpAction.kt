package com.borysmukovnin.quester.models.actions

import com.borysmukovnin.quester.models.Action
import org.bukkit.entity.Player

class ExpAction : Action {

    private var amount: Int = 0

    var Amount: Int
        get() = amount
        set(value) {
            amount = value
        }

    override fun execute(player: Player) {
        player.giveExp(amount)
    }
    override fun deepCopy(): Action {
        val copy = ExpAction()
        copy.Amount = this.amount
        return copy
    }

}