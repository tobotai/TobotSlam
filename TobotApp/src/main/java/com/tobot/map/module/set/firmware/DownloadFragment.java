package com.tobot.map.module.set.firmware;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.upgrade.FileDownload;
import com.tobot.map.module.upgrade.OnFileDownloadListener;
import com.tobot.map.util.MediaScanner;
import com.tobot.map.util.SystemUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2022/12/02
 */
public class DownloadFragment extends BaseFragment implements OnFileDownloadListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_url)
    EditText etUrl;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final int MSG_DOWNLOAD_FAIL = 2;
    private static final int MSG_DOWNLOAD_COMPLETE = 3;
    private OnDataUpdateListener mOnDataUpdateListener;
    private MainHandler mMainHandler;
    private ProgressDialog mProgressDialog;
    private String mFilePath;

    public static DownloadFragment newInstance(OnDataUpdateListener listener) {
        DownloadFragment fragment = new DownloadFragment();
        fragment.setOnDataUpdateListener(listener);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_download;
    }

    @Override
    protected void init() {
    }

    @Override
    public void onPause() {
        super.onPause();
        closeProgressDialog();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onFileDownloadProgress(int progress) {
        if (getActivity() != null && mMainHandler != null) {
            mMainHandler.obtainMessage(MSG_UPDATE_PROGRESS, progress, 0).sendToTarget();
        }
    }

    @Override
    public void onFileDownloadFail(String msg) {
        if (getActivity() != null && mMainHandler != null) {
            mMainHandler.obtainMessage(MSG_DOWNLOAD_FAIL, msg).sendToTarget();
        }
    }

    @Override
    public void onFileDownloadFinish() {
        if (getActivity() != null && mMainHandler != null) {
            mMainHandler.sendEmptyMessage(MSG_DOWNLOAD_COMPLETE);
        }
    }

    @OnClick({R.id.btn_download})
    public void onClickView(View view) {
        if (view.getId() == R.id.btn_download) {
            download();
        }
    }

    private void setOnDataUpdateListener(OnDataUpdateListener listener) {
        mOnDataUpdateListener = listener;
    }

    private void download() {
        SystemUtils.hideKeyboard(getActivity());
        String url = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            showToastTips(getString(R.string.download_url_empty_tips));
            return;
        }

        try {
            int index = url.lastIndexOf("/");
            String fileName = url.substring(index + 1);
            Logger.i(BaseConstant.TAG, "fileName=" + fileName);
            if (TextUtils.isEmpty(fileName)) {
                showToastTips(getString(R.string.url_invalid));
                return;
            }

            String filePath = BaseConstant.getFirmwareDirectory(getActivity()) + File.separator + fileName;
            Logger.i(BaseConstant.TAG, "filePath=" + filePath);
            File file = new File(filePath);
            // 若存在则不再下载
            if (file.exists()) {
                showToastTips(getString(R.string.file_exist_tips));
                return;
            }

            mFilePath = filePath;
            if (mMainHandler == null) {
                mMainHandler = new MainHandler(new WeakReference<>(this));
            }
            showProgressDialog(0);
            FileDownload.getInstance().downLoad(getActivity(), url, file, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog(int progress) {
        if (isProgressDialogShow()) {
            mProgressDialog.updateTips(progress);
            return;
        }

        mProgressDialog = ProgressDialog.newInstance(getString(R.string.downloading));
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            mProgressDialog.show(fragmentManager, "PROGRESS_DIALOG");
        }
    }

    private void downloadResult(boolean isSuccess, String msg) {
        // 通知页面刷新
        new MediaScanner().scanFile(getActivity(), mFilePath);
        if (isSuccess && mOnDataUpdateListener != null) {
            mOnDataUpdateListener.onDataUpdate();
        }
        closeProgressDialog();
        showToastTips(msg);
    }

    private void closeProgressDialog() {
        if (isProgressDialogShow()) {
            mProgressDialog.getDialog().dismiss();
            mProgressDialog = null;
        }
    }

    private boolean isProgressDialogShow() {
        return mProgressDialog != null && mProgressDialog.getDialog() != null && mProgressDialog.getDialog().isShowing();
    }

    private static class MainHandler extends Handler {
        private final DownloadFragment mFragment;

        private MainHandler(WeakReference<DownloadFragment> reference) {
            super(Looper.getMainLooper());
            mFragment = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mFragment != null) {
                switch (msg.what) {
                    case MSG_UPDATE_PROGRESS:
                        mFragment.showProgressDialog(msg.arg1);
                        break;
                    case MSG_DOWNLOAD_FAIL:
                        mFragment.downloadResult(false, (String) msg.obj);
                        break;
                    case MSG_DOWNLOAD_COMPLETE:
                        mFragment.downloadResult(true, mFragment.getString(R.string.download_finish));
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
