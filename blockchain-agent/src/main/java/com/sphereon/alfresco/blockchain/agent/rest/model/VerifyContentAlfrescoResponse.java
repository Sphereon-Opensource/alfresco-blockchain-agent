package com.sphereon.alfresco.blockchain.agent.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@ApiModel(description = "Verify Content response")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class VerifyContentAlfrescoResponse {
    @XmlElement
    @ApiModelProperty
    private String nodeId;

    @XmlElement
    @ApiModelProperty(notes = "The hash in base64 format that you supplied or that was calculated. This is the actual hash for the content", required = true, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hash;

    @XmlElement
    @ApiModelProperty(notes = "The calculated signature in hex form", required = true, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hexSignature;

    @XmlElement
    @ApiModelProperty(notes = "The calculated signature in base64 form", required = true, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String base64Signature;

    @XmlElement
    @ApiModelProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OffsetDateTime registrationTime;

    @XmlElement
    @ApiModelProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VerifyBlockchainEntry> blockchainEntries;

    @XmlElement
    @ApiModelProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AlfrescoBlockchainRegistrationState registrationState;

    public VerifyContentAlfrescoResponse() {
        this.blockchainEntries = new ArrayList<>();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getBase64Signature() {
        return base64Signature;
    }

    public void setBase64Signature(String base64Signature) {
        this.base64Signature = base64Signature;
    }

    public String getHexSignature() {
        return hexSignature;
    }

    public void setHexSignature(String hexSignature) {
        this.hexSignature = hexSignature;
    }

    public OffsetDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(OffsetDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    public List<VerifyBlockchainEntry> getBlockchainEntries() {
        return blockchainEntries;
    }

    public void setBlockchainEntries(List<VerifyBlockchainEntry> blockchainEntries) {
        this.blockchainEntries = blockchainEntries;
    }

    public AlfrescoBlockchainRegistrationState getRegistrationState() {
        return registrationState;
    }

    public void setRegistrationState(AlfrescoBlockchainRegistrationState registrationState) {
        this.registrationState = registrationState;
    }

    public String getChainId(final VerifyBlockchainEntryChainType type) {
        return blockchainEntries.stream()
                .filter(entry -> entry.getChainType().equals(type))
                .findFirst()
                .map(VerifyBlockchainEntry::getChainId)
                .orElse(null);
    }
}
