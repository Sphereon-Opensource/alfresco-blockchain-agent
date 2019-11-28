package com.sphereon.alfresco.blockchain.agent.rest.model;

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
public class VerifyNodesResponse {
    @XmlElement
    @ApiModelProperty(required = true)
    private List<VerifyContentAlfrescoResponse> contentResponses = new ArrayList<>();

    public static VerifyNodesResponse wrap(List<VerifyContentAlfrescoResponse> result) {
        final var response = new VerifyNodesResponse();
        response.setContentResponses(result);
        return response;
    }

    public List<VerifyContentAlfrescoResponse> getContentResponses() {
        return contentResponses;
    }

    public void setContentResponses(List<VerifyContentAlfrescoResponse> contentResponses) {
        this.contentResponses = contentResponses;
    }
}
