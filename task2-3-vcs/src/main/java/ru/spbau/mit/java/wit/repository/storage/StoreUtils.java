package ru.spbau.mit.java.wit.repository.storage;

import org.apache.commons.codec.binary.Hex;
import ru.spbau.mit.java.wit.log.Logging;
import ru.spbau.mit.java.wit.model.id.ShaId;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */

class StoreUtils {
    private static Logger logger = Logging.getLogger(StoreUtils.class.getName());
    private StoreUtils() {}

    /**
     * Use that packer if you need to use {@code StoreUtils::writeSha}
     * or {@code StoreUtils::write} function to write File somewhere.
     */
    static InputStream filePack(File f) throws IOException {
        return new BufferedInputStream(new FileInputStream(f));
    }

    public static InputStream idPack(InputStream is) {
        return is;
    }

    static InputStream stringPack(String str) {
        byte[] bytes = str.getBytes();
        return new ByteArrayInputStream(bytes, 0, bytes.length);
    }

    static String stringUnpack(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        // TODO: br is not explicitly closed, but unpack caller must call
        // TODO: close on `is` input parameter
        return br.readLine();
    }

    /**
     * Writes given object {@code obj} to specified directory.
     * File at specified directory is created with name equal to
     * SHA-1 hash code of contents, calculated on returned by {@code packer}
     * byte stream
     *
     * @param obj     object to be written to file
     * @param dirName directory, to write file at
     * @param packer  function to convert given object to byte stream
     */
    static <T> ShaId writeSha(T obj, Path dirName, IOFunction<T, InputStream> packer)
            throws IOException {

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        File tmpFile = dirName.resolve(UUID.randomUUID().toString()).toFile();

        try (OutputStream out =
                     new BufferedOutputStream(new FileOutputStream(tmpFile));
             InputStream in = packer.apply(obj)) {

            byte[] buf = new byte[1024];
            int cnt;
            while (true) {
                cnt = in.read(buf);
                if (cnt == -1) {
                    break;
                }
                md.update(buf, 0, cnt);
                out.write(buf, 0, cnt);
            }
        }

        ShaId id = ShaId.create(Hex.encodeHexString(md.digest()));
        File newFile = dirName.resolve(id.toString()).toFile();

        if (!tmpFile.renameTo(newFile)) {
            logger.info("File is already exists in store " +
                        "(or sha codes clash happen and universe is near it's end)");
        }

        return id;
    }

    /**
     * Simple object writer, which takes object, file name and object -> byte stream
     * converter and writes that byte stream to given file
     *
     * @param obj      object to be written
     * @param fileName name of target file to be written
     * @param packer   object -> byte stream converter
     */
    static <T> void write(T obj, Path fileName, IOFunction<T, InputStream> packer)
            throws IOException {

        try (OutputStream out =
                     new BufferedOutputStream(new FileOutputStream(fileName.toFile()));
             InputStream in = packer.apply(obj)) {

            byte[] buf = new byte[1024];
            int cnt;
            while (true) {
                cnt = in.read(buf);
                if (cnt == -1) {
                    break;
                }
                out.write(buf, 0, cnt);
            }
        }
    }

    /**
     * Reads file, but using unpacker to retrieve actual object
     */
    static <T> T read(Path fileName, IOFunction<InputStream, T> unpacker)
            throws IOException {

        try (InputStream is =
                     new BufferedInputStream(new FileInputStream(fileName.toFile()))) {
            return unpacker.apply(is);
        }
    }
}
