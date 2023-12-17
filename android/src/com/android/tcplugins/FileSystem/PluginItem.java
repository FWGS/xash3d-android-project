package com.android.tcplugins.FileSystem;

import android.os.Parcel;
import android.os.Parcelable;

public class PluginItem implements Parcelable, Comparable<PluginItem> {
    public static final Parcelable.Creator<PluginItem> CREATOR = new Parcelable.Creator<PluginItem>() {
        public PluginItem createFromParcel(Parcel parcel) {
            return new PluginItem(parcel);
        }

        public PluginItem[] newArray(int i) {
            return new PluginItem[i];
        }
    };
    public int attr;
    public String description;
    public boolean directory;
    public int iconFlags;
    public long lastModified;
    public long length;
    public String name;
    public int unixAttr;

    public int describeContents() {
        return 0;
    }

    public PluginItem() {
        this.name = null;
        this.description = null;
        this.directory = false;
        this.length = -1;
        this.lastModified = -1;
        this.iconFlags = 0;
        this.attr = 0;
        this.unixAttr = 0;
    }

    private PluginItem(Parcel parcel) {
        this.name = null;
        this.description = null;
        this.directory = false;
        this.length = -1;
        this.lastModified = -1;
        this.iconFlags = 0;
        this.attr = 0;
        this.unixAttr = 0;
        readFromParcel(parcel);
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeString(this.description);
        parcel.writeByte(this.directory ? (byte) 1 : 0);
        parcel.writeLong(this.length);
        parcel.writeLong(this.lastModified);
        parcel.writeInt(this.iconFlags);
        parcel.writeInt(this.attr);
        parcel.writeInt(this.unixAttr);
    }

    public void readFromParcel(Parcel parcel) {
        this.name = parcel.readString();
        this.description = parcel.readString();
        this.directory = parcel.readByte() != 0;
        this.length = parcel.readLong();
        this.lastModified = parcel.readLong();
        this.iconFlags = parcel.readInt();
        this.attr = parcel.readInt();
        this.unixAttr = parcel.readInt();
    }

    public int compareTo(PluginItem pluginItem) {
        return this.name.compareToIgnoreCase(pluginItem.name);
    }
}
