package pw.bitset.remotely.trackpad;

public interface TrackpadListener {
    void onMove(int deltaX, int deltaY);
    void onClick();
    void onDoubleClick();
}
