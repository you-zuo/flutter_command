package com.yz.cmd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

import static androidx.core.content.ContextCompat.startActivity;

public class CommandUtils {
    private static final String TAG = "CommandUtils";

    //申请root
    public static Observable<Boolean> init(final String packageCodePath) {


        final Observable<Boolean> observable;

        observable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {

                Process process = null;
                DataOutputStream os = null;
                String command = "chmod 777 " + packageCodePath;
                try {
                    process = Runtime.getRuntime().exec("su");

                    os = new DataOutputStream(process.getOutputStream());

                    os.writeBytes(command + "\n");

                    os.writeBytes("exit\n");

                    os.flush();

                    process.waitFor();

                } catch (Exception e) {
                    Log.d(TAG, "ROOT 错误" + e.getMessage());
                    emitter.onError(e);
                    emitter.onComplete();

                } finally {

                    try {

                        if (os != null) {

                            os.close();

                        }

                        process.destroy();

                    } catch (Exception e) {

                    }
                }

                Log.d(TAG, "Root 申请成功");
                emitter.onNext(true);
                emitter.onComplete();
            }
        });

        return observable;
    }

    public static Observable<Boolean> runRootCmd(String command) {
        return runRootCmd(command, ";");
    }

    public static Observable<Boolean> runRootCmd(final String command, final String split) {
        final Observable<Boolean> observable;
        observable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {
                Process process = null;
                DataOutputStream os = null;
                //判断是否有root权限
                if (hasRootPerssion()) {
                    try {
                        process = Runtime.getRuntime().exec("su");
                        OutputStream outstream = process.getOutputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(outstream);
                        String temp = "";
                        String[] cmds = command.split(split);
                        for (int i = 0; i < cmds.length; i++)
                            temp += cmds[i] + "\n";
                        dataOutputStream.writeBytes(temp);
                        dataOutputStream.flush();
                        dataOutputStream.writeBytes("exit\n");
                        dataOutputStream.flush();
                        process.waitFor();

                        Log.d(TAG, "runRootCmd temp  " + temp);

                        emitter.onNext(true);
                    } catch (Exception e) {
                        emitter.onError(e);
                        emitter.onNext(false);
                    } finally {
                        try {
                            if (os != null) {
                                os.close();
                            }
                            process.destroy();
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                        emitter.onComplete();
                    }
                } else {
                    emitter.onNext(false);
                    Log.d(TAG, "没有root权限");
                    emitter.onComplete();
                }

            }
        });
        return observable;
    }

    //root权限检测
    private static boolean hasRootPerssion() {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    private static boolean returnResult(int value) {
        // 代表成功
        if (value == 0) {

            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }

    //安装应用
    public static Observable<Boolean> installApk(final String apkPath, final Context context) {
        Observable<Boolean> observable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {
                // 先判断手机是否有root权限
                if (hasRootPerssion()) {
                    // 有root权限，利用静默安装实现
                    PrintWriter PrintWriter = null;
                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec("su");
                        PrintWriter = new PrintWriter(process.getOutputStream());
                        PrintWriter.println("chmod 777 " + apkPath);
                        PrintWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
                        PrintWriter.println("pm install -r " + apkPath);
                        PrintWriter.flush();
                        PrintWriter.close();
                        int value = process.waitFor();
                        emitter.onNext(returnResult(value));
                    } catch (Exception e) {
                        emitter.onError(e);
                        emitter.onNext(false);
                        emitter.onComplete();
                    } finally {
                        if (process != null) {
                            process.destroy();
                        }
                        emitter.onComplete();
                    }

                } else {
                    // 没有root权限，利用意图进行安装
                    File file = new File(apkPath);
                    if (!file.exists()) {
                        emitter.onNext(false);
                        emitter.onComplete();
                    }
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    context.startActivity(intent);
                    emitter.onNext(true);
                    emitter.onComplete();
                }
            }
        });

        return observable;
    }

    //卸载应用
    public static Observable<Boolean> uninstallApk(final String packageName, final Context context) {
        Observable<Boolean> observable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {
                if (hasRootPerssion()) {
                    // 有root权限，利用静默卸载实现
                    PrintWriter PrintWriter = null;
                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec("su");
                        PrintWriter = new PrintWriter(process.getOutputStream());
                        PrintWriter.println("LD_LIBRARY_PATH=/vendor/lib:/system/lib ");
                        PrintWriter.println("pm uninstall " + packageName);
                        PrintWriter.flush();
                        PrintWriter.close();
                        int value = process.waitFor();
                        emitter.onNext(returnResult(value));
                    } catch (Exception e) {
                        emitter.onNext(false);
                        emitter.onError(e);

                    } finally {
                        if (process != null) {
                            process.destroy();
                        }
                        emitter.onComplete();
                    }
                } else {
                    Uri packageURI = Uri.parse("package:" + packageName);
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                    uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(uninstallIntent);
                    emitter.onNext(true);
                    emitter.onComplete();
                }
            }
        });
        return observable;
    }


    public static void restartApk(Context context) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(context, intent, null);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
