package com.rubysinn.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "rubysinn_settings";
    private static final String KEY_URL = "server_url";
    private WebView webView;
    private ProgressBar progress;
    private TextView status;
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        buildShell();
        String saved = prefs.getString(KEY_URL, "");
        if (saved.isEmpty()) showServerDialog(true); else loadServer(saved);
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(14), dp(8), dp(10), dp(8));
        bar.setBackgroundColor(Color.rgb(23,23,23));

        TextView brand = new TextView(this);
        brand.setText("RUBY'S INN");
        brand.setTextColor(Color.rgb(214,180,106));
        brand.setTextSize(17);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bar.addView(brand, new LinearLayout.LayoutParams(0, dp(44), 1));

        Button refresh = barButton("↻");
        refresh.setOnClickListener(v -> { if (webView != null) webView.reload(); });
        bar.addView(refresh);
        Button settings = barButton("⚙");
        settings.setOnClickListener(v -> showServerDialog(false));
        bar.addView(settings);
        root.addView(bar);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(3)));

        status = new TextView(this);
        status.setText("Connect to your reception server");
        status.setTextColor(Color.DKGRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(12), dp(8), dp(12), dp(8));
        status.setVisibility(View.GONE);
        root.addView(status);

        webView = new WebView(this);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value >= 100 ? View.GONE : View.VISIBLE);
            }
            @Override public void onReceivedTitle(WebView view, String title) {
                if (title != null && !title.trim().isEmpty()) status.setText(title);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                if (scheme.equals("http") || scheme.equals("https")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "No app available for this link", Toast.LENGTH_SHORT).show(); }
                return true;
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showConnectionError();
            }
        });
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private Button barButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(20);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setMinWidth(dp(44));
        b.setMinimumHeight(dp(44));
        return b;
    }

    private void showServerDialog(boolean firstRun) {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(prefs.getString(KEY_URL, "http://192.168.1.100:3000"));
        input.setHint("http://192.168.1.x:3000");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        int pad = dp(20);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(pad, dp(8), pad, 0);
        wrap.addView(input, new LinearLayout.LayoutParams(-1, dp(52)));
        new AlertDialog.Builder(this)
            .setTitle(firstRun ? "Connect Ruby's Inn" : "Reception server address")
            .setMessage("Enter the full IP address of your Windows reception computer, including :3000. Example: http://192.168.1.25:3000")
            .setView(wrap)
            .setCancelable(!firstRun)
            .setPositiveButton("Connect", (d, which) -> {
                String url = normalize(input.getText().toString());
                if (url == null) {
                    Toast.makeText(this, "Enter a valid server address", Toast.LENGTH_LONG).show();
                    showServerDialog(firstRun);
                } else {
                    prefs.edit().putString(KEY_URL, url).apply();
                    loadServer(url);
                }
            })
            .setNegativeButton(firstRun ? "Exit" : "Cancel", (d, w) -> { if (firstRun) finish(); })
            .show();
    }

    private String normalize(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        if (value.isEmpty()) return null;
        if (!value.startsWith("http://") && !value.startsWith("https://")) value = "http://" + value;
        Uri uri = Uri.parse(value);
        if (uri.getHost() == null || !(uri.getScheme().equals("http") || uri.getScheme().equals("https"))) return null;
        if (uri.getPort() == -1 && uri.getScheme().equals("http")) value = value.replaceFirst("/$", "") + ":3000";
        return value.replaceFirst("/$", "");
    }

    private void loadServer(String url) {
        status.setVisibility(View.VISIBLE);
        status.setText("Connecting to " + url);
        webView.loadUrl(url);
    }

    private void showConnectionError() {
        status.setVisibility(View.VISIBLE);
        status.setText("Cannot reach reception server. Check Wi-Fi, server power and address. Tap ⚙ to change the address.");
    }

    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
