package com.udpmail.common.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Response<T> {

    private String requestId;
    private StatusCode status;
    private String message;
    private T data;

    public Response() {
    }

    public Response(
            String requestId,
            StatusCode status,
            String message,
            T data
    ) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success(
            String requestId,
            String message,
            T data
    ) {
        return new Response<>(
                requestId,
                StatusCode.SUCCESS,
                message,
                data
        );
    }

    public static <T> Response<T> error(
            String requestId,
            StatusCode status,
            String message
    ) {
        return new Response<>(
                requestId,
                status,
                message,
                null
        );
    }

    @JsonIgnore
    public boolean isSuccess() {
        return status == StatusCode.SUCCESS;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public StatusCode getStatus() {
        return status;
    }

    public void setStatus(StatusCode status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}