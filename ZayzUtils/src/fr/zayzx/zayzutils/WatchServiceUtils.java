package fr.zayzx.zayzutils;

import java.io.IOException;
import java.nio.file.*;

public final class WatchServiceUtils {

    private WatchServiceUtils() {}

    public static void watch(Path path, Runnable onChange) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        new Thread(() -> {
            while (true) {
                try {
                    WatchKey key = watchService.take();
                    key.pollEvents();
                    onChange.run();
                    key.reset();
                } catch (Exception ignored) {}
            }
        }).start();
    }
}
