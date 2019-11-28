package com.sphereon.alfresco.blockchain.agent.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;

public class VerifyBlockchainEntry {
    @XmlElement
    @ApiModelProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VerifyBlockchainEntryChainType chainType;

    @XmlElement
    @ApiModelProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String entryId;

    @XmlElement
    @ApiModelProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String chainId;

    public VerifyBlockchainEntryChainType getChainType() {
        return chainType;
    }

    public void setChainType(VerifyBlockchainEntryChainType chainType) {
        this.chainType = chainType;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }
}
