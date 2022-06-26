package com.omegawallet.app.widget;


import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alphawallet.app.R;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.widget.DialogInfoItem;
import com.alphawallet.ethereum.EthereumNetworkBase;
import com.alphawallet.ethereum.NetworkInfo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class AWalletSwapResultDialog extends BottomSheetDialog {
    public static final int NONE = 0;
    public static final int SUCCESS = R.drawable.ic_redeemed;
    public static final int ERROR = R.drawable.ic_error;
    public static final int NO_SCREENSHOT = R.drawable.ic_no_screenshot;
    public static final int WARNING = R.drawable.ic_warning;

    public enum TEXT_STYLE
    {
        CENTERED,
        LEFT
    }

    private final ImageView icon;
    private final DialogInfoItem gasUsedText;
    private final DialogInfoItem fromText;
    private final DialogInfoItem toText;
    private final DialogInfoItem txHashText;
    private final MaterialButton button;

    private final TransactionReceipt transactionReceipt;
    private final Activity activity;
    private final Token fromToken;
    private final Token toToken;

    public AWalletSwapResultDialog(@NonNull Activity activity, TransactionReceipt transactionReceipt, Token fromToken, Token toToken) {
        super(activity);
        this.activity = activity;
        this.transactionReceipt = transactionReceipt;
        this.fromToken = fromToken;
        this.toToken = toToken;

        View view = View.inflate(getContext(), R.layout.dialog_swap_result, null);
        setContentView(view);

        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        icon = findViewById(R.id.dialog_icon);
        gasUsedText = findViewById(R.id.gas_used);

        fromText = findViewById(R.id.sent_from);
        toText = findViewById(R.id.sent_to);
        txHashText = findViewById(R.id.tx_hash);
        txHashText.setOnClickListener(v -> transactionHashClick());

        button = findViewById(R.id.button_ok);

        button.setOnClickListener(v -> dismiss());

        fillData(transactionReceipt);
    }

    private void fillData(TransactionReceipt transactionReceipt) {
        gasUsedText.setLabel("Gas Used");
        gasUsedText.setMessage(transactionReceipt.getGasUsed().toString());
        fromText.setLabel("Sent From");
        fromText.setMessage(transactionReceipt.getFrom());
        toText.setLabel("Sent To");
        toText.setMessage(transactionReceipt.getTo());
        txHashText.setLabel("Tx Hash");
        txHashText.setMessage(transactionReceipt.getTransactionHash());
    }

    private void transactionHashClick() {
        NetworkInfo networkInfo = EthereumNetworkBase.getNetworkByChain(fromToken.getTokenInfo().chainId);
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(networkInfo.etherscanUrl + "/" + transactionReceipt.getTransactionHash()));
            activity.startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getOwnerActivity(), "No application can handle this request."
                    + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
