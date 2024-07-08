package net.pixeldreamstudios.mobs_of_mythology.entity;

import net.minecraft.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.pixeldreamstudios.mobs_of_mythology.MobsOfMythology;
import net.pixeldreamstudios.mobs_of_mythology.entity.abstraction.AbstractKoboldEntity;
import net.pixeldreamstudios.mobs_of_mythology.entity.variant.KoboldVariant;
import net.pixeldreamstudios.mobs_of_mythology.registry.ItemRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class KoboldEntity extends AbstractKoboldEntity {
    private int heldCounter = 0;

    public KoboldEntity(EntityType<? extends AbstractKoboldEntity> entityType, Level world) {
        super(entityType, world, Monster.XP_REWARD_SMALL);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData) {
        KoboldVariant variant = Util.getRandom(KoboldVariant.values(), this.random);
        setVariant(variant);
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobsOfMythology.config.koboldHealth)
                .add(Attributes.ATTACK_DAMAGE, MobsOfMythology.config.koboldAttackDamage)
                .add(Attributes.ATTACK_SPEED, 2)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25f, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.75f));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, (new HurtByTargetGoal(this, new Class[0])).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal(this, true));
    }

    @Override
    public <T> T getVariant() {
        return (T) KoboldVariant.byId(this.getTypeVariant() & 255);
    }

    @Override
    public void tick() {
        super.tick();
        if (isHoldingItem()) {
            heldCounter++;
            if (heldCounter == 20 * 3) {
                if (this.getItemInHand(InteractionHand.MAIN_HAND).is(Items.DIAMOND)) {
                    this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.5f);
                    this.produceParticles(ParticleTypes.HAPPY_VILLAGER);
                    Random random = new Random();
                    this.spawnAtLocation(new ItemStack(ItemRegistry.BRONZE_INGOT, random.nextInt(3)));
                    this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    this.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
                    heldCounter = 0;
                } else {
                    this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.5f);
                    this.produceParticles(ParticleTypes.ANGRY_VILLAGER);
                    this.spawnAtLocation(getItemInHand(InteractionHand.MAIN_HAND));
                    this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    this.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
                    heldCounter = 0;
                }
            }
        }
    }

    private void setVariant(KoboldVariant variant) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
    }


    public boolean isHoldingItem() {
        return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
    }

    private void decrementStackUnlessInCreative(Player player, ItemStack stack) {
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack playerStack = player.getItemInHand(hand);
        if (!isHoldingItem() && !playerStack.isEmpty()) {
            ItemStack koboldStack2 = playerStack.copy();
            koboldStack2.setCount(1);
            setItemInHand(InteractionHand.MAIN_HAND, koboldStack2);
            decrementStackUnlessInCreative(player, playerStack);
            level().playSound(player, this, SoundEvents.VILLAGER_TRADE, SoundSource.NEUTRAL, 2.0F, 1.5f);
            this.produceParticles(ParticleTypes.POOF);
            getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, player.getUUID());
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
}
