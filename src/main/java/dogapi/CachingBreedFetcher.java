package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {
    // TODO Task 2: Complete this class
    private final BreedFetcher fetcher;
    private final Map<String, List<String>> cache;
    private int callsMade = 0;

    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.fetcher = fetcher;
        this.cache = new HashMap<>();
    }
    /**
     * Returns a list of sub-breeds for the given breed. If the result was
     * previously fetched successfully, returns the cached result instead of
     * calling the underlying fetcher again.
     *
     * If the underlying fetcher throws a BreedNotFoundException, this call
     * is not cached and the exception is propagated.
     */

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        // Normalize the breed name to avoid duplicate keys
        String key = breed.trim().toLowerCase(Locale.ROOT);

        // If cached, return the stored list immediately
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        // Otherwise, call the underlying fetcher
        callsMade++;
        List<String> result = fetcher.getSubBreeds(breed); // may throw BreedNotFoundException

        // Cache only successful responses
        cache.put(key, Collections.unmodifiableList(new ArrayList<>(result)));
        return result;
    }
    /**
     * Returns the number of times the underlying fetcher has been called.
     * This only increments when a cache miss occurs (and not for cached calls).
     */

    public int getCallsMade() {
        return callsMade;
    }
}