package com.example.basemodlue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2019/7/11 08:34
 * 通用的dialog工具类
 */
public class DialogUtil {
    private static final String TAG = "DialogUtil";
    private static Dialog dialog;
    private static final int NORMAL_WIDTH = 300;//常用宽度，dp

    public static boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public static void dismiss() {
        if (isShowing()) {
            dialog.dismiss();
        }
    }

    public static void show() {
        if (dialog != null) {
            dismiss();
            dialog.show();
        } else {
            Log.e(TAG, "dialog is null!");
        }
    }

    /**
     * 加载中Dialog
     * 使用系统自带的ProgressDialog
     *
     * @param context 上下文
     * @param msg     内容
     */
    public static void loading(Context context, String msg) {
        dialog = new ProgressDialog(context);
        ((ProgressDialog) dialog).setMessage(msg);
        dialog.setCanceledOnTouchOutside(false);//点击外部不消失
        show();
    }

    /**
     * 常见的Dialog初始化
     *
     * @param context              上下文
     * @param contentView          要显示的View
     * @param canceledTouchOutside 是否允许点击外部消失
     */
    public static void initNormalDialog(Context context, View contentView, boolean canceledTouchOutside) {
        dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(canceledTouchOutside);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = UnitUtil.dip2px(context, NORMAL_WIDTH);
        window.setAttributes(layoutParams);
    }

    /**
     * 可自定义Dialog的Window属性
     *
     * @param context      上下文
     * @param contentView  要显示的View
     * @param layoutParams 属性参数
     */
    public static void initCustomDialog(Context context, View contentView, WindowManager.LayoutParams layoutParams) {
        dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.setContentView(contentView);
        Window window = dialog.getWindow();
        window.setAttributes(layoutParams);
    }

    /*---------------------监听器开始---------------------*/

    /**
     * 确认监听器
     */
    public interface onConfirmListener {
        void onConfirm();
    }
}
