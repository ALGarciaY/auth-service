package co.com.bancolombia.model.user.errors;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final BusinessError error;
    private final Object details; // opcional

    public BusinessException(BusinessError error) {
        super(error.getDefaultMsg());
        this.error = error;
        this.details = null;
    }

    public BusinessException(BusinessError error, String overrideMessage) {
        super(overrideMessage != null ? overrideMessage : error.getDefaultMsg());
        this.error = error;
        this.details = null;
    }

    public BusinessException(BusinessError error, Object details) {
        super(error.getDefaultMsg());
        this.error = error;
        this.details = details;
    }

    public BusinessException(BusinessError error, String overrideMessage, Object details) {
        super(overrideMessage != null ? overrideMessage : error.getDefaultMsg());
        this.error = error;
        this.details = details;
    }
}
