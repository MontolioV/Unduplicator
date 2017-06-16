package com.unduplicator;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Directory Handler takes directory and puts checksums of all files recursively in storage.
 * <p>Created by MontolioV on 06.06.17.
 */
public class DirectoryHandler extends RecursiveAction {
    private final File DIRECTORY;
    private final CheckSumMaker CHECKSUM_MAKER;
    private final ConcurrentLinkedQueue<FileAndChecksum> QUEUE_FILE_AND_CHECKSUM;
    private final ConcurrentLinkedQueue<String> QUEUE_EX_MESSAGES;
    private final String HASH_ALGORITHM;
    private ArrayList<DirectoryHandler> tasks = new ArrayList<>();

    public DirectoryHandler(File directory,
                            String hashAlgorithm,
                            ConcurrentLinkedQueue<FileAndChecksum> queueProcessed,
                            ConcurrentLinkedQueue<String> queueExMessages)
    {
        this.CHECKSUM_MAKER = new CheckSumMaker(hashAlgorithm);
        this.DIRECTORY = directory;
        this.QUEUE_FILE_AND_CHECKSUM = queueProcessed;
        this.HASH_ALGORITHM = hashAlgorithm;
        this.QUEUE_EX_MESSAGES = queueExMessages;
    }

    /**
     * The main computation performed by this task.
     */
    @Override
    protected void compute() {
        if (DIRECTORY.listFiles() == null) {
            if (DIRECTORY.isFile()) {
                processFile(DIRECTORY);
            } else {
                QUEUE_EX_MESSAGES.offer("Not a folder, not a file. Maybe I/O trouble. \n" + DIRECTORY.toString());
            }
        } else {
            for (File fileFromDir : DIRECTORY.listFiles()) {
                if (fileFromDir.isDirectory()) {
                    DirectoryHandler task = new DirectoryHandler(fileFromDir,
                            HASH_ALGORITHM,
                            QUEUE_FILE_AND_CHECKSUM,
                            QUEUE_EX_MESSAGES);
                    task.fork();
                    tasks.add(task);
                } else {
                    processFile(fileFromDir);
                }
            }
        }

        tasks.forEach(ForkJoinTask::join);
    }

    private void processFile(File file) {
        try {
            FileAndChecksum pair = new FileAndChecksum(file, CHECKSUM_MAKER.makeCheckSum(file));
            QUEUE_FILE_AND_CHECKSUM.offer(pair);
        } catch (IOException e) {
            StringJoiner sj = new StringJoiner("\n");
            sj.add(e.toString());

            Exception causeExc = (Exception) e.getCause();
            while (causeExc != null) {
                sj.add(causeExc.toString());
                causeExc = (Exception) causeExc.getCause();
            }

            QUEUE_EX_MESSAGES.offer(sj.toString());
        }
    }
}
