package com.skole.s304114mappe3.Dialog;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DialogFragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skole.s304114mappe3.ListView.SeAlleReservasjoner;
import com.skole.s304114mappe3.MainActivityNy;
import com.skole.s304114mappe3.R;
import com.skole.s304114mappe3.RegistrerRom;
import com.skole.s304114mappe3.klasser.Reservasjon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SeReservasjonsInfoFragment extends AppCompatActivity implements SlettReservasjonDialog.DialogClickListener{


    //--------DIALOG KNAPPER TIL AVBESTILLDIALOG--------
    @Override
    public void jaClick() {
        fullforAvbestilling();
    }

    @Override
    public void neiClick() {
        return;
    }

    //--------VERDIER--------
    Integer ID;
    //--------OBJEKT--------
    Reservasjon reservasjon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //--------DB HANDLER--------

        ////--------HENTER ID TIL BESTILLINGEN SOM SKAL VISES FRA MINNE - DEFINERT I SEBESTILLINGER OG I NOTIFIKASJON/SERVICE--------
        ID = getSharedPreferences("APP_INFO",MODE_PRIVATE).getInt("VISNINGSID", 0);


        //--------KJØRER SEBESTILLINGSINFO FRAGMENTET--------
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SeBestillingsInfo()).commit();

    }
    //-------CREATE SLUTTER---------


    //-----------------------METODER TIL HOVEDKLASSE-----------------------------


    //-------FULLFØRER AVBESTILLING - BENYTTES INNAD I jaClick() TIL visAvbestillDialog()---------
    public void fullforAvbestilling() {
        //legger til rom gjennom url
        readWebpage();

        //INFOMELDING UT
        //MELDING TIL LOGG
        Log.d("Legg inn: ", "Rom lagt til");

        Intent intent_tilbake = new Intent (SeReservasjonsInfoFragment.this, SeAlleReservasjoner.class);
        startActivity(intent_tilbake);
        finish();
    }
    //-----------------------METODER TIL HOVEDKLASSE-----------------------------



    //forsøk på å kjøre websiden
    private class LastSide extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String s = "";
            String hele = "";
            for (String url : urls) {
                try{
                    URL minurl= new URL(urls[0]);
                    HttpURLConnection con = (HttpURLConnection) minurl.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while((s = in.readLine()) != null) {
                        hele = hele + s;
                    }
                    in.close();
                    con.disconnect();
                    return hele;
                }
                catch(Exception e) {
                    return"Noe gikk feil";
                }
            }
            return hele;
        }

        @Override
        protected void onPostExecute(String ss) {
            //textView.setText(ss);
        }
    }

    public void readWebpage() {

        LastSide task = new LastSide();

        //String sID = ID+"";
        String sID = 1+"";


        //må fikse  denne strengen så den er uten mellomrom og nordiske tegn og kan brukes i url
        String url = "http://student.cs.hioa.no/~s304114/SlettReservasjon.php/?id="+sID;
        //FJERNER MELLOMROM I STRENGEN
        String urlUtenMellomrom = url.replaceAll(" ", "");


        task.execute(new String[]{urlUtenMellomrom});
    }


    //--------FRAGMENT STARTER--------
    public static class SeBestillingsInfo extends Fragment {

        //--------TEKST--------
        TextView resDato,resTidFra, resTidTil, resRomNr;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.activity_se_reservasjons_info_fragment, container, false);


            //BENYTTER METODENE FRA HOVEDKLASSEN TIL Å HENTE BESTILLING, RETURANT OG STRENG MED DELTAKELSER
            //final Bestilling bestilling = ((SeBestillingsInfoFragment)getActivity()).getBestiling();
            //final Resturant resturanten = ((SeBestillingsInfoFragment)getActivity()).getResturant();
            //String visVenner = ((SeBestillingsInfoFragment)getActivity()).visDeltakelser();


            //--------TEKST--------
            resDato = v.findViewById(R.id.resDato);
            resTidFra = v.findViewById(R.id.resTidFra);
            resTidTil = v.findViewById(R.id.resTidTil);
            resRomNr = v.findViewById(R.id.resRomNr);

            //--------SETTER TEKST MED HENTET RESTURANT, BESTILLING OG DELTAKELSR--------
            resDato.setText("14.11.2019");
            resTidFra.setText("10.00");
            resTidTil.setText("14.00");
            resRomNr.setText("PH360");


            //--------KNAPPER--------
            Button btnOk = v.findViewById(R.id.btnOk);
            Button btnAvbestill = v.findViewById(R.id.btnAvbestill);


            //--------LISTENERS--------
            //KLIKK PÅ OK
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //BENYTTER SAMME METODE SOM TILBAKE KNAPP - VISNING AV BESTILLINGER OPPDATERES DERMED FORTLØPENDE
                    ((SeReservasjonsInfoFragment)getActivity()).onBackPressed();
                }
            });

            //KLIKK PÅ AVBESTILL
            btnAvbestill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //VISER DIALOG VED AVBESTILLING - SPØR OM BRUKER ER SIKKER PÅ SLETTING/AVBESTILLING
                    visAvbestillDialog();

                }
            });
            return v;
        }


        //-------VISER DIALOG VED AVBESTILL KNAPP-----
        private void visAvbestillDialog() {
            DialogFragment dialog = new SlettReservasjonDialog();
            dialog.show(getFragmentManager(), "Avslutt");
        }

    } //SLUTT FRAGMENT


    //-------TILBAKEKNAPP - OPPDATERER INTENT FOR Å OPPDATERE EVENTUELL SLETTING/AVBESTILLING---------
    @Override
    public void onBackPressed() {
        Intent intent_tilbake = new Intent (SeReservasjonsInfoFragment.this, SeAlleReservasjoner.class);
        startActivity(intent_tilbake);
        finish();
    }
}
