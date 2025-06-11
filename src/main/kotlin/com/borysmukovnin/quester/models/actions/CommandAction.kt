package com.borysmukovnin.quester.models.actions

import com.borysmukovnin.quester.models.Action
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class CommandAction : Action {
    private var command: String = ""
    // {player_name} variable
    var Command: String
        get() = command
        set(value) {
            command = value
        }
    override fun execute(player: Player) {
        val parsedCommand = command.replace("{player_name}", player.name)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand)
    }
    override fun deepCopy(): Action {
        val copy = CommandAction()
        copy.Command = this.command
        return copy
    }

}