package com.yz.cmd;

import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * CmdPlugin
 */
public class CmdPlugin implements FlutterPlugin, MethodCallHandler {
    private Boolean rootCmd = false;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "cmd");
        channel.setMethodCallHandler(new CmdPlugin());
    }

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "cmd");
        channel.setMethodCallHandler(new CmdPlugin());
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        /*if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else {
            result.notImplemented();
        }*/
        switch (call.method) {
            case "init":
                cmdInit(result, call.argument("packageCodePath") + "");
                return;
            case "runRootCmd":
                runRootCmd(result, call.argument("command") + "");
                return;
            default:
                break;
        }
    }

    private void runRootCmd(final Result result, String command) {
        if (rootCmd) {

            RxJavaUtil.toSubscribe(CommandUtils.runRootCmd(command)).subscribe(new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                }

                @Override
                public void onNext(Boolean aBoolean) {
                    result.success(aBoolean);
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });

        } else {
            result.success(false);
        }

    }

    private void cmdInit(final Result result, String packageCodePath) {

        RxJavaUtil.toMainThread(CommandUtils.init(packageCodePath)).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                rootCmd = aBoolean;
                result.success(aBoolean);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    }
}
