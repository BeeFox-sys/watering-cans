package xyz.beefox.wateringcans;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Iterator;
import java.util.Random;

public class WateringCanItem extends Item {

    public int MAX_LEVEL = 64;

    public WateringCanItem(Settings settings){
        super(settings);
        this.getDefaultStack().getOrCreateNbt().putInt("level", 0);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);
        BlockHitResult blockHitResult = raycast(world, playerEntity, RaycastContext.FluidHandling.SOURCE_ONLY);
        if(blockHitResult.getType() == HitResult.Type.MISS){
            return TypedActionResult.pass(itemStack);
        } else {
            NbtCompound nbt = itemStack.getNbt();
            if(nbt == null){
                itemStack.getOrCreateNbt().putInt("level", 0);
                nbt = itemStack.getNbt();
            }
            int level = nbt.getInt("level");
            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (!world.canPlayerModifyAt(playerEntity, blockPos)) {
                    return TypedActionResult.pass(itemStack);
                }

                if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                    world.playSound(playerEntity, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    world.emitGameEvent(playerEntity, GameEvent.FLUID_PICKUP, blockPos);
                    itemStack.getNbt().putInt("level", MAX_LEVEL);
                    return TypedActionResult.success(itemStack);
                } else {
                    if(level <= 0){
                        return TypedActionResult.pass(itemStack);
                    }
                    BlockPos hitPos = blockHitResult.getBlockPos();
                    Iterable<BlockPos> blockList = BlockPos.iterateOutwards(hitPos, 1, 1, 1);
                    for (Iterator<BlockPos> blocks = blockList.iterator(); blocks.hasNext();){
                        BlockPos block = blocks.next();
                        if(world instanceof ServerWorld && !world.getBlockState(block).isAir() && world.getBlockState(block).getBlock() instanceof Fertilizable){
                            Fertilizable fertilizable = (Fertilizable)world.getBlockState(block).getBlock();
                            if(fertilizable.canGrow(world, world.random, block, world.getBlockState(block))){
                                fertilizable.grow((ServerWorld) world, world.random, block, world.getBlockState(block));
                            }
                        }
                    }
                    //Bonemeal Blocks in a 5x3x5 radius
                    ParticleUtil.spawnParticles(world, playerEntity.getBlockPos(), ParticleTypes.SPLASH, UniformIntProvider.create(5, 10), Direction.UP, () -> Vec3d.fromPolar(playerEntity.getPitch(), playerEntity.getYaw()).multiply(0.3), 1);
                    playerEntity.playSound(SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.PLAYERS, 0.4F, 1.0F);

                    itemStack.getNbt().putInt("level",level - 1);
                    return TypedActionResult.success(itemStack);
                }
            }
        }
        return TypedActionResult.pass(itemStack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 22725714;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if(nbt == null){
            stack.getOrCreateNbt().putInt("level", 0);
            nbt = stack.getNbt();
        }
        int level = nbt.getInt("level");

        return  Math.min(13, Math.round((float) 13 * ((float) level / (float) MAX_LEVEL)));
    }

}
