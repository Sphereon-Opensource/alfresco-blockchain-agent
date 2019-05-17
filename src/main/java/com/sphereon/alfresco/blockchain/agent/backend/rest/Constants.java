package com.sphereon.alfresco.blockchain.agent.backend.rest;

import springfox.documentation.service.Tag;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;

public final class Constants {

    /**
     * #########################################################################
     * # ENDPOINTS                                                             #
     * #########################################################################
     */
    public static final class Endpoints {

        public static final class AlfrescoBlockchain {
            public static final String BASE = "/alfresco-blockchain";
            public static final String VERIFY = BASE + "/verify";
            public static final String VERIFY_ENTRIES = VERIFY + "/entries";
        }
    }


    /**
     * #########################################################################
     * # PARAMS                                                             #
     * #########################################################################
     */
    public static final class Param {
        public static final String NODE_IDS = "nodeIds";
    }


    /**
     * #########################################################################
     * # Controller Operations                                                 #
     * #########################################################################
     */
    public static final class VerifyEntries {
        public static final String SHORT_DESCRIPTION = "Verify alfresco entries";
        public static final String OPERATION_ID = "verifyEntries";
        public static final String LONG_DESCRIPTION = "Performs verification on the blockchain for the given node entry id's.";
    }

    /**
     * #########################################################################
     * # TAGS                                                                  #
     * #########################################################################
     */

    public static final class Tags {
        public static final String BLOCKCHAIN = "Blockchain";
    }


    public enum TagEnum {
        ALFRESCO_BLOCKCHAIN_SERVICE(Tags.BLOCKCHAIN, "Blockchain related APIs for Alfresco");

        private final String tagName;
        private final String description;


        TagEnum(String tagName, String description) {
            this.tagName = tagName;
            this.description = description;
        }


        public Tag asTag() {
            return new Tag(tagName, description);
        }


        public String getTagName() {
            return tagName;
        }


        public String getDescription() {
            return description;
        }


        public static Tags[] all() {
            return (Tags[]) Arrays.stream(TagEnum.values()).map(TagEnum::asTag).toArray();
        }


        public static Docket initDocket(Docket docket) {
            return docket.tags(ALFRESCO_BLOCKCHAIN_SERVICE.asTag());
        }
    }

}
