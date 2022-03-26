package net.dreamer.machineletons.mixin;

import net.dreamer.machineletons.Machineletons;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonEntityMixin extends HostileEntity implements RangedAttackMob {
    @Shadow protected abstract PersistentProjectileEntity createArrowProjectile(ItemStack arrow,float damageModifier);
    int isShooting = 0;
    float pullProgress;
    LivingEntity currentTargetAssis, currentTarget;

    protected AbstractSkeletonEntityMixin(EntityType<? extends HostileEntity> entityType,World world) {
        super(entityType,world);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("isShooting", (short)isShooting);
    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    public void readCustomDataFromNbtInject(NbtCompound nbt, CallbackInfo info) {
        this.isShooting = nbt.getShort("isShooting");
    }

    @Inject(at = @At("HEAD"), method = "attack", cancellable = true)
    public void attackInject(LivingEntity target,float pullProgress, CallbackInfo info) {
        isShooting = 1;
        currentTargetAssis = getTarget();
        if(currentTargetAssis != null) {
            currentTarget = currentTargetAssis;
        }
        this.pullProgress = pullProgress;
        info.cancel();
    }

    public boolean isPlayerInSpectator() {
        return currentTarget.isPlayer() && currentTarget.isSpectator();
    }

    public boolean isPlayerInCreative() {
        return currentTarget.isPlayer() && ((PlayerEntity) currentTarget).getAbilities().creativeMode;
    }

    private boolean isEligibleTarget() {
        return canSee(currentTarget) && !currentTarget.isDead() && !isPlayerInCreative() && !isPlayerInSpectator();
    }

    @Inject(at = @At("HEAD"), method = "tickMovement")
    public void tickMovementInject(CallbackInfo info) {
        if(isShooting > 0 && isShooting <= world.getGameRules().getInt(Machineletons.SKELETON_ARROW_COUNT) && currentTarget != null && isEligibleTarget() && !this.isDead()) {
            ItemStack itemStack = this.getArrowType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this,Items.BOW)));
            PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack,pullProgress);
            double d = currentTarget.getX() - this.getX(), j = currentTarget.getBodyY(0.3333333333333333D) - persistentProjectileEntity.getY(), f = currentTarget.getZ() - this.getZ(), g = Math.sqrt(d * d + f * f);

            persistentProjectileEntity.setVelocity(d,j + g * 0.20000000298023224D,f,1.6F,(float) (14 - this.world.getDifficulty().getId() * 4));
            this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT,1.0F,1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.world.spawnEntity(persistentProjectileEntity);
            isShooting++;

            if(isShooting == world.getGameRules().getInt(Machineletons.SKELETON_ARROW_COUNT)) {
                isShooting = 0;
            }
        }

        if(isShooting > 0 && currentTarget != null && !isEligibleTarget()) {
            isShooting = 0;
        }
    }
}
