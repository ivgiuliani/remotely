package pw.bitset.remotely.api;

import com.google.gson.annotations.SerializedName;

public class Keycode {
    @SerializedName("keycode") public final int keycode;

    public Keycode(int keycode) {
        this.keycode = keycode;
    }
}
