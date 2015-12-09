package pw.bitset.remotely.api;

import com.google.gson.annotations.SerializedName;

public class Pong {
    @SerializedName("pong") public final boolean pong;

    public Pong(boolean pong) {
        this.pong = pong;
    }
}
