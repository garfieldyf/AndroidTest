package android.ext.util;

import java.io.IOException;

/**
 * Class <tt>ErrnoException</tt> is wrapper for system <tt>errno</tt>.
 * @author Garfield
 */
public final class ErrnoException extends RuntimeException {
    private static final long serialVersionUID = -6868038019772577506L;

    /**
     * Operation not permitted.
     */
    public static final int EPERM = 1;

    /**
     * No such file or directory.
     */
    public static final int ENOENT = 2;

    /**
     * No such process.
     */
    public static final int ESRCH = 3;

    /**
     * Interrupted system call.
     */
    public static final int EINTR = 4;

    /**
     * I/O error.
     */
    public static final int EIO = 5;

    /**
     * No such device or address.
     */
    public static final int ENXIO = 6;

    /**
     * Argument list too long.
     */
    public static final int E2BIG = 7;

    /**
     * Exec format error.
     */
    public static final int ENOEXEC = 8;

    /**
     * Bad file number.
     */
    public static final int EBADF = 9;

    /**
     * No child processes.
     */
    public static final int ECHILD = 10;

    /**
     * Try again.
     */
    public static final int EAGAIN = 11;

    /**
     * Out of memory.
     */
    public static final int ENOMEM = 12;

    /**
     * Permission denied.
     */
    public static final int EACCES = 13;

    /**
     * Bad address.
     */
    public static final int EFAULT = 14;

    /**
     * Block device required.
     */
    public static final int ENOTBLK = 15;

    /**
     * Device or resource busy.
     */
    public static final int EBUSY = 16;

    /**
     * File exists.
     */
    public static final int EEXIST = 17;

    /**
     * Cross-device link.
     */
    public static final int EXDEV = 18;

    /**
     * No such device.
     */
    public static final int ENODEV = 19;

    /**
     * Not a directory.
     */
    public static final int ENOTDIR = 20;

    /**
     * Is a directory.
     */
    public static final int EISDIR = 21;

    /**
     * Invalid argument.
     */
    public static final int EINVAL = 22;

    /**
     * File table overflow.
     */
    public static final int ENFILE = 23;

    /**
     * Too many open files.
     */
    public static final int EMFILE = 24;

    /**
     * Not a typewriter.
     */
    public static final int ENOTTY = 25;

    /**
     * Text file busy.
     */
    public static final int ETXTBSY = 26;

    /**
     * File too large.
     */
    public static final int EFBIG = 27;

    /**
     * No space left on device.
     */
    public static final int ENOSPC = 28;

    /**
     * Illegal seek.
     */
    public static final int ESPIPE = 29;

    /**
     * Read-only file system.
     */
    public static final int EROFS = 30;

    /**
     * Too many links.
     */
    public static final int EMLINK = 31;

    /**
     * Broken pipe.
     */
    public static final int EPIPE = 32;

    /**
     * Math argument out of domain of func.
     */
    public static final int EDOM = 33;

    /**
     * Math result not representable.
     */
    public static final int ERANGE = 34;

    /**
     * Resource deadlock would occur.
     */
    public static final int EDEADLK = 35;

    /**
     * File name too long.
     */
    public static final int ENAMETOOLONG = 36;

    /**
     * No record locks available.
     */
    public static final int ENOLCK = 37;

    /**
     * Function not implemented.
     */
    public static final int ENOSYS = 38;

    /**
     * Directory not empty.
     */
    public static final int ENOTEMPTY = 39;

    /**
     * Too many symbolic links encountered.
     */
    public static final int ELOOP = 40;

    /**
     * Operation would block (same as {@link #EAGAIN}).
     */
    public static final int EWOULDBLOCK = EAGAIN;

    /**
     * No message of desired type.
     */
    public static final int ENOMSG = 42;

    /**
     * Identifier removed.
     */
    public static final int EIDRM = 43;

    /**
     * Channel number out of range.
     */
    public static final int ECHRNG = 44;

    /**
     * Level 2 not synchronized.
     */
    public static final int EL2NSYNC = 45;

    /**
     * Level 3 halted.
     */
    public static final int EL3HLT = 46;

    /**
     * Level 3 reset.
     */
    public static final int EL3RST = 47;

    /**
     * Link number out of range.
     */
    public static final int ELNRNG = 48;

