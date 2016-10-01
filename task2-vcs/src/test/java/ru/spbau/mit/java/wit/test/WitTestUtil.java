package ru.spbau.mit.java.wit.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import ru.spbau.mit.java.wit.command.WitAdd;
import ru.spbau.mit.java.wit.command.WitBranch;
import ru.spbau.mit.java.wit.command.WitCommit;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by: Egor Gorbunov
 * Date: 10/2/16
 * Email: egor-mailbox@ya.com
 */
public class WitTestUtil {
    private final Path userRepoDir;
    private final WitStorage storage;

    public WitTestUtil(Path userRepoDir, WitStorage storage) {
        this.userRepoDir = userRepoDir;
        this.storage = storage;
    }

    public Path writeFile(String file, List<String> content) throws IOException {
        Path p = userRepoDir.resolve(file);
        Files.createDirectories(p.getParent());
        FileUtils.writeLines(p.toFile(), content);
        return p;
    }

    public ShaId commitFile(String file, List<String> content) throws IOException {
        Path p = writeFile(file, content);
        WitAdd addCmd = new WitAdd();
        addCmd.setFileNames(Collections.singletonList(p.toString()));
        addCmd.execute(userRepoDir, storage);
        WitCommit commitCmd = new WitCommit();
        commitCmd.setMsg(content + "_" + UUID.randomUUID().toString());
        commitCmd.execute(userRepoDir, storage);
        return storage.readBranch(storage.readCurBranchName()).getHeadCommitId();
    }

    public ShaId commitFile(String file) throws IOException {
        ArrayList<String> content = new ArrayList<>();
        int n = RandomUtils.nextInt(1, 1000);
        for (int i = 0; i < n; ++i) {
            RandomStringUtils.random(RandomUtils.nextInt(1, 120));
        }
        return commitFile(file, content);
    }

    public ShaId commitFile(String file, String content) throws IOException {
        return commitFile(file, Collections.singletonList(content));
    }

    public String createBranch() throws IOException {
        String name = UUID.randomUUID().toString();
        WitBranch bCmd = new WitBranch();
        bCmd.setBranchName(name);
        bCmd.execute(userRepoDir, storage);
        return name;
    }

}
