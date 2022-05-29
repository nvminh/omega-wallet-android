package com.omegawallet.app.ui.widget.adapter;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.entity.tokens.TokenCardMeta;
import com.alphawallet.app.service.AssetDefinitionService;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.widget.TokensAdapterCallback;
import com.alphawallet.app.ui.widget.adapter.TokensAdapter;

public class SwapTokensAdapter extends TokensAdapter {
    private Token fromToken;

    public SwapTokensAdapter(TokensAdapterCallback tokensAdapterCallback, AssetDefinitionService aService, TokensService tService, ActivityResultLauncher<Intent> launcher, Token fromToken) {
        super(tokensAdapterCallback, aService, tService, launcher);
        this.fromToken = fromToken;
    }

    protected SwapTokensAdapter(TokensAdapterCallback tokensAdapterCallback, AssetDefinitionService aService) {
        super(tokensAdapterCallback, aService);
    }

    @Override
    public boolean canDisplayToken(TokenCardMeta token) {
        return super.canDisplayToken(token);
    }
}
