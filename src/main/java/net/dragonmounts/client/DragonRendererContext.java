package net.dragonmounts.client;

import net.dragonmounts.client.model.dragon.*;
import net.dragonmounts.client.variant.VariantAppearance;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.registry.DragonVariant;
import net.dragonmounts.util.CircularBuffer;
import net.dragonmounts.util.math.LinearInterpolation;
import net.dragonmounts.util.math.MathUtil;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static net.dragonmounts.client.model.dragon.DragonNeckModelPart.*;
import static net.dragonmounts.client.model.dragon.DragonTailModelPart.*;
import static net.dragonmounts.client.model.dragon.DragonWingModelPart.FINGER_COUNT;
import static net.dragonmounts.util.math.Interpolation.*;
import static net.dragonmounts.util.math.MathUtil.TO_RAD_FACTOR;

/**
 * Animation control class to put useless reptiles in motion.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonRendererContext {
    // constants
    private static final int JAW_OPENING_TIME_FOR_ATTACK = 5;
    private static final float HEAD_TILT_DURING_BREATH = -0.1F;
    public static final float SIZE_OF_ADULT_HEAD = 1.0F;
    public static final float SIZE_OF_BABY_HEAD = 2.0F;
    public static final float SCALE_OF_BABY = 0.2F;
    public static final float SCALE_OF_ADULT = 1.0F;
    private static final float[] FOLDED_FINGER_Y = new float[]{2.7F, 2.8F, 2.9F, 3.0F};
    private static final float[] UNFOLDED_FINGER_Y = new float[]{0.1F, 0.9F, 1.7F, 2.5F};

    private final ClientDragonEntity dragon;
    public DragonVariant variant;
    public VariantAppearance appearance;
    public DragonLegConfig config;
    private DragonModel model;
    // entity parameters
    public float partialTicks;
    private float relativeHealth;
    private float moveTime;
    private float moveSpeed;
    private float lookYaw;
    private float lookPitch;
    private double prevRenderYawOffset;
    private double yawAbs;
    private boolean exhaling;
    public boolean isSaddled;
    public boolean isInGUI;

    // timing vars
    private float animBase;
    private float cycleOfs;
    private float ground;
    private float flutter;
    private float walk;
    private float sit;
    private float bite;
    private float speed;

    // timing interp vars
    private final LinearInterpolation animTimer = new LinearInterpolation(0);
    private final LinearInterpolation groundTimer = new LinearInterpolation.Clamped(1, 0, 1);
    private final LinearInterpolation flutterTimer = new LinearInterpolation.Clamped(0, 0, 1);
    private final LinearInterpolation walkTimer = new LinearInterpolation.Clamped(0, 0, 1);
    private final LinearInterpolation sitTimer = new LinearInterpolation.Clamped(0, 0, 1);
    private final LinearInterpolation biteTimer = new LinearInterpolation.Clamped(0, 0, 1);
    private final LinearInterpolation speedTimer = new LinearInterpolation.Clamped(1, 0, 1);

    // trails
    private final CircularBuffer yTrail = new CircularBuffer(8);
    private final CircularBuffer yawTrail = new CircularBuffer(16);
    private final CircularBuffer pitchTrail = new CircularBuffer(16);

    // model flags
    private boolean openJaw;
    private boolean wingsDown;

    // animation parameters
    private final float[] wingArm = new float[3];
    private final float[] wingForearm = new float[3];
    private final float[] wingArmFlutter = new float[3];
    private final float[] wingForearmFlutter = new float[3];
    private final float[] wingArmGlide = new float[3];
    private final float[] wingForearmGlide = new float[3];
    private final float[] wingArmGround = new float[3];
    private final float[] wingForearmGround = new float[3];

    // final X rotation angles for ground
    private final float[] xGround = {0, 0, 0, 0};

    // X rotation angles for ground
    // 1st dim - front, hind
    // 2nd dim - thigh, crus, foot, toe
    private final float[][] xGroundStand = {
            {0.8F, -1.5F, 1.3F, 0},
            {-0.3F, 1.5F, -0.2F, 0},
    };
    private final float[][] xGroundSit = {
            {0.3F, -1.8F, 1.8F, 0},
            {-0.8F, 1.8F, -0.9F, 0},
    };

    // X rotation angles for walking
    // 1st dim - animation keyframe
    // 2nd dim - front, hind
    // 3rd dim - thigh, crus, foot, toe
    private final float[][][] xGroundWalk = {{
            {0.4F, -1.4F, 1.3F, 0},    // move down and forward
            {0.1F, 1.2F, -0.5F, 0}     // move back
    }, {
            {1.2F, -1.6F, 1.3F, 0},    // move back
            {-0.3F, 2.1F, -0.9F, 0.6F} // move up and forward
    }, {
            {0.9F, -2.1F, 1.8F, 0.6F}, // move up and forward
            {-0.7F, 1.4F, -0.2F, 0}    // move down and forward
    }};

    // final X rotation angles for walking
    private final float[] xGroundWalk2 = {0, 0, 0, 0};

    // Y rotation angles for ground, thigh only
    private final float[] yGroundStand = {-0.25F, 0.25F};
    private final float[] yGroundSit = {0.1F, 0.35F};
    private final float[] yGroundWalk = {-0.1F, 0.1F};

    // X rotation angles for air
    // 1st dim - front, hind
    // 2nd dim - thigh, crus, foot, toe
    private final float[][] xAirAll = {{0, 0, 0, 0}, {0, 0, 0, 0}};

    // Y rotation angles for air, thigh only
    private final float[] yAirAll = {-0.1F, 0.1F};

    public DragonRendererContext(ClientDragonEntity dragon) {
        this.dragon = dragon;
        //just to avoid nullptr:
        this.appearance = (this.variant = dragon.getVariant()).getAppearance(VariantAppearances.ENDER_FEMALE);
        this.config = this.variant.type.isSkeleton ? DragonLegConfig.SKELETON : DragonLegConfig.DEFAULT;
    }

    public Vec3d getThroatPosition(double offsetX, double offsetY, double offsetZ) {
        return this.getThroatPosition(this.model.head, offsetX, offsetY, offsetZ);
    }

    /**
     * Calculate the position of the dragon's throat
     *
     * @return the world [x,y,z] of the throat
     */
    public Vec3d getThroatPosition(DragonHeadModel.Part head, double offsetX, double offsetY, double offsetZ) {
        if (head == null) return null;
        final float scale = this.dragon.getScaleFactor();
        final float modelScale = scale * this.dragon.getVariant().getAppearance(VariantAppearances.ENDER_FEMALE).positionScale;
        final float headScale = modelScale * this.getRelativeHeadSize(scale);
        final double centerOffsetY = 6D * modelScale;
        final double centerOffsetZ = 19D * modelScale;
        // the head offset plus the headLocation.rotationPoint is the origin of the head, i.e. the point about which the
        // head rotates, relative to the origin of the body (getPositionEyes)
        return new Vec3d(-head.pivotX * modelScale, -head.pivotY * modelScale, (head.pivotZ - 15D) * modelScale)
                // offset of the throat position relative to the head origin - rotate and pitch to match head
                .add(new Vec3d(offsetX * headScale, (offsetY + 2D) * headScale, (offsetZ - 8D) * headScale).rotateX(head.pitch).rotateY(-head.yaw))
                .add(0D, centerOffsetY, centerOffsetZ)
                .rotateX(-MathUtil.TO_RAD_FACTOR * this.getModelPitch(this.partialTicks))//rotate body
                .subtract(0D, centerOffsetY, centerOffsetZ)
                .rotateY(MathUtil.PI - MathUtil.TO_RAD_FACTOR * this.dragon.bodyYaw)
                .add(this.dragon.getPos())
                .add(0D, this.dragon.getStandingEyeHeight(), 0D);
    }

    public void animate(DragonModel model, float lookYaw, float lookPitch, float moveTime, float moveSpeed) {
        this.model = model;
        this.lookYaw = lookYaw;
        this.lookPitch = lookPitch;
        this.moveTime = moveTime;
        this.moveSpeed = moveSpeed;
        float anim = animTimer.get(partialTicks);
        ground = groundTimer.get(partialTicks);
        flutter = flutterTimer.get(partialTicks);
        walk = walkTimer.get(partialTicks);
        sit = sitTimer.get(partialTicks);
        bite = biteTimer.get(partialTicks);
        //breath = breathTimer.get(partialTicks);
        speed = speedTimer.get(partialTicks);
        //roar = roarTimer.get(partialTicks);
        animBase = anim * MathUtil.PI * 2;
        // check if the wings are moving down and trigger the event
        if (((this.cycleOfs = MathHelper.sin(this.animBase - 1F) + 1F) > 1F) ^ this.wingsDown) {
            if (this.flutter != 0) {
                this.dragon.onWingsDown(this.speed);
            }
            this.wingsDown = !this.wingsDown;
        }
        cycleOfs = (cycleOfs * cycleOfs + cycleOfs * 2) * 0.05F;
        // reduce up/down amplitude
        cycleOfs *= clampedLinear(0.5F, 1.0F, flutter);
        cycleOfs *= clampedLinear(1.0F, 0.5F, ground);

        model.body.back.visible = !this.isSaddled;
        // animate body parts
        this.animHeadAndNeck(model.head, model.neck);
        this.animTail(model.tail, this.appearance.hasSideTailScale(this.dragon), this.appearance.hasTailHorns(this.dragon));
        this.animWings(model.wing);
        this.animLegs(model);
    }

    public void tick(boolean firstTick) {
        if (firstTick) {
            this.yTrail.fill((float) dragon.getY());
            this.yawTrail.fill(dragon.bodyYaw);
            this.pitchTrail.fill(this.getModelPitch(0F));
        }
        // update flags
        this.isSaddled = this.dragon.isSaddled();
        boolean onGround = !this.dragon.isFlying();
        // don't move anything during death sequence
        if (this.dragon.isDead()) {
            animTimer.sync();
            groundTimer.sync();
            flutterTimer.sync();
            walkTimer.sync();
            sitTimer.sync();
            //roarTimer.sync();
            return;
        }
        this.appearance = (this.variant = this.dragon.getVariant()).getAppearance(VariantAppearances.ENDER_FEMALE);
        this.config = this.variant.type.isSkeleton ? DragonLegConfig.SKELETON : DragonLegConfig.DEFAULT;
        this.relativeHealth = this.dragon.getHealth() / this.dragon.getMaxHealth();
        float speedMax = 0.05F;
        Vec3d motion = this.dragon.getVelocity();
        float speedEnt = (float) (motion.x * motion.x + motion.z * motion.z);
        // update main animation timer, depend timing speed on movement
        this.animTimer.add(onGround ? 0.035F : 0.07F - MathHelper.clamp(speedEnt / speedMax, 0, 1) * 0.035F);
        // update ground transition // groundVal: Float = ...
        this.groundTimer.set(onGround ? this.groundTimer.get() * 0.95F + 0.08F : this.groundTimer.get() - 0.1F);
        // update flutter transition // flutterFlag: Boolean = ...
        this.flutterTimer.add(!onGround && (this.dragon.verticalCollision || motion.y > -0.1 || speedEnt < speedMax) ? 0.1F : -0.1F);
        if (this.dragon.isInSittingPose()) {
            // update walking transition // walkFlag: Boolean = ...; walkVal: Float = 0.1F
            this.walkTimer.add(-0.1F);
            // update sitting transition // sitVal: Float
            this.sitTimer.set(this.sitTimer.get() * 0.95F + 0.095F);
        } else {
            // update walking transition // walkFlag: Boolean = ...; walkVal: Float = 0.1F
            this.walkTimer.add(this.moveSpeed > 0.1F ? 0.1F : -0.1F);
            // update sitting transition // sitVal: Float
            this.sitTimer.set(this.sitTimer.get() * 0.95F - 0.095F);
        }
        /* update bite opening transition and breath transitions
        DragonBreathHelper.BreathState breathState = dragon.getBreathHelper().getCurrentBreathState();
        switch (breathState) {
            case IDLE: {  // breath is idle, handle bite attack
                int ticksSinceLastAttack = dragon.getTicksSinceLastAttack();
                final int JAW_OPENING_TIME_FOR_ATTACK = 5;
                boolean jawFlag = (ticksSinceLastAttack >= 0 && ticksSinceLastAttack < JAW_OPENING_TIME_FOR_ATTACK);
                biteTimer.add(jawFlag ? 0.2F : -0.2F);
                breathTimer.set(0.0F);

                int roarticks = dragon.roarTicks;
                final int JAW_OPENING_TIME_FOR_ROAR = 20;
                boolean jawFlag1 = (roarticks >= 0 && roarticks < JAW_OPENING_TIME_FOR_ROAR);
                roarTimer.add(jawFlag1 ? 0.2F : -0.2F);
                break;
            }
            case STARTING: {
                biteTimer.set(0.0F);
                breathTimer.set(dragon.getBreathHelper().getBreathStateFractionComplete());
                break;
            }
            case STOPPING: {
                float breathStateFractionComplete = dragon.getBreathHelper().getBreathStateFractionComplete();
                breathTimer.set(1.0F - breathStateFractionComplete);
                break;
            }
            case SUSTAIN: {
                breathTimer.set(1.0F);
                break;
            }
            default: {
                DragonMounts.loggerLimit.error_once("unexpected breathstate:" + breathState);
                return;
            }
        }*/

        // update speed transition // speedFlag: Boolean = ...; speedValue: Float = 0.05F
        this.speedTimer.add(onGround || speedEnt > speedMax || !this.dragon.canHover() || this.dragon.getPassengerList().size() > 1 || this.dragon.isHighEnough() ? 0.05F : -0.05F);
        // update trailers
        double yawDiff = dragon.bodyYaw - prevRenderYawOffset;
        prevRenderYawOffset = dragon.bodyYaw;
        // filter out 360 degrees wrapping
        if (yawDiff < 180 && yawDiff > -180) {
            yawAbs += yawDiff;
        }
        // TODO: where's yOffset?
        //yTrail.update(entity.posY - entity.yOffset);
        yTrail.update((float) dragon.getY());
        yawTrail.update((float) yawAbs);
        pitchTrail.update(this.getModelPitch(this.partialTicks));
    }

    protected void animHeadAndNeck(DragonHeadModel.Part head, DragonNeckModelPart neck) {
        neck.pitch = neck.yaw = neck.roll = 0;
        neck.pivotX = 0;
        neck.pivotY = 14;
        neck.pivotZ = -8;
        for (int i = 0; i < NECK_SEGMENT_COUNT_INT; ++i) {
            float vertMulti = (i + 1) / NECK_SEGMENT_COUNT_FLOAT;
            float rotArg = vertMulti * this.speed;
            neck.pitch = MathHelper.cos((float) i * 0.45F + this.animBase) * 0.15F// basic up/down movement
                    * (this.exhaling ? clampedLinear(0.2F, 1, this.flutter) : 1F)
                    * clampedLinear(1, 0.2F, this.sit)
                    // reduce rotation when on ground
                    * clampedSmoothLinear(1, 0.5F, this.walk)
                    // flex neck down when hovering
                    + vertMulti - rotArg
                    // lower neck on low health
                    - clampedLinear(0, MathHelper.sin(vertMulti * MathUtil.PI * 0.9F) * 0.63F, this.ground * this.relativeHealth);
            // use looking yaw
            neck.yaw = this.lookYaw * TO_RAD_FACTOR * rotArg;
            // update size (scale)
            neck.scaleX = neck.scaleY = clampedLinear(1.6F, 1, vertMulti);
            neck.scaleZ = 0.6F;
            // hide the first and every second scale
            neck.scale.visible = i == 0 || (i & 1) == 0;
            // update segment
            neck.save(i);
            // move next segment behind the current one
            float size = NECK_SIZE * neck.scaleZ - 1.4F;
            neck.pivotY += MathHelper.sin(neck.pitch) * size;
            size *= MathHelper.cos(neck.pitch);
            neck.pivotX -= MathHelper.sin(neck.yaw) * size;
            neck.pivotZ -= MathHelper.cos(neck.yaw) * size;
        }
        head.scaleX = head.scaleY = head.scaleZ = 0.92F;
        head.pitch = 1F + this.lookPitch * TO_RAD_FACTOR - this.speed; // + breath * HEAD_TILT_DURING_BREATH
        head.yaw = neck.yaw;
        head.roll = neck.roll * 0.2F;
        head.pivotX = neck.pivotX;
        head.pivotY = neck.pivotY;
        head.pivotZ = neck.pivotZ;
        head.lowerJaw.pitch = this.bite * 0.72F/* + breath * 0.58F + roar * 0.67F*/ + (1 - MathHelper.sin(this.animBase)) * 0.1F * this.flutter;

    }

    protected void animWings(DragonWingModelPart wing) {
        // move wings slower while sitting
        float a3 = (sit > 0F ? 0.6F : 1F) * animBase;
        // animation speeds
        float a1 = a3 * 0.35F;
        float a2 = a3 * 0.5F;
        a3 *= 0.75F;
        if (ground < 1) {
            // fluttering
            wingArmFlutter[0] = 0.125F - MathHelper.cos(animBase) * 0.2F;
            wingArmFlutter[1] = 0.25F;
            wingArmFlutter[2] = (MathHelper.sin(animBase) + 0.125F) * 0.8F;
            wingForearmFlutter[0] = 0;
            wingForearmFlutter[1] = -wingArmFlutter[1] * 2;
            wingForearmFlutter[2] = -(MathHelper.sin(animBase + 2) + 0.5F) * 0.75F;
            // gliding
            wingArmGlide[0] = -0.25F - MathHelper.cos(animBase * 2) * MathHelper.cos(animBase * 1.5F) * 0.04F;
            wingArmGlide[1] = 0.25F;
            wingArmGlide[2] = 0.35F + MathHelper.sin(animBase) * 0.05F;
            wingForearmGlide[0] = 0;
            wingForearmGlide[1] = -wingArmGlide[1] * 2;
            wingForearmGlide[2] = -0.25F + (MathHelper.sin(animBase + 2) + 0.5F) * 0.05F;
        }
        if (ground > 0) {
            // standing
            wingArmGround[0] = 0;
            wingArmGround[1] = 1.4F - MathHelper.sin(a1) * MathHelper.sin(a2) * 0.02F;
            wingArmGround[2] = 0.8F + MathHelper.sin(a2) * MathHelper.sin(a3) * 0.05F;
            // walking
            wingArmGround[1] += MathHelper.sin(moveTime * 0.5F) * 0.02F * walk;
            wingArmGround[2] += MathHelper.cos(moveTime * 0.5F) * 0.05F * walk;
            wingForearmGround[0] = 0;
            wingForearmGround[1] = -wingArmGround[1] * 2;
            wingForearmGround[2] = 0;
        }
        // interpolate between fluttering and gliding
        smoothLinear(wingArmGlide, wingArmFlutter, wingArm, flutter);
        smoothLinear(wingForearmGlide, wingForearmFlutter, wingForearm, flutter);
        // interpolate between flying and grounded
        smoothLinear(wingArm, wingArmGround, wingArm, ground);
        smoothLinear(wingForearm, wingForearmGround, wingForearm, ground);
        // apply angles
        wing.pitch = wingArm[0];
        //model.wingArm.pitch += 1 - speed;
        wing.yaw = wingArm[1];
        wing.roll = wingArm[2];
        wing.forearm.pitch = wingForearm[0];
        wing.forearm.yaw = wingForearm[1];
        wing.forearm.roll = wingForearm[2];
        // interpolate between folded and unfolded wing angles
        // set wing finger angles
        float rotX = 0;
        float rotYOfs = MathHelper.sin(a1) * MathHelper.sin(a2) * 0.03F;
        float rotYMulti = 1;
        for (int i = 0; i < FINGER_COUNT; ++i) {
            ModelPart finger = wing.getFinger(i);
            finger.pitch = rotX += 0.005F; // reduce Z-fighting
            finger.yaw = clampedSmoothLinear(UNFOLDED_FINGER_Y[i], FOLDED_FINGER_Y[i] + rotYOfs * rotYMulti, ground);
            rotYMulti -= 0.2F;
        }
    }

    protected void animTail(DragonTailModelPart tail, boolean hasTailScale, boolean hasTailHorn) {
        tail.middleScale.visible = !(tail.leftScale.visible = tail.rightScale.visible = hasTailScale);
        tail.pitch = tail.yaw = tail.roll = 0F;
        tail.pivotX = 0F;
        tail.pivotY = 16F;
        tail.pivotZ = 62F;
        float rotXStand;
        float rotYStand = 0;
        float rotXAir = 0;
        float rotYAir = 0;
        for (int i = 0; i < TAIL_SEGMENT_COUNT_INT; ++i) {
            float vertMulti = (i + 1) / TAIL_SEGMENT_COUNT_FLOAT;
            float angleLimit = 160F * vertMulti;
            // idle
            float amp = 0.1F + i / (TAIL_SEGMENT_COUNT_FLOAT * 2F);
            rotXStand = (i - TAIL_SEGMENT_COUNT_FLOAT * 0.6F) * -amp * 0.4F + (
                    MathHelper.sin(this.animBase * 0.2f) * MathHelper.sin(this.animBase * 0.37F) * 0.4F * amp - 0.1F
            ) * (1F - this.sit);
            rotYStand = (rotYStand + MathHelper.sin(i * 0.45F + this.animBase * 0.5f)) * amp * 0.4f;
            rotXAir -= MathHelper.sin(i * 0.45F + this.animBase) * 0.04F * clampedLinear(0.3f, 1, this.flutter);
            // interpolate between flying, (sitting and standing)
            tail.pitch = clampedLinear(rotXAir, clampedLinear(rotXStand, rotXStand * 0.8F, this.sit), this.ground);
            tail.yaw = clampedLinear(rotYAir, clampedLinear(rotYStand, MathHelper.sin(vertMulti * MathUtil.PI) * MathUtil.PI * 1.2F - 0.5F, this.sit), this.ground);
            // body movement
            tail.pitch += MathHelper.clamp(this.pitchTrail.get(this.partialTicks, 0, i + 1) * 2, -angleLimit, angleLimit) * TO_RAD_FACTOR;
            tail.pitch -= (1 - this.speed) * vertMulti * 2F;
            tail.yaw += MathUtil.PI - MathHelper.clamp(this.yawTrail.get(this.partialTicks, 0, i + 1) * 2, -angleLimit, angleLimit) * TO_RAD_FACTOR;
            // display horns near the tip
            tail.leftHorn.visible = tail.rightHorn.visible = hasTailHorn && i > TAIL_SEGMENT_COUNT_INT - 7 && i < TAIL_SEGMENT_COUNT_INT - 3;
            // update scale
            tail.scaleX = tail.scaleY = tail.scaleZ = clampedLinear(1.5f, 0.3f, vertMulti);
            // update segment
            tail.save(i);
            // move next segment behind the current one
            float size = TAIL_SIZE * tail.scaleZ - 0.7F;
            tail.pivotY += MathHelper.sin(tail.pitch) * size;
            size *= MathHelper.cos(tail.pitch);
            tail.pivotZ -= MathHelper.cos(tail.yaw) * size;
            tail.pivotX -= MathHelper.sin(tail.yaw) * size;
        }
    }

    protected void animLeg(DragonLegModelPart model, int z) {
        int i = model.index;
        model.pivotZ = z;
        // final X rotation angles for air
        float[] xAir = xAirAll[i];
        // interpolate between sitting and standing
        smoothLinear(xGroundStand[i], xGroundSit[i], xGround, sit);
        // align the toes, so they're always horizontal on the ground
        xGround[3] = -(xGround[0] + xGround[1] + xGround[2]);
        // apply walking cycle
        if (walk > 0) {
            // interpolate between the keyframes, based on the cycle
            splineArrays(moveTime * 0.2F, model.left, xGroundWalk2, xGroundWalk[0][i], xGroundWalk[1][i], xGroundWalk[2][i]);
            // align the toes, so they're always horizontal on the ground
            xGroundWalk2[3] -= xGroundWalk2[0] + xGroundWalk2[1] + xGroundWalk2[2];
            smoothLinear(xGround, xGroundWalk2, xGround, walk);
        }
        float yAir = yAirAll[i];
        float yGround;
        // interpolate between sitting and standing
        yGround = clampedSmoothLinear(yGroundStand[i], yGroundSit[i], sit);
        // interpolate between standing and walking
        yGround = clampedSmoothLinear(yGround, yGroundWalk[i], walk);
        // interpolate between flying and grounded
        model.yaw = clampedSmoothLinear(yAir, yGround, ground);
        model.pitch = clampedSmoothLinear(xAir[0], xGround[0], ground);
        model.shank.pitch = clampedSmoothLinear(xAir[1], xGround[1], ground);
        model.foot.pitch = clampedSmoothLinear(xAir[2], xGround[2], ground);
        model.toe.pitch = clampedSmoothLinear(xAir[3], xGround[3], ground);
        //update proxy: model.thighProxy[i].update();
    }

    protected void animLegs(DragonModel model) {
        // dangling legs for flying
        if (this.ground < 1) {
            float footAirOfs = this.cycleOfs * 0.1F;
            float footAirX = 0.75F + this.cycleOfs * 0.1F;
            this.xAirAll[0][0] = 1.3F + footAirOfs;
            this.xAirAll[0][1] = -(0.7F * this.speed + 0.1F + footAirOfs);
            this.xAirAll[0][2] = footAirX;
            this.xAirAll[0][3] = footAirX * 0.5F;
            this.xAirAll[1][0] = footAirOfs + 0.6F;
            this.xAirAll[1][1] = footAirOfs + 0.8F;
            this.xAirAll[1][2] = footAirX;
            this.xAirAll[1][3] = footAirX * 0.5F;
        }
        this.animLeg(model.foreRightLeg.load(this.config), 4);
        this.animLeg(model.hindRightLeg.load(this.config), 46);
        this.animLeg(model.foreLeftLeg.load(this.config), 4);
        this.animLeg(model.hindLeftLeg.load(this.config), 46);
    }

    public float getModelPitch(float partialTicks) {
        // pitchMovingMax: Float = 60; pitchHover: Float = 90
        return clampedSmoothLinear(60F, MathHelper.clamp(yTrail.get(partialTicks, 5, 0) * 10F, -90F, 90F), speed);
    }

    public float getModelOffsetX() {
        return 0;
    }

    public float getModelOffsetY() {
        return 1.375F - (this.sit * 0.55F);
    }

    public float getModelOffsetZ() {
        return -1.5F;
    }

    private static final float ARG_B = (SIZE_OF_ADULT_HEAD * SCALE_OF_ADULT - SIZE_OF_BABY_HEAD * SCALE_OF_BABY);
    private static final float ARG_A = SIZE_OF_ADULT_HEAD * (SCALE_OF_ADULT + ARG_B);

    /**
     * Baby dragon has a relatively larger head compared to its body size (makes it look cuter)
     */
    public float getRelativeHeadSize(float scale) {
        // used to be 1.4F / (scale + 0.4F) i.e. a rational function of the form head_size = A / (scale + B)
        // We want the headsize of the adult to be SIZE_OF_ADULT_HEAD at SCALE_OF_ADULT, and
        //    headsize of the baby to be SIZE_OF_BABY_HEAD at SCALE_OF_BABY
        //  we can rearrange to solve for A and B
        scale = MathHelper.clamp(scale, SCALE_OF_BABY, SCALE_OF_ADULT);
        return ARG_A * (scale + ARG_B);
    }
}
