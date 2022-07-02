package com.omegawallet.app.service;

import com.alphawallet.app.repository.TokenRepository;
import com.alphawallet.app.service.GasService;
import com.alphawallet.app.service.KeyService;
import com.alphawallet.ethereum.EthereumNetworkBase;
import com.omegawallet.app.web3.swap.IERC20;
import com.omegawallet.app.web3.swap.IUniswapV2Pair;
import com.omegawallet.app.web3.swap.UniswapV2Router02;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SwapService {

    enum TOKEN {
        WETH("WETH",
                "0xc778417E063141139Fce010982780140Aa0cD5Ab",
                3),
        MIH02(
                "MIH02",
                "0x8752c35EF21A719Ca32Ad99dD959c911236da5c2",
                3),
        MINH(
                "MINH",
                "0x6756cd4c9692292Be5D71a6491B43a88691f4847",
                3),
        DAI("DAI",
                "0x31f42841c2db5173425b5223809cf3a38fede360",
                3);

        private String symbol;
        private String address;
        private long chainId;
        TOKEN(String symbol, String address, long chainId) {
            this.symbol = symbol;
            this.address = address;
            this.chainId = chainId;
        }
    }

    private static final String SWAP_ADDRESS = "0xAFe340fd5004391EA27F876e2Fd1B45140792DC2";
    private final GasService gasService;

    private ContractGasProvider gasProvider = new DefaultGasProvider();

    private static Map<Long, Set<String>> swapMap = new HashMap<>();

    static {
        Set<String> set = new HashSet<>();
        set.add(TOKEN.DAI.address.toLowerCase() + TOKEN.MIH02.address.toLowerCase());
        set.add(TOKEN.MIH02.address.toLowerCase() + TOKEN.DAI.address.toLowerCase());
        set.add(TOKEN.MINH.address.toLowerCase() + TOKEN.MIH02.address.toLowerCase());
        set.add(TOKEN.MIH02.address.toLowerCase() + TOKEN.MINH.address.toLowerCase());
        set.add(TOKEN.WETH.address.toLowerCase() + TOKEN.MIH02.address.toLowerCase());
        set.add(TOKEN.MIH02.address.toLowerCase() + TOKEN.WETH.address.toLowerCase());
        set.add(TOKEN.WETH.address.toLowerCase() + TOKEN.MINH.address.toLowerCase());
        set.add(TOKEN.MINH.address.toLowerCase() + TOKEN.WETH.address.toLowerCase());


        swapMap.put(EthereumNetworkBase.ROPSTEN_ID, set);
    }

    public SwapService(KeyService keyService, GasService gasService) {
        this.gasService = gasService;
    }

    private UniswapV2Router02 getUniswapRouter(long chainId, Credentials credentials) {
        return UniswapV2Router02.load(SWAP_ADDRESS, TokenRepository.getWeb3jService(chainId), credentials, gasProvider);
    }

    private IUniswapV2Pair getIUniswapV2Pair(long chainId, Credentials credentials) {
        return IUniswapV2Pair.load(SWAP_ADDRESS, TokenRepository.getWeb3jService(chainId), credentials, gasProvider);
    }

    private IERC20 getIERC20(long chainId, Credentials credentials, String token) {
        return IERC20.load(token, TokenRepository.getWeb3jService(chainId), credentials, gasProvider);
    }

    public RemoteFunctionCall<List> getAmountsOut(BigInteger amountIn, List<String> path, long chainId, Credentials credentials) {
        return getUniswapRouter(chainId, credentials).getAmountsOut(amountIn, path);
    }

    public RemoteFunctionCall<TransactionReceipt> swapExactTokensForTokens(long chainId, Credentials credentials, BigInteger amountIn, BigInteger amountOutMin, List<String> tokens, String address, BigInteger deadline) {
        return getUniswapRouter(chainId, credentials).swapExactTokensForTokens(amountIn, amountOutMin, tokens, address, deadline);
    }

    public boolean canSwap(long chainId, String tokenA, String tokenB) {
        Set<String> set = swapMap.get(chainId);
        return set != null && set.contains((tokenA + tokenB).toLowerCase());
    }

    public RemoteFunctionCall<TransactionReceipt> approve(long chainId, Credentials credentials, String token, BigInteger value) {
        return getIERC20(chainId, credentials, token).approve(SWAP_ADDRESS, value);
    }
}
