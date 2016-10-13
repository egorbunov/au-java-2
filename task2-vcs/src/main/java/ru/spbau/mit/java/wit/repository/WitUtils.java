package ru.spbau.mit.java.wit.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class WitUtils {

    /**
     * Returns proper wit storage root basing to given directory
     * @param baseDir base directory for version control
     * @return wit vcs storage root dir
     */
    public static Path resolveStoragePath(Path baseDir) {
        return baseDir.resolve(".wit");
    }

    /**
     * Returns path, from which service specific part was removed,
     * so this path is path to actual user repository data
     */
    public static Path stripWitStoragePath(Path witStoragePath) {
        return witStoragePath.getParent();
    }

    private static Path getWitLeastServicePrefix(Path witRoot) {
        Path servicePrefix = witRoot;
        while (!servicePrefix.getParent().equals(stripWitStoragePath(witRoot))) {
            // that is done because service path may be not just root/.wit, but root/.what/.wit
            servicePrefix = servicePrefix.getParent();
        }
        return servicePrefix;
    }

    /**
     * Checks if given path is under wit service storage directory
     */
    public static boolean isWitServicePath(Path path, Path witRoot) {
        return path.startsWith(getWitLeastServicePrefix(witRoot));
    }

    /**
     * Returns stream of file ABSOLUTE paths such that
     * service wit (.wit/...) paths are not included
     *
     * @param repositoryRoot path to some directory
     * @param witRoot path to storage repository root
     */
    public static Stream<Path> walk(Path repositoryRoot, Path witRoot) throws IOException {
        Path leastServicePrefix = getWitLeastServicePrefix(witRoot);
        return Files.walk(repositoryRoot)
                .filter(p -> !p.startsWith(leastServicePrefix));
    }

    public static class CollectedPaths {
        public final Set<Path> existingPaths = new HashSet<>();
        public final Set<Path> nonExistingPaths = new HashSet<>();
        public final Set<Path> prohibitedPaths = new HashSet<>();
    }

    /**
     * Walks through all files specified recursively; Filters non-existing files and files,
     * which are under wit service directories
     * @param fileNames list of file names to walk through
     * @param witRoot root of wit storage path
     * @return collected paths with non-existing, prohibited (service or outside repo) and existing
     */
    public static CollectedPaths collectExistingFiles(List<String> fileNames,
                                              Path witRoot) throws IOException {
        // checking if all files are existing
        Path userRepositoryPath = WitUtils.stripWitStoragePath(witRoot);

        CollectedPaths res = new CollectedPaths();

        List<Path> pathToTraverse = new ArrayList<>();
        for (String s : fileNames) {
            Path p = Paths.get(s);
            if (Files.notExists(p)) {
                res.nonExistingPaths.add(p);
            } else {
                p = p.toAbsolutePath().normalize();
                if (WitUtils.isWitServicePath(p, witRoot) || !p.startsWith(userRepositoryPath)) {
                    res.prohibitedPaths.add(p);
                } else {
                    pathToTraverse.add(p);
                }
            }
        }

        // collecting files
        for (Path p : pathToTraverse) {
            WitUtils.walk(p, witRoot)
                    .filter(Files::isRegularFile)
                    .forEach(fp -> res.existingPaths.add(fp));
        }

        return res;
    }
}
