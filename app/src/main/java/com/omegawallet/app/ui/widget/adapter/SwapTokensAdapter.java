package com.omegawallet.app.ui.widget.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;

import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.entity.tokens.TokenCardMeta;
import com.alphawallet.app.service.AssetDefinitionService;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.widget.TokensAdapterCallback;
import com.alphawallet.app.ui.widget.adapter.TokensAdapter;
import com.alphawallet.app.ui.widget.holder.BinderViewHolder;
import com.alphawallet.app.ui.widget.holder.TokenHolder;
import com.omegawallet.app.TokenUtil;
import com.omegawallet.app.service.SwapService;

public class SwapTokensAdapter extends TokensAdapter {
    private Token fromToken;
    private View selectedItem;
    private Token selectedToken;
    private int unselectedColor = -1;
    private final SwapService swapService;

    public SwapTokensAdapter(TokensAdapterCallback tokensAdapterCallback, AssetDefinitionService aService, TokensService tService, SwapService swapService, ActivityResultLauncher<Intent> launcher, Token fromToken) {
        super(tokensAdapterCallback, aService, tService, launcher);
        this.swapService = swapService;
        this.fromToken = fromToken;
    }

    @Override
    public boolean canDisplayToken(TokenCardMeta token) {
        return super.canDisplayToken(token) && canSwap(token);
    }

    private boolean canSwap(TokenCardMeta token) {
        return fromToken.getTokenInfo().chainId == token.getChain() && !fromToken.getAddress().equalsIgnoreCase(token.getAddress()) &&
                swapService.canSwap(token.getChain(), TokenUtil.getAddress(fromToken), TokenUtil.getAddress(tokensService.getToken(token.getChain(), token.getAddress())));
    }

    @Override
    public void onBindViewHolder(final BinderViewHolder holder, int position) {
        if(unselectedColor == -1) {
            unselectedColor = holder.itemView.getDrawingCacheBackgroundColor();
        }
        items.get(position).view = holder;
        holder.bind(items.get(position).value);

        holder.itemView.setOnClickListener(view -> {
            if(holder.getClass() == TokenHolder.class) {
                selectedItem = view;
                selectedToken = ((TokenHolder)holder).token;
                notifyDataSetChanged();
            }
        });

        holder.itemView.setBackgroundColor(holder.itemView == selectedItem ? Color.LTGRAY : unselectedColor);
    }

    public Token getSelectedToken() {
        return selectedToken;
    }
}
