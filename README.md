# Sphereon Alfresco Blockchain Agent

// TODO: Shortly describe functionality of Sphereon blockchain integration. Then explain role of agent in this.

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

## Blockchain integration

Add the following configuration to be able to sign entries before they are posted to blockchain:

```
sphereon.blockchain.agent.cert-path=/opt/sphereon/alfresco/cert/Alfresco-Blockchain.pfx
sphereon.blockchain.agent.cert-password=dummy-password
sphereon.blockchain.agent.cert-alias=blockchain
```

### Connection to blockchain

The Sphereon Alfresco blockchain integration can work with either a direct Factom connection (option 1) or the Sphereon Blockchain Proof API (option 2).
The difference is that in the later case, the Blockchain Proof API does the heavy lifting regarding blockchain interaction. Using a direct Factom connection requires a `factomd` node connected to the Factom blockchain network and an entrycredits address to fund posting entries to the blockchain.

#### Option 1: Factom connection

// TODO: Describe configuration needed

#### Option 2: Sphereon Blockchain Proof API connection

To use the Sphereon Blockchain Proof API, configure the following properties:
```
BLOCKCHAIN_CONSUMER_KEY=uTqiAhah_lcYM3VZdNg34yrzvqsa
BLOCKCHAIN_CONSUMER_SECRET=y8NxjeMMEb8fNN_heZK7f1lEqaEa
sphereon.store.application-name
sphereon.blockchain.agent.blockchain-proof.config-name=alfresco-blockchain-factom
sphereon.blockchain.agent.blockchain-proof.context=factom
```

The consumer key and secret can be obtained from [store.sphereon.com](https://store.sphereon.com).
Register an account and use it to sign in. Then, create an application. This application will correspond to the blockchain-agent deployment within or outside of the Alfresco cluster.
The application should subscribe to the Blockchain Proof and Easy Blockchain APIs in the store. Generate the consumer key and secret from the application detail page under the tab "Production Keys".

The `config-name` and `context` have functional defaults as shown above, but they can be overriden by a custom configuration. See the Blockchain Proof API documentation (found at store.sphereon.com) for more information on configurations.
