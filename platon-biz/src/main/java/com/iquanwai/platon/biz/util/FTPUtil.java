package com.iquanwai.platon.biz.util;

import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class FTPUtil {

    private FTPClient client;

    private static final Integer FTP_PORT = 21;

    Logger logger = LoggerFactory.getLogger(getClass());

    public FTPUtil() {
        client = new FTPClient();
    }

    public FTPUtil(boolean isFTPS) {
        if (isFTPS) {
            client = new FTPSClient("SSL", true);
        } else {
            client = new FTPClient();
        }
    }

    public boolean changeDir(String remotePath) throws IOException {
        return client.changeWorkingDirectory(remotePath);
    }

    public boolean connect() throws IOException {
        return connect(ConfigUtils.getFtpHost(), ConfigUtils.getFtpUser(), ConfigUtils.getFtpPassword(), FTP_PORT);
    }

    public boolean connect(String host, String login, String password, int port) throws IOException {
        logger.info("FTP request connect to " + host + ":" + port);
        client.connect(host, port);
        int reply = client.getReplyCode();

        if (client instanceof FTPSClient) {
            try {
                ((FTPSClient) client).execPROT("P");
            } catch (SSLException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        if (FTPReply.isPositiveCompletion(reply)) {
            logger.info("FTP request login as " + login);
            if (client.login(login, password)) {
                client.enterLocalPassiveMode();
                return true;
            }
        }
        disconnect();
        return false;
    }

    public void disconnect() throws IOException {
        logger.info("FTP request disconnect");
        client.disconnect();
    }

    public boolean downloadFile(String remotePath, String localFile) throws IOException {
        boolean rst;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(localFile);
            rst = client.retrieveFile(remotePath, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return rst;
    }

    public InputStream getFileStream(String remotePath) throws IOException {
        InputStream rst = null;
        rst = client.retrieveFileStream(remotePath);
        return rst;
    }

    // this method must be called after file transferring
    public boolean completeTransfer() throws IOException {
        return client.completePendingCommand();
    }

    public Vector<String> listFileInDir(String remoteDir) throws IOException {
        if (changeDir(remoteDir)) {
            FTPFile[] files = client.listFiles();
            Vector<String> v = new Vector<String>();
            for (FTPFile file : files) {
                if (!file.isDirectory()) {
                    v.addElement(file.getName());
                }
            }
            return v;
        } else {
            return null;
        }
    }

    public boolean renameFile(String oldFileName, String newFileName) {
        try {
            return client.rename(oldFileName, newFileName);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public boolean storeFile(String filePath, InputStream local) {
        try {
            return client.storeFile(filePath, local);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public void setBinaryType() {
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            logger.error("setting ftp to binary type with io exception;", e);
        }
    }

    public void setTimeout(int timeout) {
        client.setConnectTimeout(timeout);
    }

}
