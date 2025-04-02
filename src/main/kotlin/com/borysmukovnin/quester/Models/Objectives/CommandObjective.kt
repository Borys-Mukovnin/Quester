package com.borysmukovnin.quester.Models.Objectives

import com.borysmukovnin.quester.Models.Objective
import org.bukkit.entity.Player

class CommandObjective : Objective {
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 0
    private var _command: String = "say Hello world!"

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

    var Command: String
        get() = _command
        set(value) {
            _command = value
        }
    override fun isComplete(): Boolean {
        return ProgressCurrent == ProgressGoal
    }
}