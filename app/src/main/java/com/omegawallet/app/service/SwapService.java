package com.omegawallet.app.service;

import com.alphawallet.app.repository.TokenRepository;
import com.alphawallet.app.service.GasService;
import com.alphawallet.app.service.KeyService;
import com.omegawallet.app.web3.swap.UniswapV2Router02;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.RemoteFunctionCall;

import java.math.BigInteger;
import java.util.List;

public class SwapService {
    private static final String SWAP_ADDRESS = "0xAFe340fd5004391EA27F876e2Fd1B45140792DC2";
    private final GasService gasService;

    public SwapService(KeyService keyService, GasService gasService) {
        this.gasService = gasService;
    }

    private UniswapV2Router02 getUniswapRouter(long chainId, Credentials credentials) {
        return UniswapV2Router02.load(SWAP_ADDRESS, TokenRepository.getWeb3jService(chainId), credentials, gasService);
    }

    public RemoteFunctionCall<List> getAmountsOut(BigInteger amountIn, List<String> path, long chainId, Credentials credentials) {
        return getUniswapRouter(chainId, credentials).getAmountsOut(amountIn, path);
    }
}
