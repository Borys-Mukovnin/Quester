package com.borysmukovnin.quester.Models.Objectives

import com.borysmukovnin.quester.Models.Objective

class TradeObjective : Objective {
    private var _completion: Boolean = false
    private var _progressCurrent: Int = 0
    private var _progressGoal: Int = 0
    private var _amount: Int? = 1

    override var Completion: Boolean
        get() = _completion
        set(value) {
            _completion = value
        }
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
    var Amount: Int?
        get() = _amount
        set(value) {
            _amount = value
        }
}