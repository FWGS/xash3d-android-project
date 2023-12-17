package com.android.tcplugins.FileSystem;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteDialogCallback extends IInterface {

    public static class Default implements IRemoteDialogCallback {
        public IBinder asBinder() {
            return null;
        }

        public String requestProc(int i, String str, String str2) throws RemoteException {
            return null;
        }
    }

    String requestProc(int i, String str, String str2) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteDialogCallback {
        private static final String DESCRIPTOR = "com.android.tcplugins.FileSystem.IRemoteDialogCallback";
        static final int TRANSACTION_requestProc = 1;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteDialogCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IRemoteDialogCallback)) {
                return new Proxy(iBinder);
            }
            return (IRemoteDialogCallback) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                String requestProc = requestProc(parcel.readInt(), parcel.readString(), parcel.readString());
                parcel2.writeNoException();
                parcel2.writeString(requestProc);
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IRemoteDialogCallback {
            public static IRemoteDialogCallback sDefaultImpl;
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String requestProc(int i, String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (!this.mRemote.transact(1, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestProc(i, str, str2);
                    }
                    obtain2.readException();
                    String readString = obtain2.readString();
                    obtain2.recycle();
                    obtain.recycle();
                    return readString;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRemoteDialogCallback iRemoteDialogCallback) {
            if (Proxy.sDefaultImpl != null || iRemoteDialogCallback == null) {
                return false;
            }
            Proxy.sDefaultImpl = iRemoteDialogCallback;
            return true;
        }

        public static IRemoteDialogCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