    /**
     * Protocol driver not attached.
     */
    public static final int EUNATCH = 49;

    /**
     * No CSI structure available.
     */
    public static final int ENOCSI = 50;

    /**
     * Level 2 halted.
     */
    public static final int EL2HLT = 51;

    /**
     * Invalid exchange.
     */
    public static final int EBADE = 52;

    /**
     * Invalid request descriptor.
     */
    public static final int EBADR = 53;

    /**
     * Exchange full.
     */
    public static final int EXFULL = 54;

    /**
     * No anode.
     */
    public static final int ENOANO = 55;

    /**
     * Invalid request code.
     */
    public static final int EBADRQC = 56;

    /**
     * Invalid slot.
     */
    public static final int EBADSLT = 57;

    /**
     * Resource deadlock avoided (same as {@link #EDEADLK}).
     */
    public static final int EDEADLOCK = EDEADLK;

    /**
     * Bad font file format.
     */
    public static final int EBFONT = 59;

    /**
     * Device not a stream.
     */
    public static final int ENOSTR = 60;

    /**
     * No data available.
     */
    public static final int ENODATA = 61;

    /**
     * Timer expired.
     */
    public static final int ETIME = 62;

    /**
     * Out of streams resources.
     */
    public static final int ENOSR = 63;

    /**
     * Machine is not on the network.
     */
    public static final int ENONET = 64;

    /**
     * Package not installed.
     */
    public static final int ENOPKG = 65;

    /**
     * Object is remote.
     */
    public static final int EREMOTE = 66;

    /**
     * Link has been severed.
     */
    public static final int ENOLINK = 67;

    /**
     * Advertise error.
     */
    public static final int EADV = 68;

    /**
     * Srmount error.
     */
    public static final int ESRMNT = 69;

    /**
     * Communication error on send.
     */
    public static final int ECOMM = 70;

    /**
     * Protocol error.
     */
    public static final int EPROTO = 71;

    /**
     * Multihop attempted.
     */
    public static final int EMULTIHOP = 72;

    /**
     * RFS specific error.
     */
    public static final int EDOTDOT = 73;

    /**
     * Not a data message.
     */
    public static final int EBADMSG = 74;

    /**
     * Value too large for defined data type.
     */
    public static final int EOVERFLOW = 75;

    /**
     * Name not unique on network.
     */
    public static final int ENOTUNIQ = 76;

    /**
     * File descriptor in bad state.
     */
    public static final int EBADFD = 77;

    /**
     * Remote address changed.
     */
    public static final int EREMCHG = 78;

    /**
     * Can not access a needed shared library.
     */
    public static final int ELIBACC = 79;

    /**
     * Accessing a corrupted shared library.
     */
    public static final int ELIBBAD = 80;

    /**
     * .lib section in a.out corrupted.
     */
    public static final int ELIBSCN = 81;

    /**
     * Attempting to link in too many shared libraries.
     */
    public static final int ELIBMAX = 82;

    /**
     * Cannot exec a shared library directly.
     */
    public static final int ELIBEXEC = 83;

    /**
     * Illegal byte sequence.
     */
    public static final int EILSEQ = 84;

    /**
     * Interrupted system call should be restarted.
     */
    public static final int ERESTART = 85;

    /**
     * Streams pipe error.
     */
    public static final int ESTRPIPE = 86;

    /**
     * Too many users.
     */
    public static final int EUSERS = 87;

    /**
     * Socket operation on non-socket.
     */
    public static final int ENOTSOCK = 88;

    /**
     * Destination address required.
     */
    public static final int EDESTADDRREQ = 89;

    /**
     * Message too long.
     */
    public static final int EMSGSIZE = 90;

    /**
     * Protocol wrong type for socket.
     */
    public static final int EPROTOTYPE = 91;

    /**
     * Protocol not available.
     */
    public static final int ENOPROTOOPT = 92;

    /**
     * Protocol not supported.
     */
    public static final int EPROTONOSUPPORT = 93;

    /**
     * Socket type not supported.
     */
    public static final int ESOCKTNOSUPPORT = 94;

    /**
     * Operation not supported on transport endpoint.
     */
    public static final int EOPNOTSUPP = 95;

    /**
     * Protocol family not supported.
     */
    public static final int EPFNOSUPPORT = 96;

