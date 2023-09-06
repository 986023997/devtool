package com.binturong.diy.devTool.client.telnet;

/**
 * @author 朱林
 * @description
 * @date 2021/11/23 17:13
 */
public class Response {
    private String responseId;
    private String requestId;
    private Object result;

    public Response() {
    }

    public Response(String responseId, String requestId, Object result) {
        this.responseId = responseId;
        this.requestId = requestId;
        this.result = result;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
