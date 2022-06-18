package com.omegawallet.app.widget;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.alphawallet.app.R;
import com.alphawallet.app.entity.ActionSheetInterface;
import com.alphawallet.app.entity.SignAuthenticationCallback;
import com.alphawallet.app.entity.StandardFunctionInterface;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.service.KeyService;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.HomeActivity;
import com.alphawallet.app.ui.WalletConnectActivity;
import com.alphawallet.app.ui.widget.entity.ActionSheetCallback;
import com.alphawallet.app.util.Utils;
import com.alphawallet.app.widget.ActionSheetMode;
import com.alphawallet.app.widget.AddressDetailView;
import com.alphawallet.app.widget.AmountDisplayWidget;
import com.alphawallet.app.widget.AssetDetailView;
import com.alphawallet.app.widget.BalanceDisplayWidget;
import com.alphawallet.app.widget.ConfirmationWidget;
import com.alphawallet.app.widget.FunctionButtonBar;
import com.alphawallet.app.widget.NetworkDisplayWidget;
import com.alphawallet.app.widget.SignDataWidget;
import com.alphawallet.app.widget.WalletConnectRequestWidget;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.omegawallet.app.service.SwapService;

import org.web3j.protocol.core.RemoteFunctionCall;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by JB on 17/11/2020.
 */
public class SwapActionSheetDialog extends BottomSheetDialog implements StandardFunctionInterface, ActionSheetInterface
{
    public static final String WETH_ADDRESS = "0xc778417E063141139Fce010982780140Aa0cD5Ab";

    private final ImageView cancelButton;
    private final BalanceDisplayWidget balanceDisplay;
    private final NetworkDisplayWidget networkDisplay;
    private final ConfirmationWidget confirmationWidget;
    private final AmountDisplayWidget amountDisplay;

    private final AmountDisplayWidget toAmountDisplay;

    private final AssetDetailView assetDetailView;
    private final FunctionButtonBar functionBar;
    private final WalletConnectRequestWidget walletConnectRequestWidget;
    private final Activity activity;

    private final Token fromToken;
    private final Token toToken;
    private final TokensService tokensService;
    private final KeyService keyService;
    private final SwapService swapService;

    private final ActionSheetCallback actionSheetCallback;
    private final Wallet wallet;
    private SignAuthenticationCallback signCallback;
    private ActionSheetMode mode;
//    private final long callbackId;

    private String txHash = null;
    private boolean actionCompleted;
    private boolean use1559Transactions = false;
    private final BigInteger sendAmount;

    public SwapActionSheetDialog(@NonNull Activity activity, BigInteger sendAmount, Wallet wallet, Token fromToken, Token toToken,
                                 TokensService ts, SwapService swapService, KeyService keyService,
                                 ActionSheetCallback aCallBack)
    {
        super(activity);
        View view = View.inflate(getContext(), R.layout.dialog_swap_action_sheet, null);
        setContentView(view);

        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        this.swapService = swapService;
        this.keyService = keyService;
        this.wallet = wallet;
        this.sendAmount = sendAmount;

        balanceDisplay = findViewById(R.id.balance);
        networkDisplay = findViewById(R.id.network_display_widget);
        cancelButton = findViewById(R.id.image_close);
        confirmationWidget = findViewById(R.id.confirmation_view);

        amountDisplay = findViewById(R.id.amount_display);
        amountDisplay.setLabelAmount("From");
        toAmountDisplay = findViewById(R.id.to_amount_display);
        toAmountDisplay.setLabelAmount("To");
        assetDetailView = findViewById(R.id.asset_detail);
        functionBar = findViewById(R.id.layoutButtons);
        this.activity = activity;
        if (activity instanceof HomeActivity)
        {
            mode = ActionSheetMode.SEND_TRANSACTION_DAPP;
        }
        else if (activity instanceof WalletConnectActivity)
        {
            mode = ActionSheetMode.SEND_TRANSACTION_WC;
        }
        else
        {
            mode = ActionSheetMode.SEND_TRANSACTION;
        }

        signCallback = null;
        walletConnectRequestWidget = null;

        actionSheetCallback = aCallBack;
        actionCompleted = false;

        this.fromToken = fromToken;
        this.toToken = toToken;
        tokensService = ts;

        balanceDisplay.setupBalance(fromToken, tokensService, null);
        networkDisplay.setNetwork(fromToken.tokenInfo.chainId);

        functionBar.setupFunctions(this, new ArrayList<>(Collections.singletonList(R.string.action_confirm)));
        functionBar.revealButtons();

        updateAmount();

        setupCancelListeners();
        isAttached = true;
    }


    public void onDestroy()
    {
        if (assetDetailView != null) assetDetailView.onDestroy();
    }

    public void setURL(String url)
    {
        AddressDetailView requester = findViewById(R.id.requester);
        requester.setupRequester(url);

        amountDisplay.setVisibility(View.VISIBLE);
        amountDisplay.setAmountUsingToken(sendAmount, tokensService.getServiceToken(fromToken.tokenInfo.chainId), tokensService);
    }

    @Override
    public void updateAmount()
    {
        showAmount(getTransactionAmount().toBigInteger());
    }

