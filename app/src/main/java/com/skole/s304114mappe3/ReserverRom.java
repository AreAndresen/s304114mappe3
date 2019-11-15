package com.skole.s304114mappe3;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.skole.s304114mappe3.ListView.SeAlleReservasjoner;
import com.skole.s304114mappe3.klasser.Rom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ReserverRom extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

//public class ReserverRom extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
  //      TimePickerDialog.OnTimeSetListener, SeBestillingsInfoDialog.DialogClickListener {


    /*--------DIALOG KNAPPER TIL SEBESTILLINGSINFOTDIALOG--------
    @Override
    public void bestillClick() {
        fullforBestilling();
    }

    @Override
    public void avbrytClick() {
        Toast.makeText(getApplicationContext(),"Avbrutt bestilling",Toast.LENGTH_LONG).show();
        return;
    }*/
    private ImageView logo;

    //--------KNAPPER--------
    private Button btnAvbryt, btnLeggTilVenn, btnReserver;

    //--------TEKST--------
    private TextView visDato,visRomNr;

    //--------SPINNERE--------
    private Spinner spinStart, spinSlutt, spinnerRomNr;

    //--------VERDIER--------
    private String dato, datoIdag;

    private String valgtRomNr;

    private String tidFra, tidTil;

    ArrayList<Rom> alleRom = new ArrayList<Rom>();
    Rom valgtRom;

    //--------OBJEKTER--------
    //private Venn valgtVenn, valgtVennSlett;
    //private Resturant valgtResturant;

    //--------ARRAYS--------
    //private ArrayList<Venn> valgteVenner = new ArrayList<Venn>();

    //--------LISTVIEW--------
    ListView vennerListView;

    //--------DB HANDLER--------
    //DBhandler db;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserver_rom);


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.minmeny);
        //toolbar.setNavigationIcon(R.drawable.ic_action_name); //android: //src="@drawable/logo"
        //toolbar.setTitleTextColor(getResources().getColor(R.color.colorText2));
        setActionBar(toolbar);


        //kjorJson();


        logo = findViewById(R.id.logo2);

        //--------KNAPPER--------
        btnAvbryt = (Button) findViewById(R.id.btnAvbryt);
        //btnLeggTilVenn = (Button) findViewById(R.id.btnLeggTilVenn);
        btnReserver = (Button) findViewById(R.id.btnReserver);


        //--------TEKST--------
        visDato = (TextView) findViewById(R.id.visDato);
        visRomNr = (TextView) findViewById(R.id.visRomNr);

        //--------SETTER OUTPUT--------

        ////--------HENTER ID TIL BESTILLINGEN SOM SKAL VISES FRA MINNE - DEFINERT I SEBESTILLINGER OG I NOTIFIKASJON/SERVICE--------
        valgtRomNr = getSharedPreferences("APP_INFO",MODE_PRIVATE).getString("ROMNR", ""); //putString("ROMNR", valgtRomNr).apply();

        visRomNr.setText(valgtRomNr);;
        //visTid = (TextView) findViewById(R.id.visTid);
        //vennerListView = (ListView) findViewById(R.id.list);


        //--------SPINNERE--------
        spinStart = (Spinner) findViewById(R.id.spinStart);
        spinSlutt = (Spinner) findViewById(R.id.spinSlutt);
        //spinnerRomNr = (Spinner) findViewById(R.id.spinnerRomNr);

        populerSpinStart();
        populerSpinSlutt();
        //lagRomSpinner();

        //--------HENTER DAGENS DATO I RIKTIG FORMAT TIL SAMMENLIGNING AV DET SOM LIGGER I DB--------
        Calendar c = Calendar.getInstance();
        int aarD = c.get(Calendar.YEAR);
        int mndD = c.get(Calendar.MONTH);
        int dagD = c.get(Calendar.DAY_OF_MONTH);

        mndD++;
        datoIdag = dagD+"/"+mndD+"/"+aarD;

        //KLIKK PÅ VELG DATO
        visDato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //OPPRETTER DATOFRAGMENTET FOR SETTING AV DATO
                DialogFragment datoValg = new DatoFragment();
                datoValg.show(getSupportFragmentManager(), "dato valg");


                //--------FORMATERER DATOENE FOR SAMMENLIGNING--------
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date dato2 = null;
                Date dato4 = null;

                //------------------FIKS DENNE INSTANSIERINGEN AV DATOER FEIL VED DATOMETODEN MÅ INSTANSIERES FØRST ------------------------------------->>>>

                try {
                    dato2 = sdf.parse("01/01/2017");
                    dato4 = sdf.parse("01/04/2017");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //--------SAMMENLIGNINGER AV FORMATERTE DATOER--------
                //HVIS DATO ER I DAG
                if(dato2.after(dato4)) {
                    visDato.setText(dato);
                }
                else{
                    Toast.makeText(ReserverRom.this, "Det er ikke mulig å bestille rom tilbake i tid.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //KLIKK PÅ UTFØR BESTILLING
        btnReserver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //KONTROLLERER AT ALLE FELTER SOM ER OBLIGATORISKE ER BENYTTET
                if (!visDato.getText().toString().equals("")) {

                    //OPPRETTER SEBESTILLINGSINFODIALOG OG VISER VALGT INFO
                    readWebpage();

                    //INFOMELDING UT
                    toastMessage("Reservasjon registrert!");
                    //MELDING TIL LOGG
                    Log.d("Legg inn: ", "Rom lagt til");


                    Intent intent_tilbake = new Intent (ReserverRom.this, MainActivityNy.class);
                    startActivity(intent_tilbake);
                    finish();

                }
                else{
                    //INFOMELDING UT - FEIL INPUT
                    Toast.makeText(ReserverRom.this, "Dato for reservasjon må være fylt ut.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //KLIKK PÅ TILBAKE
        btnAvbryt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (ReserverRom.this, MainActivityNy.class);
                startActivity(intent);
                finish();
            }
        });
        //--------SLUTT LISTENERS--------




    }

    /*public void kjorJson(){
        //JSON GREIER
        //textView = (TextView) findViewById(R.id.jasontekst);
        getJSON task = new getJSON();
        task.execute(new String[]{"http://student.cs.hioa.no/~s304114/HentRom.php"});
    }*/



    /*METODER FOR Å HENTE JSONOBJEKTENE FRA URL  - Sette inn ArrayList HER?
    private class getJSON extends AsyncTask<String, Void, ArrayList<Rom>> {
        JSONObject jsonObject;
        ArrayList<Rom> jsonArray = new ArrayList<>();

        //kjører i bakgrunnen - heavy work
        @Override
        protected ArrayList<Rom> doInBackground(String... urls) {
            //String retur = "";

            String s = "";
            String output = "";

            for (String url : urls) {
                try{
                    URL urlen = new URL(urls[0]);
                    HttpURLConnection conn = (HttpURLConnection)urlen.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");
                    if(conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed: HTTP errorcode: "+ conn.getResponseCode());
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    System.out.println("Output from Server .... \n");
                    while((s = br.readLine()) != null) {
                        output = output + s;
                    }
                    conn.disconnect();
                    try{
                        JSONArray mat = new JSONArray(output);

                        for (int i = 0; i < mat.length(); i++) {
                            //henter alle json objectene
                            JSONObject jsonobject = mat.getJSONObject(i);

                            String romNr = jsonobject.getString("romNr");
                            String bygg = jsonobject.getString("bygg");
                            String antSitteplasser = jsonobject.getString("antSitteplasser");
                            String lat = jsonobject.getString("lat");
                            String len = jsonobject.getString("len");
                            //retur = retur +beskrivelse+": "+lat+ " "+len+"\n";

                            Double latD = Double.parseDouble(lat);
                            Double lenD = Double.parseDouble(len);

                            LatLng koordinater = new LatLng(latD, lenD);

                            Rom nyttRom = new Rom(romNr, bygg, antSitteplasser, koordinater);

                            jsonArray.add(nyttRom);
                        }
                        return jsonArray;
                    }
                    catch(JSONException e) {
                        e.printStackTrace();
                    }
                    return jsonArray;
                }
                catch(Exception e) {
                    //return "Noe gikk feil";
                    e.printStackTrace();
                }
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(ArrayList<Rom> jsonArray) {
            alleRom = jsonArray;

            lagRomSpinner();
        }
    }*/



    //--------POPULERER VENNERLISTVIEWET - MULIGHET FOR LESTTING DIREKTE--------
    private void populerSpinStart() {

        //GENERERER ARRAYADAPTER TIL LISTVIEWET
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.klokkeslett,  R.layout.farge_spinner);
        adapter.setDropDownViewResource(R.layout.spinner_design);

        spinStart.setAdapter(adapter);

        spinStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapterView.getItemAtPosition(i).toString();

                tidFra = (String) spinStart.getItemAtPosition(i);


                String sTidFra = tidFra.replaceAll(":", "");

                if(tidTil == null) {
                    tidTil = "07:00";
                }

                String sTidTil = tidTil.replaceAll(":", "");

                int tidFraInt = Integer.parseInt(sTidFra);
                int tidTilInt = Integer.parseInt(sTidTil);

                if(tidFraInt > tidTilInt) {
                    Toast.makeText(adapterView.getContext(), "Starttid fra kan ikke være etter tid til.", Toast.LENGTH_SHORT).show();
            }


                Toast.makeText(adapterView.getContext(), tidFra, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    //--------POPULERER VENNERLISTVIEWET - MULIGHET FOR LESTTING DIREKTE--------
    private void populerSpinSlutt() {

        //GENERERER ARRAYADAPTER TIL LISTVIEWET
        final ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.klokkeslett, R.layout.farge_spinner);
        adapter2.setDropDownViewResource(R.layout.spinner_design);

        spinSlutt.setAdapter(adapter2);

        spinSlutt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {

                //GIR VALGTRESTURANT VERDIEN TIL VALGT OBJEKT FRA SPINNER
                tidTil = (String) spinSlutt.getItemAtPosition(i);


                String sTidFra = tidFra.replaceAll(":", "");
                String sTidTil = tidTil.replaceAll(":", "");

                int tidFraInt = Integer.parseInt(sTidFra);
                int tidTilInt = Integer.parseInt(sTidTil);

                if(tidFraInt > tidTilInt) {
                    Toast.makeText(adapterView.getContext(), "Tid fra kan ikke være etter tid til.", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(adapterView.getContext(), tidFraInt+" OG "+tidTilInt+" . "+tidTil, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }



    //--------GENERERER SPINNER MED ALLE RESTURATENE SOM ER LAGT TIL I DB--------
    private void lagRomSpinner() {

        //LEGGER ALLE RESTURANTER I RESTURANT-ARRAY - HENTET FRA DB
        ArrayList<Rom> alleRomNy = alleRom;

        //GENERERER ARRAYADAPTER TIL SPINNER
        final ArrayAdapter<Rom> adapterRes = new ArrayAdapter<Rom>(this, R.layout.spinner_design, alleRomNy);
        adapterRes.setDropDownViewResource(R.layout.spinner_design);

        //spinnerRomNr.setAdapter(adapterRes);

        /*VED VALG/KLIKK AV RESTURANT I SPINNEREN
        spinnerRomNr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //GIR VALGTRESTURANT VERDIEN TIL VALGT OBJEKT FRA SPINNER
                valgtRom = (Rom) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });*/
    }




    /*--------OPPRETTER SEBESTILLINGSINFODIALOG--------
    private void visBestillingsinfo()  {

        //OPPRETTER NYTT DIALOGFRAGMENT
        SeBestillingsInfoDialog bFragment = new SeBestillingsInfoDialog();

        //OVERFØRER BESTILLINGSINFO TIL FRAGMENTET MED METODE FRA FRAGMENTET
        bFragment.hentInfo(dato, tid, valgtResturant, valgteVenner, db);

        //VISER DIALOGVINDUET
        bFragment.show(getFragmentManager(), "Bestillingsinfo");
    }*/


    //--------INNEBYGD METODE FOR SETTING AV DATO--------
    @Override
    public void onDateSet(DatePicker view, int aar, int mnd, int dag) {

        //MÅ LEGGE INN DENNE ETTERSOM MÅNEDSTALLET VISER 9 FOR OKTOBER OSV.
        mnd++;

        //GENERERER STRING PÅ 22/10/2019 FORMAT
        dato = dag+"."+mnd+"."+aar;

    }





    //REGISTRERER RESERVASJON
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

        //lager stringer til url url
        String hentDato = dato;
        String hentTidFra = tidFra;;//beskrivelse.getText().toString();
        String hentTidTil = tidTil;;//beskrivelse.getText().toString();
        String hentRomNr = valgtRomNr;


        //String noSpaceStr = str.replaceAll("\\s", ""); // using built in method
        //System.out.println(noSpaceStr);


        //må fikse  denne strengen så den er uten mellomrom og nordiske tegn og kan brukes i url
        String url = "http://student.cs.hioa.no/~s304114/LeggTilReservasjon.php/?dato="+hentDato+"&tidFra="+hentTidFra+"&tidTil="+hentTidTil+"&romNr="+hentRomNr;
        //FJERNER MELLOMROM I STRENGEN
        String urlUtenMellomrom = url.replaceAll(" ", "");


        task.execute(new String[]{urlUtenMellomrom});
    }




    //En metode for å lage To o l b a rfra minmeny.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.minmeny, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.seRom:
                Intent intent = new Intent (ReserverRom.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.registrerRom:
                Intent intent_statistikk = new Intent (ReserverRom.this, RegistrerRom.class);
                startActivity(intent_statistikk);
                break;
            case R.id.SeAlleReservasjoner:
                Intent intent_preferanser = new Intent (ReserverRom.this, SeAlleReservasjoner.class);
                startActivity(intent_preferanser);
                finish();
                break;
            default:
                // If wegothere, theuser'saction wasnot recognized
                // Invokethesuperclassto handle it.
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    //-------TILBAKE KNAPP - FORHINDRER STACK---------
    @Override
    public void onBackPressed() {
        Intent intent = new Intent (ReserverRom.this, MainActivityNy.class);
        startActivity(intent);
        finish();
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }


}