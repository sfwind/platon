package com.iquanwai.platon.biz.util;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by justin on 2018/1/6.
 */
public class SFTPUtil {

    private final static int PORT = 22;

    Logger logger = LoggerFactory.getLogger(getClass());

    public void upload(String path, String fileName, InputStream is){

        JSch jsch = new JSch();
        Session session = null;
        try {
            // 采用指定的端口连接服务器
            session = jsch.getSession(ConfigUtils.getFtpUser(), ConfigUtils.getFtpHost(), PORT);

            // 设置登陆主机的密码
            session.setPassword(ConfigUtils.getFtpPassword()); // 设置密码
            // 设置第一次登陆的时候提示，可选值：(ask | yes | no)
            session.setConfig("StrictHostKeyChecking", "no");
            // 设置登陆超时时间
            session.connect(10000);

            // 创建sftp通信通道
            Channel channel = session.openChannel("sftp");
            channel.connect(1000);
            ChannelSftp sftp = (ChannelSftp) channel;

            // 进入服务器指定的文件夹
            sftp.cd(path);
            logger.info("开始传输");
            // 以下代码实现从本地上传一个文件到服务器
            OutputStream os = sftp.put(fileName);

            byte b[] = new byte[1024];
            int n;
            while ((n = is.read(b)) != -1) {
                os.write(b, 0, n);
            }
            logger.info("传输结束");
            os.flush();
            os.close();
            is.close();
        } catch (JSchException | SftpException | IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            if(session != null){
                session.disconnect();
            }
        }

    }
}
