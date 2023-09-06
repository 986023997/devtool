package com.binturong.diy.devTool.core.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * @author zhulin
 * @date 2023-08-31 10:57
 */
public class AgentConfig {

    private Long javaPid;
    private String agentPath;
    private String corePath;
    private Integer serverPort;

    public String getCorePath() {
        return corePath;
    }

    public void setCorePath(String corePath) {
        this.corePath = corePath;
    }

    public long getJavaPid() {
        return javaPid;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public void setJavaPid(Long javaPid) {
        this.javaPid = javaPid;
    }

    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }



    public static AgentConfig parse(String args) {
        AgentConfig agentConfig = JSONObject.parseObject(args, AgentConfig.class);
        return agentConfig;
    }
}
