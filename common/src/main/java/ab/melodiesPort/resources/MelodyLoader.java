package ab.melodiesPort.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import ab.melodiesPort.util.MidiParser;
import ab.melodiesPort.util.Utils;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class MelodyLoader extends SinglePreparationResourceReloader<Map<Identifier, MelodyLoader.LazyMelody>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final String dataType = "melodies";

    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        AtomicReference<T> value = new AtomicReference<>();
        return () -> {
            T val = value.get();
            if (val == null) {
                val = value.updateAndGet(cur -> cur == null ? Objects.requireNonNull(delegate.get()) : cur);
            }
            return val;
        };
    }

    public static class LazyMelody {
        public final String name;
        public final Supplier<Melody> supplier;
        public final MelodyDescriptor descriptor;

        public LazyMelody(String name, Supplier<Melody> supplier) {
            this.name = name;
            this.supplier = memoize(supplier);
            this.descriptor = new MelodyDescriptor(name);
        }

        public Melody get() {
            return supplier.get();
        }

        public MelodyDescriptor getDescriptor() {
            return descriptor;
        }
    }

    @Override
    protected Map<Identifier, LazyMelody> prepare(ResourceManager manager, Profiler profiler) {
        Map<Identifier, LazyMelody> map = Maps.newHashMap();

        Map<Identifier, Resource> resources = manager.findResources(dataType, path -> path.getPath().endsWith(".midi") || path.getPath().endsWith(".mid"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try {
                String name = Utils.toTitle(Utils.removeLastPart(Utils.getLastPart(entry.getKey().getPath(), "/"), "."));
                Identifier identifier = new Identifier(entry.getKey().getNamespace(), entry.getKey().getPath());
                map.put(identifier, new LazyMelody(name, () -> {
                    try {
                        return MidiParser.parseMidi(entry.getValue().getInputStream(), name);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error("Couldn't load melody {} ({})", entry.getKey(), exception);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<Identifier, LazyMelody> prepared, ResourceManager manager, Profiler profiler) {
        ServerMelodyManager.setDatapackMelodies(prepared);
    }
}
