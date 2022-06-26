package com.omegawallet.app.util;

import com.alphawallet.app.entity.tokens.Token;

public class TokenUtil {
    public static final String WETH_ADDRESS = "0xc778417E063141139Fce010982780140Aa0cD5Ab";

    public static String getAddress(Token token) {
        if("ETH".equalsIgnoreCase(token.tokenInfo.symbol)) {
            return WETH_ADDRESS;
        }
        return token.tokenInfo.address;
    }
}
