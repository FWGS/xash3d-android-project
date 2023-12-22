package su.xash.fwgslib;
import android.os.ParcelFileDescriptor;
interface Sender {
    ParcelFileDescriptor getFD();
	void sendFD(in ParcelFileDescriptor fd);
}