package uk.nhs.digital.iucds.middleware.client;

import java.io.ByteArrayInputStream;
import org.springframework.stereotype.Component;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Component
public class SftpClient {

  private String SFTPHOST;
  private String SFTPPORT;
  private String SFTPUSER;
  private String privateKey;
  private String targetDirectory;

  public SftpClient() {
  }
  
  public SftpClient(String sFTPHOST, String sFTPPORT, String sFTPUSER, String privateKey, String targetDirectory) {
    this.SFTPHOST = sFTPHOST;
    this.SFTPPORT = sFTPPORT;
    this.SFTPUSER = sFTPUSER;
    this.privateKey = privateKey;
    this.targetDirectory = targetDirectory;
  }
  
  public void SendFileToServer(byte[] transform, String fileName) throws SftpException {
    log.info("preparing the host information for sftp.");
    try {
      JSch jsch = new JSch();
      jsch.addIdentity(privateKey);
      Session session = jsch.getSession(SFTPUSER, SFTPHOST, Integer.parseInt(SFTPPORT));
      session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
      java.util.Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config);
      session.connect(1200);
      log.info("Host connected.");
      
      Channel channel = session.openChannel("sftp");
      ChannelSftp sftp = (ChannelSftp) channel;
      sftp.connect(600);
      channel = session.openChannel("sftp");
      channel.connect();
      log.info("sftp channel opened and connected.");

      try {
        sftp.cd(targetDirectory);
        sftp.put(new ByteArrayInputStream(transform), fileName);
        log.info("File transfered successfully to host.");
      } catch (Exception e) {
        log.error("Exception found while tranfer the response."); 
        log.error(e.getMessage());
      }
      channel.disconnect();
      log.info("Channel disconnected.");
      sftp.disconnect();
      log.info("Host Session disconnected.");
    } catch (JSchException e) {
      log.error("Exception found while tranfer the response.");
      log.error(e.getMessage());
    }
  }
}