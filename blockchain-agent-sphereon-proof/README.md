# Sphereon Alfresco Blockchain Agent - Sphereon Proof integration

The Alfresco blockchain agent can write proof-of-existence to Blockchain using Sphereon's Blockchain Proof API.

## Authentication
To authenticate with Sphereons API Gateway, a so-called consumer-key and consumer-secret have to be configured. To obtain a key and secret, perform the following steps:
- Go to [store.sphereon.com](https://store.sphereon.com)
- Sign up for an account
- Register an application (Create application) in the Application section. We'll assume the name "alfresco-blockchain" in the configuration below
  + This application will correspond to the blockchain-agent deployment
- Generate sandbox and/or production keys for the application from the application detail page
  + These will be used in the runtime configuration of the alfresco blockchain agent
- Subscribe this application to the Blockchain Proof, Easy Blockchain and Crypto Keys API in the API section

## Configuration
To use the Sphereon Blockchain Proof API, configure the following properties:
```
BLOCKCHAIN_CONSUMER_KEY=consumer-key-from-store
BLOCKCHAIN_CONSUMER_SECRET=consumer-secret-from-store
sphereon.store.application-name=alfresco-blockchain
sphereon.blockchain.agent.blockchain-proof.config-name=alfresco-blockchain-factom
sphereon.blockchain.agent.blockchain-proof.context=factom
```

## Blockchain Proof configuration

TODO: Explain configuration options and how to configure

The `config-name` and `context` from the configuration above have functional defaults, but they can be overriden by a custom configuration.
To create a configuration, go to the Blockchain Proof API in the API section and use the API Console to create a configuration.
