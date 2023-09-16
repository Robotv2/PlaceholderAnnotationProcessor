package fr.robotv2.placeholderannotation.util;

import fr.robotv2.placeholderannotation.annotations.Cache;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public enum CacheSystem {

    INSTANCE;

    private static class Pair<A, B> {

        public final A fst;
        public final B snd;

        public Pair(A fst, B snd) {
            this.fst = fst;
            this.snd = snd;
        }

        public String toString() {
            return "Pair[" + fst + "," + snd + "]";
        }

        public boolean equals(Object other) {

            if(!(other instanceof Pair<?, ?>)) {
                return false;
            }

            final Pair<?, ?> pair = (Pair<?, ?>) other;
            return Objects.equals(fst, pair.fst) && Objects.equals(snd, pair.snd);
        }

        public int hashCode() {
            if (fst == null) return (snd == null) ? 0 : snd.hashCode() + 1;
            else if (snd == null) return fst.hashCode() + 2;
            else return fst.hashCode() * 17 + snd.hashCode();
        }

        public static <A,B> Pair<A,B> of(A a, B b) {
            return new Pair<>(a,b);
        }
    }

    private final ScheduledExecutorService CACHE_POOL = Executors.newSingleThreadScheduledExecutor();
    private final Map<Pair<UUID, String>, String> cache = new ConcurrentHashMap<>();

    public boolean isCached(UUID uuid, String placeholder) {
        return getCache(uuid, placeholder) != null;
    }

    public String getCache(UUID uuid, String placeholder) {
        return this.cache.get(Pair.of(uuid, placeholder));
    }

    public void cache(UUID uuid, String placeholder, String result, Cache cache) {
        final Pair<UUID, String> keyPair = Pair.of(uuid, placeholder);
        this.cache.put(keyPair, result);
        this.CACHE_POOL.schedule(() -> this.cache.remove(keyPair), cache.value(), cache.unit());
    }
}
