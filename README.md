# Sphereon Alfresco Blockchain Agent

The Sphereon Alfresco Blockchain integration enables Alfresco users to register proof-of-existence of files on a blockchain. Because by nature, Blockchains are immutable thus proof-of-existence cannot be tampered with and it is therefore a reliable way to prove that files within Alfresco have existed latest at the date and time of registration.

This repository contains the blockchain agent, a back-end service that can be run within or outside of an Alfresco Kubernetes Cluster. The agent bridges the gap between files within Alfresco and the Blockchain. It contains functionality to register the hashes of files onto the blockchain, and verify that a certain hash was written to the blockchain in the past. It is important to know that the content of files itself is never written to the Blockchain.

## Modules

There are two flavors: The `blockchain-agent-sphereon-proof` uses the [Sphereon Proof API](https://sphereon.com/developers/blockchain-api/), which is a commercially supported and easy way to get started with this Alfresco and blockchain. The Proof API will do the heavy lifting with regards to blockchain communication, which means it only requires an API key-secret pair for authenticating with Sphereon's API gateway. For [Factom Protocol](https://factomprotocol.org) users who want to run their own Factom daemon and optionally a Factom Wallet daemon there is the `blockchain-agent-factom`. This variant will connect to a Factom daemon to write entries to the Factom Blockchain, which is a more direct approach but requires setup of an additional daemon and funding using Entry Credits. It is also not commercially supported at this time. 

Whichever is best for you, the specifics of configuring either is specified in their own README. Common configuration, such as the Alfresco connection, is specified in this README.

## Alfresco

To connect the agent with Alfresco, set the following properties in `application.properties` or environment variables:
```
sphereon.blockchain.agent.alfresco.dns-name=http://alfresco.demo/
sphereon.blockchain.agent.alfresco.username=dummy-user
sphereon.blockchain.agent.alfresco.password=dummy-password

sphereon.blockchain.agent.alfresco.query.model=http://alfresco.sphereon.com/model/blockchain/1.0
```

The Alfresco connection of this agent was tested by running a cluster started using the `docker-compose` file on [github.com:Alfresco/acs-packaging](https://github.com/Alfresco/acs-packaging/blob/206309884df4f97444491a93d41ead7ed9dea780/docker-alfresco/test/docker-compose.yml)

More information on this can be found at [docs.alfresco.com](https://docs.alfresco.com/6.1/concepts/acs-deploy-architecture.html)

### Alfresco Configuration

TODO

## Blockchain integration

Add the following configuration to be able to sign entries before they are posted to blockchain:

```
sphereon.blockchain.agent.cert-path=/opt/sphereon/alfresco/cert/Alfresco-Blockchain.pfx
sphereon.blockchain.agent.cert-password=dummy-password
sphereon.blockchain.agent.cert-alias=blockchain
```
