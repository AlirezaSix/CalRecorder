package com.callrecorder.payamgostar.models;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callrecorder.payamgostar.Constants;
import com.callrecorder.payamgostar.DateHelper;

import de.codecrafters.tableview.TableDataAdapter;

/**
 * Created by j.amini on 12/26/2017.
 */

public class CallDataAdapter extends TableDataAdapter<CallEntity> {

    public CallDataAdapter(Context context, CallEntity[] data) {
        super(context, data);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        View renderedView = null;
        CallEntity call = getRowData(rowIndex);

        switch (columnIndex) {
            case 0:
                renderedView = getPhoneNumberView(call);
                break;

            case 1:
                renderedView = getCallTypeView(call);
                break;

            case 2:
                renderedView = getCallDateView(call);
                break;

            case 3:
                renderedView = getCallStatusView(call);
                break;
        }

        return renderedView;
    }

    private TextView getDefaultRowTextView(boolean ltr) {
        TextView view = new TextView(getContext());
        view.setPadding(20, 10, 20, 10);
        view.setTextSize(16);

        if(ltr)
            view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        else
            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int color = getResources().getColor(de.codecrafters.tableview.R.color.abc_primary_text_material_light);
        view.setTextColor(color);
        return view;
    }

    private View getPhoneNumberView(CallEntity call) {
        TextView view = getDefaultRowTextView(true);
        view.setText(call.phoneNumber);
        return view;
    }

    private View getCallTypeView(CallEntity call) {
        TextView view = getDefaultRowTextView(false);

        String callType;
        switch (call.callType) {
            case Constants.MISS_CALL:
                callType = "از دست رفته";
                break;

            case Constants.INCOMING_CALL:
                callType = "ورودی";
                break;

            case Constants.OUTGOING_CALL:
            default:
                callType = "خروجی";
                break;
        }

        view.setText(callType);
        return view;
    }

    private View getCallDateView(CallEntity call) {
        TextView view = getDefaultRowTextView(false);
        String persianDate = DateHelper.getShamsiDate(call.startDate);
        String persianTime = DateFormat.format("HH:mm:ss", call.startDate).toString();

        view.setText(persianDate + " " + persianTime);
        return view;
    }

    private View getCallStatusView(CallEntity call) {
        TextView view = getDefaultRowTextView(false);
        view.setText(call.isSynced ? "ارسال شده" : "در انتظار");
        return view;
    }
}
