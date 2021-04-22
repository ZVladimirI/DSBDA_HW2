package lab2;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 *Serializable data class for output key {@link Serializable}
 */
@AllArgsConstructor
@Data
public class ReturnRecordKey implements Serializable {
    /**
     * Post identifier
     */
    private long postId;

    /**
     * Interaction type name
     */
    private String interaction;

    /**
     * Custom implementation of toString() method
     * @return string representation
     */
    @Override
    public String toString() {
        return postId + "," + interaction;
    }
}
