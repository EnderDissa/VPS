package com.example.warehouse.auth;


import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;

@Getter
public class AuthRequest implements Serializable {
    private static final long serialVersionUID = 2L;

    public static final int CREATED = 0;
    public static final int SUCCESS = 1;
    public static final int ERROR = 2;

    private final String id;
    private final RequestType type;
    private final Object payload;

    private int status = CREATED;
    private Object result = null;

    public AuthRequest(String id, RequestType type, Object payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
    }

    public AuthRequest(RequestType type, Object payload) {
        this(UUID.randomUUID().toString(), type, payload);
    }

    public void setResult(boolean success, Object result) {
        if (this.status != CREATED) {
            throw new IllegalStateException("Result already exists");
        }
        this.status = success ? SUCCESS : ERROR;
        this.result = result;
    }

    @Override
    public String toString() {
        return "AuthRequest(type=" + type +
                ", payload=" + payload +
                ", status=" + status +
                ", id=" + id +
                ", result=" + result + ")";
    }

    @Getter
    public static class Response implements Serializable {
        private final int status;
        private final Object result;

        public Response(int status, Object result) {
            this.status = status;
            this.result = result;
        }

        @Override
        public String toString() {
            return "Response(status=" + status + ", result=" + result + ")";
        }
    }
}