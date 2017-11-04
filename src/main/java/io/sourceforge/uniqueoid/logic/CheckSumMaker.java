package io.sourceforge.uniqueoid.logic;

import io.sourceforge.uniqueoid.ResourcesProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Created by MontolioV on 30.05.17.
 */
public class CheckSumMaker {
    private final MessageDigest MESSAGE_DIGEST;
    private ResourcesProvider resProvider = ResourcesProvider.getInstance();
    private final int MAX_BUFFER_SIZE;

    public CheckSumMaker(FindTaskSettings findTaskSettings) {
        MAX_BUFFER_SIZE = findTaskSettings.getMaxBufferSize();
        try {
            this.MESSAGE_DIGEST = MessageDigest.getInstance(findTaskSettings.getHashAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                    resProvider.getStrFromExceptionBundle("noSuchHashAlgorithm"), e);
        }
    }

    public String makeCheckSum(File file) throws IOException {
        if (file.length() == 0) {
            throw new IOException(
                    resProvider.getStrFromExceptionBundle("fileIsEmpty") +
                    "\t" + file.toString());
        }

        byte[] bytesHash = makeBytesHash(file);
        return stringRepresentation(bytesHash);
    }

    private byte[] makeBytesHash(File file) throws IOException {
        try (BufferedInputStream bufferedIS = new BufferedInputStream(new FileInputStream(file))) {
            int bufferSize = file.length() < MAX_BUFFER_SIZE ? (int) file.length() : MAX_BUFFER_SIZE;
            byte[] digestBuffer = new byte[bufferSize];
            int inputStreamResponse = bufferedIS.read(digestBuffer);

            while (inputStreamResponse > -1) {
                if (inputStreamResponse == bufferSize) {
                    MESSAGE_DIGEST.update(digestBuffer);
                } else {
                    MESSAGE_DIGEST.update(digestBuffer, 0, inputStreamResponse);
                }
                inputStreamResponse = bufferedIS.read(digestBuffer);
            }
            return MESSAGE_DIGEST.digest();
        } catch (IOException e) {
            throw new IOException(resProvider.getStrFromExceptionBundle("hashingFail") +
                    "\t" + file.getAbsolutePath(), e);
        }
    }

    private String stringRepresentation(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
