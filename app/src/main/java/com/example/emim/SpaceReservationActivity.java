package com.example.emim;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class SpaceReservationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ViewFlipper formSteps;
    private int stepIndex = 0;

    private Button btnAnterior, btnProximo;

    // Step 1 fields
    private TextInputEditText inputEndereco, inputLatitude, inputLongitude;
    private Spinner spinnerFrequency;

    private TextInputLayout inputNome;

    // Step 2 fields
    private Spinner spinnerTipoDocumento;
    private EditText inputDescricaoDocumento, inputFormatoDocumento;

    // Confirmation labels (Step 3)
    private TextView labelNome, labelEndereco, labelFrequencia,
            labelLatitude, labelLongitude,
            labelDocTipo, labelDocDescricao, labelDocFormato;

    // Step 4 fields
    private Spinner spinnerPagamentoTipo;
    private EditText inputDetalhesPagamento;

    // Map
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.space_reservation_layout);

        // ViewFlipper + nav buttons
        formSteps  = findViewById(R.id.formSteps);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnProximo  = findViewById(R.id.btnProximo);

        // Step 1 → Detalhes
        inputNome     = findViewById(R.id.inputNome);
        inputEndereco = findViewById(R.id.inputEndereco);
        inputLatitude = findViewById(R.id.inputLatitude);
        inputLongitude= findViewById(R.id.inputLongitude);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        ArrayAdapter<String> adapterFreq = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Mensal", "Trimestral", "Anual"}
        );
        adapterFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapterFreq);

        // Step 2 → Documentos
        spinnerTipoDocumento     = findViewById(R.id.spinnerTipoDocumento);
        inputDescricaoDocumento  = findViewById(R.id.inputDescricaoDocumento);
        inputFormatoDocumento    = findViewById(R.id.inputFormatoDocumento);
        ArrayAdapter<String> adapterDoc = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"RG", "CPF", "Passaporte"}
        );
        adapterDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDocumento.setAdapter(adapterDoc);

        // Step 3 → Confirmação labels
        labelNome         = findViewById(R.id.labelNome);
        labelEndereco     = findViewById(R.id.labelEndereco);
        labelFrequencia   = findViewById(R.id.labelFrequencia);
        labelLatitude     = findViewById(R.id.labelLatitude);
        labelLongitude    = findViewById(R.id.labelLongitude);
        labelDocTipo      = findViewById(R.id.labelTipoDocumento);
        labelDocDescricao = findViewById(R.id.labelDescricaoDocumento);
        labelDocFormato   = findViewById(R.id.labelFormatoDocumento);

        // Step 4 → Pagamento
        spinnerPagamentoTipo   = findViewById(R.id.spinnerPagamentoTipo);
        inputDetalhesPagamento = findViewById(R.id.inputDetalhesPagamento);
        ArrayAdapter<String> adapterPag = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Cartão de Crédito", "Transferência", "Dinheiro"}
        );
        adapterPag.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPagamentoTipo.setAdapter(adapterPag);

        // MapView setup
        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = (savedInstanceState != null)
                ? savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
                : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // Navigate backwards
        btnAnterior.setOnClickListener(v -> {
            if (stepIndex > 0) {
                stepIndex--;
                formSteps.setDisplayedChild(stepIndex);
            }
        });

        // Navigate forwards
        btnProximo.setOnClickListener(v -> {
            // Before showing the Confirmação screen, fill in the labels
            if (stepIndex == 1) {
                preencherConfirmacao();
            }
            // Advance if there are more steps
            if (stepIndex < formSteps.getChildCount() - 1) {
                stepIndex++;
                formSteps.setDisplayedChild(stepIndex);
            }
        });


        EditText inputDataInicio = findViewById(R.id.inputDataInicio);
        EditText inputDataFim    = findViewById(R.id.inputDataFim);

        View.OnClickListener dateClickListener = v -> {
            final EditText target = (EditText) v;
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                day, month + 1, year);
                        target.setText(formatted);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        };

        inputDataInicio.setOnClickListener(dateClickListener);
        inputDataFim   .setOnClickListener(dateClickListener);
    }

    private void preencherConfirmacao() {
        labelNome.setText("Nome: " + inputNome.getEditText());
        labelEndereco.setText("Endereço: " + inputEndereco.getText());
        labelFrequencia.setText("Frequência: " + spinnerFrequency.getSelectedItem());
        labelLatitude.setText("Latitude: " + inputLatitude.getText());
        labelLongitude.setText("Longitude: " + inputLongitude.getText());
        labelDocTipo.setText("Tipo de Documento: " + spinnerTipoDocumento.getSelectedItem());
        labelDocDescricao.setText("Descrição: " + inputDescricaoDocumento.getText());
        labelDocFormato.setText("Formato: " + inputFormatoDocumento.getText());

        // Center the map on the user’s coordinates (or show an error)
        updateMap();
    }

    private void updateMap() {
        if (googleMap != null) {
            googleMap.clear();
            try {
                double lat = -25.810490330945026, lng = 32.52870188826419;
                LatLng location = new LatLng(lat, lng);
                googleMap.addMarker(new MarkerOptions().position(location).title("Localização"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            } catch (Exception e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override public void onMapReady(GoogleMap map) { googleMap = map; }
    @Override public void onResume()        { super.onResume(); mapView.onResume(); }
    @Override public void onStart()         { super.onStart();  mapView.onStart(); }
    @Override public void onPause()         { mapView.onPause(); super.onPause(); }
    @Override public void onStop()          { mapView.onStop();  super.onStop(); }
    @Override public void onDestroy()       { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory()     { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
