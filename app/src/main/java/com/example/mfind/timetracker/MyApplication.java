package com.example.mfind.timetracker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.data.StringFormat;

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "worktime-app-development@googlegroups.com")
@AcraDialog(resText = R.string.dialog_text,
        resCommentPrompt = R.string.dialog_comment)
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.i(TAG, "### ### ### attachBaseContext: ACRA INIT");

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
        builder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.JSON);

        //builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class).setEnabled(boolean enabled);
        //builder.getPluginConfigurationBuilder(DialogConfigurationBuilder.class).setResText(R.string.acra_toast_text);


        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}

