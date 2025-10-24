package com.moigferdsrte.divein.network;

import com.moigferdsrte.divein.Divein;
import com.moigferdsrte.divein.extension.AnimationEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Packets {
    public record AnimationPublish(int playerId, AnimationEffect.Visuals visuals, Vec3 velocity) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AnimationPublish> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "publish"));

        public static final StreamCodec<FriendlyByteBuf, AnimationPublish> CODEC = StreamCodec.ofMember(AnimationPublish::write, AnimationPublish::read);
        public static AnimationPublish read(FriendlyByteBuf buffer) {
            int playerId = buffer.readInt();
            var visuals = new AnimationEffect.Visuals(
                    buffer.readUtf(),
                    AnimationEffect.Particles.valueOf(buffer.readUtf()));
            Vec3 velocity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            return new AnimationPublish(playerId, visuals, velocity);
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeInt(playerId);
            buffer.writeUtf(visuals.animationName());
            buffer.writeUtf(visuals.particles().toString());
            buffer.writeDouble(velocity.x);
            buffer.writeDouble(velocity.y);
            buffer.writeDouble(velocity.z);
        }


        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    public record DiveAnimation(int playerId, AnimationEffect.Visuals visuals, Vec3 velocity) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<DiveAnimation> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation"));

        public static final StreamCodec<FriendlyByteBuf, DiveAnimation> CODEC = StreamCodec.ofMember(DiveAnimation::write, DiveAnimation::read);

        public static DiveAnimation read(FriendlyByteBuf buffer) {
            int playerId = buffer.readInt();
            var visuals = new AnimationEffect.Visuals(
                    buffer.readUtf(),
                    AnimationEffect.Particles.valueOf(buffer.readUtf()));
            Vec3 velocity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            return new DiveAnimation(playerId, visuals, velocity);
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeInt(playerId);
            buffer.writeUtf(visuals.animationName());
            buffer.writeUtf(visuals.particles().toString());
            buffer.writeDouble(velocity.x);
            buffer.writeDouble(velocity.y);
            buffer.writeDouble(velocity.z);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
