package com.tobot.map.module.set;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.common.PermissionHelper;
import com.tobot.map.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2021/12/29
 */
public class FileDebugActivity extends BaseBackActivity {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_content)
    TextView tvContent;
    private static final String FILE_DIR = BaseConstant.DIRECTORY + "/" + BaseConstant.DIRECTORY_TEST_SECOND;
    private static final String FILE_NAME = "test.txt";

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_file_debug;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.file_read_write);
    }

    @OnClick({R.id.btn_debug})
    public void onClickView(View v) {
        if (v.getId() == R.id.btn_debug) {
            write();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void write() {
        StringBuilder builder = new StringBuilder();
        // 输出设备型号
        setContent(builder, "MANUFACTURER=" + android.os.Build.MANUFACTURER);
        setContent(builder, "BRAND=" + android.os.Build.BRAND);
        setContent(builder, "MODEL=" + android.os.Build.MODEL);
        setContent(builder, "ANDROID VERSION=" + android.os.Build.VERSION.RELEASE);
        setContent(builder, "SDK VERSION=" + android.os.Build.VERSION.SDK_INT);
        // 开始写内容
        setContent(builder, "start write");
        // Android11以上版本请求读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            boolean isExternalStorageManager = Environment.isExternalStorageManager();
            setContent(builder, "isExternalStorageManager=" + isExternalStorageManager);
            if (!isExternalStorageManager) {
                setContent(builder, "request externalStorageManager");
                setContent(builder, "write fail");
                return;
            }
        }

        boolean isRequestPermission = PermissionHelper.isRequestPermission(this);
        setContent(builder, "isRequestPermission=" + isRequestPermission);
        if (isRequestPermission) {
            setContent(builder, "request permission");
            setContent(builder, "write fail");
            return;
        }

        String dir = FileUtils.getFolder(this, FILE_DIR);
        setContent(builder, "dir=" + dir);

        File file = new File(dir);
        boolean isDirExists = file.exists();
        setContent(builder, "isDirExists=" + isDirExists);
        if (!isDirExists) {
            boolean isSuccess = file.mkdirs();
            setContent(builder, "isDirCreateSuccess=" + isSuccess);
            if (!isSuccess) {
                setContent(builder, "write fail");
                return;
            }
        }

        file = new File(dir, FILE_NAME);
        boolean isFileExists = file.exists();
        setContent(builder, "isFileExists=" + isFileExists);
        if (!isFileExists) {
            try {
                boolean isSuccess = file.createNewFile();
                setContent(builder, "isFileCreateSuccess=" + isSuccess);
                if (!isSuccess) {
                    setContent(builder, "write fail");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                setContent(builder, "fileCreate error=" + e.getMessage());
                setContent(builder, "write fail");
                return;
            }
        }

        setContent(builder, "write content begin");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            PrintWriter pw = new PrintWriter(writer);
            pw.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            pw.println("this is test file");
            writer.close();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
            setContent(builder, "write content error=" + e.getMessage());
            setContent(builder, "write fail");
            return;
        }

        setContent(builder, "write content end");
        setContent(builder, "write success");
    }

    private void setContent(StringBuilder builder, String content) {
        builder.append(content);
        builder.append("\n");
        tvContent.setText(builder.toString());
    }
}
