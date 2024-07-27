package net.pixeldreamstudios.mobs_of_mythology.entity.mobs;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.pixeldreamstudios.mobs_of_mythology.MobsOfMythology;
import net.pixeldreamstudios.mobs_of_mythology.entity.constant.DefaultMythAnimations;
import net.pixeldreamstudios.mobs_of_mythology.entity.variant.SporelingVariant;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SporelingEntity extends PathfinderMob implements GeoEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public static final RawAnimation BOUNCE = RawAnimation.begin().thenPlay("bounce");
    protected static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(SporelingEntity.class, EntityDataSerializers.INT);
    private Component currentLine;
    private String[] lines;
    private List<String> greetings;
    private int lineCooldown = 60;
    private boolean touched = false;
    private boolean startCountdown = false;
    private boolean talk = true;
    private SoundEvent interactSound;

    public SporelingEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobsOfMythology.config.mushroomHealth)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SporelingVariant variant = Util.getRandom(SporelingVariant.values(), this.random);
        setVariant(variant);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
    }

    public SporelingVariant getVariant() {
        return SporelingVariant.byId(this.getTypeVariant() & 255);
    }

    private void setVariant(SporelingVariant variant) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
    }

    private int getTypeVariant() {
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Variant", this.getTypeVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(DATA_ID_TYPE_VARIANT, nbt.getInt("Variant"));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.80f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.75f));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (startCountdown) {
            lineCooldown--;
            if (lineCooldown <= 0) {
                startCountdown = false;
                talk = true;
                lineCooldown = 60;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "livingController", 3, state -> {
            if (state.isMoving()) {
                state.getController().setAnimation(DefaultMythAnimations.WALK);
                return PlayState.CONTINUE;
            }
            state.getController().setAnimation(DefaultMythAnimations.IDLE);
            return PlayState.CONTINUE;
        }));
        controllerRegistrar.add(new AnimationController<>(this, "bounceController", 3, state -> {
            if (touched) {
                state.getController().forceAnimationReset();
                state.getController().setAnimation(BOUNCE);
                touched = false;
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (getVariant().equals(SporelingVariant.RED)) {
            interactSound = SoundEvents.VILLAGER_YES;
            //TODO Make this data-driven
            lines = MobsOfMythology.config.redMushroomLines;
            greetings = Arrays.asList(
                    "Hello there, ",
                    "Hey there, ",
                    "Howdy, ",
                    "Howdy-do, ",
                    "Salutations, ",
                    "Hiya, ",
                    "Godspeed, "
            );
        } else {
            interactSound = SoundEvents.VILLAGER_NO;
            lines = MobsOfMythology.config.brownMushroomLines;
        }
        currentLine = Component.literal(lines[random.nextInt(lines.length)]);
        if (talk) {
            touched = true;
            talk = false;
            startCountdown = true;
            Component previousLine = currentLine;
            do {
                currentLine = Component.literal(lines[random.nextInt(lines.length)]);
                if (currentLine.equals(Component.literal("playerGreeting"))) {
                    currentLine = Component.literal(greetings.get(random.nextInt(greetings.size())) + player.getScoreboardName() + '!');
                }
            } while (currentLine.equals(previousLine));
            MinecraftServer server = player.getServer();
            if (server != null) {
                this.playSound(interactSound, 1.0f, 15);
                server.tell(new TickTask(0, () -> player.displayClientMessage(currentLine, true)));
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        this.playSound(SoundEvents.VILLAGER_AMBIENT, 1.0f, 15);
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        this.playSound(SoundEvents.VILLAGER_HURT, 1.0f, 15);
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        this.playSound(SoundEvents.VILLAGER_DEATH, 1.0f, 15);
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WOLF_STEP, 0.25f, 15);
    }
}
