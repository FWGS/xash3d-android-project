#include <jni.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <android/log.h>
int fd[2];

JNIEXPORT int JNICALL Java_su_xash_storage_1helper_StorageHelperActivity_getFd()
{


		int fd1 = open("/data/data/su.xash.storage_helper/files/test", O_RDONLY);
	socketpair( AF_UNIX, SOCK_STREAM, 0, &fd[0] );
	
	return fd1;//fd[1];
}

int send_fd(int fd1)
{
	struct msghdr msg = {0};
	unsigned char c = 0;
	struct iovec io = { .iov_base = &c, .iov_len = 1, };
	char cmsg_buf[CMSG_SPACE(sizeof(fd1))];
	msg.msg_control = cmsg_buf;
	msg.msg_controllen = CMSG_SPACE(sizeof(fd1));
	struct cmsghdr *cmsg = CMSG_FIRSTHDR(&msg);
	cmsg->cmsg_level = SOL_SOCKET;
	cmsg->cmsg_type = SCM_RIGHTS;
	cmsg->cmsg_len = CMSG_LEN(4);
	memcpy(CMSG_DATA(cmsg), &fd1, 4);
	__android_log_print( ANDROID_LOG_VERBOSE, "XashStorage", "send %d %d %s", fd1, sendmsg(fd[0], &msg, 0), strerror(errno));
}

JNIEXPORT void JNICALL Java_su_xash_storage_1helper_StorageHelperActivity_sendPath()
{
	int fd1 = open( "/data/data/su.xash.storage_helper/files/test", O_RDONLY );
	close(fd[1]);
	while(1)
		send_fd(fd1);
}
#if 0
#include <sys/socket.h>
#include <sys/un.h>
DECLARE_JNI_INTERFACE( int, receiveFD, jint sockfd )
{
	fchdir(sockfd);
	renameat(sockfd, "dir", -100, "/data/data/su.xash.engine/files/dir");
	/*byte resp = 0;
	struct msghdr msg = {0};
	struct iovec io = { .iov_base = &resp, .iov_len = sizeof( resp ) };
	char cmsg_buf[20];
	struct cmsghdr *cmsg;
	int fd;
	msg.msg_iov = &io;
	msg.msg_iovlen = 1;
	msg.msg_control = cmsg_buf;
	msg.msg_controllen = sizeof(cmsg_buf);
	cmsg = CMSG_FIRSTHDR(&msg);
	cmsg->cmsg_level = SOL_SOCKET;
	cmsg->cmsg_type = SCM_RIGHTS;
	cmsg->cmsg_len = CMSG_LEN( sizeof( int ));
	recvmsg(sockfd, &msg, 0);
	memcpy( &fd, CMSG_DATA(cmsg), sizeof(fd));
	return fd;*/
}
#endif
