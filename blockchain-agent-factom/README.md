## Daemons
`blockchain-agent-factom` uses the [Factom Java client](https://github.com/bi-foundation/factom-java) to connect to [FactomD](https://github.com/FactomProject/factomd) and [WalletD](https://github.com/FactomProject/factom-walletd).
The defaults for Factom-java-client are used if no configuration is specified, which are `localhost8088` for FactomD and `localhost:8089` for WalletD.

The defaults can be overwritten:
```
sphereon.blockchain.agent.factom.factomd.url=http://localhost:8088/v2
sphereon.blockchain.agent.factom.factomd.timeout=1000
sphereon.blockchain.agent.factom.factomd.username=foo
sphereon.blockchain.agent.factom.factomd.password=bar

sphereon.blockchain.agent.factom.walletd.url=http://localhost:8089/v2
sphereon.blockchain.agent.factom.walletd.timeout=1000
sphereon.blockchain.agent.factom.walletd.username=foo
sphereon.blockchain.agent.factom.walletd.password=bar
```

## Chains

Proof of existence will be written to a single chain. A fixed chain ID can be set, or a chain can be created deterministically.

## Configuration
Configure an entry credits address that the local wallet has access to:
```
sphereon.blockchain.agent.factom.entry-credits.address=EC2uddT5TUToHGU34tp7fdhZagGwH5w2fFnpQ3GNNfUjeb7X18kF
```

Configure either a chain ID, or supply chain names from which a chain will deterministically be created:
```
sphereon.blockchain.agent.factom.chain.id=fe07dab8c2917366f376b454736ac07865626074691b30ffddddda4ff02a9451
sphereon.blockchain.agent.factom.chain.names=sphereon-alfresco,demo
```
If a chain ID is specified, the create property will be ignored.

# Tests

The alfresco-blockchain-agent-factom module is tested using Wiremock.
Mappings and responses have been defined in `src/resources/factom-walletd-wiremock` and `/src/resources/factomd-wiremock`.
Within both of these folders, there are two folders corresponding to the two main tasks: registration and verification.
Within these folders, the standard Wiremock setup applies of having `mapping` files (to match to an incoming HTTP requests) and `responses` (responses sent back as referenced from mappings).

The tests use `WireMockClassRule` to activate the needed combination of FactomD or WalletD and registration or verification.
