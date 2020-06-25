package com.tobot.map.module.set.wifi;

import android.net.wifi.ScanResult;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseV4Dialog;
import com.tobot.map.module.common.PermissionHelper;
import com.tobot.map.util.ToastUtils;

/**
 * @author houdeming
 * @date 2018/9/24
 */
public class WifiConfigDialog extends BaseV4Dialog implements View.OnClickListener, BaseRecyclerAdapter.OnItemClickListener<ScanResult> {
    private Button btnWifi;
    private EditText etWifiPwd;
    private OnWifiListener mListener;
    private WifiListDialog mWifiListDialog;
    private String mWifiName;

    public static WifiConfigDialog newInstance() {
        return new WifiConfigDialog();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_wifi_config;
    }

    @Override
    protected void initView(View view) {
        btnWifi = view.findViewById(R.id.btn_select_wifi);
        etWifiPwd = view.findViewById(R.id.et_wifi_pwd);
        btnWifi.setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    @Override
    protected boolean isCanCancelByBack() {
        return true;
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    @Override
    public void onPause() {
        super.onPause();
        closeWifiListDialog();
    }

    @Override
    public void onItemClick(int position, ScanResult data) {
        if (data != null) {
            closeWifiListDialog();
            mWifiName = data.SSID;
            btnWifi.setText(mWifiName);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_wifi:
                // Android 6.0以上的手机获取WiFi列表需要权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PermissionHelper.isRequestPermission(getActivity())) {
                    ToastUtils.getInstance(getActivity()).show(getString(R.string.permission_location_open_tips));
                    return;
                }
                if (!isWifiListDialogShow()) {
                    mWifiListDialog = WifiListDialog.newInstance();
                    mWifiListDialog.setOnItemClickListener(this);
                    mWifiListDialog.show(getFragmentManager(), "WIFI_LIST_DIALOG");
                }
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_confirm:
                String wifiPwd = etWifiPwd.getText().toString().trim();
                // 如果只通过WIFI账号也可以连接到网络的话，则不需要输入密码
                if (TextUtils.isEmpty(mWifiName)) {
                    ToastUtils.getInstance(getActivity()).show(getString(R.string.tv_select_wifi));
                    return;
                }
                dismiss();
                if (mListener != null) {
                    mListener.onWifi(mWifiName, wifiPwd);
                }
                break;
            default:
                break;
        }
    }

    public void setOnWifiListener(OnWifiListener listener) {
        mListener = listener;
    }

    private void closeWifiListDialog() {
        if (isWifiListDialogShow()) {
            mWifiListDialog.getDialog().dismiss();
            mWifiListDialog = null;
        }
    }

    private boolean isWifiListDialogShow() {
        return mWifiListDialog != null && mWifiListDialog.getDialog() != null && mWifiListDialog.getDialog().isShowing();
    }

    public interface OnWifiListener {
        /**
         * WiFi的回调
         *
         * @param wifiName
         * @param wifiPwd
         */
        void onWifi(String wifiName, String wifiPwd);
    }
}
