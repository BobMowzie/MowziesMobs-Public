package com.bobmowzie.mowziesmobs.client.particle;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.client.particle.types.AdvancedParticleType;
import com.bobmowzie.mowziesmobs.client.particle.types.DecalParticleType;
import com.bobmowzie.mowziesmobs.client.particle.types.RibbonParticleType;
import com.bobmowzie.mowziesmobs.client.particle.types.TerrainParticleType;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleHandler {
    public static final DeferredRegister<ParticleType<?>> REG = DeferredRegister.create(Registries.PARTICLE_TYPE, MMCommon.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARKLE = REG.register("sparkle", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<ParticleVanillaCloudExtended.Data>> VANILLA_CLOUD_EXTENDED = REG.register("vanilla_cloud_extended", () -> new ParticleType<>(false) {
        @Override
        public @NotNull MapCodec<ParticleVanillaCloudExtended.Data> codec() {
            return ParticleVanillaCloudExtended.Data.CODEC;
        }

        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ParticleVanillaCloudExtended.Data> streamCodec() {
            return ParticleVanillaCloudExtended.Data.STREAM_CODEC;
        }
    });

    public static final DeferredHolder<ParticleType<?>, ParticleType<ParticleSnowFlake.Data>> SNOWFLAKE = REG.register("snowflake", () -> new ParticleType<>(false) {
        @Override
        public @NotNull MapCodec<ParticleSnowFlake.Data> codec() {
            return ParticleSnowFlake.Data.CODEC;
        }

        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ParticleSnowFlake.Data> streamCodec() {
            return ParticleSnowFlake.Data.STREAM_CODEC;
        }
    });

    public static final DeferredHolder<ParticleType<?>, ParticleType<ParticleCloud.Data>> CLOUD = REG.register("cloud_soft", () -> new ParticleType<>(false) {
        @Override
        public @NotNull MapCodec<ParticleCloud.Data> codec() {
            return ParticleCloud.Data.CODEC;
        }

        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ParticleCloud.Data> streamCodec() {
            return ParticleCloud.Data.STREAM_CODEC;
        }
    });

    public static final DeferredHolder<ParticleType<?>, ParticleType<ParticleOrb.Data>> ORB = REG.register("orb_0", () -> new ParticleType<>(false) {
        @Override
        public @NotNull MapCodec<ParticleOrb.Data> codec() {
            return ParticleOrb.Data.CODEC;
        }

        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ParticleOrb.Data> streamCodec() {
            return ParticleOrb.Data.STREAM_CODEC;
        }
    });

    public static final DeferredHolder<ParticleType<?>, ParticleType<ParticleRing.Data>> RING = REG.register("ring_0", () -> new ParticleType<>(false) {
        @Override
        public @NotNull MapCodec<ParticleRing.Data> codec() {
            return ParticleRing.Data.CODEC;
        }

        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ParticleRing.Data> streamCodec() {
            return ParticleRing.Data.STREAM_CODEC;
        }
    });

    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> RING2 = registerAdvanced("ring");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> RING_BIG = registerAdvanced("ring_big");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> PIXEL = registerAdvanced("pixel");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> ORB2 = registerAdvanced("orb");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> EYE = registerAdvanced("eye");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> BUBBLE = registerAdvanced("bubble");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> SUN = registerAdvanced("sun");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> SUN_NOVA = registerAdvanced("sun_nova");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> FLARE = registerAdvanced("flare");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> FLARE_RADIAL = registerAdvanced("flare_radial");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> BURST_IN = registerAdvanced("ring1");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> BURST_MESSY = registerAdvanced("burst_messy");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> RING_SPARKS = registerAdvanced("sparks_ring");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> BURST_OUT = registerAdvanced("ring2");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> GLOW = registerAdvanced("glow");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> ARROW_HEAD = registerAdvanced("arrow_head");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> LEAF = registerAdvanced("leaf");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> MOON_FULL = registerAdvanced("moon_full");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> MOON_GIBBOUS = registerAdvanced("moon_gibbous");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> MOON_HALF = registerAdvanced("moon_half");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> MOON_CRESCENT = registerAdvanced("moon_crescent");
    public static final DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> MOON_NEW = registerAdvanced("moon_new");

    public static final DeferredHolder<ParticleType<?>, ParticleType<TerrainParticleType>> TERRAIN = registerTerrain("terrain");
    public static final DeferredHolder<ParticleType<?>, ParticleType<DecalParticleType>> PLAYER_FOOTPRINT = registerDecal("player_footprint");

    public static final DeferredHolder<ParticleType<?>, ParticleType<DecalParticleType>> STRIX_FOOTPRINT = registerDecal("strix_footprint");
    public static final DeferredHolder<ParticleType<?>, ParticleType<DecalParticleType>> GROUND_CRACK = registerDecal("crack");

    public static final DeferredHolder<ParticleType<?>, ParticleType<RibbonParticleType>> RIBBON_FLAT = registerRibbon("ribbon_flat");
    public static final DeferredHolder<ParticleType<?>, ParticleType<RibbonParticleType>> RIBBON_STREAKS = registerRibbon("ribbon_streaks");
    public static final DeferredHolder<ParticleType<?>, ParticleType<RibbonParticleType>> RIBBON_GLOW = registerRibbon("ribbon_glow");
    public static final DeferredHolder<ParticleType<?>, ParticleType<RibbonParticleType>> RIBBON_SQUIGGLE = registerRibbon("ribbon_squiggle");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleHandler.SPARKLE.get(), ParticleSparkle.Provider::new);
        event.registerSpriteSet(ParticleHandler.VANILLA_CLOUD_EXTENDED.get(), ParticleVanillaCloudExtended.Provider::new);
        event.registerSpriteSet(ParticleHandler.SNOWFLAKE.get(), ParticleSnowFlake.Provider::new);
        event.registerSpriteSet(ParticleHandler.CLOUD.get(), ParticleCloud.Provider::new);
        event.registerSpriteSet(ParticleHandler.ORB.get(), ParticleOrb.Provider::new);
        event.registerSpriteSet(ParticleHandler.RING.get(), ParticleRing.Provider::new);

        event.registerSpriteSet(ParticleHandler.RING2.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.RING_BIG.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.PIXEL.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.ORB2.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.EYE.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.BUBBLE.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.SUN.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.SUN_NOVA.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.FLARE.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.FLARE_RADIAL.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.BURST_IN.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.BURST_MESSY.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.RING_SPARKS.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.BURST_OUT.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.GLOW.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.ARROW_HEAD.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.LEAF.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.TERRAIN.get(), AdvancedTerrainParticle.Factory::new);
        event.registerSpriteSet(ParticleHandler.MOON_FULL.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.MOON_GIBBOUS.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.MOON_HALF.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.MOON_CRESCENT.get(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(ParticleHandler.MOON_NEW.get(), AdvancedParticleBase.Factory::new);

        event.registerSpriteSet(ParticleHandler.STRIX_FOOTPRINT.get(), ParticleDecal.Provider::new);
        event.registerSpriteSet(ParticleHandler.GROUND_CRACK.get(), ParticleDecal.Provider::new);
        event.registerSpriteSet(ParticleHandler.PLAYER_FOOTPRINT.get(), ParticleDecal.Provider::new);

        event.registerSpriteSet(ParticleHandler.RIBBON_FLAT.get(), ParticleRibbon.Provider::new);
        event.registerSpriteSet(ParticleHandler.RIBBON_STREAKS.get(), ParticleRibbon.Provider::new);
        event.registerSpriteSet(ParticleHandler.RIBBON_GLOW.get(), ParticleRibbon.Provider::new);
        event.registerSpriteSet(ParticleHandler.RIBBON_SQUIGGLE.get(), ParticleRibbon.Provider::new);
    }

    private static DeferredHolder<ParticleType<?>, ParticleType<AdvancedParticleType>> registerAdvanced(String key) {
        return REG.register(key, location -> new ParticleType<>(false) {
            @Override
            public @NotNull MapCodec<AdvancedParticleType> codec() {
                return AdvancedParticleType.CODEC;
            }

            @Override
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, AdvancedParticleType> streamCodec() {
                return AdvancedParticleType.STREAM_CODEC;
            }
        });
    }

    private static DeferredHolder<ParticleType<?>, ParticleType<DecalParticleType>> registerDecal(String key) {
        return REG.register(key, location -> new ParticleType<>(false) {
            @Override
            public @NotNull MapCodec<DecalParticleType> codec() {
                return DecalParticleType.CODEC;
            }

            @Override
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, DecalParticleType> streamCodec() {
                return DecalParticleType.STREAM_CODEC;
            }
        });
    }

    private static DeferredHolder<ParticleType<?>, ParticleType<RibbonParticleType>> registerRibbon(String key) {
        return REG.register(key, location -> new ParticleType<>(false) {
            @Override
            public @NotNull MapCodec<RibbonParticleType> codec() {
                return RibbonParticleType.CODEC;
            }

            @Override
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, RibbonParticleType> streamCodec() {
                return RibbonParticleType.STREAM_CODEC;
            }
        });
    }

    private static DeferredHolder<ParticleType<?>, ParticleType<TerrainParticleType>> registerTerrain(String key) {
        return REG.register(key, location -> new ParticleType<>(false) {
            @Override
            public @NotNull MapCodec<TerrainParticleType> codec() {
                return TerrainParticleType.CODEC;
            }

            @Override
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, TerrainParticleType> streamCodec() {
                return TerrainParticleType.STREAM_CODEC;
            }
        });
    }
}
