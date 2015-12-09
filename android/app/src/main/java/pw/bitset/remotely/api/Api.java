package pw.bitset.remotely.api;

import pw.bitset.remotely.data.Service;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class Api {
    public static RemotelyService get(Service service) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%s:%d", service.host, service.port))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(RemotelyService.class);
    }
}
