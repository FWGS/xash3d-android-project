package com.android.tcplugins.FileSystem;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteCopyCallback extends IInterface {

    public static class Default implements IRemoteCopyCallback {
        public IBinder asBinder() {
            return null;
        }

        public void close() throws RemoteException {
        }

        public boolean open(long j) throws RemoteException {
            return false;
        }

        public int read(byte[] bArr, int i) throws RemoteException {
            return 0;
        }
    }

    void close() throws RemoteException;

    boolean open(long j) throws RemoteException;

    int read(byte[] bArr, int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteCopyCallback {
        private static final String DESCRIPTOR = "com.android.tcplugins.FileSystem.IRemoteCopyCallback";
        static final int TRANSACTION_close = 3;
        static final int TRANSACTION_open = 1;
        static final int TRANSACTION_read = 2;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteCopyCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IRemoteCopyCallback)) {
                return new Proxy(iBinder);
            }
            return (IRemoteCopyCallback) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            byte[] bArr;
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                boolean open = open(parcel.readLong());
                parcel2.writeNoException();
                parcel2.writeInt(open ? 1 : 0);
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                int readInt = parcel.readInt();
                if (readInt < 0) {
                    bArr = null;
                } else {
                    bArr = new byte[readInt];
                }
                int read = read(bArr, parcel.readInt());
                parcel2.writeNoException();
                parcel2.writeInt(read);
                parcel2.writeByteArray(bArr);
                return true;
            } else if (i == 3) {
                parcel.enforceInterface(DESCRIPTOR);
                close();
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IRemoteCopyCallback {
            public static IRemoteCopyCallback sDefaultImpl;
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

            public boolean open(long j) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeLong(j);
                    boolean z = false;
                    if (!this.mRemote.transact(1, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().open(j);
                    }
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int read(byte[] bArr, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bArr == null) {
                        obtain.writeInt(-1);
                    } else {
                        obtain.writeInt(bArr.length);
                    }
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().read(bArr, i);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readByteArray(bArr);
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void close() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().close();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRemoteCopyCallback iRemoteCopyCallback) {
            if (Proxy.sDefaultImpl != null || iRemoteCopyCallback == null) {
                return false;
            }
            Proxy.sDefaultImpl = iRemoteCopyCallback;
            return true;
        }

        public static IRemoteCopyCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
