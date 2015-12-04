package pw.bitset.remotely.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Service implements Parcelable {
    public final String name;
    public final String host;
    public final int port;

    public Service(String name,String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return String.format("%s (%s:%d)", name, host, port);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Service &&
                ((Service) other).port == port &&
                ((Service) other).host.equals(host) &&
                ((Service) other).name.equals(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Service> CREATOR = new Creator<Service>() {
        @Override
        public Service createFromParcel(Parcel in) {
            String name = in.readString();
            String host = in.readString();
            int port = in.readInt();

            return new Service(name, host, port);
        }

        @Override
        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(host);
        dest.writeInt(port);
    }
}
