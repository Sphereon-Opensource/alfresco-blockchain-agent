package com.sphereon.alfresco.blockchain.agent.rest;

import com.sphereon.alfresco.blockchain.agent.rest.model.ErrorResponse;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyNodesRequest;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyNodesResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.ondemand.VerifyRegistrations;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.MDC;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController(value = "Alfresco blockchain functions")
@Api(description = "API for blockchain functions using the Alfresco content repository", value = "API for blockchain functions", tags = Constants.Tags.BLOCKCHAIN)
public class AlfrescoBlockchainController {
    private static final String HEADER_AUTHORIZATION = "Authorization";

    private static final XLogger logger = XLoggerFactory.getXLogger(AlfrescoBlockchainController.class);

    private final VerifyRegistrations verifyRegistrations;

    public AlfrescoBlockchainController(final VerifyRegistrations verifyRegistrations) {
        this.verifyRegistrations = verifyRegistrations;
    }

    @ApiOperation(nickname = Constants.VerifyEntries.OPERATION_ID, value = Constants.VerifyEntries.SHORT_DESCRIPTION, notes = Constants.VerifyEntries.LONG_DESCRIPTION)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Node entries successfully proc."),
            @ApiResponse(code = 400, message = "Verification request failed", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "One or more node id's could not be found", response = ErrorResponse.class)
    })
    @PostMapping(value = Constants.Endpoints.AlfrescoBlockchain.VERIFY_ENTRIES, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public VerifyNodesResponse verifyEntries(@ApiParam(required = true, name = Constants.Param.NODE_IDS) @RequestBody final VerifyNodesRequest verifyNodesRequest,
                                             final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.entry(verifyNodesRequest);
        }

        Assert.notNull(verifyNodesRequest, "Non-null verifyNodesRequest expected");
        Assert.notNull(verifyNodesRequest.getNodeIds(), "Non-null verifyNodesRequest.nodeIds expected");

        final var credentials = request.getHeader(HEADER_AUTHORIZATION);
        final var verifyResponses = verifyRegistrations.execute(verifyNodesRequest.getNodeIds(), credentials);
        if (logger.isDebugEnabled()) {
            logger.exit(verifyResponses);
        }
        MDC.clear();

        // TODO: throw RestExceptions with documented http codes
        return VerifyNodesResponse.wrap(verifyResponses);
    }
}
