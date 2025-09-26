package mininglib.internal.event

import mininglib.core.MiningManager
import mininglib.block.BlockInstance
import mininglib.mining.MiningTool
import mininglib.mining.MiningPlayer
import mininglib.util.EmptyValue
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class MiningEventHandler : Listener {
    private val miningManager : MiningManager

    constructor(miningManager : MiningManager) {
        this.miningManager = miningManager
    }

    /**
     * Ensures a MiningPlayer object exists for any joining Player entity, and applies two potion effects:
     * Mining fatigue, so player clients do not prematurely break blocks;
     * haste, so players do not see a jarringly slow arm-swing animation.
     */
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        e.getPlayer().addPotionEffect(
            PotionEffect(
                PotionEffectType.MINING_FATIGUE,
                Int.MAX_VALUE,
                5,
                true,
                false,
                false
            )
        )
        e.getPlayer().addPotionEffect(
            PotionEffect(
                PotionEffectType.HASTE,
                Int.MAX_VALUE,
                1,
                true,
                false,
                false
            )
        )


        //create new MiningPlayer object to handle unregistered players
        if (miningManager.getMiningPlayer(e.getPlayer()) == null) {
            miningManager.registerPlayer(MiningPlayer(e.player, miningManager))

            //testing only
            e.getPlayer().inventory.addItem(miningManager.testStick.itemStack.clone())
        }
    }


    //update mining stats to reflect new item when switching items
    /**
     * Updates tracked breaking tool for the Player's MiningPlayer representation to ensure proper block breaking behavior.
     */
    @EventHandler
    fun onPlayerSwitchItem(e: PlayerItemHeldEvent) {
        val item = e.getPlayer().inventory.getItem(e.newSlot)
        val miningTool: MiningTool = miningManager.evaluateItem(item) //defaults to hand stats if no custom item is found
        val mp: MiningPlayer? = miningManager.getMiningPlayer(e.getPlayer())
        mp?.heldItem = miningTool
    }

    @EventHandler
    fun onPlayerHitBlock(e : BlockDamageEvent) {
        e.isCancelled = true
        val player = miningManager.getMiningPlayer(e.player)
        val block = miningManager.getBlock(e.block.type)
        if (block != EmptyValue.BLOCKDEFINITION && player?.currentBlock?.location != e.block.location) {
            player?.currentBlock = BlockInstance(block, e.block.location, miningManager.config)
        }

    }

    @EventHandler
    fun onPlayerStopHittingBlock(e : BlockDamageAbortEvent) {
        val player = miningManager.getMiningPlayer(e.player)
        player?.currentBlock = EmptyValue.BLOCKINSTANCE
    }
}