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
 WebView web;
 EditText search;
 FrameLayout root;
 View full;
 WebChromeClient.CustomViewCallback fullCallback;
 boolean lite=false;
 boolean desktop=true;
 String mobileAgent;
 void applyLayout(){
  WebSettings s=web.getSettings();
  if(desktop){
   s.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
   s.setUseWideViewPort(true);s.setLoadWithOverviewMode(true);
  } else {
   s.setUserAgentString(mobileAgent);s.setUseWideViewPort(true);s.setLoadWithOverviewMode(true);
  }
  s.setLoadsImagesAutomatically(!lite);
 }
 String homeUrl(){return desktop?"https://www.youtube.com/":"https://m.youtube.com/";}

 void go(String url){ web.loadUrl(url); }
 void openOfficial(){
   Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/"));
   try { startActivity(intent); } catch(Exception e){ Toast.makeText(this,"Nao foi possivel abrir o YouTube",Toast.LENGTH_SHORT).show(); }
 }
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  lite=getPreferences(0).getBoolean("lite",false);
  desktop=getPreferences(0).getBoolean("desktop",true);
  root=new FrameLayout(this);
  setContentView(root);
  LinearLayout page=new LinearLayout(this);
  page.setOrientation(LinearLayout.VERTICAL);
  root.addView(page,new FrameLayout.LayoutParams(-1,-1));

  LinearLayout top=new LinearLayout(this);
  page.addView(top);
  search=new EditText(this);
  search.setSingleLine(true);
  search.setHint("Pesquisar videos e musicas");
  search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
  top.addView(search,new LinearLayout.LayoutParams(0,-2,1));
  Runnable find=()->{
   try { go((desktop?"https://www.youtube.com/results?search_query=":"https://m.youtube.com/results?search_query=")+URLEncoder.encode(search.getText().toString(),"UTF-8")); }
   catch(Exception ignored){}
  };
  search.setOnEditorActionListener((v,id,event)->{find.run();return true;});
  Button findButton=new Button(this);
  findButton.setText("Buscar");
  top.addView(findButton);
  findButton.setOnClickListener(v->find.run());

  web=new WebView(this);
  page.addView(web,new LinearLayout.LayoutParams(-1,0,1));
  WebSettings settings=web.getSettings();
  settings.setJavaScriptEnabled(true);
  settings.setDomStorageEnabled(true);
  settings.setMediaPlaybackRequiresUserGesture(true);
  settings.setAllowFileAccess(false);
  settings.setAllowContentAccess(false);
  settings.setJavaScriptCanOpenWindowsAutomatically(false);
  settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  settings.setCacheMode(WebSettings.LOAD_DEFAULT);
  mobileAgent=settings.getUserAgentString();
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
    Uri u=r.getUrl();
    String h=u.getHost();
    if(h!=null && (h.equals("youtube.com")||h.endsWith(".youtube.com")||h.equals("youtu.be")||h.equals("accounts.google.com"))) return false;
    try { startActivity(new Intent(Intent.ACTION_VIEW,u)); } catch(Exception ignored){}
    return true;
   }
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public void onShowCustomView(View v,CustomViewCallback callback){
    full=v;fullCallback=callback;root.addView(v,new FrameLayout.LayoutParams(-1,-1));
   }
   @Override public void onHideCustomView(){closeFull();}
  });
  LinearLayout controls=new LinearLayout(this);
  page.addView(controls);
  Button home=new Button(this);home.setText("Inicio");
  controls.addView(home,new LinearLayout.LayoutParams(0,-2,1));
  home.setOnClickListener(v->go(homeUrl()));
  Button back=new Button(this);back.setText("Voltar");
  controls.addView(back,new LinearLayout.LayoutParams(0,-2,1));
  back.setOnClickListener(v->{if(web.canGoBack())web.goBack();});
  Button official=new Button(this);official.setText("Original");
  controls.addView(official,new LinearLayout.LayoutParams(0,-2,1));
  official.setOnClickListener(v->openOfficial());

  LinearLayout bottom=new LinearLayout(this);page.addView(bottom);
  CheckBox computer=new CheckBox(this);
  computer.setText("PC");computer.setChecked(desktop);
  bottom.addView(computer,new LinearLayout.LayoutParams(0,-2,1));
  computer.setOnCheckedChangeListener((b,checked)->{
   desktop=checked;getPreferences(0).edit().putBoolean("desktop",checked).apply();
   applyLayout();go(homeUrl());
  });
  CheckBox economy=new CheckBox(this);
  economy.setText("Economia de dados");
  economy.setChecked(lite);
  bottom.addView(economy,new LinearLayout.LayoutParams(0,-2,1));
  economy.setOnCheckedChangeListener((b,checked)->{
   lite=checked;
   getPreferences(0).edit().putBoolean("lite",checked).apply();
   web.getSettings().setLoadsImagesAutomatically(!checked);
   Toast.makeText(this,checked?"Imagens extras desativadas; escolha 144p no video":"Imagens ativadas",Toast.LENGTH_LONG).show();
   web.reload();
  });
  Button favorite=new Button(this);favorite.setText("Salvar");
  bottom.addView(favorite,new LinearLayout.LayoutParams(0,-2,1));
  favorite.setOnClickListener(v->{
   String u=web.getUrl();
   if(u!=null && (u.startsWith("https://m.youtube.com/")||u.startsWith("https://www.youtube.com/"))){
    getPreferences(0).edit().putString("favorite",u).apply();
    Toast.makeText(this,"Favorito salvo",Toast.LENGTH_SHORT).show();
   }
  });
  Button saved=new Button(this);saved.setText("Favorito");
  bottom.addView(saved,new LinearLayout.LayoutParams(0,-2,1));
  saved.setOnClickListener(v->go(getPreferences(0).getString("favorite",homeUrl())));
  applyLayout();
  go(homeUrl());
 }
 void closeFull(){
  if(full!=null){
   root.removeView(full);
   full=null;
   if(fullCallback!=null){fullCallback.onCustomViewHidden();fullCallback=null;}
  }
 }
 @Override public void onBackPressed(){
  if(full!=null)closeFull();
  else if(web.canGoBack())web.goBack();
  else super.onBackPressed();
 }
}
