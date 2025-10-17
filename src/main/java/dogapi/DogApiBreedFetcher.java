package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */

    @Override
    public List<String> getSubBreeds(String breed) throws BreedFetcher.BreedNotFoundException{
        // return statement included so that the starter code can compile and run.
        if (breed == null) {
            throw new BreedNotFoundException("Breed cannot be null");
        }
        String normalized = breed.trim().toLowerCase(Locale.ROOT).replace(' ','-');
        if (normalized.isEmpty()) {
            throw new BreedNotFoundException("Breed cannot be empty");
        }
        String url = "https://dog.ceo/api/breed/" + normalized + "/list";
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Unexpected response: " + (response == null ? "null" : response.code()));
            }

            String body = response.body().string();
            JSONObject json = new JSONObject(body);

            // dog.ceo returns {"status":"success","message":[ ...subbreeds... ]}
            String status = json.optString("status", "");
            if (!"success".equalsIgnoreCase(status)) {
                // Some failures still come with a JSON message, but spec requires we map to BreedNotFoundException
                throw new BreedNotFoundException("Breed not found: " + breed);
            }

            JSONArray msg = json.optJSONArray("message");
            if (msg == null) {
                return Collections.emptyList();
            }

            List<String> result = new ArrayList<>(msg.length());
            for (int i = 0; i < msg.length(); i++) {
                result.add(msg.getString(i));
            }
            return result;
        } catch (IOException | RuntimeException e) {
            // Per interface contract, report *any* failure as BreedNotFoundException
            throw new BreedNotFoundException("Failed to fetch sub-breeds for: " + breed);
        }
    }
}