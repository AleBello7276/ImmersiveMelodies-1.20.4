package ab.melodiesPort.item;

import ab.melodiesPort.Common;
import ab.melodiesPort.Sounds;
import ab.melodiesPort.client.MelodyProgress;
import ab.melodiesPort.client.MelodyProgressManager;
import ab.melodiesPort.cobalt.network.NetworkHandler;
import ab.melodiesPort.network.s2c.MelodyListMessage;
import ab.melodiesPort.network.s2c.OpenGuiRequest;
import ab.melodiesPort.resources.Melody;
import ab.melodiesPort.resources.Note;
import ab.melodiesPort.resources.ServerMelodyManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.registry.Registries;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InstrumentItem extends Item {
    public static final String TAG_PLAYING = "playing";
    public static final String TAG_MELODY = "melody";
    public static final String TAG_START_TIME = "start_time";
    public static final String TAG_TRACKS = "enabled_tracks";


    private final Sounds.Instrument sound;
    private final long sustain;

    private final Vector3f offset;

    public InstrumentItem(Settings settings, Sounds.Instrument sound, long sustain, Vector3f offset) {
        super(settings);

        this.sound = sound;
        this.sustain = sustain;
        this.offset = offset;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            NetworkHandler.sendToPlayer(new MelodyListMessage(user), (ServerPlayerEntity) user);
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.SELECTOR), (ServerPlayerEntity) user);
        }

        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // State
        if (isPlaying(stack)) {
            tooltip.add(Text.translatable("immersive_melodies.playing").formatted(Formatting.GREEN));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    public boolean isPlaying(ItemStack stack) {
        return stack.getOrCreateNbt().getBoolean(TAG_PLAYING);
    }

    public void inventoryClientTick(ItemStack stack, World world, Entity entity) {
        // check if the item is in the hand, and is the primary instrument as you cant play two at once
        boolean isPrimary = false;
        for (ItemStack handItem : entity.getHandItems()) {
            if (handItem == stack) {
                isPrimary = true;
                break;
            } else if (handItem.getItem() instanceof InstrumentItem) {
                break;
            }
        }

        // play
        if (isPlaying(stack) && isPrimary && world.isClient && Common.soundManager.audible(entity)) {
            MelodyProgress progress = MelodyProgressManager.INSTANCE.getProgress(entity);
            progress.tick(stack);

            // sync
            MelodyProgressManager.INSTANCE.sync(world.getTime());

            Melody melody = progress.getMelody();

            // get enabled tracks
            Set<Integer> enabledTracks = getEnabledTracks(stack);
            for (int track = 0; track < melody.getTracks().size(); track++) {
                int lastIndex = MelodyProgressManager.INSTANCE.getProgress(entity).getLastIndex(track);
                List<Note> notes = melody.getTracks().get(track).getNotes();
                for (int i = lastIndex; i < notes.size(); i++) {
                    Note note = notes.get(i);
                    if (progress.getTime() >= note.getTime()) {
                        if (enabledTracks.isEmpty() || enabledTracks.contains(track)) {
                            float volume = note.getVelocity() / 255.0f * 2.0f;
                            float pitch = (float) Math.pow(2, (note.getNote() - 24) / 12.0);
                            int octave = 1;
                            while (octave < 8 && pitch > 4.0 / 3.0) {
                                pitch /= 2;
                                octave++;
                            }
                            long length = note.getLength();
                            long sustain = Math.min(this.sustain, note.getSustain());

                            // sound
                            Common.soundManager.playSound(entity.getX(), entity.getY(), entity.getZ(),
                                    sound.get(octave), SoundCategory.NEUTRAL,
                                    volume, pitch, length, sustain,
                                    note.getTime() - progress.getTime(), entity);

                            // particle
                            if (entity instanceof LivingEntity livingEntity && !Common.soundManager.isFirstPerson(entity)) {
                                double x = Math.sin(-livingEntity.bodyYaw / 180.0 * Math.PI);
                                double z = Math.cos(-livingEntity.bodyYaw / 180.0 * Math.PI);
                                world.addParticle(ParticleTypes.NOTE,
                                        entity.getX() + x * offset.z + z * offset.x, entity.getY() + entity.getHeight() / 2.0 + offset.y, entity.getZ() + z * offset.z - x * offset.x,
                                        x * 5.0, 0.0, z * 5.0);
                            }

                            MelodyProgressManager.INSTANCE.setLastNote(entity, volume, pitch, length);
                        }
                        //Mark as done
                        if (i == notes.size() - 1) {
                            MelodyProgressManager.INSTANCE.setLastIndex(entity, track, i + 1);
                        }
                    } else {
                        MelodyProgressManager.INSTANCE.setLastIndex(entity, track, i);
                        break;
                    }
                }
            }


            // Rewind
            if (!(entity instanceof PlayerEntity) && progress.getTime() > melody.getLength()) {
                rewind(stack, world);
            }
        }
    }

    public void inventoryServerTick(ItemStack stack, ServerWorld world, Entity entity) {
        // autoplay
        if (!(entity instanceof PlayerEntity) && !isPlaying(stack)) {
            Identifier randomMelody = ServerMelodyManager.getRandomMelody();
            play(stack, randomMelody, world, entity);
        }
    }

    public void play(ItemStack stack, Identifier melody, World world, Entity entity) {
        stack.getOrCreateNbt().putString(TAG_MELODY, melody.toString());
        stack.getOrCreateNbt().putBoolean(TAG_PLAYING, true);
        stack.getOrCreateNbt().putLong(TAG_START_TIME, world.getTime());

        refreshTracks(stack, entity);
    }

    public Identifier getMelody(ItemStack stack) {
        return new Identifier(stack.getOrCreateNbt().getString(TAG_MELODY));
    }

    public void refreshTracks(ItemStack stack, Entity entity) {
        String identifier = ServerMelodyManager.getIdentifier(entity, Registries.ITEM.getId(this));
        Set<Integer> enabledTracks = ServerMelodyManager.getSettings().getEnabledTracks(getMelody(stack), identifier);
        stack.getOrCreateNbt().putIntArray(TAG_TRACKS, enabledTracks.stream().mapToInt(i -> i).toArray());
    }

    public void rewind(ItemStack stack, World world) {
        stack.getOrCreateNbt().putLong(TAG_START_TIME, world.getTime());
    }

    public void play(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean(TAG_PLAYING, true);
    }

    public void pause(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean(TAG_PLAYING, false);
    }

    public Set<Integer> getEnabledTracks(ItemStack stack) {
        if (!stack.getOrCreateNbt().contains(TAG_TRACKS)) {
            return Set.of();
        }
        int[] array = stack.getOrCreateNbt().getIntArray(TAG_TRACKS);
        return Arrays.stream(array).boxed().collect(Collectors.toSet());
    }
}