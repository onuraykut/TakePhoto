package com.onur.kryptow.takephoto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.jcraft.jsch.*;
import com.squareup.picasso.Picasso;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static Button buton;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static List<String> gphoto = new ArrayList<>();
    public static String ip_adress,username,password,image_name,server_path,device_path;
    public static Integer port;
    //PopupWindow popUp;
    LinearLayout layout;
    TextView tv;
    protected Spinner spinner_islem;
    protected ArrayAdapter<String> dataAdapterForIslem;
    //private Context mContext;

    private String[] islem_listesi={"Capture Image and Save in Camera","Capture Image and Download Phone"};
    private RelativeLayout mRelativeLayout;
    private PopupWindow mPopupWindow;
    private static int islem_secim_index;
    public static ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final SharedPreferences.Editor editor=prefs.edit();

        photo= findViewById(R.id.imageView2);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "onuraykut@outlook.com", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ip_adress=prefs.getString("ip_adress_key","0.0.0.0");
        username=prefs.getString("username_key","username");
        password=prefs.getString("password_key","password");
        port=Integer.parseInt(prefs.getString("port_key","22"));
        server_path=prefs.getString("server_path_key","/home");
        device_path=prefs.getString("device_path_key","/home");
        image_name=prefs.getString("image_name_key","captured_image.jpeg");
        gphoto.add(0,"gphoto2 --capture-image");
        spinner_islem = findViewById(R.id.spinner);
        dataAdapterForIslem = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, islem_listesi);
        dataAdapterForIslem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_islem.setAdapter(dataAdapterForIslem);
        islem_secim_index=prefs.getInt("islem_position",0);
        spinner_islem.setSelection(islem_secim_index);

        spinner_islem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                islem_secim_index=position;
                editor.putInt("islem_position",position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        buton = findViewById(R.id.button);
        buton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {

                new AsyncTask<Integer, Void, Void>(){
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        gphoto.add(1, "gphoto2 --capture-image-and-download --filename "+server_path+image_name+" --force-overwrite");

                    }
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {
                            executeRemoteCommand(username, password,ip_adress, port);
                            //verifyStoragePermissions(this);
                            Downloader(username,ip_adress,password,image_name);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void result) {
                        //uzun islem bitince yapilacaklar
                        super.onPostExecute(result);
                        Uri uri = Uri.fromFile(new File((device_path+"/"+image_name)));
                        Picasso.get().load(uri)
                                .resize(330, 250).centerCrop().into(photo);
                        //show_popup();
                    }
                }.execute(1);
            }
        });
    }
    /*
    public void show_popup(){
        mContext = getApplicationContext();
        Activity mActivity;
        // Get the activity
        mActivity = MainActivity.this;
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.custom_layout,null);
        mPopupWindow = new PopupWindow(
                customView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        TextView outpout =(TextView) customView.findViewById(R.id.tv);
        outpout.setText(deneme);
        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            mPopupWindow.setElevation(25.0f);
        }

        // Get a reference for the custom view close button
        ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER,0,0);
    } */


    public static String executeRemoteCommand(String username,String password,String hostname,int port)
            throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        channelssh.setCommand(gphoto.get(islem_secim_index));
        InputStream in=channelssh.getInputStream();
        channelssh.connect();

        byte[] tmp=new byte[1024];
        while(true){
            while(in.available()>0){
                int i=in.read(tmp, 0, 1024);

                if(i<0)break;
                System.out.print(new String(tmp, 0, i));
               // deneme=deneme+new String(tmp, 0, i);  //konsol ciktisi


            }
            if(channelssh.isClosed()){
                if(in.available()>0) continue;
                System.out.println("exit-status: "+channelssh.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }
        channelssh.disconnect();

        return baos.toString();
    }
    public void Downloader(String user,String host,String pass,String fileName) {

        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;

        try {

            session = jsch.getSession(user, host, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(pass);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
            sftpChannel.cd(server_path); //cd to dir that contains file

            try {
                byte[] buffer = new byte[1024];
                BufferedInputStream bis = new BufferedInputStream(sftpChannel.get(fileName));
                File newFile = new File(device_path, "/" + fileName);
                OutputStream os = new FileOutputStream(newFile); //CRASHES HERE
                BufferedOutputStream bos = new BufferedOutputStream(os);
                int readCount;
                while( (readCount = bis.read(buffer)) > 0) {
                    Log.d("Downloading", " " + fileName );
                    bos.write(buffer, 0, readCount);
                }

                bis.close();
                bos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d( " ", fileName + " has been downloaded. MAYBE");

            sftpChannel.exit();
            session.disconnect();

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
              }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home_page) {
            startActivity(new Intent(this, MainActivity.class));
            // Handle the camera action


        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(this, SettingsActivity.class));

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
