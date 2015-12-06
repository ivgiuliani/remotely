package pw.bitset.remotely.api;

import com.google.gson.annotations.SerializedName;

public class DeltaCoordinates {
    @SerializedName("delta_x") public final int deltaX;
    @SerializedName("delta_y") public final int deltaY;

    public DeltaCoordinates(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
}
