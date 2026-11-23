package br.com.jsyoutube.app;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.*;
import android.widget.*;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import java.net.URLEncoder;
public class MainActivity extends Activity {
 WebView web; EditText search; FrameLayout root; View full; WebChromeClient.CustomViewCallback cb;
 void go(String url){web.loadUrl(url);}
 @Override public void onCreate(Bundle b){
 super.onCreate(b);
 root=new FrameLayout(this); setContentView(root);
 LinearLayout page=new LinearLayout(this);page.setOrientation(1);root.addView(page,new FrameLayout.LayoutParams(-1,-1));
 LinearLayout top=new LinearLayout(this);page.addView(top);
 search=new EditText(this);search.setSingleLine(true);search.setHint("Pesquisar no YouTube");search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);top.addView(search,new LinearLayout.LayoutParams(0,-2,1));
 Runnable find=()->{try{go("https://m.youtube.com/results?search_query="+URLEncoder.encode(search.getText().toString(),"UTF-8"));}catch(Exception ignored){}};
 search.setOnEditorActionListener((v,id,event)->{find.run();return true;});

 web=new WebView(this);page.addView(web,new LinearLayout.LayoutParams(-1,0,1));
 WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setMediaPlaybackRequiresUserGesture(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);web.setWebContentsDebuggingEnabled(false);
 web.setWebViewClient(new WebViewClient(){
  @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request){
   Uri uri=request.getUrl(); String host=uri.getHost();
   if(host!=null && (blockedHost(host,"doubleclick.net") || blockedHost(host,"googlesyndication.com") || blockedHost(host,"googleadservices.com") || blockedHost(host,"google-analytics.com") || blockedHost(host,"googletagmanager.com") || blockedHost(host,"adservice.google.com") || blockedHost(host,"ads.youtube.com") || blockedHost(host,"adnxs.com") || blockedHost(host,"taboola.com") || blockedHost(host,"outbrain.com"))){
    return new WebResourceResponse("text/plain","UTF-8",new java.io.ByteArrayInputStream(new byte[0]));
   }
   return super.shouldInterceptRequest(view,request);
  }
@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();String h=u.getHost();if(h!=null&&(h.equals("youtube.com")||h.endsWith(".youtube.com")||h.equals("youtu.be")||h.equals("accounts.google.com")))return false;try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception ignored){}return true;}});
 web.setWebChromeClient(new WebChromeClient(){@Override public void onShowCustomView(View v,CustomViewCallback callback){full=v;cb=callback;root.addView(v,new FrameLayout.LayoutParams(-1,-1));}@Override public void onHideCustomView(){closeFull();}});
 LinearLayout nav=new LinearLayout(this);page.addView(nav);
 Button home=new Button(this);home.setText("Início");nav.addView(home,new LinearLayout.LayoutParams(0,-2,1));home.setOnClickListener(v->go("https://m.youtube.com/"));
 Button back=new Button(this);back.setText("Voltar");nav.addView(back,new LinearLayout.LayoutParams(0,-2,1));back.setOnClickListener(v->{if(web.canGoBack())web.goBack();});
 Button fav=new Button(this);fav.setText("Favorito");nav.addView(fav,new LinearLayout.LayoutParams(0,-2,1));fav.setOnClickListener(v->{String url=web.getUrl();if(url!=null&&url.startsWith("https://m.youtube.com/")){getPreferences(0).edit().putString("favorite",url).apply();Toast.makeText(this,"Salvo",0).show();}});
 Button saved=new Button(this);saved.setText("Salvos");nav.addView(saved,new LinearLayout.LayoutParams(0,-2,1));saved.setOnClickListener(v->go(getPreferences(0).getString("favorite","https://m.youtube.com/")));
 web.requestFocus();
 go("https://m.youtube.com/");
 }
 static boolean blockedHost(String host,String domain){return host.equals(domain)||host.endsWith("."+domain);}
 void closeFull(){if(full!=null){root.removeView(full);full=null;if(cb!=null)cb.onCustomViewHidden();}}
 @Override public void onBackPressed(){if(full!=null)closeFull();else if(web.canGoBack())web.goBack();else super.onBackPressed();}
}