package com.example.emim;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emim.controller.Coordinate;
import com.example.emim.controller.CoordinatesAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MaintanceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ViewFlipper formSteps;
    private int stepIndex = 0;

    private Button btnAnterior, btnProximo;

    // Step 1 fields
    private TextInputEditText inputEndereco;
    private TextInputLayout  inputLatitude, inputLongitude;
    private Spinner spinnerExecutor;

    private TextInputLayout inputNome;

    // Step 2 fields
    private Spinner spinnerTipoDocumento;
    private TextInputLayout inputDescricaoDocumento, inputFormatoDocumento;

    // Confirmation labels (Step 3)
    private TextView labelNome, labelEndereco, labelFrequencia,
            labelLatitude, labelLongitude,
            labelDocTipo, labelDocDescricao;

    // Step 4 fields
    private Spinner spinnerPagamentoTipo;
    private EditText inputDetalhesPagamento;

    // Map
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_ATTACH_DOC = 2001;


    ImageButton btnCarregarFoto;
    ImageView ivPreview;
    ImageButton btnAnexarDoc;
    TextView tvDocName;
    private ImageView stepIcon1, stepIcon2, stepIcon3, stepIcon4;


    private static final int REQUEST_LOCATION_PERM = 1002;

    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView rvCoords;
    private CoordinatesAdapter adapter;

    private final List<Coordinate> coordinateList = new ArrayList<>();
    private Polyline currentPolyline;
    private Marker startMarker, endMarker;
    private static final String ROADS_API_URL =
            "https://roads.googleapis.com/v1/snapToRoads?interpolate=true&key=AIzaSyAMRW9dUZnP7IP8LDjVU7swa8-ixrrHa8I";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintanance_layout);

        stepIcon1 = findViewById(R.id.stepIcon1);
        stepIcon2 = findViewById(R.id.stepIcon2);
        stepIcon3 = findViewById(R.id.stepIcon3);
        stepIcon4 = findViewById(R.id.stepIcon4);


        btnCarregarFoto = findViewById(R.id.btnCarregarFoto);
        ivPreview = findViewById(R.id.ivFotoPreview);

        btnAnexarDoc = findViewById(R.id.btnAnexarDocumento);
        tvDocName = findViewById(R.id.tvNomeDocumentoAnexado);

        // ViewFlipper + nav buttons
        formSteps = findViewById(R.id.formSteps);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnProximo = findViewById(R.id.btnProximo);

        // Step 1 → Detalhes
        inputNome = findViewById(R.id.inputNome);
        inputEndereco = findViewById(R.id.inputEndereco);
        inputLatitude = findViewById(R.id.inputLatitude);
        inputLongitude = findViewById(R.id.inputLongitude);
        spinnerExecutor = findViewById(R.id.spinnerExecutor);
        ArrayAdapter<String> adapterFreq = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Próprio", "Emim", "Empreteiros"}
        );
        adapterFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExecutor.setAdapter(adapterFreq);

        // Step 2 → Documentos
        spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);
        inputDescricaoDocumento = findViewById(R.id.inputDescricaoDocumento);
        ArrayAdapter<String> adapterDoc = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"BI", "Passaporte", "Declaração de residência", "Outro"}
        );
        adapterDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDocumento.setAdapter(adapterDoc);

        // Step 3 → Confirmação labels
        labelNome = findViewById(R.id.labelNome);
        labelEndereco = findViewById(R.id.labelEndereco);
        labelFrequencia = findViewById(R.id.labelFrequencia);
        labelDocTipo = findViewById(R.id.labelTipoDocumento);


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
                updateStepIndicator();
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
                updateStepIndicator();
            } else {

                // aqui, ao tocar “Concluir”, você pode finalizar a Activity ou submeter o form
                finish();

            }
        });
        updateStepIndicator();


        EditText inputDataInicio = findViewById(R.id.inputDataInicio);
        EditText inputDataFim = findViewById(R.id.inputDataFim);

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
        inputDataFim.setOnClickListener(dateClickListener);


        btnCarregarFoto.setOnClickListener(v -> {
            Intent pickIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            pickIntent.setType("image/*");
            startActivityForResult(pickIntent, REQUEST_PICK_IMAGE);
        });


        btnAnexarDoc.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_ATTACH_DOC);
        });

        Spinner spinnerMetodoPagamento = findViewById(R.id.spinnerMetodoPagamento);

