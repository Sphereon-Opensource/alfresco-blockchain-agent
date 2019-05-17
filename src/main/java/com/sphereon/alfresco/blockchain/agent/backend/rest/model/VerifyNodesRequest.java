package com.sphereon.alfresco.blockchain.agent.backend.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@ApiModel(description = "Committed context and settings response")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class VerifyNodesRequest {

    @XmlElement
    @ApiModelProperty(required = true)
    private List<String> nodeIds = new ArrayList<>();


    public List<String> getNodeIds() {
        return nodeIds;
    }


    public void setNodeIds(List<String> nodeIds) {
        this.nodeIds = nodeIds;
    }
}
