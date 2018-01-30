package de.einsatzkomponente.einsatzstatus.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

import de.einsatzkomponente.einsatzstatus.R;
import de.einsatzkomponente.einsatzstatus.network.InstallationCaller;
import de.einsatzkomponente.einsatzstatus.network.StatusSender;

public class MainActivity extends AppCompatActivity
{
    TextView tv_status;
    SharedPreferences sharedPrefs;
    String api_key;
    String server_url;
    Button btn_feueralarm, btn_hilfeleistung, btn_uebung, btn_sonstiger, btn_einsatzbereit, btn_paypal;
    InstallationCaller ic;
    final String registration_url = "https://einsatzkomponente.de/gateway/sts_validation.php?domain=";

    //Link zum Paypal (z. B. PayPal.me Link)
    final String paypal_link = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Lade die vom Nutzer eingegebenen Daten aus den Einstellungen
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        api_key = sharedPrefs.getString("pref_apikey", "12345");
        server_url = sharedPrefs.getString("pref_url", "");

        tv_status = findViewById(R.id.status);
        btn_feueralarm = findViewById(R.id.feueralarm);
        btn_hilfeleistung = findViewById(R.id.hilfeleistung);
        btn_uebung = findViewById(R.id.uebung);
        btn_sonstiger = findViewById(R.id.sonstiger);
        btn_einsatzbereit = findViewById(R.id.einsatzbereit);
        btn_paypal = findViewById(R.id.paypal);

        //Im OnClickListener wird anhand der geklickten View entschieden, welcher Status gesendet werden soll
        View.OnClickListener ocl = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(view == btn_feueralarm) {
                    new StatusSender(MainActivity.this).execute(server_url, "300", api_key);
                } else if (view == btn_hilfeleistung) {
                    new StatusSender(MainActivity.this).execute(server_url, "400", api_key);
                } else if (view == btn_uebung) {
                    new StatusSender(MainActivity.this).execute(server_url, "500", api_key);
                } else if (view == btn_sonstiger) {
                    new StatusSender(MainActivity.this).execute(server_url, "600", api_key);
                } else if (view == btn_einsatzbereit) {
                    new StatusSender(MainActivity.this).execute(server_url, "999", api_key);
                }
            }
        };

        //OnClickListener
        btn_feueralarm.setOnClickListener(ocl);
        btn_hilfeleistung.setOnClickListener(ocl);
        btn_uebung.setOnClickListener(ocl);
        btn_sonstiger.setOnClickListener(ocl);
        btn_einsatzbereit.setOnClickListener(ocl);
        btn_paypal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try
                {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paypal_link));
                    startActivity(browserIntent);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        //Registrierung, wenn Domain != null und bisher noch nicht registriert wurde
        String domain = getDomainName(server_url);
        if (domain != null && !sharedPrefs.getBoolean("registered", false))
        {
            ic = new InstallationCaller();
            ic.execute(registration_url + domain);
            sharedPrefs.edit().putBoolean("registered", true).apply();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        String old_server_url = server_url;

        //Hole neue Parameter aus den Einstellungen
        api_key = sharedPrefs.getString("pref_apikey", "12345");
        server_url = sharedPrefs.getString("pref_url", "");

        //Prüfe, ob sich die Domain geändert hat. Wenn ja, registriere erneut
        if(!getDomainName(old_server_url).equals(getDomainName(server_url)))
        {
            ic = new InstallationCaller();
            ic.execute(registration_url + getDomainName(server_url));
        }
    }

    //Erstelle Einstellungs-Menü
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //Wenn Eintrag im Menü ausgewählt wird, führe eine Aktion durch
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.preferences:
            {
                Intent intent = new Intent();
                intent.setClassName(this, "de.einsatzkomponente.einsatzstatus.ui.MyPreferenceActivity");
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getDomainName(String url)
    {
        URI uri = null;
        try
        {
            uri = new URI(url);
            return uri.getHost() != null ? uri.getHost() : "";
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
            return "";
        }
    }
}




