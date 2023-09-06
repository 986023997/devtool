package com.binturong.diy.devTool.client.telnet.future;

import java.util.concurrent.Future;

/**
 * @author 朱林
 * @description
 * @date 2021/11/24 17:17
 */
public interface WriteFuture<T> extends Future<T> {

    void setResponse(T t);
    String getRequestId();

    void setWriteResult(boolean success);

    void setCause(Throwable cause);

    boolean isWriteSuccess();

}
