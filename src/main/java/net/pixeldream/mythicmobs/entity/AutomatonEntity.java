package net.pixeldream.mythicmobs.entity;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.pixeldream.mythicmobs.config.MythicMobsConfigs;
import net.pixeldream.mythicmobs.entity.constant.DefaultAnimations;
import net.pixeldream.mythicmobs.registry.ItemRegistry;
import net.pixeldream.mythicmobs.registry.TagRegistry;
import org.jetbrains.annotations.Nullable;

public class AutomatonEntity extends TameableEntity implements GeoEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private long ticksUntilAttackFinish = 0;
    private static final TrackedData<Boolean> SITTING = DataTracker.registerData(DrakeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public AutomatonEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new FollowOwnerGoal(this, 1.0, 3.0F, 16.0F, false));
        this.goalSelector.add(5, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, (new RevengeGoal(this, new Class[0])).setGroupRevenge(new Class[0]));
        this.targetSelector.add(4, new ActiveTargetGoal<MobEntity>(this, MobEntity.class, 5, false, false, entity -> entity instanceof Monster));
        this.targetSelector.add(6, new UntamedActiveTargetGoal<MobEntity>(this, MobEntity.class, false, entity -> entity instanceof Monster));
        this.targetSelector.add(7, new UniversalAngerGoal(this, true));
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SITTING, false);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsSitting", this.dataTracker.get(SITTING));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(SITTING, nbt.getBoolean("IsSitting"));
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, MythicMobsConfigs.automatonHealth).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, MythicMobsConfigs.automatonAttackDamage);
    }

    protected void produceParticles(ParticleEffect parameters) {
        double d = this.random.nextGaussian() * 0.02;
        double e = this.random.nextGaussian() * 0.02;
        double f = this.random.nextGaussian() * 0.02;
        this.getWorld().addParticle(parameters, this.getParticleX(0.5), this.getRandomBodyY() + 1.0, this.getParticleZ(1.0), d, e, f);
    }

    @Override
    protected void pushAway(Entity entity) {
        if (entity instanceof Monster && !(entity instanceof CreeperEntity) && this.getRandom().nextInt(20) == 0) {
            this.setTarget((LivingEntity) entity);
        }
        super.pushAway(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (getHealth() < (double) 50) {
            if (getHealth() < (double) 25) {
                produceParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE);
                return;
            }
            produceParticles(ParticleTypes.SMOKE);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean bl = super.damage(source, amount);
        if (bl) {
            this.playSound(SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, 1.0f, 1.0f);
        }
        return bl;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_DEATH;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (hand == Hand.MAIN_HAND && !isTamed()) {
            if (!this.getWorld().isClient()) {
                super.setOwner(player);
                this.navigation.recalculatePath();
                this.setTarget(null);
                this.getWorld().sendEntityStatus(this, (byte) 7);
                setSit(false);
                MinecraftServer server = player.getServer();
                if (server != null) {
                    server.send(new ServerTask(0, () -> player.sendMessage(Text.literal("I will protect you at all costs, " + player.getEntityName() + "."), true)));
                }
            }
            return ActionResult.SUCCESS;
        } else if (itemStack.getRegistryEntry().isIn(TagRegistry.Items.BRONZE_INGOTS)) {
            float f = this.getHealth();
            this.heal(25.0f);
            if (this.getHealth() == f) {
                return ActionResult.PASS;
            }
            float g = 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f;
            this.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0f, g);
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            return ActionResult.PASS;
        } else if (isTamed() && !this.getWorld().isClient() && hand == Hand.MAIN_HAND) {
            MinecraftServer server = player.getServer();
            setSit(!isSitting());
            if (server != null) {
                server.send(new ServerTask(0, () -> player.sendMessage(Text.literal(!isSitting() ? "I will follow you." : "I will wait for you."), true)));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.success(this.getWorld().isClient);
    }

    public void setSit(boolean sitting) {
        this.dataTracker.set(SITTING, sitting);
        super.setSitting(sitting);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_IRON_GOLEM_STEP, 1.0f, 1.0f);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        produceParticles(ParticleTypes.POOF);
        dropStack(new ItemStack(ItemRegistry.BRONZE_INGOT, random.nextBetween(1, 4)));
        dropStack(new ItemStack(ItemRegistry.GEAR, random.nextBetween(1, 3)));
        super.onDeath(damageSource);
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.845f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 3, state -> {
            if (state.isMoving() && !handSwinging) {
                state.getController().setAnimation(DefaultAnimations.WALK);
                return PlayState.CONTINUE;
            } else if (handSwinging) {
                state.getController().setAnimation(DefaultAnimations.ATTACK);
                ticksUntilAttackFinish++;
                if (ticksUntilAttackFinish > 20 * 4) {
                    handSwinging = false;
                    ticksUntilAttackFinish = 0;
                }
                return PlayState.CONTINUE;
            }
            state.getController().setAnimation(DefaultAnimations.IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public EntityView method_48926() {
        return getWorld();
    }
}
