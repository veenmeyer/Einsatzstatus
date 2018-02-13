package de.einsatzkomponente.einsatzstatus.network;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.einsatzkomponente.einsatzstatus.R;

/**
 * Created by Simon Wheeler on 06.01.2018.
 */

public class StatusSender extends AsyncTask<String, Void, String[]>
{
    private Activity activity;
    private TextView tv_status;
    private Button btn_feueralarm, btn_hilfeleistung, btn_uebung, btn_sonstiger, btn_einsatzbereit;
    private String textToAppend = " gemeldet";

    public StatusSender(Activity activity)
    {
        this.activity = activity;
    }

    //Wird aufgerufen, bevor die POST-Anfrage gesendet wird
    @Override
    protected  void onPreExecute()
    {
        //Hole alle relevanten Views, die benötigt werden
        tv_status = activity.findViewById(R.id.status);
        btn_feueralarm = activity.findViewById(R.id.feueralarm);
        btn_hilfeleistung = activity.findViewById(R.id.hilfeleistung);
        btn_uebung = activity.findViewById(R.id.uebung);
        btn_sonstiger = activity.findViewById(R.id.sonstiger);
        btn_einsatzbereit = activity.findViewById(R.id.einsatzbereit);

        resetButtonTexts();
        //Setze Status-Text
        tv_status.setText(activity.getString(R.string.tv_status_refresh));

    }

    /* POST-Anfrage an den Server
     * params[0]: URL
     * params[1]: status
     * params[2]: api
    */
    @Override
    protected String[] doInBackground(String... params)
    {
        URL url;
        String response[] = new String[2];
        String resp = "";

        //Diese zwei Zeilen dienen dazu, dass es egal ist, ob der Nutzer einen Slash am Ende derURL angibt oder nicht.
        if(!params[0].endsWith("/"))
            params[0]+="/";

        try {
            //Hier wird der Pfad zum Modul an die URL angehängt
            url = new URL(params[0]+"modules/mod_eiko_einsatzstatus/eiko_gateway.php");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            /* Setzte POST-Parameter
             * params[0]: URL
             * params[1]: status-Nummer
             * params[2]: API-Key
             */
            writer.write("ordner="+params[0]+"&einsatz="+params[1]+"&api="+params[2]+"&prefordner="+params[3]);

            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    resp+=line;
                }
            }
            else {
                resp="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response[0] = resp.trim();
        response[1] = params[1].trim();

        return response;
    }

    //Wenn der POST fertig ist, wird diese Methode ausgeführt
    @Override
    protected void onPostExecute(String[] response)
    {
        /* response hat folgende Werte:
         * [0]: Return-Wert des PHP-Skripts auf dem Webserver
         *      200 = OK
         *      401 = API-Key falsch
         *      404 = Status nicht gefunden
        */
        boolean ok = false;

        //Wechsle in den Skript-Responsecode
        switch(response[0])
        {
            case "200":
                ok = true;
                tv_status.setText(activity.getString(R.string.tv_status_200));
                break;
            case "401":
                tv_status.setText(activity.getString(R.string.tv_status_401));
                break;
            case "404":
                tv_status.setText(activity.getString(R.string.tv_status_404));
                break;
            default:
                tv_status.setText(activity.getString(R.string.tv_status_error)+response[0]);
                break;
        }
        //Wenn Skript Status 200 geliefert hat, wird die Variable "ok" auf "true" gesetzt und diese Abfrage läuft durch
        if(ok)
        {
            //Wechsle zum gesendeten Status, um den passenden Buttontext zu ändern
            switch (response[1])
            {
                case "300":
                    appendButtonText(btn_feueralarm, textToAppend);
                    break;
                case "400":
                    appendButtonText(btn_hilfeleistung, textToAppend);
                    break;
                case "500":
                    appendButtonText(btn_uebung, textToAppend);
                    break;
                case "600":
                    appendButtonText(btn_sonstiger, textToAppend);
                    break;
                case "999":
                    appendButtonText(btn_einsatzbereit, textToAppend);
                    break;
            }
        }
    }

    //Fügt einen Text an den Buttontext hinzu
    private void appendButtonText(Button btn, String textToAppend)
    {
        String tmp = btn.getText().toString();
        tmp+=textToAppend;
        btn.setText(tmp);
    }

    //Setzt den Buttontext auf ursprünglichen Wert
    private void resetButtonTexts()
    {
        btn_feueralarm.setText(activity.getString(R.string.btn_feueralarn));
        btn_hilfeleistung.setText(activity.getString(R.string.btn_hilfeleistung));
        btn_uebung.setText(activity.getString(R.string.btn_uebung));
        btn_sonstiger.setText(activity.getString(R.string.btn_sonstiger));
        btn_einsatzbereit.setText(activity.getString(R.string.btn_einsatzbereit));
    }
}