// 2. Create the data source
        String[] paymentMethods = new String[]{"Mpesa", "Emola", "Banco"};

// 3. Create an ArrayAdapter using a simple spinner layout
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                paymentMethods
        );

// 4. Specify the layout to use when the list of choices appears
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// 5. Apply the adapter to the spinner
        spinnerMetodoPagamento.setAdapter(paymentAdapter);


        TextView tvTaxaAmount = findViewById(R.id.tvTaxaAmount);

        // generate random between min (inclusive) and max (inclusive)
        int min = 2000;
        int max = 100000;
        Random rnd = new Random();
        int randomTaxa = rnd.nextInt(max - min + 1) + min;

        // format with thousand separators, prefix currency code
        String formatted = NumberFormat
                .getNumberInstance(Locale.getDefault())
                .format(randomTaxa);
        tvTaxaAmount.setText("MZN " + formatted);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 1) Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERM
            );
        } else {
            fetchAndFillLocation();
        }


        Button btnAdd = findViewById(R.id.btnAddCoordinate);
        rvCoords      = findViewById(R.id.rvCoordinates);

        adapter = new CoordinatesAdapter(new ArrayList<>(), pos -> adapter.remove(pos));
        rvCoords.setLayoutManager(new LinearLayoutManager(this));
        rvCoords.setAdapter(adapter);

        // clique no botão: adiciona à lista
        btnAdd.setOnClickListener(v -> {
            String lat = inputLatitude.getEditText().getText().toString().trim();
            String lon = inputLongitude.getEditText().getText().toString().trim();
            if (lat.isEmpty() || lon.isEmpty()) {
                Toast.makeText(this, "Preencha latitude e longitude", Toast.LENGTH_SHORT).show();
                return;
            }
            Coordinate coord = new Coordinate(lat, lon);
            adapter.add(coord);                // show in RecyclerView
            coordinateList.add(coord);
            inputLatitude.getEditText().setText("");
            inputLongitude.getEditText().setText("");
            drawPolyline();
        });

    }


    private void preencherConfirmacao() {
        labelNome.setText("Nome: " + inputNome.getEditText().getText());
        labelEndereco.setText("Endereço: " + inputEndereco.getText());
        labelFrequencia.setText("Executor: " + spinnerExecutor.getSelectedItem());
        labelDocTipo.setText("Tipo de Documento: " + spinnerTipoDocumento.getSelectedItem());
//        labelDocDescricao.setText("Descrição: " + inputDescricaoDocumento.getEditText().getText());

        // Center the map on the user’s coordinates (or show an error)
       // updateMap();
    }

    private void updateMap() {
        if (googleMap != null) {
            googleMap.clear();
            try {
                double lat = Double.parseDouble(inputLatitude.getEditText().getText().toString()),
                        lng = Double.parseDouble(inputLongitude.getEditText().getText().toString());
                LatLng location = new LatLng(lat, lng);
                googleMap.addMarker(new MarkerOptions().position(location).title("Localização"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            } catch (Exception e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        drawPolyline();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
            ivPreview.setVisibility(View.VISIBLE);
        }

        if (requestCode == REQUEST_ATTACH_DOC && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String name = uri.getLastPathSegment(); // or query DisplayName via ContentResolver
            tvDocName.setText(name);
        }
    }

    private void updateStepIndicator() {
        // array para facilitar
        ImageView[] icons = {stepIcon1, stepIcon2, stepIcon3, stepIcon4};
        int lastIndex = icons.length - 1;

        for (int i = 0; i < icons.length; i++) {
            if (i <= stepIndex) {
                // passos concluídos ou atual: círculo verde + check
                icons[i].setBackgroundResource(R.drawable.circle_green);
                icons[i].setImageResource(R.drawable.ic_baseline_check_24);
            } else {
                // passos futuros: círculo cinza + ícone padrão
                icons[i].setBackgroundResource(R.drawable.circle_gray);
                // se quiser trocar o src por outro ícone, faça aqui;
                // por simplicidade mantemos o mesmo:
                icons[i].setImageResource(R.drawable.ic_baseline_check_24);
            }
        }

        // ** Ajuste do texto e visibilidade dos botões na última tela **
        if (stepIndex == lastIndex) {
            // botão de “Concluir” e sem “Anterior”
            btnProximo.setText("Concluir");
            btnAnterior.setVisibility(View.GONE);
        } else {
            // botão “Próximo” e mostra “Anterior”
            btnProximo.setText("Próximo");
            btnAnterior.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERM &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndFillLocation();
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndFillLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, (Location location) -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                       inputLatitude.getEditText().setText(String.format(Locale.getDefault(), "%.6f", lat));
                        inputLongitude.getEditText().setText(String.format(Locale.getDefault(), "%.6f", lng));
                    } else {
                        Toast.makeText(this, "Não foi possível obter localização", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao obter localização: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void drawPolyline() {
        if (googleMap == null || coordinateList.isEmpty()) return;

        // Remove any existing polyline and markers
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        if (startMarker != null) {
            startMarker.remove();
        }
        if (endMarker != null) {
            endMarker.remove();
        }

        // Build the “path” parameter for the Roads API
        StringBuilder pathBuilder = new StringBuilder();
        for (Coordinate c : coordinateList) {
            if (pathBuilder.length() > 0) {
                pathBuilder.append("|");
            }
            pathBuilder
                    .append(c.latitude)
                    .append(",")
                    .append(c.longitude);
        }

        String encodedPath;
        try {
            encodedPath = URLEncoder.encode(pathBuilder.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodedPath = pathBuilder.toString();  // fallback, though unlikely
        }

        // Prepare the Roads API request (interpolate=true smooths between points)
        String url = ROADS_API_URL + "&path=" + encodedPath;
        Request request = new Request.Builder()
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MaintanceActivity.this,
                                "Erro Roads API: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onFailure(call, new IOException("HTTP " + response.code()));
                    return;
                }

                // Parse the JSON into our model
                String json = response.body().string();
                SnapToRoadsResponse snapResp = gson.fromJson(json, SnapToRoadsResponse.class);
                List<LatLng> snappedPoints = new ArrayList<>();
                for (SnapToRoadsResponse.SnappedPoint sp : snapResp.snappedPoints) {
                    snappedPoints.add(new LatLng(
                            sp.location.latitude,
                            sp.location.longitude
                    ));
                }

                runOnUiThread(() -> {
                    // 1) Draw the polyline
                    currentPolyline = googleMap.addPolyline(new PolylineOptions()
                            .addAll(snappedPoints)
                            .width(6)
                            .color(Color.BLUE)
                    );

                    // 2) Place start & end markers
                    LatLng start = snappedPoints.get(0);
                    LatLng end   = snappedPoints.get(snappedPoints.size() - 1);
                    startMarker = googleMap.addMarker(new MarkerOptions()
                            .position(start)
                            .title("Início")
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_GREEN)));
                    endMarker = googleMap.addMarker(new MarkerOptions()
                            .position(end)
                            .title("Fim")
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED)));

                    // 3) Build bounds & animate camera
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    for (LatLng pt : snappedPoints) {
                        boundsBuilder.include(pt);
                    }
                    LatLngBounds bounds = boundsBuilder.build();

                    googleMap.setOnMapLoadedCallback(() -> {
                        googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(bounds, /* padding */100)
                        );
                    });
                });
            }
        });
    }


}


class SnapToRoadsResponse {
    List<SnappedPoint> snappedPoints;

    static class SnappedPoint {
        Location location;
        // placeId, originalIndex if you need them
    }
    static class Location {
        double latitude;
        double longitude;
    }
}
