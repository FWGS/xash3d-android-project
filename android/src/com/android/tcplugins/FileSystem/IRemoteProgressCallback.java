package com.android.tcplugins.FileSystem;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteProgressCallback extends IInterface {

    public static class Default implements IRemoteProgressCallback {
        public IBinder asBinder() {
            return null;
        }

        public void logProc(int i, String str) throws RemoteException {
        }

        public void progressPercent(int i) throws RemoteException {
        }

        public void setFromToNames(String str, String str2) throws RemoteException {
        }
    }

    void logProc(int i, String str) throws RemoteException;

    void progressPercent(int i) throws RemoteException;

    void setFromToNames(String str, String str2) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteProgressCallback {
        private static final String DESCRIPTOR = "com.android.tcplugins.FileSystem.IRemoteProgressCallback";
        static final int TRANSACTION_logProc = 3;
        static final int TRANSACTION_progressPercent = 1;
        static final int TRANSACTION_setFromToNames = 2;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteProgressCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IRemoteProgressCallback)) {
                return new Proxy(iBinder);
            }
            return (IRemoteProgressCallback) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                progressPercent(parcel.readInt());
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                setFromToNames(parcel.readString(), parcel.readString());
                return true;
            } else if (i == 3) {
                parcel.enforceInterface(DESCRIPTOR);
                logProc(parcel.readInt(), parcel.readString());
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IRemoteProgressCallback {
            public static IRemoteProgressCallback sDefaultImpl;
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

            public void progressPercent(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (this.mRemote.transact(1, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().progressPercent(i);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void setFromToNames(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (this.mRemote.transact(2, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().setFromToNames(str, str2);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void logProc(int i, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    if (this.mRemote.transact(3, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().logProc(i, str);
                    }
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRemoteProgressCallback iRemoteProgressCallback) {
            if (Proxy.sDefaultImpl != null || iRemoteProgressCallback == null) {
                return false;
            }
            Proxy.sDefaultImpl = iRemoteProgressCallback;
            return true;
        }

        public static IRemoteProgressCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
