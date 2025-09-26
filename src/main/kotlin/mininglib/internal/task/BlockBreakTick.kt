package mininglib.internal.task

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.BlockPosition
import mininglib.core.Config
import mininglib.core.MiningManager
import mininglib.block.BlockInstance
import mininglib.mining.MiningPlayer
import mininglib.util.EmptyValue
import org.bukkit.Location
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class BlockBreakTick : BukkitRunnable {
    private val miningManager : MiningManager
    private val config : Config
    private val manager = ProtocolLibrary.getProtocolManager()

    constructor(miningManager : MiningManager) {
        this.miningManager = miningManager
        config = miningManager.config
    }

    private fun generateBlockBreakPacket(player : Player, location : Location, progress : Float) {
        val packet = PacketContainer(PacketType.Play.Server.BLOCK_BREAK_ANIMATION)
        packet.integers.write(0, -player.entityId)
        packet.blockPositionModifier.write(0, BlockPosition(location.blockX,location.blockY, location.blockZ))
        packet.integers.write(1, (progress * 9).toInt())
        manager.sendServerPacket(player, packet)
    }



    private fun calculateDropAmount(baseAmount : Int, fortune: Int) : Int {
        if (miningManager.config.ignoreMiningFortune) return baseAmount

        // everyone always has 1x normal drops, intrinsically, so add 1 to multiplier so it's not ever 0
        val fortuneMultiplier = 1 + fortune / 100

        var total = baseAmount * fortuneMultiplier
        val bonusFortune = fortune % 100
        val remainder : Double

        val fortuneAsPercentage = bonusFortune.toDouble() / 100

        if (miningManager.config.dynamicFortuneScaling) {
            val bonus = fortuneAsPercentage * baseAmount
            total += bonus.toInt()
            remainder = (bonus % 1) * 100
        } else {
            remainder = fortuneAsPercentage
        }
        if (Math.random() <= remainder) total += baseAmount

        return total
    }

    override fun run() {
        for (player : MiningPlayer in miningManager.players) {
            val currentTile = player.currentBlock
            if (currentTile != EmptyValue.BLOCKINSTANCE) {
                //increase damage progress
                currentTile.damage += player.miningSpeed
                player.minecraftPlayer.server.consoleSender.sendMessage(currentTile.damage.toString())
                player.minecraftPlayer.server.consoleSender.sendMessage(currentTile.strength.toString())
                /*
                 * getProgress returns 0-9, break stage values are 0-9
                 * since blocks start off with no animation, an extraneous value (I used -1) is needed
                 */
                generateBlockBreakPacket(player.minecraftPlayer, currentTile.location, currentTile.getProgress())

                if (currentTile.isBroken() && currentTile.canDrop) {

                    //remove block
                    currentTile.canDrop = false

                    currentTile.location.block.type = currentTile.block.brokenMaterial
                    generateBlockBreakPacket(player.minecraftPlayer, currentTile.location, 0f)

                    //play registered action at broken block
                    currentTile.block.brokenAction.run(currentTile, player)

                    //create loot for drops
                    for (conditionalDrop in currentTile.block.possibleDrops) {

                        if (conditionalDrop.condition(player)) {

                            // drop exp if any
                            val expValue = conditionalDrop.exp.pickRandom()
                            if (expValue > 0) {
                                val orb = player.minecraftPlayer.world.spawn(
                                    currentTile.location.clone().add(0.5, 0.5, 0.5),
                                    ExperienceOrb::class.java
                                )
                                orb.experience = expValue
                            }

                            // drop loot if any
                            val droppedItem = conditionalDrop.drops.pickRandom().clone()
                            val baseAmount = droppedItem.amount
                            if (baseAmount > 0) {
                                var quantity = calculateDropAmount(baseAmount, player.miningFortune)
                                while (quantity > 64) {
                                    quantity -= 64
                                    droppedItem.amount = 64
                                    player.minecraftPlayer.world.dropItemNaturally(
                                        currentTile.location,
                                        droppedItem.clone()
                                    )
                                }

                                if (quantity > 0) {
                                    droppedItem.amount = quantity
                                    player.minecraftPlayer.world.dropItemNaturally(currentTile.location, droppedItem)
                                }
                            }
                        }
                    }
                    val newBlock = miningManager.getBlock(currentTile.location.block.type)
                    if (newBlock != EmptyValue.BLOCKDEFINITION) {
                        player.currentBlock = BlockInstance(newBlock, currentTile.location, miningManager.config)
                    }
                }

            }
        }
    }
}