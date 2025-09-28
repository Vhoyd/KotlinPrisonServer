package dev.vhoyd.prisonServerKT

import dev.vhoyd.blockworks.block.BlockBreakAction
import dev.vhoyd.blockworks.block.BlockBreakSound
import dev.vhoyd.blockworks.block.BlockDataLoader
import dev.vhoyd.blockworks.block.BlockDefinition
import dev.vhoyd.blockworks.block.BlockInstance
import dev.vhoyd.blockworks.loot.ConditionalDrop
import dev.vhoyd.blockworks.loot.WeightedEntryPool
import dev.vhoyd.blockworks.loot.WeightedEntryUtil
import dev.vhoyd.blockworks.mining.MiningPlayer
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.WitherSkeleton
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class PrisonBlockDataLoader : BlockDataLoader {
    override fun loadAll() : List<BlockDefinition> {
        val bedrockDrop = ConditionalDrop(
            WeightedEntryPool(WeightedEntryUtil.single(5)),
            WeightedEntryPool(WeightedEntryUtil.single(ItemStack(Material.BEDROCK, 2)))
        ) { true }

        // bedrock
        val bedrock = BlockDefinition(
            Material.BEDROCK,
            bedrockDrop,
            0,
            250,
            BlockBreakSound(Sound.BLOCK_STONE_BREAK, 1f, 0.8f),
            Material.GRASS_BLOCK
        )

        val grassDrop = ConditionalDrop(
            WeightedEntryUtil.single(0),
            WeightedEntryUtil.single(ItemStack(Material.DIRT, 1))
        ) { true }
        val mossDrop = ConditionalDrop(
            WeightedEntryUtil.single(1),
            WeightedEntryUtil.single(ItemStack(Material.MOSS_BLOCK, 1))

        ) { (Random.nextInt() % 2) == 1 }


        // grass
        val grass = BlockDefinition(
            Material.GRASS_BLOCK,
            listOf(grassDrop, mossDrop),
            0,
            400,
            BlockBreakSound(Sound.BLOCK_GRASS_BREAK, 1f, 0.8f),
            Material.AIR
        )

        val barrierAction = BlockBreakAction { tile: BlockInstance, player: MiningPlayer ->
            val s = BlockBreakSound(Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1f, 1.2f)
            s.run(tile, player)
            tile.location.world
                .spawnParticle(Particle.ENCHANT, tile.location.clone().add(0.5, 0.5, 0.5), 25)
            if (Random.nextInt() % 3 == 0) {
                tile.location.block.type = Material.STONE
            }
        }

        val barrierDrop = ConditionalDrop(
            WeightedEntryUtil.single(15),
            WeightedEntryUtil.single(ItemStack(Material.AMETHYST_SHARD, 1))
        ) { true }


        // barrier
        val barrier = BlockDefinition(
            Material.BARRIER,
            barrierDrop,
            0,
            1000,
            barrierAction,
            Material.BEDROCK,
        )

        val stoneAction = BlockBreakAction { tile : BlockInstance, player : MiningPlayer ->
            BlockBreakSound(Sound.ENTITY_BLAZE_HURT, 1f, 0.5f).run(tile, player)
            tile.location.world.spawn(tile.location.clone().add(0.5, 0.0, 0.5), WitherSkeleton::class.java)
        }

        val stoneDrop = ConditionalDrop(
            expYield = WeightedEntryUtil.single(5),
            drops = WeightedEntryUtil.single(ItemStack(Material.AIR, 1), 0),
            condition = { _ : MiningPlayer -> true }
        )

        val stone = BlockDefinition(
            Material.STONE,
            stoneDrop,
            0,
            30,
            stoneAction,
            Material.COBBLESTONE
        )

        val reinforcedDeepslateAction = BlockBreakSound(Sound.BLOCK_DEEPSLATE_BREAK, 1f, 1f)
        val reinForcedDeepslate = BlockDefinition(
            Material.REINFORCED_DEEPSLATE,
            stoneDrop,
            0,
            10,
            reinforcedDeepslateAction,
            Material.REINFORCED_DEEPSLATE
        )

        return listOf(bedrock, grass, barrier, stone, reinForcedDeepslate)
    }
}