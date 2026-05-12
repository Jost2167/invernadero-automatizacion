package com.jost.invernadero.taigasync.pipeline;

public record SyncResult(
        int processedStories,
        int schemasWritten,
        int validationFailures,
        int collisionFailures,
        int skippedStories,
        int commentsPosted,
        int storiesMarkedDone) {
}
