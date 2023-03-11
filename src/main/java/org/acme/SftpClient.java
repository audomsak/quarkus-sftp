package org.acme;


import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class SftpClient {
    @Inject
    Logger log;

    private static final String REMOTE_HOST = "localhost"; // replace your remote host here
    private static final String USERNAME = "user"; // replace your username here
    private static final String PASSWORD = "pass"; // replace your password here
    private static final int REMOTE_PORT = 22;
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;

    private Session connect() throws JSchException {
        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT);

        // authenticate using password
        jschSession.setPassword(PASSWORD);
        jschSession.setConfig("StrictHostKeyChecking", "no");

        // 10 seconds session timeout
        jschSession.connect(SESSION_TIMEOUT);
        log.info("Successfully connected to server.");

        return jschSession;
    }

    private void disconnect(Session jschSession) {
        if (jschSession != null) {
            jschSession.disconnect();
            log.info("Success fully disconnected from server.");
        }
    }

    public Boolean upload(String filePath, byte[] data) {
        Session jschSession = null;

        try {
            jschSession = connect();
            Channel sftp = jschSession.openChannel("sftp");

            // 5 seconds timeout
            sftp.connect(CHANNEL_TIMEOUT);

            ChannelSftp channelSftp = (ChannelSftp) sftp;

            // transfer file from local to remote server
            channelSftp.put(new ByteArrayInputStream(data), filePath);
            channelSftp.exit();

            log.info("File upload was completed.");

            return true;
        } catch (JSchException | SftpException e) {
            log.error("Failed to upload file to server", e);
        } finally {
            disconnect(jschSession);
        }

        return false;
    }

    public ByteArrayOutputStream download(String filePath) {
        Session jschSession = null;

        try {
            jschSession = connect();
            Channel sftp = jschSession.openChannel("sftp");
            // 5 second timeout
            sftp.connect(CHANNEL_TIMEOUT);

            ChannelSftp channelSftp = (ChannelSftp) sftp;

            // transfer file from local to remote server
            InputStream input = channelSftp.get(filePath);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(input, output);

            channelSftp.exit();
            log.info("File download was completed.");

            return output;
        } catch (JSchException | SftpException | IOException e) {
            log.error("Failed to download file from server.", e);
        } finally {
            disconnect(jschSession);
        }

        return null;
    }
}
