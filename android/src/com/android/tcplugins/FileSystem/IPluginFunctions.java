package com.android.tcplugins.FileSystem;

import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.tcplugins.FileSystem.IRemoteCopyCallback;
import com.android.tcplugins.FileSystem.IRemoteDialogCallback;
import com.android.tcplugins.FileSystem.IRemoteProgressCallback;
import java.util.ArrayList;
import java.util.List;

public interface IPluginFunctions extends IInterface {

    public static class Default implements IPluginFunctions {
        public IBinder asBinder() {
            return null;
        }

        public boolean deleteFile(String str) throws RemoteException {
            return false;
        }

        public String disconnect(String str) throws RemoteException {
            return null;
        }

        public int execute(String[] strArr, String str) throws RemoteException {
            return 0;
        }

        public Bitmap getBitmap(String str) throws RemoteException {
            return null;
        }

        public List<PluginItem> getDirectoryList(String str) throws RemoteException {
            return null;
        }

        public int getFile(String str, String[] strArr, int i, long j, long j2) throws RemoteException {
            return 0;
        }

        public IRemoteCopyCallback getFileCallback(String str) throws RemoteException {
            return null;
        }

        public String getLocalFileName(String str) throws RemoteException {
            return null;
        }

        public String getModifiedLocalName(String str, String str2) throws RemoteException {
            return null;
        }

        public int getSupportedFunctions() throws RemoteException {
            return 0;
        }

        public boolean makeDir(String str) throws RemoteException {
            return false;
        }

        public int putFile(String str, String str2, int i) throws RemoteException {
            return 0;
        }

        public int putFileFromCallback(IRemoteCopyCallback iRemoteCopyCallback, String str, int i, long j, long j2) throws RemoteException {
            return 0;
        }

        public void registerCallbacks(IRemoteProgressCallback iRemoteProgressCallback, IRemoteDialogCallback iRemoteDialogCallback) throws RemoteException {
        }

        public boolean removeDir(String str) throws RemoteException {
            return false;
        }

        public int renMovFile(String str, String str2, boolean z, boolean z2, long j, long j2) throws RemoteException {
            return 0;
        }

        public void setAbortFlag(String str, boolean z) throws RemoteException {
        }

        public void statusInfo(String str, int i, int i2) throws RemoteException {
        }
    }

    boolean deleteFile(String str) throws RemoteException;

    String disconnect(String str) throws RemoteException;

    int execute(String[] strArr, String str) throws RemoteException;

    Bitmap getBitmap(String str) throws RemoteException;

    List<PluginItem> getDirectoryList(String str) throws RemoteException;

    int getFile(String str, String[] strArr, int i, long j, long j2) throws RemoteException;

    IRemoteCopyCallback getFileCallback(String str) throws RemoteException;

    String getLocalFileName(String str) throws RemoteException;

    String getModifiedLocalName(String str, String str2) throws RemoteException;

    int getSupportedFunctions() throws RemoteException;

    boolean makeDir(String str) throws RemoteException;

    int putFile(String str, String str2, int i) throws RemoteException;

    int putFileFromCallback(IRemoteCopyCallback iRemoteCopyCallback, String str, int i, long j, long j2) throws RemoteException;

    void registerCallbacks(IRemoteProgressCallback iRemoteProgressCallback, IRemoteDialogCallback iRemoteDialogCallback) throws RemoteException;

    boolean removeDir(String str) throws RemoteException;

    int renMovFile(String str, String str2, boolean z, boolean z2, long j, long j2) throws RemoteException;

    void setAbortFlag(String str, boolean z) throws RemoteException;

    void statusInfo(String str, int i, int i2) throws RemoteException;

