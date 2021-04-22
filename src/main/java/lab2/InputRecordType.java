package lab2;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Serializable data class for input key for Spark {@link Serializable}
 */
@AllArgsConstructor
@Data
public class InputRecordType implements Serializable {
    /**
     * Post identifier
     */
    private long postId;

    /**
     * User identifier
     */
    private long userId;

    /**
     * Interaction timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Interaction type identifier
     */
    private int interactionTypeId;
}