    /**
     * Address family not supported by protocol.
     */
    public static final int EAFNOSUPPORT = 97;

    /**
     * Address already in use.
     */
    public static final int EADDRINUSE = 98;

    /**
     * Cannot assign requested address.
     */
    public static final int EADDRNOTAVAIL = 99;

    /**
     * Network is down.
     */
    public static final int ENETDOWN = 100;

    /**
     * Network is unreachable.
     */
    public static final int ENETUNREACH = 101;

    /**
     * Network dropped connection because of reset.
     */
    public static final int ENETRESET = 102;

    /**
     * Software caused connection abort.
     */
    public static final int ECONNABORTED = 103;

    /**
     * Connection reset by peer.
     */
    public static final int ECONNRESET = 104;

    /**
     * No buffer space available.
     */
    public static final int ENOBUFS = 105;

    /**
     * Transport endpoint is already connected.
     */
    public static final int EISCONN = 106;

    /**
     * Transport endpoint is not connected.
     */
    public static final int ENOTCONN = 107;

    /**
     * Cannot send after transport endpoint shutdown.
     */
    public static final int ESHUTDOWN = 108;

    /**
     * Too many references: cannot splice.
     */
    public static final int ETOOMANYREFS = 109;

    /**
     * Connection timed out.
     */
    public static final int ETIMEDOUT = 110;

    /**
     * Connection refused.
     */
    public static final int ECONNREFUSED = 111;

    /**
     * Host is down.
     */
    public static final int EHOSTDOWN = 112;

    /**
     * No route to host.
     */
    public static final int EHOSTUNREACH = 113;

    /**
     * Operation already in progress.
     */
    public static final int EALREADY = 114;

    /**
     * Operation now in progress.
     */
    public static final int EINPROGRESS = 115;

    /**
     * Stale NFS file handle.
     */
    public static final int ESTALE = 116;

    /**
     * Structure needs cleaning.
     */
    public static final int EUCLEAN = 117;

    /**
     * Not a XENIX named type file.
     */
    public static final int ENOTNAM = 118;

    /**
     * No XENIX semaphores available.
     */
    public static final int ENAVAIL = 119;

    /**
     * Is a named type file.
     */
    public static final int EISNAM = 120;

    /**
     * Remote I/O error.
     */
    public static final int EREMOTEIO = 121;

    /**
     * Quota exceeded.
     */
    public static final int EDQUOT = 122;

    /**
     * No medium found.
     */
    public static final int ENOMEDIUM = 123;

    /**
     * Wrong medium type.
     */
    public static final int EMEDIUMTYPE = 124;

    /**
     * Operation Canceled.
     */
    public static final int ECANCELED = 125;

    /**
     * Required key not available.
     */
    public static final int ENOKEY = 126;

    /**
     * Key has expired.
     */
    public static final int EKEYEXPIRED = 127;

    /**
     * Key has been revoked.
     */
    public static final int EKEYREVOKED = 128;

    /**
     * Key was rejected by service.
     */
    public static final int EKEYREJECTED = 129;

    /**
     * Owner died.
     */
    public static final int EOWNERDEAD = 130;

    /**
     * State not recoverable.
     */
    public static final int ENOTRECOVERABLE = 131;

    /**
     * The system <tt>errno</tt>.
     */
    public final int errno;

    /**
     * Constructs a new <tt>ErrnoException</tt> with the current stack trace and
     * the specified system <tt>errno</tt> and detail message.
     * @param errno The system <tt>errno</tt>.
     * @param detailMessage The detail message for this exception.
     * @see #ErrnoException(int, String, Throwable)
     */
    public ErrnoException(int errno, String detailMessage) {
        super(detailMessage);
        this.errno = errno;
    }

    /**
     * Constructs a new <tt>ErrnoException</tt> with the current stack trace and
     * the specified system <tt>errno</tt>, detail message and the specified cause.
     * @param errno The system <tt>errno</tt>.
     * @param detailMessage The detail message for this exception.
     * @param throwable The cause of this exception.
     * @see #ErrnoException(int, String)
     */
    public ErrnoException(int errno, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.errno = errno;
    }

    /**
     * Throws this exception as an <tt>IOException</tt>.
     */
    public final void throwAsIOException() throws IOException {
        throw new IOException(getMessage());
    }
}
