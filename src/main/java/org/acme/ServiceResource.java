package org.acme;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;

@Path("/sftp")
public class ServiceResource {
    @Inject
    SftpClient sftpClient;

    @GET
    @Path("/download")
    @Produces(MediaType.TEXT_PLAIN)
    public String download() {
        String filePath = "/upload/package.json";
        ByteArrayOutputStream output = sftpClient.download(filePath);

        if (output != null && output.toByteArray().length > 0) {
            return "download completed and found data in the file:\n" + output.toString();
        } else {
            return "download completed but not found data in the file.";
        }
    }
}