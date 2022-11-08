package edu.foodfun

import com.lin.cardlib.CardSetting
import com.lin.cardlib.utils.ReItemTouchHelper

class RecommendCardSetting : CardSetting() {
    override fun isLoopCard(): Boolean {
        return false
    }

    override fun getSwipeDirection(): Int {
        return (ReItemTouchHelper.LEFT or ReItemTouchHelper.RIGHT or ReItemTouchHelper.DOWN)
    }

    override fun couldSwipeOutDirection(): Int {
        return (ReItemTouchHelper.LEFT or ReItemTouchHelper.RIGHT or ReItemTouchHelper.DOWN)
    }
}