    public static abstract class Stub extends Binder implements IPluginFunctions {
        private static final String DESCRIPTOR = "com.android.tcplugins.FileSystem.IPluginFunctions";
        static final int TRANSACTION_deleteFile = 3;
        static final int TRANSACTION_disconnect = 14;
        static final int TRANSACTION_execute = 9;
        static final int TRANSACTION_getBitmap = 10;
        static final int TRANSACTION_getDirectoryList = 2;
        static final int TRANSACTION_getFile = 6;
        static final int TRANSACTION_getFileCallback = 16;
        static final int TRANSACTION_getLocalFileName = 11;
        static final int TRANSACTION_getModifiedLocalName = 18;
        static final int TRANSACTION_getSupportedFunctions = 1;
        static final int TRANSACTION_makeDir = 5;
        static final int TRANSACTION_putFile = 7;
        static final int TRANSACTION_putFileFromCallback = 17;
        static final int TRANSACTION_registerCallbacks = 13;
        static final int TRANSACTION_removeDir = 4;
        static final int TRANSACTION_renMovFile = 8;
        static final int TRANSACTION_setAbortFlag = 12;
        static final int TRANSACTION_statusInfo = 15;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPluginFunctions asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IPluginFunctions)) {
                return new Proxy(iBinder);
            }
            return (IPluginFunctions) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            IBinder iBinder;
            if (i != 1598968902) {
                boolean z = false;
                switch (i) {
                    case TRANSACTION_getSupportedFunctions:
                        parcel.enforceInterface(DESCRIPTOR);
                        int supportedFunctions = getSupportedFunctions();
                        parcel2.writeNoException();
                        parcel2.writeInt(supportedFunctions);
                        return true;
                    case TRANSACTION_getDirectoryList:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<PluginItem> directoryList = getDirectoryList(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(directoryList);
                        return true;
                    case TRANSACTION_deleteFile:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean deleteFile = deleteFile(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(deleteFile ? 1 : 0);
                        return true;
                    case TRANSACTION_removeDir:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean removeDir = removeDir(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(removeDir ? 1 : 0);
                        return true;
                    case TRANSACTION_makeDir:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean makeDir = makeDir(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(makeDir ? 1 : 0);
                        return true;
                    case TRANSACTION_getFile:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString = parcel.readString();
                        String[] createStringArray = parcel.createStringArray();
                        int file = getFile(readString, createStringArray, parcel.readInt(), parcel.readLong(), parcel.readLong());
                        parcel2.writeNoException();
                        parcel2.writeInt(file);
                        parcel2.writeStringArray(createStringArray);
                        return true;
                    case TRANSACTION_putFile:
                        parcel.enforceInterface(DESCRIPTOR);
                        int putFile = putFile(parcel.readString(), parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(putFile);
                        return true;
                    case TRANSACTION_renMovFile:
                        parcel.enforceInterface(DESCRIPTOR);
                        int renMovFile = renMovFile(parcel.readString(), parcel.readString(), parcel.readInt() != 0, parcel.readInt() != 0, parcel.readLong(), parcel.readLong());
                        parcel2.writeNoException();
                        parcel2.writeInt(renMovFile);
                        return true;
                    case TRANSACTION_execute:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] createStringArray2 = parcel.createStringArray();
                        int execute = execute(createStringArray2, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(execute);
                        parcel2.writeStringArray(createStringArray2);
                        return true;
                    case TRANSACTION_getBitmap:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bitmap bitmap = getBitmap(parcel.readString());
                        parcel2.writeNoException();
                        if (bitmap != null) {
                            parcel2.writeInt(1);
                            bitmap.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getLocalFileName:
                        parcel.enforceInterface(DESCRIPTOR);
                        String localFileName = getLocalFileName(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeString(localFileName);
                        return true;
                    case TRANSACTION_setAbortFlag:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString2 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            z = true;
                        }
                        setAbortFlag(readString2, z);
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_registerCallbacks:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerCallbacks(IRemoteProgressCallback.Stub.asInterface(parcel.readStrongBinder()), IRemoteDialogCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_disconnect:
                        parcel.enforceInterface(DESCRIPTOR);
                        String disconnect = disconnect(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeString(disconnect);
                        return true;
                    case TRANSACTION_statusInfo:
                        parcel.enforceInterface(DESCRIPTOR);
                        statusInfo(parcel.readString(), parcel.readInt(), parcel.readInt());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_getFileCallback:
                        parcel.enforceInterface(DESCRIPTOR);
                        IRemoteCopyCallback fileCallback = getFileCallback(parcel.readString());
                        parcel2.writeNoException();
                        if (fileCallback != null) {
                            iBinder = fileCallback.asBinder();
                        } else {
                            iBinder = null;
                        }
                        parcel2.writeStrongBinder(iBinder);
                        return true;
                    case TRANSACTION_putFileFromCallback:
                        parcel.enforceInterface(DESCRIPTOR);
                        int putFileFromCallback = putFileFromCallback(IRemoteCopyCallback.Stub.asInterface(parcel.readStrongBinder()), parcel.readString(), parcel.readInt(), parcel.readLong(), parcel.readLong());
                        parcel2.writeNoException();
                        parcel2.writeInt(putFileFromCallback);
                        return true;
                    case TRANSACTION_getModifiedLocalName:
                        parcel.enforceInterface(DESCRIPTOR);
                        String modifiedLocalName = getModifiedLocalName(parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeString(modifiedLocalName);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IPluginFunctions {
            public static IPluginFunctions sDefaultImpl;
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

            public int getSupportedFunctions() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportedFunctions();
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public List<PluginItem> getDirectoryList(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDirectoryList(str);
                    }
                    obtain2.readException();
                    ArrayList<PluginItem> createTypedArrayList = obtain2.createTypedArrayList(PluginItem.CREATOR);
                    obtain2.recycle();
                    obtain.recycle();
                    return createTypedArrayList;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean deleteFile(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    boolean z = false;
                    if (!this.mRemote.transact(3, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteFile(str);
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

            public boolean removeDir(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    boolean z = false;
                    if (!this.mRemote.transact(4, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeDir(str);
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

            public boolean makeDir(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    boolean z = false;
                    if (!this.mRemote.transact(5, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().makeDir(str);
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

            public int getFile(String str, String[] strArr, int i, long j, long j2) throws RemoteException {
                String[] strArr2 = strArr;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    String str2 = str;
                    obtain.writeString(str);
                    obtain.writeStringArray(strArr);
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    obtain.writeLong(j2);
                    try {
                        if (this.mRemote.transact(6, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                            obtain2.readException();
                            int readInt = obtain2.readInt();
                            obtain2.readStringArray(strArr);
                            obtain2.recycle();
                            obtain.recycle();
                            return readInt;
                        }
                        int file = Stub.getDefaultImpl().getFile(str, strArr, i, j, j2);
                        obtain2.recycle();
                        obtain.recycle();
                        return file;
                    } catch (Throwable th) {
                        obtain2.recycle();
                        obtain.recycle();
                        throw th;
                    }
                } catch (Throwable th2) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw new RemoteException();
                }
            }

            public int putFile(String str, String str2, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(7, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().putFile(str, str2, i);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int renMovFile(String str, String str2, boolean z, boolean z2, long j, long j2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    String str3 = str;
                    obtain.writeString(str);
                    String str4 = str2;
                    obtain.writeString(str2);
                    int i = 1;
                    obtain.writeInt(z ? 1 : 0);
                    if (!z2) {
                        i = 0;
                    }
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    obtain.writeLong(j2);
                    try {
                        if (this.mRemote.transact(8, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                            obtain2.readException();
                            int readInt = obtain2.readInt();
                            obtain2.recycle();
                            obtain.recycle();
                            return readInt;
                        }
                        int renMovFile = Stub.getDefaultImpl().renMovFile(str, str2, z, z2, j, j2);
                        obtain2.recycle();
                        obtain.recycle();
                        return renMovFile;
                    } catch (Throwable th) {
                        obtain2.recycle();
                        obtain.recycle();
                        throw th;
                    }
                } catch (Throwable th2) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw new RemoteException();
                }
            }

            public int execute(String[] strArr, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStringArray(strArr);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(9, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().execute(strArr, str);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readStringArray(strArr);
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public Bitmap getBitmap(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(10, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBitmap(str);
                    }
                    obtain2.readException();
                    Bitmap bitmap = obtain2.readInt() != 0 ? (Bitmap) Bitmap.CREATOR.createFromParcel(obtain2) : null;
                    obtain2.recycle();
                    obtain.recycle();
                    return bitmap;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public String getLocalFileName(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(11, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLocalFileName(str);
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

            public void setAbortFlag(String str, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(z ? 1 : 0);
                    if (this.mRemote.transact(12, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAbortFlag(str, z);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void registerCallbacks(IRemoteProgressCallback iRemoteProgressCallback, IRemoteDialogCallback iRemoteDialogCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iRemoteProgressCallback != null ? iRemoteProgressCallback.asBinder() : null);
                    if (iRemoteDialogCallback != null) {
                        iBinder = iRemoteDialogCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    if (this.mRemote.transact(13, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallbacks(iRemoteProgressCallback, iRemoteDialogCallback);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public String disconnect(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(14, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnect(str);
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

            public void statusInfo(String str, int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    if (this.mRemote.transact(15, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().statusInfo(str, i, i2);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public IRemoteCopyCallback getFileCallback(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(16, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileCallback(str);
                    }
                    obtain2.readException();
                    IRemoteCopyCallback asInterface = IRemoteCopyCallback.Stub.asInterface(obtain2.readStrongBinder());
                    obtain2.recycle();
                    obtain.recycle();
                    return asInterface;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int putFileFromCallback(IRemoteCopyCallback iRemoteCopyCallback, String str, int i, long j, long j2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iRemoteCopyCallback != null ? iRemoteCopyCallback.asBinder() : null);
                    String str2 = str;
                    obtain.writeString(str);
                    int i2 = i;
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    obtain.writeLong(j2);
                    try {
                        if (this.mRemote.transact(17, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                            obtain2.readException();
                            int readInt = obtain2.readInt();
                            obtain2.recycle();
                            obtain.recycle();
                            return readInt;
                        }
                        int putFileFromCallback = Stub.getDefaultImpl().putFileFromCallback(iRemoteCopyCallback, str, i, j, j2);
                        obtain2.recycle();
                        obtain.recycle();
                        return putFileFromCallback;
                    } catch (Throwable th) {
                        obtain2.recycle();
                        obtain.recycle();
                        throw th;
                    }
                } catch (Throwable th2) {
                    obtain2.recycle();
                    obtain.recycle();
                    throw new RemoteException();
                }
            }

            public String getModifiedLocalName(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (!this.mRemote.transact(18, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModifiedLocalName(str, str2);
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

        public static boolean setDefaultImpl(IPluginFunctions iPluginFunctions) {
            if (Proxy.sDefaultImpl != null || iPluginFunctions == null) {
                return false;
            }
            Proxy.sDefaultImpl = iPluginFunctions;
            return true;
        }

        public static IPluginFunctions getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
