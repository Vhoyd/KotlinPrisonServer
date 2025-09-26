package mininglib.mining

import mininglib.core.MiningManager
import mininglib.block.BlockInstance
import mininglib.util.EmptyValue.Companion.BLOCKINSTANCE
import org.bukkit.entity.Player

// TODO: dependency injection for value math

/**
 * Class for tracking extra library data about any Player object, such as mining speed, current target block, etc.
 */
class MiningPlayer {
    var currentBlock: BlockInstance = BLOCKINSTANCE
    var heldItem: MiningTool
    var breakingPower: Int
        get() = heldItem.breakingPower
        set(value) {internalbreakingpower = value - heldItem.breakingPower}
    var miningSpeed : Int
        get() = heldItem.miningSpeed + internalspeed
        set(value) {internalspeed = value - heldItem.miningSpeed;}
    var miningFortune : Int
        get() = heldItem.miningFortune + internalfortune
        set(value) {internalfortune = value - heldItem.miningFortune;}
    val minecraftPlayer : Player

    private var internalspeed : Int = 0
    private var internalfortune : Int = 0
    private var internalbreakingpower : Int = 0

    constructor(player: Player, miningManager: MiningManager) : this(player, miningManager, 0, 0, 0)

    constructor(player: Player, miningManager: MiningManager, miningSpeed : Int, miningFortune: Int, breakingPower : Int) {
        this.minecraftPlayer = player
        this.internalspeed = miningSpeed
        this.internalfortune = miningFortune
        this.internalbreakingpower = breakingPower
        heldItem = miningManager.emptyHand
        miningManager.registerPlayer(this)
    }
}