    @Override
    public void handleClick(String action, int id)
    {
        switch (mode)
        {
            case SEND_TRANSACTION_WC:
            case SEND_TRANSACTION:
            case SEND_TRANSACTION_DAPP:
            case SPEEDUP_TRANSACTION:
            case CANCEL_TRANSACTION:
                //check gas and warn user
//                if (!gasWidgetInterface.checkSufficientGas())
//                {
//                    askUserForInsufficientGasConfirm();
//                }
//                else
//                {
//                    sendTransaction();
//                }
                sendTransaction();
                break;
            case SIGN_MESSAGE:
                signMessage();
                break;
            case SIGN_TRANSACTION:
                signTransaction();
                break;
            case MESSAGE:
                actionSheetCallback.buttonClick(0, fromToken);
                break;
            case WALLET_CONNECT_REQUEST:
                if (id == R.string.approve)
                {
                    actionSheetCallback.notifyWalletConnectApproval(walletConnectRequestWidget.getChainIdOverride());
                    tryDismiss();
                }
                else
                {
                    actionSheetCallback.denyWalletConnect();
                }
                break;
        }

        actionSheetCallback.notifyConfirm(mode.toString());
    }

    private BigDecimal getTransactionAmount()
    {

        return new BigDecimal(sendAmount);
    }

    private void signMessage()
    {
        //get authentication
        functionBar.setVisibility(View.GONE);

        //authentication screen
        SignAuthenticationCallback localSignCallback = new SignAuthenticationCallback()
        {
            final SignDataWidget signWidget = findViewById(R.id.sign_widget);

            @Override
            public void gotAuthorisation(boolean gotAuth)
            {
                actionCompleted = true;
                //display success and hand back to calling function
                if (gotAuth)
                {
                    confirmationWidget.startProgressCycle(1);
                    signCallback.gotAuthorisationForSigning(gotAuth, signWidget.getSignable());
                }
                else
                {
                    cancelAuthentication();
                }
            }

            @Override
            public void cancelAuthentication()
            {
                confirmationWidget.hide();
                signCallback.gotAuthorisationForSigning(false, signWidget.getSignable());
            }
        };

        actionSheetCallback.getAuthorisation(localSignCallback);
    }

    private void tryDismiss()
    {
        if (Utils.stillAvailable(activity) && isAttached && isShowing()) dismiss();
    }

    private void setupCancelListeners()
    {
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        setOnDismissListener(v -> {
            actionSheetCallback.dismissed(txHash, 0, actionCompleted);
        });
    }

    private void signTransaction()
    {
        functionBar.setVisibility(View.GONE);

        //get approval and push transaction
        //authentication screen
        signCallback = new SignAuthenticationCallback()
        {
            @Override
            public void gotAuthorisation(boolean gotAuth)
            {
                actionCompleted = true;
                confirmationWidget.startProgressCycle(4);
                //send the transaction
//                actionSheetCallback.signTransaction(formTransaction());
            }

            @Override
            public void cancelAuthentication()
            {
                confirmationWidget.hide();
                functionBar.setVisibility(View.VISIBLE);
            }
        };

        actionSheetCallback.getAuthorisation(signCallback);
    }

    public void completeSignRequest(boolean gotAuth)
    {
        if (signCallback != null)
        {
            actionCompleted = true;

            switch (mode)
            {
                case SEND_TRANSACTION_WC:
                case SEND_TRANSACTION:
                case SEND_TRANSACTION_DAPP:
                case SPEEDUP_TRANSACTION:
                case CANCEL_TRANSACTION:
                case SIGN_TRANSACTION:
                    signCallback.gotAuthorisation(gotAuth);
                    break;

                case SIGN_MESSAGE:
                    actionCompleted = true;
                    //display success and hand back to calling function
                    confirmationWidget.startProgressCycle(1);
                    signCallback.gotAuthorisation(gotAuth);
                    break;
            }
        }
    }

    private void sendTransaction()
    {
        functionBar.setVisibility(View.GONE);

    }

    @Override
    public void lockDragging(boolean lock)
    {
        getBehavior().setDraggable(!lock);

        //ensure view fully expanded when locking scroll. Otherwise we may not be able to see our expanded view
        if (lock)
        {
            FrameLayout bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).setState(STATE_EXPANDED);
        }
    }

    @Override
    public void fullExpand()
    {
        FrameLayout bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).setState(STATE_EXPANDED);
    }

    private void showAmount(final BigInteger amountVal)
    {
        amountDisplay.setAmountUsingToken(amountVal, fromToken, tokensService);

//        BigInteger networkFee = gasWidgetInterface.getGasPrice(candidateTransaction.gasPrice).multiply(gasWidgetInterface.getGasLimit());
        BigInteger balanceAfterTransaction = fromToken.balance.toBigInteger();//.subtract(gasWidgetInterface.getValue());
        balanceDisplay.setNewBalanceText(fromToken, getTransactionAmount(), BigInteger.ZERO, balanceAfterTransaction);

        toAmountDisplay.setAmountFromString(toToken.tokenInfo.symbol);
        //toAmountDisplay.setAmountUsingToken(BigInteger.valueOf(10), toToken, tokensService);
        keyService.getCredentials(wallet, activity, credentials -> {
            try {
                RemoteFunctionCall<List> call = swapService.getAmountsOut(amountVal, Arrays.asList(getAddress(fromToken), getAddress(toToken)), fromToken.getTokenInfo().chainId, credentials);
                List list = call.sendAsync().get();
                toAmountDisplay.setAmountUsingToken((BigInteger) list.get(1), toToken, tokensService);
            } catch (Exception ex) {
                Log.e("showAmount", "Error", ex);
            }
            return null;
        });


    }

    private String getAddress(Token token) {
        if("ETH".equalsIgnoreCase(token.tokenInfo.symbol)) {
            return WETH_ADDRESS;
        }
        return token.tokenInfo.address;
    }

    private boolean isAttached;


    public void success()
    {
        if (!activity.isFinishing() && !activity.isDestroyed() && isAttached)
        {
            confirmationWidget.completeProgressMessage(".", this::dismiss);
        }
    }
}
