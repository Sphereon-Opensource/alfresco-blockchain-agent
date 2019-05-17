package com.sphereon.alfresco.blockchain.agent.backend.rest.model;

import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
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
    private List<VerifyContentResponse> contentResponses = new ArrayList<>();


    public static VerifyNodesResponse wrap(List<VerifyContentResponse> result) {
        VerifyNodesResponse response = new VerifyNodesResponse();
        response.setContentResponses(result);
        return response;
    }


    public List<VerifyContentResponse> getContentResponses() {
        return contentResponses;
    }


    public void setContentResponses(List<VerifyContentResponse> contentResponses) {
        this.contentResponses = contentResponses;
    }
}
