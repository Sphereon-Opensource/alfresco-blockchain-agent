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

## Configuration
Configure an entry credits address that the local wallet has access to:
```
sphereon.blockchain.agent.factom.entry-credits.address=EC2uddT5TUToHGU34tp7fdhZagGwH5w2fFnpQ3GNNfUjeb7X18kF
```

Configure either a chain ID, or enable chain-creation:
```
sphereon.blockchain.agent.factom.chain.id=fe07dab8c2917366f376b454736ac07865626074691b30ffddddda4ff02a9451
sphereon.blockchain.agent.factom.chain.create=true
```
If a chain ID is specified, the create property will be ignored.
